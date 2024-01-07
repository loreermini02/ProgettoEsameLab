import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandle implements Runnable {
    private Socket clientSocket;
    private PrintWriter outputStream = null;
    private UserManager userManager;
    private HotelManager hotelManager;
    private ReviewManager reviewManager;
    private ConcurrentHashMap<String, Socket> allLoggedUsers;

    ClientHandle(Socket s, ConcurrentHashMap<String, Socket> loggedUsers) throws Exception {
        this.clientSocket = s;
        this.allLoggedUsers = loggedUsers;

        this.outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

        this.userManager = new UserManager();
        this.hotelManager = new HotelManager();
        this.reviewManager = new ReviewManager();
    }

    @Override
    public void run() {
        String clientCommand = "";
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
        
                        if (loggedUser != null)
                            // Controlla se ci sono già delle recensioni fatte dall'Utente e aggiorna il contatore
                            loggedUser.setNumReview(reviewManager.getNumReviewByUsername(loggedUser.getUsername()));
        
                        allLoggedUsers.putIfAbsent(loggedUser.getUsername(), clientSocket); 

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

    private void register(Scanner inputStream) {
        String username = "", password = "";

        username = inputStream.nextLine();
        
        password = inputStream.nextLine();
        System.out.printf("%s: Messaggio dal client %s: Username: %s, Password: %s\n", Thread.currentThread().getName(), clientSocket.getInetAddress(), username, password);
        
        if ((username.isBlank() || password.isBlank()) || !userManager.checkUsername(username)[0].isEmpty()) {
            //System.out.printf("%s già registrato\n", username);
            outputStream.println("DENIED");
        } else {
            userManager.addUser(username, password);
            System.out.printf("Nuova registrazione (%s)\n", username);
            outputStream.println("ACCEPT");
        }     
    }   

    private LoggedUser login(Scanner inputStream) {
        String username = "", password = "";
        LoggedUser loggedUser = null; 

        username = inputStream.nextLine();
        password = inputStream.nextLine();

        System.out.printf("%s: Messaggio dal client %s: Username: %s, Password: %s\n", Thread.currentThread().getName(), clientSocket.getInetAddress(), username, password);

        if (!(username.isBlank() && password.isBlank()) && userManager.checkUsername(username, password)) {
            loggedUser = new LoggedUser(username, password);

            System.out.printf("Nuovo Accesso (%s)\n", username);
            outputStream.println("ACCEPT");
        } else {
            outputStream.println("DENIED");                        
        }
        
        return loggedUser;
    }

    private void insertReview(Scanner inputStream, LoggedUser loggedUser) {
        String nomeHotel = "", nomeCitta = "", dateLastReview;
        int globalScore = 0;
        int[] singleScores = {0,0,0,0};
        Hotel hotel = null;


        nomeHotel = inputStream.nextLine();
        nomeCitta = inputStream.nextLine();
        hotel = hotelManager.searchHotel(nomeHotel,nomeCitta);

        if (hotel == null) {
            outputStream.println("HOTEL_NOT_FOUND");
        }
        else {
            outputStream.println("HOTEL_FOUND");

            dateLastReview = reviewManager.getDateLastReviewByUser(loggedUser.getUsername(), hotel.getId());

            if (!dateLastReview.isBlank() && checkDate(dateLastReview)) {
                outputStream.println("REQUEST_ACCEPTED");
                globalScore = inputStream.nextInt();
                singleScores[0] = inputStream.nextInt();
                singleScores[1] = inputStream.nextInt();
                singleScores[2] = inputStream.nextInt();
                singleScores[3] = inputStream.nextInt();

                
                outputStream.println("ACCEPT");
                reviewManager.addReview(loggedUser, hotel.getId(), hotel.getName(), hotel.getCity(), globalScore, singleScores);
                System.out.println("Nuova recensione effettuata");

                hotel.IncrementNumReview();
            } else {
                outputStream.println("REQUEST_REJECTED");
            }
        }
    }

    private void showBadge(Scanner inputStream, LoggedUser loggedUser) {
        String msg = "Tuo badge: " + loggedUser.getBadges() + ", " + loggedUser.getNumReview() + " recensione/i fatta/e";

        outputStream.println(msg);
    }

    private void searchHotel(Scanner inputStream) {
        String nomeHotel = "", nomeCitta = "";
        Hotel hotel = null;
        List<String> services;
        int[] rating = {0,0,0,0};

        nomeHotel = inputStream.nextLine();
        nomeCitta = inputStream.nextLine();
        hotel = hotelManager.searchHotel(nomeHotel, nomeCitta);
        
        if (hotel == null) {
            outputStream.println("HOTEL_NOT_FOUND");
        } else {
            outputStream.println("HOTEL_FOUND");
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
            outputStream.println("END");
            
            System.out.println("Effettuata nuova ricerca di un Hotel");
        }
    }

    private void searchAllHotels(Scanner inputStream) {
        String nomeCitta = "";
        List <Hotel> hotels = null;
        List<String> services;
        int[] rating = {0,0,0,0};

        nomeCitta = inputStream.nextLine();
        hotels = hotelManager.searchHotelByCity(nomeCitta);

        if (hotels.isEmpty()) {
            outputStream.println("HOTEL_NOT_FOUND");
        } else {
            outputStream.println("HOTEL_FOUND");

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

    //Other Methods
    private synchronized void reloadReview() {
        List<Hotel> allHotel = null;
        List<Review> allReviews = null; 

        int totRate, avgRate;
        int[] totSingleRate = {0,0,0,0}, avgSingleRate = {0,0,0,0}, defaultRating = {0,0,0,0};

        List<String> dateRecensioni = new ArrayList<>();

        allHotel = hotelManager.searchAllHotels();

        for (Hotel hotel : allHotel) {
            totRate = 0;
            avgRate = 0;
            
            totSingleRate[0] = 0;
            totSingleRate[1] = 0;
            totSingleRate[2] = 0;
            totSingleRate[3] = 0;

            avgSingleRate[0] = 0;
            avgSingleRate[1] = 0;
            avgSingleRate[2] = 0;
            avgSingleRate[3] = 0;

            allReviews = reviewManager.getAllReviewByHotel(hotel.getId());
            for (Review review : allReviews) {
                totRate += review.getGlobalScore();
                totSingleRate[0] += review.getSingleScores()[0];
                totSingleRate[1] += review.getSingleScores()[1];
                totSingleRate[2] += review.getSingleScores()[2];
                totSingleRate[3] += review.getSingleScores()[3]; 
                
                dateRecensioni.add(review.getDateTime());
            }

            if (allReviews.size() > 0) {
                avgRate = Math.round(totRate/allReviews.size());
                avgSingleRate[0] = Math.round(totSingleRate[0] / allReviews.size());
                avgSingleRate[1] = Math.round(totSingleRate[1] / allReviews.size());
                avgSingleRate[2] = Math.round(totSingleRate[2] / allReviews.size());
                avgSingleRate[3] = Math.round(totSingleRate[3] / allReviews.size());    
                
                hotel.setNumReviews(allReviews.size());
                hotel.setRate(avgRate);
                hotel.setRatings(avgSingleRate);
                hotel.setDateLastReview(dateRecensioni.get(dateRecensioni.size() - 1));
                hotel.setScore(calcScore(hotel));
                
            } else {
                hotel.setNumReviews(0);
                hotel.setRate(0);
                hotel.setRatings(defaultRating);
                hotel.setScore(0.0);
                hotel.setDateLastReview("");
            }
            
            hotelManager.loadReview(hotel);
        }
    } 

    private double calcScore(Hotel hotel) {
        String dateLastReviewString = hotel.getDateLastReview();
        double ranking = 0.0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            
        // Converti la stringa in un oggetto LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateLastReviewString, formatter);
    
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
    
        ranking = 0.6 * hotel.getRate() + 0.3 * Math.log(hotel.getNumReviews()) + 0.1 * duration.toDays();

        return ranking;
    }

    private boolean checkDate(String date) {   
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        
        // Converti la stringa in un oggetto LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

        Duration duration = Duration.between(dateTime, LocalDateTime.now());

        // E' possibile fare una recensione dello stesso hotel solo se sono passati 30 giorni dall'ultima
        return (duration.toDays() > 30);
    }
}
