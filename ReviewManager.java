import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class ReviewManager {
    private static final String REVIEW_FILE_PATH = "JSON/Review.json";

    public void addReview(LoggedUser loggedUser, int idHotel, String nomeHotel, String nomeCitta, int globalScore, int[] singleScores) {
        LocalDateTime dateTime = LocalDateTime.now();
        
        // Serializza la data in una stringa formattata
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = dateTime.format(formatter);

        Review newReview = new Review(loggedUser.getUsername(), idHotel, nomeHotel, nomeCitta, globalScore, singleScores, formattedDateTime);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Leggi il JSON esistente
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(REVIEW_FILE_PATH)));
            Type listType = new TypeToken<List<Review>>() {}.getType();
            List<Review> reviews = gson.fromJson(jsonContent, listType);

            // Aggiungi la nuova recensione
            reviews.add(newReview);

            // Scrivi l'array aggiornato nel file
            try (FileWriter writer = new FileWriter(REVIEW_FILE_PATH)) {
                gson.toJson(reviews, writer);
            }   
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Incrementa le review effettuate da quel determinato utente
        loggedUser.setNumReview(loggedUser.getNumReview() + 1);
    }

    public int getNumReviewByUsername (String username) {
        String fieldName, existingUsername = null;
        int numReview = 0;

        try (JsonReader jsonReader = new JsonReader(new FileReader(REVIEW_FILE_PATH))) {
            jsonReader.beginArray();

            // Itera attraverso gli oggetti nell'array
            while (jsonReader.hasNext()) {
                jsonReader.beginObject(); // Inizia a leggere un oggetto

                // Leggi le proprietà dell'oggetto
                while (jsonReader.hasNext()) {
                    fieldName = jsonReader.nextName();
                    switch (fieldName) {
                        case "username":
                            existingUsername = jsonReader.nextString();
                            break;
                        default:
                            jsonReader.skipValue(); // Ignora il valore di altri campi
                            break;
                    }
                }
                
                jsonReader.endObject(); // Fine dell'oggetto

                // Verifica se l'username corrente corrisponde a quello cercato
                if (existingUsername != null && existingUsername.equals(username)) {
                    numReview++;
                }
            }

            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }

        return numReview;
    }

    public List<Review> getAllReviewByHotel (int idHotel) {
        String fieldName, username = "", existingHotel = "", existingCity = "", formattedDateTime = "";
        int globalScore = 0, existingIdHotel = -1;
        int[] singleScores = {0,0,0,0};

        List<Review> allReviews = new ArrayList<>();

        try (JsonReader jsonReader = new JsonReader(new FileReader(REVIEW_FILE_PATH))) {    
            jsonReader.beginArray(); // Assume che il file JSON inizi con un oggetto
    
            // Itera attraverso gli oggetti nell'array
            while (jsonReader.hasNext()) {
                jsonReader.beginObject();

                // Leggi le proprietà dell'oggetto
                while (jsonReader.hasNext()) {
                    fieldName = jsonReader.nextName();
                    switch (fieldName) {
                        case "username":
                            username = jsonReader.nextString();
                            break;
                        case "idHotel":
                            existingIdHotel = jsonReader.nextInt();
                            break;
                        case "nomeHotel":
                            existingHotel = jsonReader.nextString();
                            break;
                        case "nomeCitta":
                            existingCity = jsonReader.nextString();
                            break;
                        case "globalScore":
                            globalScore = jsonReader.nextInt();
                            break;
                        case "singleScores":
                            singleScores = readRatingList(jsonReader);
                            break;
                        case "dateTime":
                            formattedDateTime = jsonReader.nextString();
                            break;
                        default:
                            jsonReader.skipValue(); // Ignora il valore di altri campi
                            break;
                    }
                }
                jsonReader.endObject(); // Fine dell'oggetto
                
                if (existingIdHotel == idHotel)
                    allReviews.add(new Review(username, existingIdHotel, existingHotel, existingCity, globalScore, singleScores, formattedDateTime));

            }

            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }        

        return allReviews;
    }

    public String getDateLastReviewByUser(String username, int idHotel) {
        String existingDate = "", existingUsername = "", fieldName = ""; 
        int existingIdHotel = -1;
        
        try (JsonReader jsonReader = new JsonReader(new FileReader(REVIEW_FILE_PATH))) {
            jsonReader.beginArray();
            
            while(jsonReader.hasNext()) {
                jsonReader.beginObject();

                while(jsonReader.hasNext()) {
                    fieldName = jsonReader.nextName();
                    switch (fieldName) {
                        case "username":
                            existingUsername = jsonReader.nextString();
                            break;
                        case "idHotel":
                            existingIdHotel = jsonReader.nextInt();
                            break;
                        case "dateTime":
                            if (existingUsername.equalsIgnoreCase(username) && idHotel == existingIdHotel)
                                existingDate = jsonReader.nextString();
                            break;
                        default:
                            break;
                    }
                }

                jsonReader.endObject();
            }

            jsonReader.endArray();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ritorna la data più recente
        return existingDate;
    }
    // Other methods
    private int[] readRatingList(JsonReader jsonReader) throws IOException {
        int[] ratings = {0,0,0,0};
 
        jsonReader.beginArray(); // Inizia a leggere l'array delle valutazioni
        
        ratings[0] = jsonReader.nextInt();
        ratings[1] = jsonReader.nextInt();
        ratings[2] = jsonReader.nextInt();
        ratings[3] = jsonReader.nextInt();

        jsonReader.endArray(); // Fine dell'array delle valutazioni
        return ratings;  
    }

}
