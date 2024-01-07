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
    // Numero massimo di thread nel pool
    static int numThreads = 20;
    // Porta su cui il server è in ascolto
    static int port = 8080;
    static RankingManager rankingManager = new RankingManager();
    static HotelManager hotelManager = new HotelManager();
    static ConcurrentHashMap<String, Socket> loggedUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Numero errato di parametri!");
            return;
        }

        int numSec;

        try {
            numSec = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Errore: il parametro deve essere un intero!");
            return;
        }

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

        timer.schedule(task, 0, numSec * 1000); // Esegue il metodo ogni numSec secondi

        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

        // Creazione di un server socket sulla porta specificata
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Server in ascolto sulla Porta %d\n", port);

            // Ciclo infinito per accettare connessioni da client
            while (true) {
                // Accetta una connessione da un client
                Socket clientSocket = serverSocket.accept();

                System.out.printf("Nuova connessione da %s\n", clientSocket.getInetAddress());

                // Creazione di un nuovo thread per gestire il client
                threadPool.execute(new ClientHandle(clientSocket, loggedUsers));
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
        String message = "Ranking Aggiornato! A " + city + " l'hotel in 1° posizione è ' " + nomeHotel + "'";

        for (Socket userSocket : loggedUsers.values()) {
            try {
                PrintWriter writer = new PrintWriter(userSocket.getOutputStream(), true);
                writer.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
