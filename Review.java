
public class Review {
    private String username;
    private int idHotel;
    private String nomeHotel;
    private String nomeCitta;
    private int globalScore;
    private int[] singleScores;
    private String dateTime;

    Review(String username, int idHotel, String nomeHotel, String nomeCitta, int globalScore, int[] singleScores, String dateTime) {
        this.username = username;
        this.idHotel = idHotel;
        this.nomeHotel = nomeHotel;
        this.nomeCitta = nomeCitta;
        this.globalScore = globalScore;
        
        this.singleScores = singleScores;
        this.dateTime = dateTime;
    }

    public String getUsername() {
        return username;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public String getNomeHotel() {
        return nomeHotel;
    }

    public String getNomeCitta() {
        return nomeCitta;
    }

    public int getGlobalScore() {
        return globalScore;
    }

    public int[] getSingleScores() {
        return singleScores;
    }

    public String getDateTime() {
        return dateTime;
    }
}