package RawTomcat.core;

import java.util.HashMap;
import java.util.Map;

public class MyServletContainer {

    private final Map<String, TomcatServlet> servletMappings = new HashMap<>();

    public void register(String path, TomcatServlet servlet) {
        servletMappings.put(path, servlet);
    }

    public TomcatServlet findServlet(String path) {
        return servletMappings.get(path);
    }

    public Map<String, TomcatServlet> getAllMappings() {
        return servletMappings;
    }
}
