package RawTomcat.filter;

import RawTomcat.core.MyHttpRequest;
import RawTomcat.core.MyHttpResponse;

@MyFilterMapping(value = "/users", order = 2)
public class AuthFilter implements MyFilter {

    @Override
    public void doFilter(MyHttpRequest request, MyHttpResponse response, FilterChain chain) {

        String authHeader = request.getHeaders().get("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            System.out.println("[AuthFilter] BLOCKED — no Authorization header");
            response.sendError(401, "Missing Authorization header");
            return;
        }

        System.out.println("[AuthFilter] PASSED — Authorization header found");
        chain.doFilter(request, response);
    }
}
