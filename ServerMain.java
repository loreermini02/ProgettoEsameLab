import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerMain {

    static ConfigManager configManager = new ConfigManager();
    static RankingManager rankingManager = new RankingManager();
    static HotelManager hotelManager = new HotelManager();
    static ConcurrentHashMap<String, Socket> loggedUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        
        /* Variabili di Config */
        Config configFile = configManager.readConfigFile();
        int port = configFile.getServerPort();
        int notificationPort = configFile.getnNotificationPort();
        int timerReloadRanking = configFile.getTimerReloadRanking();
        int numThreads = configFile.getNumThreads();
        /* ------------------ */

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    reloadRanking();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.schedule(task, 0, timerReloadRanking * 1000); // Esegue il metodo ogni timer secondi

        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

        // Creazione di un server socket sulla porta specificata
        try (ServerSocket serverSocket = new ServerSocket(port);
            ServerSocket notificationServerSocket = new ServerSocket(notificationPort)) {
            System.out.printf("Server in ascolto sulla Porta %d\n", port);

            // Ciclo infinito per accettare connessioni da client
            while (true) {
                // Accettazione delle connessioni dei client
                Socket clientSocket = serverSocket.accept();
                Socket notificationSocket = notificationServerSocket.accept();

                System.out.printf("Nuova connessione da %s\n", clientSocket.getInetAddress());

                // Creazione di un nuovo thread per gestire il client
                threadPool.execute(new ClientHandle(clientSocket, notificationSocket, loggedUsers));
            }
        }
    }

    //Other Methods

    private static void reloadRanking() throws IOException {
        List<Hotel> allHotel;
        Map<String, String> rankBeforeUpdate, rankAfterUpdate;
        
        rankBeforeUpdate = rankingManager.readFirstInRank();

        allHotel = hotelManager.searchAllHotels();
        rankingManager.rankHotels(allHotel);

        rankAfterUpdate = rankingManager.readFirstInRank();

        checkRanking(rankBeforeUpdate, rankAfterUpdate);

        System.out.println("Ranking Aggiornato!");
    }   

    private static void checkRanking(Map<String,String> hashMap1, Map<String,String> hashMap2) {
        Set<String> citta = hashMap1.keySet();

        for (String key : citta) {
            if (!hashMap1.get(key).equals(hashMap2.get(key))) {
                notifyLoggedUser(key, hashMap2.get(key));
            }
        }
    }

    private static void notifyLoggedUser(String city, String nomeHotel) {
        String message = "NOTIFICA: Ranking Aggiornato! A " + city + " l'hotel in 1° posizione è ' " + nomeHotel + "'";

        // ExecutorService per gestire le notifiche in modo asincrono
        ExecutorService executor = Executors.newFixedThreadPool(10); // 10 threads
        
        for (Socket userSocket : loggedUsers.values()) {
            executor.submit(() -> {
                try {
                    PrintWriter out = new PrintWriter(userSocket.getOutputStream(), true);
                    out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
