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


/**
* Gestisce le opererazioni sul file Json contenente i dettagli di ogni Hotel.
*/
public class HotelManager {
    private static final String HOTEL_FILE_PATH = "JSON/Hotels.json";

    /**
    * Aggiorna le recensioni relative ad un hotel.
    *
    * @param hotel Oggetto Hotel contenente le informazioni aggiornate da salvare.
    */    
    public synchronized void loadReview (Hotel hotel) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type listType = new TypeToken<List<Hotel>>(){}.getType();

        try (FileReader jsonReader = new FileReader(HOTEL_FILE_PATH)) {
            List<Hotel> hotels = gson.fromJson(jsonReader, listType);

            for (Hotel h : hotels) {
                if (h.getId() == hotel.getId()) {
                    h.setNumReviews(hotel.getNumReviews());
                    h.setRate(hotel.getRate());
                    h.setRatings(hotel.getRatings());
                    h.setScore(hotel.getScore());
                    h.setDateLastReview(hotel.getDateLastReview());
                    
                    break;
                }
            }

            // Scrivi i nuovi dati nel file JSON
            try (FileWriter writer = new FileWriter(HOTEL_FILE_PATH)) {
                gson.toJson(hotels, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * Ricerca un hotel per nome e città.
    *
    * @param nomeHotel Nome dell’hotel da cercare.
    * @param citta Città in cui si trova l’hotel.
    * @return Oggetto Hotel che corrisponde ai criteri di ricerca oppure null se l’hotel non è trovato.
    */    
    public synchronized Hotel searchHotel (String nomeHotel, String citta) {
        String fieldName = "";

        // Proprietà Hotel
        int id = 0, rate = 0, numReviews = 0;
        Double score = 0.0;
        String name = "", description = "", city = "", phone = "", dateLastReview = "";
        List<String> services = null;
        int[] ratings = {0,0,0,0};

        try (JsonReader jsonReader = new JsonReader(new FileReader(HOTEL_FILE_PATH))) {
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
                        case "score":
                            score = jsonReader.nextDouble();
                            break;
                        case "numReviews":
                            numReviews = jsonReader.nextInt();
                            break;
                        case "dateLastReview":
                            dateLastReview = jsonReader.nextString();
                            break;
                        default:
                            jsonReader.skipValue(); // Ignora il valore di altri campi
                            break;                        
                    }
                }

                jsonReader.endObject(); // Fine dell'oggetto

                // Controlla se l'hotel corrente ha il nome cercato ed è nella città richiesta
                if (!name.isBlank() && name.equalsIgnoreCase(nomeHotel) && city.equalsIgnoreCase(citta)) {
                    return new Hotel(id, name, description, city, phone, services, rate, ratings, score, numReviews, dateLastReview);
                }
            }
            
            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
    * Ricerca tutti gli hotel nella città specificata.
    *
    * @param citta Città in cui si vogliono trovare gli hotel.
    * @return Lista di oggetti Hotel che si trovano nella città specificata.
    */    
    public synchronized List<Hotel> searchHotelByCity (String citta) {
        List<Hotel> resultHotels = new ArrayList<>();
        
        String fieldName = "";

        // Proprietà Hotel
        int id = 0, rate = 0, numReviews = 0;
        Double score = 0.0;
        String name = "", description = "", city = "", phone = "",  dateLastReview = "";
        List<String> services = null;
        int[] ratings = {0,0,0,0};

        try (JsonReader jsonReader = new JsonReader(new FileReader(HOTEL_FILE_PATH))) {
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
                        case "score":
                            score = jsonReader.nextDouble();
                            break;
                        case "numReviews":
                            numReviews = jsonReader.nextInt();
                            break;
                        case "dateLastReview":
                            dateLastReview = jsonReader.nextString();
                            break;
                        default:
                            jsonReader.skipValue(); // Ignora il valore di altri campi
                            break;                        
                    }
                }

                jsonReader.endObject(); // Fine dell'oggetto

                // Controlla se l'hotel corrente è nella città richiesta
                if (!name.isBlank() && city.equalsIgnoreCase(citta)) {
                    resultHotels.add(new Hotel(id, name, description, city, phone, services, rate, ratings, score, numReviews, dateLastReview));
                }
            }
            
            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultHotels;
    }
    
    /**
    * Restituisce tutti gli hotel presenti nel file JSON.
    *
    * @return Lista di tutti gli Hotel.
    */    
    public synchronized List<Hotel> searchAllHotels () {
       List<Hotel> resultHotels = new ArrayList<>();

        String fieldName = "";

        // Proprietà Hotel
        int id = 0, rate = 0, numReviews = 0;
        Double score = 0.0;
        String name = "", description = "", city = "", phone = "", dateLastReview = "";
        List<String> services = null;
        int[] ratings = {0,0,0,0};

        try (JsonReader jsonReader = new JsonReader(new FileReader(HOTEL_FILE_PATH))) {
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
                        case "score":
                            score = jsonReader.nextDouble();
                            break;
                        case "numReviews":
                            numReviews = jsonReader.nextInt();
                            break;
                        case "dateLastReview":
                            dateLastReview = jsonReader.nextString();
                            break;                            
                        default:
                            jsonReader.skipValue(); // Ignora il valore di altri campi
                            break;                        
                    }
                }

                jsonReader.endObject(); // Fine dell'oggetto

                // Controlla se l'hotel corrente esiste
                if (!name.isBlank()) {
                    resultHotels.add(new Hotel(id, name, description, city, phone, services, rate, ratings, score, numReviews, dateLastReview));
                }
            }
            
            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultHotels;        
    }

    // OTHER METHODS:

    /**
    * Legge una lista di stringhe da un JsonReader.
    *
    * @param jsonReader Il reader da cui leggere la lista.
    * @return Lista di stringhe lette dal JsonReader.
    */    
    private static List<String> readStringList(JsonReader jsonReader) throws IOException {
        List<String> list = new ArrayList<>();

        jsonReader.beginArray(); // Inizia a leggere l'array

        while (jsonReader.hasNext()) {
            list.add(jsonReader.nextString());
        }

        jsonReader.endArray(); // Fine dell'array
        return list;
    }

    /**
    * Legge una lista di valutazioni (array di interi) da un JsonReader.
    *
    * @param jsonReader Il reader da cui leggere la lista di valutazioni.
    * @return Array di interi che rappresentano le valutazioni.
    */    
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