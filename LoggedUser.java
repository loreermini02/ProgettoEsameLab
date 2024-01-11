/**
* La classe LoggedUser estende User ed è utilizzata 
* per rappresentare un utente loggato nel sistema.
* Memorizza il numero di recensioni scritte dall’utente 
* e fornisce un metodo per ottenere i distintivi in base al loro numero.
*/
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

    /**
    * Ottiene il distintivo (badge) per l’utente, basato sul numero di recensioni scritte.
    * I distintivi variano a seconda della quantità di contributi forniti dall’utente.
    *
    * @return Il distintivo dell’utente come stringa.
    */    
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