import java.util.List;

public class Hotel {
    private int id;
    private String name;
    private String description;
    private String city;
    private String phone;
    private List<String> services;
    private int rate;
    private int[] ratings = {0,0,0,0};

    public Hotel (int id, String name, String description, String city, String phone, List<String> services, int rate, int[] ratings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = rate;
        this.ratings = ratings;
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

    public int[] getRatings() {
        return ratings;
    }
}