import java.io.FileReader;
import java.io.IOException;

import com.google.gson.stream.JsonReader;

public class HotelManager {
    private static final String HOTELS_FILE_PATH = "JSON/Hotels.json";

    public String[] searchHotel (String nomeHotel, String citta) {
        String existingHotel = "", existingCity = "", fieldName = "";
        String[] res = {"",""}; //{NomeHotel, Citta}

        try (JsonReader jsonReader = new JsonReader(new FileReader(HOTELS_FILE_PATH))) {
            jsonReader.beginArray(); // Inizia a leggere l'array

            // Itera attraverso gli oggetti nell'array
            while (jsonReader.hasNext()) {
                jsonReader.beginObject(); // Inizia a leggere un oggetto

                // Leggi le proprietà dell'oggetto
                while (jsonReader.hasNext()) {
                    fieldName = jsonReader.nextName();
                    if ("name".equals(fieldName)) {
                        existingHotel = jsonReader.nextString();
                    } else if ("city".equals(fieldName)) {
                        existingCity = jsonReader.nextString();
                    } else {
                        jsonReader.skipValue(); // Ignora il valore di altri campi
                    }
                }

                jsonReader.endObject(); // Fine dell'oggetto

                // Controlla se l'hotel corrente ha il nome cercato ed è nella città richiesta
                if (existingHotel != null && existingHotel.equalsIgnoreCase(nomeHotel)) {
                    res[0] = nomeHotel;
                    if (existingCity.equalsIgnoreCase(citta)) {
                        res[1] = citta;
                    }
                }
            }

            jsonReader.endArray(); // Fine dell'array
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }
    
}
