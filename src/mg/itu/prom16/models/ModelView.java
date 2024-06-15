package mg.itu.prom16.models;

import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String, Object> data;

    public ModelView(String url) {
        this.url = url;
        this.data = new HashMap<>();
    }

    public void addObject(String name, Object value) {
        data.put(name, value);
    }

    public String getUrl() {
        return url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }
}
