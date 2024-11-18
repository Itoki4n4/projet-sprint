package mg.itu.utils;

import java.io.BufferedReader;
import java.sql.Timestamp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

public class JsonReader {
    public static Object readJson(BufferedReader reader,Class<?> target) throws Exception{
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            jsonBuilder.append(line);
        }

        String jsonString = jsonBuilder.toString();

        // Analyser le JSON sans parser dans un objet défini
        JsonElement element = JsonParser.parseString(jsonString);
        JsonObject jsonObject = element.getAsJsonObject();
        Gson gson = new GsonBuilder().registerTypeAdapter(Timestamp.class, new TimestampAdapter()).create();

        Object result = gson.fromJson(jsonObject,target);
        System.out.println("type;"+result.getClass().getSimpleName());
        return result;



        
    }
}


