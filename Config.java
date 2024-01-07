public class Config {
    private String serverName;
    private int serverPort;
    private int numThread;
    private int timer; // In secondi
    private int numDay;

    public Config(String serverName, int serverPort, int numThread, int timer, int numDay) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.numThread = numThread;
        this.timer = timer;
        this.numDay = numDay;
    }

    public String getServerName() {
        return this.serverName;
    }

    public int getsServerPort() {
        return this.serverPort;
    }

    public int getNumThread() {
        return this.numThread;
    }

    public int getTimer() {
        return this.timer;
    }
    
    public int getNumDay() {
        return this.numDay;
    }    
}
