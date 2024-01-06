import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClientHandle implements Runnable {
    private Socket clientSocket;
    private PrintWriter outputStream = null;
    private UserManager userManager;
    private HotelManager hotelManager;
    private ReviewManager reviewManager;

    ClientHandle(Socket s) throws Exception {
        this.clientSocket = s;
        this.outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

        this.userManager = new UserManager();
        this.hotelManager = new HotelManager();
        this.reviewManager = new ReviewManager();
    }

    @Override
    public void run() {
        String clientCommand = "";
        LoggedUser loggedUser = null;

        loadReview();
    
        try (Scanner inputStream = new Scanner(clientSocket.getInputStream())) {
            while (inputStream.hasNextLine()) {
                clientCommand = inputStream.nextLine();

                // REGISTRAZIONE
                if (clientCommand.equals("REGISTER")) {
                    register(inputStream);
                } 
                // LOG-IN
                else if (clientCommand.equals("LOGIN")) {
                    loggedUser = login(inputStream);

                    if (loggedUser != null)
                        // Controlla se ci sono già delle recensioni fatte dall'Utente e aggiorna il contatore
                        loggedUser.setNumReview(reviewManager.getNumReviewByUsername(loggedUser.getUsername()));
                }
                // INSERT REVIEW
                else if (clientCommand.equals("INSERT_REVIEW")) {
                    insertReview(inputStream, loggedUser);
                }
                else if (clientCommand.equals("SHOW_BADGE")) {
                    showBadge(inputStream, loggedUser);
                }
                else if (clientCommand.equals("SEARCH_HOTEL")) {
                    searchHotel(inputStream);
                } 
                else if (clientCommand.equals("SEARCH_ALL_HOTEL")) {
                    searchAllHotels(inputStream);
                }
            }
        } catch (Exception e) {
            System.out.println("Error:" + clientSocket);
        } finally {
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

        if (!userManager.checkUsername(username)[0].isEmpty()) {
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

        if (userManager.checkUsername(username, password)) {
            loggedUser = new LoggedUser(username, password);

            System.out.printf("Nuovo Accesso (%s)\n", username);
            outputStream.println("ACCEPT");
        } else {
            outputStream.println("DENIED");                        
        }
        
        return loggedUser;
    }

    private void insertReview(Scanner inputStream, LoggedUser loggedUser) {
        String nomeHotel = "", nomeCitta = "";
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
            globalScore = inputStream.nextInt();
            singleScores[0] = inputStream.nextInt();
            singleScores[1] = inputStream.nextInt();
            singleScores[2] = inputStream.nextInt();
            singleScores[3] = inputStream.nextInt();

            reviewManager.addReview(loggedUser, hotel.getId(), hotel.getName(), hotel.getCity(), globalScore, singleScores);;
            System.out.println("Nuova recensione effettuata");
            outputStream.println("ACCEPT");        
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

    // Other methods
    private void loadReview() {
        List<Hotel> allHotel = null;
        List<Review> allReviews = null; 

        allHotel = hotelManager.searchAllHotels();

        for (Hotel hotel : allHotel) {
            allReviews = reviewManager.getAllReviewByHotel(hotel.getId());
            for (Review review : allReviews) {
                hotelManager.loadReview(review.getIdHotel(), review.getGlobalScore(), review.getSingleScores());
            }
        }
    }
}
