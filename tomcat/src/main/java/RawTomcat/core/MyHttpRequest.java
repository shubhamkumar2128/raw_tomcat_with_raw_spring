package RawTomcat.core;

import java.util.HashMap;
import java.util.Map;

public class MyHttpRequest {

    private String method;
    private String path;
    private String fullPath;
    private String protocol;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String rawPath) {
        this.fullPath = rawPath;

        if (rawPath.contains("?")) {
            String[] parts = rawPath.split("\\?", 2);
            this.path = parts[0];

            String queryString = parts[1];
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    queryParams.put(keyValue[0], "");
                }
            }
        } else {
            this.path = rawPath;
        }
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getParam(String name) {
        return queryParams.getOrDefault(name, null);
    }

    @Override
    public String toString() {
        return "MyHttpRequest{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", queryParams=" + queryParams +
                ", protocol='" + protocol + '\'' +
                ", headers=" + headers +
                '}';
    }
}
