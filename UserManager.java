import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserManager {

    private static final String USERS_FILE_PATH = "JSON/Users.json";

    public void addUser(String username, String password) {
        // Crea l’oggetto JSON per l’utente usando Gson

        User newUser = new User(username, password);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Leggi il JSON esistente
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(USERS_FILE_PATH)));
            JsonObject rootObject = JsonParser.parseString(jsonContent).getAsJsonObject();
            JsonArray usersArray = rootObject.getAsJsonArray("users");

            // Aggiungi il nuovo utente al array di utenti
            usersArray.add(gson.toJsonTree(newUser));

            // Riscrivi il file JSON con il nuovo utente
            try (FileWriter writer = new FileWriter(USERS_FILE_PATH)) {
                gson.toJson(rootObject, writer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }   
    }

    public String[] checkUsername(String username) {
        String existingUsername = null, password = "", fieldName = "";
        String[] result = {"",""};

        try (JsonReader jsonReader = new JsonReader(new FileReader(USERS_FILE_PATH))) {
            jsonReader.beginObject(); // Assume che il file JSON inizi con un oggetto
    
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
    
                if ("users".equals(key)) {
                    jsonReader.beginArray(); // Assume che "users" sia un array
    
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
                                case "password":
                                    password = jsonReader.nextString();
                                    break;
                                default:
                                    jsonReader.skipValue(); // Ignora il valore di altri campi
                                    break;
                            }
                        }
                        jsonReader.endObject(); // Fine dell'oggetto
    
                        // Verifica se l'username corrente corrisponde a quello cercato
                        if (existingUsername != null && existingUsername.equals(username)) {
                            // Restituisce una tupla [boolean, String]
                            result[0] = username;
                            result[1] = password;

                            return result;
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
    
        result[0] = "";
        result[1] = "";
        // Username non trovato
        return result;
    }

    public boolean checkUsername (String username, String password) {
        String[] resultCheck = checkUsername(username);
        if (!resultCheck[0].isEmpty() && resultCheck[1].equals(password))
            return true;
        else
            return false;
    }
}