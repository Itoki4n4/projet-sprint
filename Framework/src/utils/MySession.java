package mg.itu.utils;

import jakarta.servlet.http.HttpSession;

public class MySession {
    HttpSession session;

    public MySession(HttpSession sess){
        this.session = sess;
    }

    public Object get(String key){
        return this.session.getAttribute(key);
    }
    public void add(String key,Object object){
        this.session.setAttribute(key,object);
    }
    public void delete(String key){
        this.session.removeAttribute(key);
    }
}
