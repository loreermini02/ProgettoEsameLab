import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RankingManager {
    private static final String RANKING_FILE_PATH = "JSON/Ranking.json";
    
    public void rankHotels(List<Hotel> allHotel) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map <String, List<Hotel>> cityToHotel = new HashMap<>();

        // Raggruppamento degli hotel per città
        for (Hotel hotel : allHotel) {
            cityToHotel.computeIfAbsent(hotel.getCity(), k -> new ArrayList<>()).add(hotel);
        }

        // Ordinamento degli hotel per città in base al punteggio
        Map<String, List<Hotel>> cityToRankedHotels = new HashMap<>();
        for (Map.Entry<String, List<Hotel>> entry : cityToHotel.entrySet()) {
            List<Hotel> rankedHotels = entry.getValue();
            rankedHotels.sort((h1, h2) -> Double.compare(h2.getScore(), h1.getScore()));
            cityToRankedHotels.put(entry.getKey(), rankedHotels);
        }

        try (Writer writer = new FileWriter(RANKING_FILE_PATH)) {
            gson.toJson(cityToRankedHotels, writer);
        }
    }
}
