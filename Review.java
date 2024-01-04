public class Review {
    private String username;
    private int globalScore;
    private int[] singleScores;

    Review(String username, int globalScore, int[] singleScores) {
        this.username = username;
        this.globalScore = globalScore;
        this.singleScores = singleScores;
    }
}
