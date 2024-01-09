/** 
* La classe ClientHandle gestisce la logica associata a una singola connessione cliente-server.
*/

import java.net.Socket;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandle implements Runnable {
    // Socket per la comunicazione con il client
    private Socket clientSocket;
    
    // Socket separato per le notifiche
    private Socket notificationSocket;
    
    private PrintWriter outputStream = null;
    
    // Gestori per file Json:
    private ConfigManager configManager;
    private UserManager userManager;
    private HotelManager hotelManager;
    private ReviewManager reviewManager;
    
    // Mantiene la traccia degli utenti loggati con il relativo socket per notifiche
    private ConcurrentHashMap<String, Socket> allLoggedUsers;

    // Numero di giorni prima che un’utente possa inserire una nuova recensione
    private int daysForNewReview;

    ClientHandle(Socket clientSocket, Socket notificationSocket, ConcurrentHashMap<String, Socket> loggedUsers) throws Exception {
        this.clientSocket = clientSocket;
        this.notificationSocket = notificationSocket;

        this.allLoggedUsers = loggedUsers;

        this.outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

        this.configManager = new ConfigManager();
        this.userManager = new UserManager();
        this.hotelManager = new HotelManager();
        this.reviewManager = new ReviewManager();


        /** Variabili di Config */
        Config configFile = configManager.readConfigFile();
        this.daysForNewReview = configFile.getDaysForNewReview();
        /** ------------------ */

    }

    @Override
    public void run() {
        String clientCommand = "";

        // Riferimento all’utente loggato
        LoggedUser loggedUser = null;

        reloadReview();    

        try (Scanner inputStream = new Scanner(clientSocket.getInputStream())) {
            while (inputStream.hasNextLine()) {
                clientCommand = inputStream.nextLine();

                switch (clientCommand) {
                    case "REGISTER":
                        register(inputStream);
                        
                        break;
                    case "LOGIN":
                        loggedUser = login(inputStream);
                        // Se il login ha successo, traccia l’utente come loggato
                        if (loggedUser != null) {
                            // Controlla se ci sono già delle recensioni fatte dall'Utente e aggiorna il contatore
                            loggedUser.setNumReview(reviewManager.getNumReviewByUsername(loggedUser.getUsername()));
                            allLoggedUsers.putIfAbsent(loggedUser.getUsername(), notificationSocket);
                        }

                        break;
                    case "INSERT_REVIEW":
                        insertReview(inputStream, loggedUser);
                        reloadReview();

                        break;
                    case "SHOW_BADGE":
                        showBadge(inputStream, loggedUser);

                        break;
                    case "SEARCH_HOTEL":
                        searchHotel(inputStream);

                        break;
                    case "SEARCH_ALL_HOTEL":
                        searchAllHotels(inputStream);

                        break;
                    case "LOGOUT":
                        allLoggedUsers.remove(loggedUser.getUsername());

                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error:" + clientSocket);
        } finally {
            if (loggedUser != null)
                allLoggedUsers.remove(loggedUser.getUsername());
            
            // Chiusura della comunicazione con il client
            System.out.printf("%s: Comunicazione CHIUSA con il client %s\n", Thread.currentThread().getName(), clientSocket.getInetAddress());
            outputStream.close();
        }
    }

    /**
    * Gestisce il processo di registrazione di un nuovo utente.
    * Legge l'username e la password inviati dal client e tenta di registrarli.
    *
    * @param inputStream Lo Scanner collegato al flusso di input del socket del client. Utilizzato per ricevere l'username e la pw.
    */
    private void register(Scanner inputStream) {
        String username = "", password = "";

        username = inputStream.nextLine();
        password = inputStream.nextLine();
        System.out.printf("%s: Tentativo di registrazione dal client %s: Username: %s, Password: %s\n", Thread.currentThread().getName(), clientSocket.getInetAddress(), username, password);
        
        // Controlla se l'username o la password sono vuoti o se l'username è già in uso.
        if ((username.isBlank() || password.isBlank()) || !userManager.checkUsername(username)[0].isEmpty()) {
            outputStream.println("DENIED");
        } else {
            userManager.addUser(username, password);
            System.out.printf("Nuova registrazione (%s)\n", username);
            outputStream.println("ACCEPT");
        }     
    }   

    /**
    * Gestisce il processo di login di un utente.
    * Legge l'username e la password inviati dal client e verifica le credenziali.
    *
    * @param inputStream Lo Scanner collegato al flusso di input del socket del client. Utilizzato per ricevere l'username e la pw.
    * @return Un’istanza di LoggedUser se il login ha successo, altrimenti null.
    */
    private LoggedUser login(Scanner inputStream) {
        String username = "", password = "";
        LoggedUser loggedUser = null; 

        username = inputStream.nextLine();
        password = inputStream.nextLine();

        System.out.printf("%s: Nuovo acccesso dal client %s\n", Thread.currentThread().getName(), clientSocket.getInetAddress());

        // Controlla se l'username e la password non sono vuoti e se le credenziali sono valide.
        if (!(username.isBlank() && password.isBlank()) && userManager.checkUsername(username, password)) {
            loggedUser = new LoggedUser(username, password);

            System.out.printf("Nuovo Accesso (%s)\n", username);
            outputStream.println("ACCEPT");
        } else {
            outputStream.println("DENIED");                        
        }
        
        return loggedUser;
    }

    /**
    * Gestisce l’inserimento di una nuova recensione da parte di un utente loggato.
    * Legge i dati necessari dallo Scanner e, dopo aver confermato la possibilità di fare una recensione, aggiunge la recensione.
    *
    * @param inputStream Lo Scanner collegato al flusso di input del socket del client. Utilizzato per ricevere l'hotel e la città.
    * @param loggedUser L’utente che sta facendo la recensione.
    */
    private void insertReview(Scanner inputStream, LoggedUser loggedUser) {
        // Dati della recensione
        String nomeHotel = "", nomeCitta = "", dateLastReview;
        int globalScore = 0;
        int[] singleScores = {0,0,0,0};
        Hotel hotel = null;


        nomeHotel = inputStream.nextLine();
        nomeCitta = inputStream.nextLine();
        hotel = hotelManager.searchHotel(nomeHotel,nomeCitta);

        // Verifica se l’hotel è stato trovato
        if (hotel == null) {
            outputStream.println("HOTEL_NOT_FOUND");
        }
        else {
            outputStream.println("HOTEL_FOUND");

            // Se l’hotel esiste, controlla se l’utente ha richiesto di fare una recensione.
            if (inputStream.nextLine().equals("REQUEST_REVIEW")) {

                // Ottieni la data dell’ultima recensione fatta dall’utente per l’hotel specificato
                dateLastReview = reviewManager.getDateLastReviewByUser(loggedUser.getUsername(), hotel.getId());

                // Controlla se l’utente è autorizzato a scrivere una nuova recensione.
                if (dateLastReview.isBlank() || checkDate(dateLastReview)) { 
                    outputStream.println("REQUEST_ACCEPTED");
    
                    // Leggi i punteggi individuali e totali per la recensione
                    globalScore = Integer.parseInt(inputStream.nextLine());
                    singleScores[0] = Integer.parseInt(inputStream.nextLine());
                    singleScores[1] = Integer.parseInt(inputStream.nextLine());
                    singleScores[2] = Integer.parseInt(inputStream.nextLine());
                    singleScores[3] = Integer.parseInt(inputStream.nextLine());
                    
                    outputStream.println("ACCEPT");

                    // Aggiungi la recensione al file json
                    reviewManager.addReview(loggedUser, hotel.getId(), hotel.getName(), hotel.getCity(), globalScore, singleScores);
                    System.out.println("Nuova recensione effettuata");
    
                    // Incrementa il numero di recensioni per l’hotel
                    hotel.IncrementNumReview();
                } else {
                    outputStream.println("REQUEST_REJECTED");
                }
            }
        }
    }

    /**
    * Invia al client le informazioni relative ai badge e al numero di recensioni fatte dall’utente loggato.
    *
    * @param inputStream Lo Scanner collegato al flusso di input del socket del client. Utilizzato per ricevere l'username e la pw.
    * @param loggedUser L’utente che sta richiedendo le informazioni sui propri badge.
    */    
    private void showBadge(Scanner inputStream, LoggedUser loggedUser) {
        String msg = "Tuo badge: " + loggedUser.getBadges() + ", " + loggedUser.getNumReview() + " recensione/i fatta/e";

        outputStream.println(msg);
    }

    /**
    * Esegue la ricerca di un hotel specifico, basandosi sul nome e sulla città.
    * Le informazioni relative all’hotel trovato vengono inviate al client.
    *
    * @param inputStream Lo Scanner collegato al flusso di input del socket del client. Utilizzato per ricevere l'hotel e la città.
    */
    private void searchHotel(Scanner inputStream) {
        // Dettagli della ricerca
        String nomeHotel = "", nomeCitta = "";
        Hotel hotel = null;
        List<String> services;
        int[] rating = {0,0,0,0};

        nomeHotel = inputStream.nextLine();
        nomeCitta = inputStream.nextLine();
        hotel = hotelManager.searchHotel(nomeHotel, nomeCitta);
        
        // Verifica se l’hotel è stato trovato e invia le informazioni corrispondenti al client
        if (hotel == null) {
            outputStream.println("HOTEL_NOT_FOUND");
        } else {
            outputStream.println("HOTEL_FOUND");

            System.out.println("Hotel Trovato");

            if (inputStream.nextLine().equals("REQUEST_SEARCH")) {

                // Dettagli dell’hotel trovato vengono inviati al client
                outputStream.printf("Nome: %s\n", hotel.getName());
                outputStream.printf("Descrizione: %s\n", hotel.getDescription());
                outputStream.printf("Città: %s\n", hotel.getCity());
                outputStream.printf("Telefono: %s\n", hotel.getPhone());
                
                outputStream.print("Servizi Offerti:\n");
                services = hotel.getServices();
                for (String s : services) {
                    outputStream.printf("- %s\n", s);
                }
                
                outputStream.printf("Rate: %d\n", hotel.getRate());
                
                outputStream.printf("Rating:\n");
                rating = hotel.getRatings();
                outputStream.printf("- Cleaning: %d\n", rating[0]);
                outputStream.printf("- Position: %d\n", rating[1]);
                outputStream.printf("- Services: %d\n", rating[2]);
                outputStream.printf("- Quality: %d\n", rating[3]);

                // Stringa di terminazione
                outputStream.println("END");
                System.out.println("Effettuata nuova ricerca di un Hotel");
            }

        }
    }

    /**
    * Ricerca tutti gli hotel in una città specifica e invia i dettagli di ciascun hotel al client.
    *
    * @param inputStream Lo Scanner collegato al flusso di input del socket del client. Utilizzato per ricevere la città.
    */
    private void searchAllHotels(Scanner inputStream) {
        // Dettagli della ricerca
        String nomeCitta = "";
        List <Hotel> hotels = null;
        List<String> services;
        int[] rating = {0,0,0,0};

        nomeCitta = inputStream.nextLine();
        hotels = hotelManager.searchHotelByCity(nomeCitta);

        // Verifica se sono stati trovati hotel e invia le informazioni al client
        if (hotels.isEmpty()) {
            outputStream.println("HOTEL_NOT_FOUND");
        } else {
            outputStream.println("HOTEL_FOUND");

            if (inputStream.nextLine().equals("REQUEST_SEARCH")) {
    
                outputStream.printf("\n\n%d Hotel trovati:\n", hotels.size());
                for (Hotel hotel : hotels) {
                    outputStream.printf("\n----------------\n\n");
                    
                    outputStream.printf("Nome: %s\n", hotel.getName());
                    outputStream.printf("Descrizione: %s\n", hotel.getDescription());
                    outputStream.printf("Città: %s\n", hotel.getCity());
                    outputStream.printf("Telefono: %s\n", hotel.getPhone());
                    
                    outputStream.print("Servizi Offerti:\n");
                    services = hotel.getServices();
                    for (String s : services) {
                        outputStream.printf("- %s\n", s);
                    }
                    
                    outputStream.printf("Rate: %d\n", hotel.getRate());
                    
                    outputStream.printf("Rating:\n");
                    rating = hotel.getRatings();
                    outputStream.printf("- Cleaning: %d\n", rating[0]);
                    outputStream.printf("- Position: %d\n", rating[1]);
                    outputStream.printf("- Services: %d\n", rating[2]);
                    outputStream.printf("- Quality: %d\n", rating[3]);
                }
    
                outputStream.println("END");            
                System.out.println("Effettuata nuova ricerca di Hotel di una città");
            }

        }
    }

    //OTHER METHODS:

    /**
    * Ricarica e calcola i nuovi punteggi e valutazioni per ogni hotel in base alle recensioni ricevute.
    * Aggiorna i dettagli di ogni hotel nel gestore degli hotel.
    */    
    private void reloadReview() {
        List<Hotel> allHotel = null;
        List<Review> allReviews = null; 

        int totRate, avgRate;
        int[] totSingleRate = {0,0,0,0}, avgSingleRate = {0,0,0,0}, defaultRating = {0,0,0,0};

        // Lista per memorizzare le date delle recensioni di un hotel
        List<String> dateRecensioni = new ArrayList<>();

        // Ottieni tutti gli hotel per aggiornare i loro punteggi
        allHotel = hotelManager.searchAllHotels();

        for (Hotel hotel : allHotel) {
            totRate = 0;
            avgRate = 0;
            
            Arrays.fill(totSingleRate, 0);
            Arrays.fill(avgSingleRate, 0);

            // Ottieni tutte le recensioni per l’hotel in questione
            allReviews = reviewManager.getAllReviewByHotel(hotel.getId());
            for (Review review : allReviews) {

                // Aggiungi i punteggi della recensione ai totali
                totRate += review.getGlobalScore();
                for (int i = 0; i < totSingleRate.length; i++) {
                    totSingleRate[i] += review.getSingleScores()[i];
                }

                // Aggiungi la data della recensione alla lista
                dateRecensioni.add(review.getDateTime());
            }

            // Se ci sono recensioni, calcola le medie e aggiorna i dettagli dell’hotel
            if (allReviews.size() > 0) {
                avgRate = Math.round(totRate/allReviews.size());

                for (int i = 0; i < avgSingleRate.length; i++) {
                    avgSingleRate[i] = Math.round((float) totSingleRate[i] / allReviews.size());
                }    
                
                hotel.setNumReviews(allReviews.size());
                hotel.setRate(avgRate);
                hotel.setRatings(avgSingleRate);
                hotel.setDateLastReview(dateRecensioni.get(dateRecensioni.size() - 1));
                hotel.setScore(calcScore(hotel));
                
            } else {
                // Se non ci sono recensioni, imposta i valori di default
                hotel.setNumReviews(0);
                hotel.setRate(0);
                hotel.setRatings(defaultRating);
                hotel.setScore(0.0);
                hotel.setDateLastReview("");
            }
            
            // Carica la recensione aggiornata nel file json
            hotelManager.loadReview(hotel);
        }
    } 

    /**
    * Calcola il punteggio di un hotel basato sulla valutazione, sul numero di recensioni e sulla data dell’ultima recensione.
    *
    * @param hotel L’hotel per il quale calcolare il punteggio.
    * @return Il punteggio calcolato.
    */
    private double calcScore(Hotel hotel) {
        String dateLastReviewString = hotel.getDateLastReview();
        double ranking = 0.0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            
        // Converti la stringa in un oggetto LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateLastReviewString, formatter);
    
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
    
        // Calcolo del punteggio basato sulla valutazione, sul numero di recensioni e sulla vecchiaia della recensione
        ranking = 0.6 * hotel.getRate() + 0.3 * Math.log(hotel.getNumReviews()) + 0.1 * duration.toDays();

        return ranking;
    }

    /**
     * Controlla se sono passati abbastanza giorni dall’ultima recensione per permettere una nuova inserzione.
     *
     * @param date Data dell’ultima recensione.
     * @return true se è possibile inserire una nuova recensione, false altrimenti.
     */    
    private boolean checkDate(String date) {   
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        
        // Converti la stringa in un oggetto LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

        // Calcola la durata tra la data dell’ultima recensione e ora
        Duration duration = Duration.between(dateTime, LocalDateTime.now());

        // Controlla se la durata supera il numero di giorni stabiliti per inserire una nuova recensione
        return (duration.toDays() > daysForNewReview);
    }
}