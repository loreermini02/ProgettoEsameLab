import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Gestisce le operazioni relative al file json contenente tutti gli utenti registrati al sistema.
 */
public class UserManager {
    private static final String USERS_FILE_PATH = "JSON/Users.json";

    /**
    * Aggiunge un nuovo utente al file JSON.
    * Il metodo è synchronized per gestire l’accesso concorrente al file.
    *
    * @param username L’username per il nuovo utente.
    * @param password La password per il nuovo utente.
    */    
    public synchronized void addUser(String username, String password) {
        // Crea l’oggetto JSON per l’utente usando Gson

        User newUser = new User(username, password);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Leggi il JSON esistente
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(USERS_FILE_PATH)));
            Type listType = new TypeToken<List<User>>() {}.getType();
            List<User> users = gson.fromJson(jsonContent, listType);

            // Aggiungi la nuova recensione
            users.add(newUser);

            // Scrivi l'array aggiornato nel file
            try (FileWriter writer = new FileWriter(USERS_FILE_PATH)) {
                gson.toJson(users, writer);
            }   
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * Verifica se l’username esiste e ne restituisce le credenziali sotto forma di array.
    *
    * @param username L’username da verificare.
    * @return Un array contenente l’username e la password se trovati, o un array vuoto altrimenti.
    */
    public synchronized String[] checkUsername(String username) {
        String existingUsername = null, password = "", fieldName = "";
        String[] result = {"",""};

        try (JsonReader jsonReader = new JsonReader(new FileReader(USERS_FILE_PATH))) {
    
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
                if (existingUsername != null && existingUsername.equalsIgnoreCase(username)) {
                    // Restituisce una tupla [boolean, String]
                    result[0] = username;
                    result[1] = password;

                    return result;
                }
            }

            jsonReader.endArray(); // Fine dell'array
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        result[0] = "";
        result[1] = "";
        // Username non trovato
        return result;
    }

    /**
    * Verifica se l’username e la password corrispondono ad un account esistente.
    *
    * @param username L’username da verificare.
    * @param password La password da verificare.
    * @return true se le credenziali corrispondono, false altrimenti.
    */
    public synchronized boolean checkUsername (String username, String password) {
        String[] resultCheck = checkUsername(username);
        if (!resultCheck[0].isEmpty() && resultCheck[1].equals(password))
            return true;
        else
            return false;
    }
}