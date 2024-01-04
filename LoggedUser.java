public class LoggedUser extends User {
    private int numReview;

    public LoggedUser(String username, String password) {
        super(username, password);
        this.numReview = 0;
    }

    public int getNumReview() {
        return this.numReview;
    }
}