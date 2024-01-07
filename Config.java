public class Config {
    private String serverName;
    private int serverPort;
    private int notificationPort;
    private int numThreads;
    private int timer; // In secondi
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
