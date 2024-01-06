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
    private double score;
    private int numReviews;
    private String dateLastReview;

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

        this.score = 0.0;
        this.numReviews = 0;

        this.dateLastReview = "";

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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getNumReviews() {
        return numReviews;
    }

    public void IncrementNumReview() {
        this.numReviews++;
    }

    public void setNumReviews(int num) {
        this.numReviews = num;
    }

    public void setDateLastReview(String date) {
        this.dateLastReview = date;
    }

}