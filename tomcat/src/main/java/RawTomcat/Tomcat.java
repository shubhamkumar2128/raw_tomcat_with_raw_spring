package RawTomcat;

import RawTomcat.core.MyHttpRequest;
import RawTomcat.core.MyHttpResponse;
import RawTomcat.core.MyServletContainer;
import RawTomcat.core.TomcatServlet;
import RawTomcat.dispatcher.ClassScanner;
import RawTomcat.dispatcher.FilterScanner;
import RawTomcat.dispatcher.HandlerMapping;
import RawTomcat.dispatcher.MyDispatcherServlet;
import RawTomcat.filter.FilterChain;
import RawTomcat.filter.MyFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Tomcat {

    private static final MyServletContainer container = new MyServletContainer();
    private static final List<FilterScanner.FilterEntry> filterEntries = new ArrayList<>();

    /**
     * Start the server with default settings.
     * Scans "RawTomcat.controller" and "RawTomcat.filter" packages.
     */
    public static void main(String[] args) throws Exception {
        String controllerPackage = "RawTomcat.controller";
        String filterPackage = "RawTomcat.filter";
        int port = 8080;

        // Allow users to pass package names and port as command-line args
        if (args.length >= 1) controllerPackage = args[0];
        if (args.length >= 2) filterPackage = args[1];
        if (args.length >= 3) port = Integer.parseInt(args[2]);

        start(controllerPackage, filterPackage, port);
    }

    /**
     * PUBLIC API — Users call this method to start the server.
     *
     * @param controllerPackage  package to scan for @MyController classes
     * @param filterPackage      package to scan for @MyFilterMapping classes
     * @param port               port number to listen on
     */
    public static void start(String controllerPackage, String filterPackage, int port) throws Exception {

        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║       RAW TOMCAT — Starting Up...         ║");
        System.out.println("║       (No web.xml! Pure annotations)      ║");
        System.out.println("╚═══════════════════════════════════════════╝\n");

        autoScanControllers(controllerPackage);
        autoScanFilters(filterPackage);
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("║ Server READY → http://localhost:" + port + "║");

        // STEP 4: Accept connections
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> handleRequest(socket)).start();
        }
    }

    public static void run(String basePackage, int port) throws Exception {
        start(basePackage, basePackage, port);
    }


    private static void autoScanControllers(String controllerPackage) {
        System.out.println("[Startup] Scanning controllers in: " + controllerPackage);

        // Determine source root (try "src" first, fall back to current directory)
        String sourceRoot = findSourceRoot(controllerPackage);

        List<Class<?>> controllerClasses = ClassScanner.scanForControllers(controllerPackage, sourceRoot);

        if (controllerClasses.isEmpty()) {
            System.out.println("  WARNING: No controllers found in " + controllerPackage);
            return;
        }

        HandlerMapping handlerMapping = new HandlerMapping();
        handlerMapping.scanControllers(controllerClasses);

        MyDispatcherServlet dispatcher = new MyDispatcherServlet(handlerMapping);
        container.register("__dispatcher__", dispatcher);

        System.out.println("  → " + controllerClasses.size() + " controller(s) loaded\n");
    }

    private static void autoScanFilters(String filterPackage) {
        System.out.println("[Startup] Scanning filters in: " + filterPackage);

        String sourceRoot = findSourceRoot(filterPackage);

        List<FilterScanner.FilterEntry> scannedFilters = FilterScanner.scanForFilters(filterPackage, sourceRoot);
        filterEntries.addAll(scannedFilters);

        System.out.println("  → " + filterEntries.size() + " filter(s) loaded\n");
    }

    private static String findSourceRoot(String packageName) {
        String packagePath = packageName.replace('.', '/');
        File srcDir = new File("src/" + packagePath);
        if (srcDir.exists()) return "src";

        File currentDir = new File(packagePath);
        if (currentDir.exists()) return ".";

        return "src";
    }

    private static void handleRequest(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            OutputStream outputStream = socket.getOutputStream();

            MyHttpRequest request = parseRequest(reader);

            if (request == null) {
                socket.close();
                return;
            }

            System.out.println("\n→ " + request.getMethod() + " " + request.getPath() +
                    (request.getQueryParams().isEmpty() ? "" : " ?" + request.getQueryParams()));

            MyHttpResponse response = new MyHttpResponse(outputStream);

            TomcatServlet servlet = container.findServlet("__dispatcher__");

            if (servlet == null) {
                response.sendError(500, "No DispatcherServlet configured");
            } else {
                List<MyFilter> matchingFilters = getMatchingFilters(request.getPath());
                FilterChain chain = new FilterChain(matchingFilters, servlet);
                chain.doFilter(request, response);
            }

            socket.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private static List<MyFilter> getMatchingFilters(String path) {
        List<MyFilter> matching = new ArrayList<>();
        for (FilterScanner.FilterEntry entry : filterEntries) {
            if (matchesPattern(entry.urlPattern, path)) {
                matching.add(entry.filter);
            }
        }
        return matching;
    }

    private static boolean matchesPattern(String pattern, String path) {
        if (pattern.equals("/*")) return true;
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.startsWith(prefix);
        }
        return pattern.equals(path);
    }


    private static MyHttpRequest parseRequest(BufferedReader reader) throws Exception {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) return null;

        String[] parts = requestLine.split(" ");
        if (parts.length < 3) return null;

        MyHttpRequest request = new MyHttpRequest();
        request.setMethod(parts[0]);
        request.setPath(parts[1]);
        request.setProtocol(parts[2]);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) break;
            String[] header = line.split(":", 2);
            if (header.length == 2) {
                request.getHeaders().put(header[0].trim(), header[1].trim());
            }
        }
        return request;
    }
}
