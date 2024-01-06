import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class HotelManager {
    private static final String HOTELS_FILE_PATH = "JSON/Hotels.json";

    public void loadReview (int idHotel, int globalScore, int[] singleScores) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type listType = new TypeToken<List<Hotel>>(){}.getType();

        try (FileReader jsonReader = new FileReader(HOTELS_FILE_PATH)) {
            List<Hotel> hotels = gson.fromJson(jsonReader, listType);

            for (Hotel hotel : hotels) {
                if (hotel.getId() == idHotel) {
                    hotel.setRate(globalScore);
                    hotel.setRatings(singleScores);

                    break;
                }
            }

            // Scrivi i nuovi dati nel file JSON
            try (FileWriter writer = new FileWriter(HOTELS_FILE_PATH)) {
                gson.toJson(hotels, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Hotel searchHotel (String nomeHotel, String citta) {
        String fieldName = "";

        // Proprietà Hotel
        int id = 0, rate = 0;
        String name = null, description = "", city = "", phone = "";
        List<String> services = null;
        int[] ratings = {0,0,0,0};

        try (JsonReader jsonReader = new JsonReader(new FileReader(HOTELS_FILE_PATH))) {
            jsonReader.beginArray(); // Inizia a leggere l'array

            // Itera attraverso gli oggetti nell'array
            while (jsonReader.hasNext()) {
                jsonReader.beginObject(); // Inizia a leggere un oggetto

                // Leggi le proprietà dell'oggetto
                while (jsonReader.hasNext()) {
                    fieldName = jsonReader.nextName();
                    switch (fieldName) {
                        case "id":
                            id = jsonReader.nextInt();
                            break;
                        case "name":
                            name = jsonReader.nextString();
                            break;
                        case "description":
                            description = jsonReader.nextString();
                            break;
                        case "city":
                            city = jsonReader.nextString();
                            break;
                        case "phone":
                            phone = jsonReader.nextString();
                            break;
                        case "services":
                            services = readStringList(jsonReader);
                            break;
                        case "rate":
                            rate = jsonReader.nextInt();
                            break;
                        case "ratings":
                            ratings = readRatingList(jsonReader);
                            break;
                        default:
                            jsonReader.skipValue(); // Ignora il valore di altri campi
                            break;                        
                    }
                }

                jsonReader.endObject(); // Fine dell'oggetto

                // Controlla se l'hotel corrente ha il nome cercato ed è nella città richiesta
                if (name != null && name.equalsIgnoreCase(nomeHotel) && city.equalsIgnoreCase(citta)) {
                    return new Hotel(id, name, description, city, phone, services, rate, ratings);
                }
            }
            
            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Hotel> searchHotelByCity (String citta) {
        List<Hotel> resultHotels = new ArrayList<>();
        
        String fieldName = "";

        // Proprietà Hotel
        int id = 0, rate = 0;
        String name = null, description = "", city = "", phone = "";
        List<String> services = null;
        int[] ratings = {0,0,0,0};

        try (JsonReader jsonReader = new JsonReader(new FileReader(HOTELS_FILE_PATH))) {
            jsonReader.beginArray(); // Inizia a leggere l'array

            // Itera attraverso gli oggetti nell'array
            while (jsonReader.hasNext()) {
                jsonReader.beginObject(); // Inizia a leggere un oggetto

                // Leggi le proprietà dell'oggetto
                while (jsonReader.hasNext()) {
                    fieldName = jsonReader.nextName();
                    switch (fieldName) {
                        case "id":
                            id = jsonReader.nextInt();
                            break;
                        case "name":
                            name = jsonReader.nextString();
                            break;
                        case "description":
                            description = jsonReader.nextString();
                            break;
                        case "city":
                            city = jsonReader.nextString();
                            break;
                        case "phone":
                            phone = jsonReader.nextString();
                            break;
                        case "services":
                            services = readStringList(jsonReader);
                            break;
                        case "rate":
                            rate = jsonReader.nextInt();
                            break;
                        case "ratings":
                            jsonReader.beginObject();
                            ratings = readRatingList(jsonReader);
                            break;
                        default:
                            jsonReader.skipValue(); // Ignora il valore di altri campi
                            break;                        
                    }
                }

                jsonReader.endObject(); // Fine dell'oggetto

                // Controlla se l'hotel corrente ha il nome cercato ed è nella città richiesta
                if (name != null && city.equalsIgnoreCase(citta)) {
                    resultHotels.add(new Hotel(id, name, description, city, phone, services, rate, ratings));
                }
            }
            
            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultHotels;
    }
    
    public List<Hotel> searchAllHotels () {
       List<Hotel> resultHotels = new ArrayList<>();

        String fieldName = "";

        // Proprietà Hotel
        int id = 0, rate = 0;
        String name = null, description = "", city = "", phone = "";
        List<String> services = null;
        int[] ratings = {0,0,0,0};

        try (JsonReader jsonReader = new JsonReader(new FileReader(HOTELS_FILE_PATH))) {
            jsonReader.beginArray(); // Inizia a leggere l'array

            // Itera attraverso gli oggetti nell'array
            while (jsonReader.hasNext()) {
                jsonReader.beginObject(); // Inizia a leggere un oggetto

                // Leggi le proprietà dell'oggetto
                while (jsonReader.hasNext()) {
                    fieldName = jsonReader.nextName();
                    switch (fieldName) {
                        case "id":
                            id = jsonReader.nextInt();
                            break;
                        case "name":
                            name = jsonReader.nextString();
                            break;
                        case "description":
                            description = jsonReader.nextString();
                            break;
                        case "city":
                            city = jsonReader.nextString();
                            break;
                        case "phone":
                            phone = jsonReader.nextString();
                            break;
                        case "services":
                            services = readStringList(jsonReader);
                            break;
                        case "rate":
                            rate = jsonReader.nextInt();
                            break;
                        case "ratings":
                            ratings = readRatingList(jsonReader);
                            break;
                        default:
                            jsonReader.skipValue(); // Ignora il valore di altri campi
                            break;                        
                    }
                }

                jsonReader.endObject(); // Fine dell'oggetto

                // Controlla se l'hotel corrente esiste
                if (name != null) {
                    resultHotels.add(new Hotel(id, name, description, city, phone, services, rate, ratings));
                }
            }
            
            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultHotels;        
    }

    // Other methods
    private static List<String> readStringList(JsonReader jsonReader) throws IOException {
        List<String> list = new ArrayList<>();

        jsonReader.beginArray(); // Inizia a leggere l'array

        while (jsonReader.hasNext()) {
            list.add(jsonReader.nextString());
        }

        jsonReader.endArray(); // Fine dell'array
        return list;
    }

    private static int[] readRatingList(JsonReader jsonReader) throws IOException {
        int[] ratings = {0,0,0,0};
 
        jsonReader.beginObject(); // Inizia a leggere l'oggetto delle valutazioni
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            switch (fieldName) {
                case "cleaning":
                    ratings[0] = jsonReader.nextInt();
                    break;

                case "position":
                    ratings[1] = jsonReader.nextInt();
                    break;
                
                case "services":
                    ratings[2] = jsonReader.nextInt();
                    break;
                
                case "quality":
                    ratings[3] = jsonReader.nextInt();
                    break;

                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject(); // Fine dell'oggetto delle valutazioni
        return ratings;        
    }
}
