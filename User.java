public class User {
    private String username;
    private String password;

    User (String username, String PW) {
        this.username = username;
        this.password = PW;
    }

    public String getUsername() {
        return this.username;
    }
}