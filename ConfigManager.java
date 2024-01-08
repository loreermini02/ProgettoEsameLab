import java.io.FileReader;
import java.io.IOException;

import com.google.gson.stream.JsonReader;

public class ConfigManager {
    static final String CONFIG_FILE_PATH = "JSON/Config.json";

    public Config readConfigFile() {
        String fieldName, serverName = "localhost";
        int serverPort = 8080, notificationPort = 1111, numThread = 20, timer = 40, numDay = 30;

        try(JsonReader jsonReader = new JsonReader(new FileReader(CONFIG_FILE_PATH))) {
            jsonReader.beginObject();

            while(jsonReader.hasNext()) {
                fieldName = jsonReader.nextName();

                switch (fieldName) {
                    case "serverName":
                        serverName = jsonReader.nextString();
                        break;
                    case "serverPort":
                        serverPort = jsonReader.nextInt();
                        break;
                    case "notificationPort":
                        notificationPort = jsonReader.nextInt();
                        break;                        
                    case "numThread":
                        numThread = jsonReader.nextInt();
                        break;    
                        
                    case "timerReloadRanking":
                        timer = jsonReader.nextInt();
                        break;    
                        
                    case "daysForNewReview":
                        numDay = jsonReader.nextInt();
                        break;                        
                    
                    default:
                        break;
                }
            }

            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        } 

        return new Config(serverName, serverPort, notificationPort, numThread, timer, numDay);
    }
}