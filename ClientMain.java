import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class ClientMain {
    static int port = 8080;
    static Map <Integer, String> comandiDisponibili = new HashMap<>();
    // Ottiene gli stream di input e output dalla connessione
    static Scanner inputStream;
    static PrintWriter outputStream;

    public static void main(String[] args) throws UnknownHostException, IOException {
        int comandoScelto = -1;
        Boolean exit = false, tryAgain;

        System.out.println("\nBENVENUT* IN HOTELIER!");
        
        comandiDisponibili.put(1, "Register");
        comandiDisponibili.put(2, "Log-In");
        comandiDisponibili.put(3, "Search All Hotels");  
        comandiDisponibili.put(4, "Search Specific Hotel");
        comandiDisponibili.put(8, "Clear Monitor");    
        comandiDisponibili.put(9, "Exit");    
 
        
        try (Socket socket = new Socket("localhost", port);
            Scanner userInput = new Scanner(System.in);
            ){
                
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new Scanner(socket.getInputStream());

            while (!exit) {
                showAllCommands(comandiDisponibili);

                do {
                    tryAgain = false;

                    System.out.print("\nInserire un Comando (o premere 0 per mostrare i comandi disponibili): ");
                    try {
                        comandoScelto = userInput.nextInt();
    
                        if (comandoScelto == 0) {
                            showAllCommands(comandiDisponibili);
                            tryAgain = true;
    
                        } else if (!comandiDisponibili.containsKey(comandoScelto)) {
                            System.out.printf("\nERRORE: Il comando %d non esiste!\n", comandoScelto);
                            tryAgain = true;
                        }
                    } catch (InputMismatchException e) {
                        System.out.printf("\nERRORE: Il comando inserito non esiste!\n");
                        tryAgain = true;
                        
                        // Consuma il resto della riga di input errata
                        userInput.nextLine();
                    }

                } while(tryAgain);
                userInput.nextLine(); // Consuma il carattere di newline residuo nel buffer

                switch (comandoScelto) {
                    case 1: // Sign-Up
                        register(userInput);
                        
                        break;

                    case 2: // Log-In
                        login(userInput);

                        break;

                    case 3: // Search All Hotels
                        searchAllHotels(userInput);
                        break;
                    
                    case 4: // Search Hotel
                        searchHotel(userInput);
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

                    case 8: // Clear
                        clearTerminal();
                        break;
                    
                    case 9: //Exit
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

        System.out.println("\n--------------------");
        System.out.println("Comandi Disponibili:");
        System.out.println("--------------------");

        // Iterazione attraverso tutte le coppie chiave-valore nel dizionario
        for (Map.Entry<Integer, String> entry : comandiDisponibili.entrySet()) {
            chiave = entry.getKey();
            valore = entry.getValue();

            System.out.printf("[Tasto %d] %s\n", chiave, valore);
        }       
    }

    private static void register(Scanner userInput) throws IOException{
        System.out.println("\nREGISTRAZIONE:");

        // Controllo se l'user è presente, se ritorno false vuol dire che l'utente ha sbagliato input e non vuole ritentare l'accesso
        if (!searchUserCheck(userInput, "REGISTER")) return;
        
        System.out.println("\nRegistrazione avvenuta con successo!");

        comandiDisponibili.remove(1); // Elimino 'Register' dai comandi disponibili
    }

    private static void login(Scanner userInput) {        
        System.out.println("\nLOG-IN:");
        
        // Controllo se l'user è presente, se ritorno false vuol dire che l'utente ha sbagliato input e non vuole ritentare l'accesso
        if (!searchUserCheck(userInput, "LOGIN")) return;
        
        System.out.println("\nLog-In avvenuto con successo!");
    
        comandiDisponibili.remove(1); // Elimino 'Register' dai comandi disponibili
        comandiDisponibili.remove(2); // Elimino 'Login' dai comandi disponibili
        comandiDisponibili.put(5, "Insert Review");
        comandiDisponibili.put(6, "Show My Badges");
        comandiDisponibili.put(7, "Log-Out");
    }
    
    private static void logOut() {
        comandiDisponibili.put(1, "Register");
        comandiDisponibili.put(2, "Log-In");
        comandiDisponibili.remove(5); // Elimino 'Insert Review' dai comandi disponibili
        comandiDisponibili.remove(6); // Elimino 'Show My Badges' dai comandi disponibili
        comandiDisponibili.remove(7); // Elimino 'Log Out' dai comandi disponibili
    }
    
    private static void searchHotel(Scanner userInput) {
        String serverResponse = "";

        System.out.println("\nRICERCA HOTEL:");
        
        // Controllo se l'hotel, se ritorno false vuol dire che l'utente ha sbagliato input e non vuole ritentare la ricerca
        if (!searchHotelCheck(userInput, "SEARCH_HOTEL")) return;

        System.out.println("\nHotel trovato\n-------------");

        while (inputStream.hasNextLine()) {
            serverResponse = inputStream.nextLine();
            
            if (serverResponse.equals("END")) {
                break;
            }

            System.out.println(serverResponse);
        }
    }

    private static void searchAllHotels(Scanner userInput) {
        String serverResponse = "";
        System.out.println("\nRICERCA DI TUTTI GLI HOTEL DI UNA CITTA':");

        int inputChoice;
        boolean tryAgain;
        String nomeCitta;

        do {
            tryAgain = false;
    
            System.out.print("\nInserire Nome Città: ");
            nomeCitta = userInput.nextLine();

            outputStream.println("SEARCH_ALL_HOTEL");
            outputStream.println(nomeCitta);

            serverResponse = inputStream.nextLine();

            if (serverResponse.equals("HOTEL_NOT_FOUND")){
                System.out.printf("\nQuesta città (%s) non ha Hotel!\n", nomeCitta);
                try {
                    System.out.print("\nPremere 0 per riprovare o qualsiasi altro numero per uscire: ");
                    inputChoice =  userInput.nextInt();
                } catch (InputMismatchException e) {
                    // Consuma il resto della riga di input errata
                    userInput.nextLine();

                    return;
                }
                
                // Consuma il carattere di nuova linea nel buffer
                userInput.nextLine();
                if (inputChoice == 0) {
                    tryAgain = true;
                } else {
                    return;
                }
            }

        }while(tryAgain);

        while (inputStream.hasNextLine()) {
            serverResponse = inputStream.nextLine();

            if (serverResponse.equals("END")) {
                break;
            }

            System.out.println(serverResponse);
            
        }
    }
    
    private static void showBadges() {
        String serverResponse;

        outputStream.println("SHOW_BADGE");
        serverResponse = inputStream.nextLine();
        System.out.printf("\n%s\n", serverResponse);
    }

    private static void insertReview(Scanner userInput) {
        String serverResponse = "";
        int globalScore = 0;
        int[] singleScores = {0,0,0,0};
        
        System.out.println("\nRECENSIONE:");
        
        // Controllo se l'hotel, se ritorno false vuol dire che l'utente ha sbagliato input e non vuole ritentare la ricerca
        if (!searchHotelCheck(userInput, "INSERT_REVIEW")) return;
        
        globalScore = inputCheck(userInput, "Inserire Global Score (0-5): ", 0, 5);
        singleScores[1] = inputCheck(userInput, "Inserire Single Score per Pulizia (0-5): ", 0, 5);
        singleScores[0] = inputCheck(userInput, "Inserire Single Score per Posizione (0-5): ", 0, 5);
        singleScores[2] = inputCheck(userInput, "Inserire Single Score per Servizio (0-5): ", 0, 5);
        singleScores[3] = inputCheck(userInput, "Inserire Single Score per Qualità (0-5): ", 0, 5);
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

    // Other Methods
    private static int inputCheck(Scanner userInput, String msg, int min, int max) {
        int value = -1;
        boolean tryAgain;

        do {
            tryAgain = false;

            try {
                System.out.print(msg);
                value = userInput.nextInt();
                if (value < min || value > max) {
                    System.out.println("Valore non valido!"); 
                    tryAgain = true;
                }
            } catch (InputMismatchException e) {
                System.out.println("Valore non valido!"); 
                tryAgain = true;

                // Consuma il resto della riga di input errata
                userInput.nextLine();
            }
                
        } while(tryAgain);

        return value;
    }

    private static boolean searchHotelCheck(Scanner userInput, String searchType) {
        int inputChoice;
        boolean tryAgain;
        String nomeHotel, nomeCitta, serverResponse;

        do {
            tryAgain = false;

            System.out.print("\nInserire Nome Hotel: ");
            nomeHotel = userInput.nextLine();
    
            System.out.print("Inserire Nome Città: ");
            nomeCitta = userInput.nextLine();

            outputStream.println(searchType);
            outputStream.println(nomeHotel);
            outputStream.println(nomeCitta);

            serverResponse = inputStream.nextLine();

            if (serverResponse.equals("HOTEL_NOT_FOUND")){
                System.out.printf("\nHotel (%s) non trovato!\n", nomeHotel);
                try {
                    System.out.print("\nPremere 0 per riprovare o qualsiasi altro numero per uscire: ");
                    inputChoice =  userInput.nextInt();
                } catch (InputMismatchException e) {
                    // Consuma il resto della riga di input errata
                    userInput.nextLine();

                    return false;
                }
                
                // Consuma il carattere di nuova linea nel buffer
                userInput.nextLine();
                if (inputChoice == 0) {
                    tryAgain = true;
                } else {
                    return false;
                }
            }

        }while(tryAgain);

        return true;
    }

    private static boolean searchUserCheck(Scanner userInput, String searchType) {
        String username, password, serverResponse;
        int inputChoice;
        boolean tryAgain;

        do {
            tryAgain = false;

            System.out.print("\nUsername: ");
            username = userInput.nextLine();
    
            System.out.print("Password: ");
            password = userInput.nextLine();

            outputStream.println(searchType);
            outputStream.println(username);
            outputStream.println(password);

            serverResponse = inputStream.nextLine();
            if (serverResponse.equals("DENIED")){ 
                if (searchType.equals("LOGIN"))
                    System.out.printf("\nUsername (%s) e/o Password (%s) sbagliati", username, password);
                else if (searchType.equals("REGISTER"))
                    System.out.printf("\nUsername (%s) già utilizzato!", username);

                try {
                    System.out.print("\nPremere 0 per riprovare o qualsiasi altro numero per uscire: ");
                    inputChoice =  userInput.nextInt();
                } catch (InputMismatchException e) {
                    // Consuma il resto della riga di input errata
                    userInput.nextLine();

                    return false;
                }
                
                // Consuma il carattere di nuova linea nel buffer
                userInput.nextLine();

                if (inputChoice == 0) {
                    tryAgain = true;
                } else {
                    return false;
                }
            }
        }while(tryAgain);

        return true;
    }

    private static void clearTerminal() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Sistemi Unix/Linux/Mac
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            } else if (os.contains("win")) {
                // Sistemi Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Altri sistemi operativi, gestione in modo generico
                System.out.println("Impossibile eseguire il clear del terminale su questo sistema operativo.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}