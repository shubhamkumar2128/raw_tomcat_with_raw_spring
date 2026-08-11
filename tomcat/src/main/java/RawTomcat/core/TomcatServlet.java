package RawTomcat.core;

public interface TomcatServlet {
    void service(MyHttpRequest request, MyHttpResponse response);
}
