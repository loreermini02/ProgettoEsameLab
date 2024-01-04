import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandle implements Runnable {
    private Socket clientSocket;
    private PrintWriter outputStream = null;
    private UserManager UserManager;
    private HotelManager HotelManager;
    private ReviewManager ReviewManager;

    ClientHandle(Socket s) throws Exception {
        this.clientSocket = s;
        this.outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

        this.UserManager = new UserManager();
        this.HotelManager = new HotelManager();
        this.ReviewManager = new ReviewManager();
    }

    @Override
    public void run() {
        String clientCommand = "";
        LoggedUser loggedUser;

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
                }
                // INSERT REVIEW
                else if (clientCommand.equals("INSERT_REVIEW")) {
                    insertReview(inputStream);
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

        if (!UserManager.checkUsername(username)[0].isEmpty()) {
            //System.out.printf("%s già registrato\n", username);
            outputStream.println("DENIED");
        } else {
            UserManager.addUser(username, password);
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

        if (UserManager.checkUsername(username, password)) {
            loggedUser = new LoggedUser(username, password);

            System.out.printf("Nuovo Accesso (%s)\n", username);
            outputStream.println("ACCEPT");
        } else {
            //System.out.printf("Username (%s) non esistente e/o Password (%s) sbagliata\n", username, password);
            outputStream.println("DENIED");                        
        }
        
        return loggedUser;
    }

    private void insertReview(Scanner inputStream) {
        String nomeHotel = "", nomeCitta = "";
        int globalScore = 0;
        int[] singleScores = {0,0,0,0};
        String[] searchResult = {"",""}; //{NomeHotel, Citta}

        nomeHotel = inputStream.nextLine();
        nomeCitta = inputStream.nextLine();
        globalScore = inputStream.nextInt();
        singleScores[0] = inputStream.nextInt();
        singleScores[1] = inputStream.nextInt();
        singleScores[2] = inputStream.nextInt();
        singleScores[3] = inputStream.nextInt();

        searchResult = HotelManager.searchHotel(nomeHotel,nomeCitta);
        if (searchResult[0].equalsIgnoreCase("") && searchResult[1].equalsIgnoreCase("")) {
            System.out.printf("Hotel (%s) e Città (%s) non esistenti!\n", nomeHotel, nomeCitta);
            outputStream.println("WRONG_HOTEL_and_CITY");
        } else if (searchResult[0].equalsIgnoreCase("")) {
            System.out.printf("Hotel (%s) non esistente!\n", nomeHotel);
            outputStream.println("WRONG_HOTEL");

        } else if (searchResult[1].equalsIgnoreCase("")) {
            System.out.printf("Città (%s) non esistente!\n", nomeCitta);
            outputStream.println("WRONG_CITY");
        } else {
            ReviewManager.addReview(nomeHotel, nomeCitta, globalScore, singleScores);;
            System.out.println("Nuova recensione effettuata");
            outputStream.println("ACCEPT");        
        }
    }
}
