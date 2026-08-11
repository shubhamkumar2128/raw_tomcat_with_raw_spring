package RawTomcat.filter;

import RawTomcat.core.MyHttpRequest;
import RawTomcat.core.MyHttpResponse;


public interface MyFilter {

    void doFilter(MyHttpRequest request, MyHttpResponse response, FilterChain chain);
}
