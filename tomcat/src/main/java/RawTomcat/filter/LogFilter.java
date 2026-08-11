package RawTomcat.filter;

import RawTomcat.core.MyHttpRequest;
import RawTomcat.core.MyHttpResponse;

@MyFilterMapping(value = "/*", order = 1)
public class LogFilter implements MyFilter {

    @Override
    public void doFilter(MyHttpRequest request, MyHttpResponse response, FilterChain chain) {

        long startTime = System.currentTimeMillis();

        System.out.println("[LogFilter] BEFORE → " + request.getMethod() + " " + request.getPath());

        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[LogFilter] AFTER  → completed in " + duration + "ms");
    }
}
