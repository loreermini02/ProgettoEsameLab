public class User {
    private String username;
    private String password;
    private int numReviews;

    User (String username, String PW) {
        this.username = username;
        this.password = PW;
        this.numReviews = 0;
    }

    public String getUsername() {
        return this.username;
    }
}
