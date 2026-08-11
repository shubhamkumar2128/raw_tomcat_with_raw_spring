package RawTomcat.dispatcher;

import RawTomcat.core.MyHttpRequest;
import RawTomcat.core.MyHttpResponse;
import RawTomcat.core.TomcatServlet;


public class MyDispatcherServlet implements TomcatServlet {

    private final HandlerMapping handlerMapping;

    public MyDispatcherServlet(HandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void service(MyHttpRequest request, MyHttpResponse response) {

        String method = request.getMethod();
        String path = request.getPath();

        // Look up which controller method handles this
        HandlerMapping.HandlerMethod handler = handlerMapping.getHandler(method, path);

        if (handler == null) {
            response.sendError(404, "No handler for " + method + " " + path);
            System.out.println("  [DispatcherServlet] No handler found for: " + method + " " + path);
            return;
        }

        try {
            String result = handler.invoke(request.getQueryParams());

            response.write(result);
            System.out.println("  [DispatcherServlet] Handled: " + method + " " + path + " → 200 OK");

        } catch (Exception e) {
            response.sendError(500, "Error invoking handler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
