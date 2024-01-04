import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    // Numero massimo di thread nel pool
    static int numThreads = 20;
    // Porta su cui il server è in ascolto
    static int port = 8080;

    public static void main(String[] args) throws Exception {
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
                threadPool.execute(new ClientHandle(clientSocket));
            }
        }
    }
}
