/**
* La classe Config rappresenta una configurazione per i parametri di connessione e comportamento del server.
* È usata per incapsulare tutte le variabili di configurazione in un singolo oggetto facilmente gestibile.
*/

public class Config {
    // Indirizzo IP
    private String serverName;

    // Porta per connessioni client-server
    private int serverPort;

    // Porta per ricezione notifiche
    private int notificationPort;

    // Numero di thread usati dal server
    private int numThreads;

    // Timer (in sec) per il ricaricamento del ranking
    private int timer;

    // Numeri giorni consentiti tra una recensione e un'altra
    private int numDay;

    public Config(String serverName, int serverPort, int notificationPort, int numThreads, int timer, int numDay) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.notificationPort = notificationPort;
        this.numThreads = numThreads;
        this.timer = timer;
        this.numDay = numDay;
    }

    public String getServerName() {
        return this.serverName;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public int getnNotificationPort() {
        return this.notificationPort;
    }

    public int getNumThreads() {
        return this.numThreads;
    }

    public int getTimerReloadRanking() {
        return this.timer;
    }
    
    public int getDaysForNewReview() {
        return this.numDay;
    }    
}