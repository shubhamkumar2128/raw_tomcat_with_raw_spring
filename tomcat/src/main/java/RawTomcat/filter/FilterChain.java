package RawTomcat.filter;

import RawTomcat.core.MyHttpRequest;
import RawTomcat.core.MyHttpResponse;
import RawTomcat.core.TomcatServlet;

import java.util.List;

public class FilterChain {

    private final List<MyFilter> filters;
    private final TomcatServlet servlet;
    private int currentPosition = 0;

    public FilterChain(List<MyFilter> filters, TomcatServlet servlet) {
        this.filters = filters;
        this.servlet = servlet;
    }


    public void doFilter(MyHttpRequest request, MyHttpResponse response) {
        if (currentPosition < filters.size()) {
            MyFilter nextFilter = filters.get(currentPosition);
            currentPosition++;
            nextFilter.doFilter(request, response, this);
        } else {
            servlet.service(request, response);
        }
    }
}
