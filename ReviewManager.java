import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class ReviewManager {
    private static final String REVIEW_FILE_PATH = "JSON/Review.json";

    public void addReview(LoggedUser loggedUser, int idHotel, String nomeHotel, String nomeCitta, int globalScore, int[] singleScores) {
        Review newReview = new Review(loggedUser.getUsername(), idHotel, nomeHotel, nomeCitta, globalScore, singleScores);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Leggi il JSON esistente
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(REVIEW_FILE_PATH)));
            JsonObject rootObject = JsonParser.parseString(jsonContent).getAsJsonObject();
            JsonArray reviewsArray = rootObject.getAsJsonArray("reviews");

            // Aggiungi la nuova recensione
            reviewsArray.add(gson.toJsonTree(newReview));

            try (FileWriter writer = new FileWriter(REVIEW_FILE_PATH)) {
                gson.toJson(rootObject, writer);
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
            jsonReader.beginObject(); // Assume che il file JSON inizi con un oggetto
    
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
    
                if ("reviews".equals(key)) {
                    jsonReader.beginArray(); // Assume che "reviews" sia un array
    
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
                } else {
                    jsonReader.skipValue();
                }
            }
    
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return numReview;
    }

    public List<Review> getAllReviewByHotel (int idHotel) {
        String fieldName, username = "", existingHotel = "", existingCity = "";
        int globalScore = 0, existingIdHotel = -1;
        int[] singleScores = {0,0,0,0};

        List<Review> allReviews = new ArrayList<>();

        try (JsonReader jsonReader = new JsonReader(new FileReader(REVIEW_FILE_PATH))) {    
            jsonReader.beginObject(); // Assume che il file JSON inizi con un oggetto
    
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
    
                if ("reviews".equals(key)) {
                    jsonReader.beginArray(); // Assume che "reviews" sia un array
    
                    // Itera attraverso gli oggetti nell'array
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject(); // Inizia a leggere un oggetto
    
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
                                case "nomeCittà":
                                    existingCity = jsonReader.nextString();
                                    break;
                                case "globalScore":
                                    globalScore = jsonReader.nextInt();
                                    break;
                                case "singleScores":
                                    singleScores = readRatingList(jsonReader);
                                    break;
                                default:
                                    jsonReader.skipValue(); // Ignora il valore di altri campi
                                    break;
                            }
                        }
                        jsonReader.endObject(); // Fine dell'oggetto
                        
                        if (existingIdHotel == idHotel)
                            allReviews.add(new Review(username, existingIdHotel, existingHotel, existingCity, globalScore, singleScores));
                    }
                    jsonReader.endArray(); // Fine dell'array
                } else {
                    jsonReader.skipValue();
                }
            }
    
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }        

        return allReviews;
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
