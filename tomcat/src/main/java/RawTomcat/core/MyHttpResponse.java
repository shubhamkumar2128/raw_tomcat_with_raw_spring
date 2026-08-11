package RawTomcat.core;

import java.io.OutputStream;
import java.io.IOException;


public class MyHttpResponse {

    private final OutputStream outputStream;

    public MyHttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String body) {
        try {
            String httpResponse =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + body.length() + "\r\n" +
                    "\r\n" +
                    body;

            outputStream.write(httpResponse.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendError(int statusCode, String message) {
        try {
            String statusText;
            switch (statusCode) {
                case 401: statusText = "Unauthorized"; break;
                case 403: statusText = "Forbidden"; break;
                case 404: statusText = "Not Found"; break;
                case 500: statusText = "Internal Server Error"; break;
                default: statusText = "Error";
            }

            String body = statusCode + " " + statusText + ": " + message;

            String httpResponse =
                    "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + body.length() + "\r\n" +
                    "\r\n" +
                    body;

            outputStream.write(httpResponse.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
