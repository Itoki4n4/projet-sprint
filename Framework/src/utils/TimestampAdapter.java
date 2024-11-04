package mg.itu.utils;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimestampAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
    private static SimpleDateFormat sdf;

    public static void setSdf(String format){
        sdf = new SimpleDateFormat(format);
    }

    @Override
    public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
        // On renvoie un long (millisecondes)
        return new JsonPrimitive(src.getTime());
    }

    @Override
    public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();

            try {
                if (prim.isNumber()) {
                    return new Timestamp(prim.getAsLong());
                } else if (prim.isString()) {
                    // Essaie de parser la chaîne au format yyyy-MM-dd'T'HH:mm
                    synchronized (sdf) {
                        try {
                            return new Timestamp(sdf.parse(prim.getAsString()).getTime());
                        } catch (ParseException e) {
                            // Si ça rate, essaye Timestamp.valueOf (format standard SQL : yyyy-MM-dd HH:mm:ss)
                            return Timestamp.valueOf(prim.getAsString());
                        }
                    }
                }
            } catch (Exception e) {
                throw new JsonParseException("Format de timestamp invalide: " + prim.getAsString(), e);
            }
        }
        return null;
    }
}
