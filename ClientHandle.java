import java.io.PrintWriter;
import java.net.Socket;
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
        String clientCommand = "Ciao";
        LoggedUser loggedUser = null;
    
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
            //System.out.printf("Username (%s) non esistente e/o Password (%s) sbagliata\n", username, password);
            outputStream.println("DENIED");                        
        }
        
        return loggedUser;
    }

    private void insertReview(Scanner inputStream, LoggedUser loggedUser) {
        String nomeHotel = "", nomeCitta = "";
        int globalScore = 0;
        int[] singleScores = {0,0,0,0};
        String searchResult;

        nomeHotel = inputStream.nextLine();
        nomeCitta = inputStream.nextLine();
        searchResult = hotelManager.searchHotel(nomeHotel,nomeCitta);

        if (searchResult.equals("")) {
            outputStream.println("WRONG_HOTEL");
        }
        else {
            outputStream.println("HOTEL_FOUND");
            globalScore = inputStream.nextInt();
            singleScores[0] = inputStream.nextInt();
            singleScores[1] = inputStream.nextInt();
            singleScores[2] = inputStream.nextInt();
            singleScores[3] = inputStream.nextInt();

            reviewManager.addReview(loggedUser, nomeHotel, nomeCitta, globalScore, singleScores);;
            System.out.println("Nuova recensione effettuata");
            outputStream.println("ACCEPT");        
        }
    }

    private void showBadge(Scanner inputStream, LoggedUser loggedUser) {
        String msg = "Tuo badge: " + loggedUser.getBadges() + ", " + loggedUser.getNumReview() + " recensione/i fatta/e";

        outputStream.println(msg);
    }

}
