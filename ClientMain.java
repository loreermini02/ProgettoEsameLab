import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class ClientMain {
    static ConfigManager configManager = new ConfigManager();
    static Map <Integer, String> comandiDisponibili = new HashMap<>();
    
    // Ottiene gli stream di input e output dalla connessione
    static Scanner inputStream;
    static PrintWriter outputStream;
    static String notify = "";

    public static void main(String[] args) throws UnknownHostException, IOException {

        /** Variabili di Config */
        Config configFile = configManager.readConfigFile();
        String serverName = configFile.getServerName();
        int port = configFile.getServerPort();
        int notificationPort = configFile.getnNotificationPort();
        /** ------------------ */

        // Variabili che gestiscono la selezione dei comandi e il flusso del programma
        int comandoScelto = -1;
        Boolean exit = false, tryAgain;

        System.out.println("\nBENVENUT* IN HOTELIER!");
        
        comandiDisponibili.put(1, "Register");
        comandiDisponibili.put(2, "Log-In");
        comandiDisponibili.put(3, "Search All Hotels");  
        comandiDisponibili.put(4, "Search Specific Hotel");
        comandiDisponibili.put(8, "Clear Monitor");    
        comandiDisponibili.put(10, "Exit");    
 
        
        try (Socket socket = new Socket(serverName, port);
            Socket notificationSocket = new Socket(serverName, notificationPort);
            Scanner userInput = new Scanner(System.in);
            ){
                
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new Scanner(socket.getInputStream());

            // Avvia un listener per eventuali notifiche dal server
            startListeningForNotifications(notificationSocket);

            // Loop principale
            while (!exit) {
                showAllCommands(comandiDisponibili);

                do {
                    // Reset del flag per i nuovi tentativi
                    tryAgain = false; 

                    System.out.print("\nInserire un Comando (o premere 0 per mostrare i comandi disponibili): ");
                    try {
                        comandoScelto = Integer.parseInt(userInput.nextLine());

                        // Gestione della selezione del comando
                        if (comandoScelto == 0) {
                            showAllCommands(comandiDisponibili);
                            tryAgain = true; // Imposta il flag per riprovare
    
                        } else if (!comandiDisponibili.containsKey(comandoScelto)) {
                            System.out.printf("\nERRORE: Il comando %d non esiste!\n", comandoScelto);
                            tryAgain = true;
                        }
                    } catch (InputMismatchException e) {
                        System.out.printf("\nERRORE: Il comando inserito non esiste!\n");
                        tryAgain = true;
                    } catch (NumberFormatException e) {
                        System.out.printf("\nERRORE: Il comando inserito non esiste!\n");
                        tryAgain = true;
                    }

                } while(tryAgain);

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
                        
                    case 9: // Mostra notifica
                        showNotify();
                        break;

                    case 10: //Exit
                        exit = true;
                        break;

                    default:
                        break;
                }
            }
        }
    }

    /** 
     * Visualizza tutti i comandi disponibili
     * 
     * @param comandiDisponibili Una mappa che associa un intero (tasto) a una stringa descrittiva di un comando
    */
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
    
    /** 
     * Gestisce il processo di registrazione dell'utente
     * 
     * @param userInput Scanner per ricevere l'input dell'utente
    */
    private static void register(Scanner userInput) throws IOException{
        System.out.println("\nREGISTRAZIONE:");

        // Tenta di registrare l’utente. Se il metodo ritorna false, l’utente ha scelto di non riprovare dopo un input errato.
        if (!searchUserCheck(userInput, "REGISTER")) return;
        
        System.out.println("\nRegistrazione avvenuta con successo!");

        // Rimuove l’opzione di registrazione dai comandi disponibili una volta che la registrazione è stata completata
        comandiDisponibili.remove(1);
    }

    /**
    * Gestisce il processo di accesso (login) dell’utente.
    *
    * @param userInput Scanner per ricevere l’input dell’utente.
    */    
    private static void login(Scanner userInput) {        
        System.out.println("\nLOG-IN:");
        
        // Tenta di effettuare il login. Se il metodo ritorna false, l’utente ha scelto di non riprovare dopo un input errato.
        if (!searchUserCheck(userInput, "LOGIN")) return;
        
        System.out.println("\nLog-In avvenuto con successo!");
    
        // Aggiorna i comandi disponibili in base allo stato dell’utente (loggato o non loggato)
        comandiDisponibili.remove(1); // Rimuove l’opzione di registrazione
        comandiDisponibili.remove(2); // Rimuove l’opzione di login
        comandiDisponibili.put(5, "Insert Review"); // Aggiunge l’opzione di inserire una recensione
        comandiDisponibili.put(6, "Show My Badges"); // Aggiunge l’opzione di visualizzare i propri distintivi
        comandiDisponibili.put(7, "Log-Out"); // Aggiunge l’opzione di logout
    }
    
    /**
    * Gestisce il logout dell’utente.
    */
    private static void logOut() {
        outputStream.println("LOGOUT");

        // Aggiorna i comandi disponibili rimuovendo le opzioni non più disponibili dopo il logout
        comandiDisponibili.put(1, "Register");
        comandiDisponibili.put(2, "Log-In");
        comandiDisponibili.remove(5); // Rimuove l’opzione di inserire una recensione
        comandiDisponibili.remove(6); // Rimuove l’opzione di visualizzare i propri distintivi
        comandiDisponibili.remove(7); // Rimuove l’opzione di logout
    }
    
    /**
    * Interagisce con l’utente per eseguire una ricerca di un hotel specifico e visualizzare i risultati.
    *
    * @param userInput Scanner per ricevere l’input dell’utente.
    */
    private static void searchHotel(Scanner userInput) {
        String serverResponse = "";

        System.out.println("\nRICERCA HOTEL:");

        // Interazione per il controllo della ricerca; ritorna se l’utente interrompe il processo
        if (!searchHotelCheck(userInput, "SEARCH_HOTEL")) {
            outputStream.println("USER_EXIT");
            return;
        }

        // Invio della richiesta di ricerca al server
        outputStream.println("REQUEST_SEARCH");

        System.out.println("\nHotel trovato\n-------------");

        // Ricezione e stampa dei dati dell’hotel dal server
        while (inputStream.hasNextLine()) {
            serverResponse = inputStream.nextLine();
            
             // Controllo per identificare la fine dei dati relativi all’hotel
            if (serverResponse.equals("END")) {
                break;
            }

            System.out.println(serverResponse);
        }
    }

    /**
    * Gestisce la ricerca di tutti gli hotel in una città specificata dall’utente.
    *
    * @param userInput Scanner per ricevere l’input dell’utente.
    */    
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

                // Offre all’utente la possibilità di riprovare o di uscire
                try {
                    System.out.print("\nPremere 0 per riprovare o qualsiasi altro numero per uscire: ");
                    inputChoice = Integer.parseInt(userInput.nextLine());
                } catch (InputMismatchException e) {
                    outputStream.println("USER_EXIT");
                    
                    return;
                } catch (NumberFormatException e) {
                    outputStream.println("USER_EXIT");

                    return;
                }

                // Se l’utente sceglie di riprovare, imposta la flag
                if (inputChoice == 0) {
                    tryAgain = true;
                } else {
                    outputStream.println("USER_EXIT");

                    return;
                }
            }

        }while(tryAgain);

        outputStream.println("REQUEST_SEARCH");

        // Ricezione e stampa dei dati degli hotel dal server
        while (inputStream.hasNextLine()) {
            serverResponse = inputStream.nextLine();

            // Controllo per individuare la fine dei dati
            if (serverResponse.equals("END")) {
                break;
            }

            System.out.println(serverResponse);
        }
    }
 
    /**
    * Visualizza i badge dell’utente.
    */    
    private static void showBadges() {
        String serverResponse;

        outputStream.println("SHOW_BADGE");

        serverResponse = inputStream.nextLine();
        System.out.printf("\n%s\n", serverResponse);
    }

    /**
    * Consente all’utente di inserire una recensione per un hotel.
    *
    * @param userInput Scanner per ricevere l’input dell’utente.
    */    
    private static void insertReview(Scanner userInput) {
        String serverResponse = "";
        int globalScore = 0;
        int[] singleScores = {0,0,0,0};
        
        System.out.println("\nRECENSIONE:");
        
        // Controllo se l'hotel, se ritorno false vuol dire che l'utente ha sbagliato input e non vuole ritentare la ricerca
        if (!searchHotelCheck(userInput, "INSERT_REVIEW")) {
            outputStream.println("USER_EXIT");
            return;
        }

        // Richiesta di autorizzazione per inserire una recensione
        outputStream.println("REQUEST_REVIEW");
        serverResponse = inputStream.nextLine();

        if (serverResponse.equals("REQUEST_REJECTED")) {
            System.out.println("\nDevono passare almeno 30 giorni prima di poter fare una nuova recensione a questo hotel!");
            return;

        } else if (serverResponse.equals("REQUEST_ACCEPTED")) {
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
            
            // Ricezione della conferma del server e notifica all’utente del successo
            serverResponse = inputStream.nextLine();
            if (serverResponse.equals("ACCEPT")) {
                System.out.println("\nNuova recensione aggiunta con successo!");
            }
        }
    }

    // OTHER METHODS:

    /**
    * Avvia un thread dedicato all'ascolto di eventuali notifiche dal server.
    * In caso di notifica aggiunge un'opzione al menu per permettere all'utente di visualizzarla
    * 
    * @param socket Il socket attraverso il quale il client riceve notifiche dal server
    */
    private static void startListeningForNotifications(Socket socket) {
        new Thread(() -> {
            try (Scanner inputStream = new Scanner(socket.getInputStream());
                ){
                String serverNotification;

                while (inputStream.hasNextLine()) {
                    serverNotification = inputStream.nextLine();
    
                    if (serverNotification.contains("NOTIFICA")) {
                        comandiDisponibili.put(9, "Mostra notifica dal Server");
                        notify = serverNotification;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /** 
    * Visualizza la notifica ricevuta dal server e rimuove l'opzione dal menu
    */  
    private static void showNotify() {
        System.out.println("\n" + notify);
        comandiDisponibili.remove(9);
    }

    /**
    * Chiede all'utente di inserire un valore intero in un intervallo specifico e ripete la richiesta in caso di input errato.
    * @param userInput Scanner usato per ricevere l’input dell’utente.
    * @param msg Il messaggio da visualizzare all’utente.
    * @param min Il valore minimo accettabile.
    * @param max Il valore massimo accettabile.
    * @return Il valore intero inserito dall’utente.     
    */
    private static int inputCheck(Scanner userInput, String msg, int min, int max) {
        int value = -1;
        boolean tryAgain;

        do {
            tryAgain = false;

            try {
                System.out.print(msg);
                value = Integer.parseInt(userInput.nextLine());
                if (value < min || value > max) {
                    System.out.println("Valore non valido!"); 
                    tryAgain = true;
                }
            } catch (InputMismatchException e) {
                System.out.println("Valore non valido!"); 
                tryAgain = true;

            } catch (NumberFormatException e) {
                System.out.println("Valore non valido!"); 
                tryAgain = true;
            }
        } while(tryAgain);

        return value;
    }

    /**
    * Interagisce con l’utente per effettuare una ricerca di un hotel e verifica la risposta dal server.
    * Se l’hotel cercato non viene trovato, offre all’utente l’opzione di riprovare.
    *
    * @param userInput Scanner per ricevere l’input dell’utente.
    * @param searchType Stringa che indica il tipo di ricerca (SEARCH_HOTEL o INSERT_REVIEW).
    * @return true se l’hotel è stato trovato o l’utente decide di non ripetere la ricerca, altrimenti false.
    */
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
            outputStream.println(nomeHotel.trim()); // Elimino gli spazi iniziali e finali
            outputStream.println(nomeCitta.trim()); // Elimino gli spazi iniziali e finali

            serverResponse = inputStream.nextLine();

            if (serverResponse.equals("HOTEL_NOT_FOUND")){
                System.out.printf("\nHotel (%s) non trovato!\n", nomeHotel);
                try {
                    System.out.print("\nPremere 0 per riprovare o qualsiasi altro numero per uscire: ");
                    inputChoice = Integer.parseInt(userInput.nextLine());
                } catch (InputMismatchException e) {
                    return false;
                } catch (NumberFormatException e) {
                    return false;
                }
                
                if (inputChoice == 0) {
                    tryAgain = true;
                } else {
                    return false;
                }
            }
        }while(tryAgain);
                
        return true;
    }

    /** 
    * Interagisce con l’utente per eseguire il processo di login o registrazione e verifica la risposta del server.
    *
    * @param userInput Scanner per ricevere l’input dell’utente.
    * @param searchType Stringa che indica il tipo di processo (LOGIN o REGISTER).
    * @return true se il processo è andato a buon fine, false altrimenti.
    */
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
            outputStream.println(username.trim()); // Elimino gli spazi iniziali e finali
            outputStream.println(password.trim()); // Elimino gli spazi iniziali e finali

            serverResponse = inputStream.nextLine();
            if (serverResponse.equals("DENIED")){ 
                if (searchType.equals("LOGIN"))
                    System.out.printf("\nUsername (%s) e/o Password (%s) sbagliati\n", username, password);
                else if (searchType.equals("REGISTER"))
                    System.out.printf("\nUsername (%s) già utilizzato!\n", username);

                try {
                    System.out.print("\nPremere 0 per riprovare o qualsiasi altro numero per uscire: ");
                    inputChoice = Integer.parseInt(userInput.nextLine());
                } catch (InputMismatchException e) {
                    return false;
                } catch (NumberFormatException e) {
                    return false;
                }

                if (inputChoice == 0) {
                    tryAgain = true;
                } else {
                    return false;
                }
            }
        }while(tryAgain);

        return true;
    }

    /**
    * Pulisce il terminale in base al sistema operativo su cui è in esecuzione il client.
    */
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