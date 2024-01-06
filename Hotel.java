import java.util.List;

public class Hotel {
    private int id;
    private String name;
    private String description;
    private String city;
    private String phone;
    private List<String> services;
    private int rate;
    private Ratings ratings;

    public Hotel (int id, String name, String description, String city, String phone, List<String> services, int rate, int[] ratings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = rate;

        this.ratings = new Ratings();
        this.ratings.cleaning = ratings[0];
        this.ratings.position = ratings[1];
        this.ratings.services = ratings[2];
        this.ratings.quality = ratings[3];

    } 

    class Ratings {
        int cleaning;
        int position;
        int services;
        int quality;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getServices() {
        return services;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int[] getRatings() {
        int[] ratingsArray = {ratings.cleaning, ratings.position, ratings.services, ratings.quality};

        return ratingsArray;
    }

    public void setRatings(int[] ratings) {
        this.ratings.cleaning = ratings[0];
        this.ratings.position = ratings[1];
        this.ratings.services = ratings[2];
        this.ratings.quality = ratings[3];
    }
}