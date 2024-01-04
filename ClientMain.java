import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClientMain {
    static int port = 8080;
    static Map <Integer, String> comandiDisponibili = new HashMap<>();
    // Ottiene gli stream di input e output dalla connessione
    static Scanner inputStream;
    static PrintWriter outputStream;

    public static void main(String[] args) throws UnknownHostException, IOException {
        int comandoScelto;
        Boolean exit = false;

        System.out.println("\nBENVENUT* IN HOTELIER!");
        
        comandiDisponibili.put(1, "Register");
        comandiDisponibili.put(2, "Log-In");
        comandiDisponibili.put(3, "Search All Hotels");  
        comandiDisponibili.put(8, "Exit");    
 
        
        try (Socket socket = new Socket("localhost", port);
            Scanner userInput = new Scanner(System.in);
            ){
                
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new Scanner(socket.getInputStream());

            while (!exit) {
                showAllCommands(comandiDisponibili);

                do {
                    System.out.print("\nInserire un Comando (o premere 0 per mostrare i comandi disponibili): ");
                    comandoScelto = userInput.nextInt();

                    if (comandoScelto == 0) {
                        showAllCommands(comandiDisponibili);

                    } else if (!comandiDisponibili.containsKey(comandoScelto)) {
                        System.out.printf("\nERRORE: Il comando %d non esiste!\n", comandoScelto);
                    }
                } while(!comandiDisponibili.containsKey(comandoScelto));
                userInput.nextLine(); // Consuma il carattere di newline residuo nel buffer

                switch (comandoScelto) {
                    case 1: // Sign-Up
                        register(userInput);
                        
                        break;

                    case 2: // Log-In
                        login(userInput);

                        break;

                    case 3: // Search All Hotels
                        searchAllHotels();
                        break;
                    
                    case 4: // Search Hotel
                        searchHotel();
                        break;
                    
                    case 5: // Insert Review
                        insertReview(userInput);

                        break;
                    
                    case 6: // Show My Badges
                        showBadges();
                        break;
                    
                    case 7: // Log-Out
                        logOut();
                        break;
                    
                    case 8: //Exit
                        exit = true;
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private static void showAllCommands (Map <Integer, String> comandiDisponibili) {
        int chiave;
        String valore;

        System.out.println("\nComandi Disponibili:");
        System.out.println("--------------------");

        // Iterazione attraverso tutte le coppie chiave-valore nel dizionario
        for (Map.Entry<Integer, String> entry : comandiDisponibili.entrySet()) {
            chiave = entry.getKey();
            valore = entry.getValue();

            System.out.printf("[Tasto %d] %s\n", chiave, valore);
        }       
    }

    private static void register(Scanner userInput) throws IOException{
        String username = "", password = "", serverResponse = "";

        System.out.println("\nREGISTRAZIONE:\n");
        
        System.out.print("Username: ");
        username = userInput.nextLine();

        System.out.print("Password: ");
        password = userInput.nextLine();
        
        outputStream.println("REGISTER");
        outputStream.println(username);
        outputStream.println(password);
        
        serverResponse = inputStream.nextLine();
        if (serverResponse.equals("DENIED")) {
            System.out.printf("\nUsername (%s) già utilizzato!\n", username);
        } else if (serverResponse.equals("ACCEPT")) {
            System.out.println("\nRegistrazione avvenuta con successo!");

            comandiDisponibili.remove(1); // Elimino 'Register' dai comandi disponibili
        }
    }

    private static void login(Scanner userInput) {
        String username = "", password = "", serverResponse = "";

        System.out.println("\nLOG-IN:\n");

        System.out.print("Username: ");
        username = userInput.nextLine();

        System.out.print("Password: ");
        password = userInput.nextLine();

        outputStream.println("LOGIN");
        outputStream.println(username);
        outputStream.println(password);

        serverResponse = inputStream.nextLine();
        if (serverResponse.equals("DENIED")) {
            System.out.printf("\nUsername (%s) e/o Password (%s) sbagliati", username, password);
        } else if (serverResponse.equals("ACCEPT")) {
            System.out.println("\nLog-In avvenuto con successo!");

            comandiDisponibili.remove(1); // Elimino 'Register' dai comandi disponibili
            comandiDisponibili.remove(2); // Elimino 'LogIn' dai comandi disponibili
            comandiDisponibili.put(4, "Search Specific Hotel");
            comandiDisponibili.put(5, "Insert Review");
            comandiDisponibili.put(6, "Show My Badges");
            comandiDisponibili.put(7, "Log-Out");
        }
    }

    private static void logOut() {
        comandiDisponibili.put(1, "Register");
        comandiDisponibili.put(2, "Log-In");
        comandiDisponibili.remove(4); // Elimino 'Search Specific Hotel' dai comandi disponibili
        comandiDisponibili.remove(5); // Elimino 'Insert Review' dai comandi disponibili
        comandiDisponibili.remove(6); // Elimino 'Show My Badges' dai comandi disponibili
        comandiDisponibili.remove(7); // Elimino 'Log Out' dai comandi disponibili
    }

    private static void searchAllHotels() {
        
    }

    private static void showBadges() {
        String serverResponse;

        outputStream.println("SHOW_BADGE");
        serverResponse = inputStream.nextLine();
        System.out.printf("\n%s\n", serverResponse);
    }

    private static void insertReview(Scanner userInput) {
        String nomeHotel = "", nomeCitta = "", serverResponse = "";
        int globalScore = 0;
        int[] singleScores = {0,0,0,0};

        System.out.println("\n RECENSIONE:\n");
    
        do {
            System.out.print("Inserire Nome Hotel: ");
            nomeHotel = userInput.nextLine();
    
            System.out.print("Inserire Nome Città: ");
            nomeCitta = userInput.nextLine();

            outputStream.println("INSERT_REVIEW");
            outputStream.println(nomeHotel);
            outputStream.println(nomeCitta);

            serverResponse = inputStream.nextLine();

            if (serverResponse.equals("WRONG_HOTEL")){
                System.out.printf("\nHotel (%s) non trovato! Riprova...\n\n", nomeHotel);
            }
        }while(!serverResponse.equals("HOTEL_FOUND"));
        
        globalScore = valueCheck(userInput, "Inserire Global Score (0-5): ", 0, 5);
        singleScores[0] = valueCheck(userInput, "Inserire Single Score per Posizione (0-5): ", 0, 5);
        singleScores[1] = valueCheck(userInput, "Inserire Single Score per Pulizia (0-5): ", 0, 5);
        singleScores[2] = valueCheck(userInput, "Inserire Single Score per Servizio (0-5): ", 0, 5);
        singleScores[3] = valueCheck(userInput, "Inserire Single Score per Qualità (0-5): ", 0, 5);
        outputStream.println(globalScore);
        outputStream.println(singleScores[0]);
        outputStream.println(singleScores[1]);
        outputStream.println(singleScores[2]);
        outputStream.println(singleScores[3]);
        
        serverResponse = inputStream.nextLine();
        if (serverResponse.equals("ACCEPT")) {
            System.out.println("\nNuova recensione aggiunta con successo!");
        }
    }

    private static void searchHotel() {}

    // Other Methods
    private static int valueCheck(Scanner userInput, String msg, int min, int max) {
        int value;

        do {
            System.out.print(msg);
            value = userInput.nextInt();

            if (value < min || value > max)
            System.out.println("Valore non valido!");
                
        } while(value < min || value > max);

        return value;
    }
}