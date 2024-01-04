public class LoggedUser extends User {
    private int numReview;

    public LoggedUser(String username, String password) {
        super(username, password);
        this.numReview = 0;
        
    }

    public int getNumReview() {
        return this.numReview;
    }

    public void setNumReview(int num) {
        this.numReview = num;
    }

    public String getBadges() {
        String badge = "";
        if (numReview < 10) {
            badge = "Recensore";
        } else if (numReview < 50) {
            badge = "Recensore Esperto";
        } else if (numReview < 100) {
            badge = "Contributore";
        } else if (numReview < 200) {
            badge = "Contributore Esperto";
        } else {
            badge = "Recensore Super";
        }

        return badge;
    }
}