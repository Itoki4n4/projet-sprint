package mg.itu.utils;

import java.util.Map;
import java.util.HashMap;

import java.util.HashMap;

public class ModelView {
    String viewUrl;
    Map<String,Object> data;
    public ModelView(){
        this.data = new HashMap<String,Object>();
    }
    public ModelView(String viewUrl){
        this.setViewUrl(viewUrl);
        this.data = new HashMap<String,Object>();
    }
    public String getViewUrl() {
        return viewUrl;
    }
    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }
    public Map<String, Object> getData() {
        return data;
    }
    public void addObject(String key,Object data) {
        this.data.put(key, data);
    }

}
