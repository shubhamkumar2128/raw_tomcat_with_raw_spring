package RawTomcat.controller;

import RawTomcat.dispatcher.annotations.MyController;
import RawTomcat.dispatcher.annotations.MyGetMapping;
import RawTomcat.dispatcher.annotations.MyPostMapping;
import RawTomcat.dispatcher.annotations.MyRequestParam;

@MyController
public class HelloController {

    @MyGetMapping("/hello")
    public String sayHello() {
        return "Hello from Raw Tomcat Controller!";
    }

    @MyGetMapping("/greet")
    public String greet(@MyRequestParam("name") String name) {
        return "Hello, " + name + "! Welcome to Raw Tomcat!";
    }

    @MyPostMapping("/hello")
    public String postHello() {
        return "POST received on /hello via Controller!";
    }
}
