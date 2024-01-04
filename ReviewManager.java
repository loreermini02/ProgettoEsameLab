import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ReviewManager {
    private static final String REVIEW_FILE_PATH = "JSON/Review.json";

    public void addReview(String nomeHotel, String nomeCitta, int globalScore, int[] singleScores) {
        Review newReview = new Review(globalScore, singleScores);
        
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
    }
}
