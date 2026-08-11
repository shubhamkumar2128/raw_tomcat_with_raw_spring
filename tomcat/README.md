# Raw Tomcat

A lightweight HTTP server framework built from scratch in Java — no Spring, no Servlet API, no `web.xml`. Pure annotation-based routing with filters, path variables, and query parameters.

## Features

- Annotation-based controller routing (`@MyGetMapping`, `@MyPostMapping`, `@MyPutMapping`, `@MyDeleteMapping`)
- Path variables (`/users/{id}`)
- Query parameters (`/search?name=phone`)
- Filter chain with URL pattern matching and ordering
- Classpath-based auto-scanning (no configuration files needed)

---

## Using Raw Tomcat in a Different Project

### Step 1: Build the JAR

In the Raw Tomcat project directory:

```bash
JAVA_HOME=/path/to/java-21 ./mvnw clean package -DskipTests
```

This produces `target/tomcat-0.0.1-SNAPSHOT.jar`.

### Step 2: Add the JAR to Your Project

**Option A — Local dependency (Maven):**

Install to your local Maven repo:

```bash
JAVA_HOME=/path/to/java-21 ./mvnw install
```

Then in your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.raw</groupId>
    <artifactId>tomcat</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**Option B — Direct JAR on classpath:**

Copy `tomcat-0.0.1-SNAPSHOT.jar` into your project's `libs/` folder, then add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.raw</groupId>
    <artifactId>tomcat</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/tomcat-0.0.1-SNAPSHOT.jar</systemPath>
</dependency>
```

Or if not using Maven, add it to your classpath manually:

```bash
java -cp "libs/tomcat-0.0.1-SNAPSHOT.jar:target/classes" com.myapp.Main
```

### Step 3: Create Your Application

#### Project Structure

```
my-app/
├── pom.xml
└── src/main/java/com/myapp/
    ├── Main.java
    ├── controller/
    │   └── HomeController.java
    └── filter/
        └── CorsFilter.java
```

#### Main.java — Entry Point

```java
package com.myapp;

import RawTomcat.Tomcat;

public class Main {
    public static void main(String[] args) throws Exception {
        // Option 1: Specify controller and filter packages + port
        Tomcat.start("com.myapp.controller", "com.myapp.filter", 8080);

        // Option 2: Use a base package (appends .controller and .filter automatically)
        // Tomcat.run("com.myapp", 8080);
    }
}
```

#### HomeController.java — Define Routes

```java
package com.myapp.controller;

import RawTomcat.dispatcher.annotations.*;

@MyController
public class HomeController {

    @MyGetMapping("/")
    public String home() {
        return "Welcome to my app!";
    }

    @MyGetMapping("/hello")
    public String hello(@MyRequestParam("name") String name) {
        return "Hello, " + name + "!";
    }

    @MyGetMapping("/users/{id}")
    public String getUser(@MyPathVariable("id") String id) {
        return "User ID: " + id;
    }

    @MyPostMapping("/users")
    public String createUser(@MyRequestParam("name") String name) {
        return "Created user: " + name;
    }

    @MyPutMapping("/users/{id}")
    public String updateUser(@MyPathVariable("id") String id,
                             @MyRequestParam("name") String name) {
        return "Updated user " + id + " to " + name;
    }

    @MyDeleteMapping("/users/{id}")
    public String deleteUser(@MyPathVariable("id") String id) {
        return "Deleted user: " + id;
    }
}
```

#### CorsFilter.java — Define Filters (Optional)

```java
package com.myapp.filter;

import RawTomcat.filter.MyFilter;
import RawTomcat.filter.MyFilterMapping;
import RawTomcat.filter.FilterChain;
import RawTomcat.core.MyHttpRequest;
import RawTomcat.core.MyHttpResponse;

@MyFilterMapping(value = "/*", order = 1)
public class CorsFilter implements MyFilter {

    @Override
    public void doFilter(MyHttpRequest request, MyHttpResponse response, FilterChain chain) {
        System.out.println("[CORS] Processing: " + request.getMethod() + " " + request.getPath());
        // Continue to next filter or servlet
        chain.doFilter(request, response);
    }
}
```

### Step 4: Run

```bash
java -cp "libs/tomcat-0.0.1-SNAPSHOT.jar:target/classes" com.myapp.Main
```

Output:
```
╔═══════════════════════════════════════════╗
║       RAW TOMCAT — Starting Up...         ║
║       (No web.xml! Pure annotations)      ║
╚═══════════════════════════════════════════╝

[Startup] Scanning controllers in: com.myapp.controller
  [ClassScanner] Found controller: com.myapp.controller.HomeController
  → 1 controller(s) loaded

[Startup] Scanning filters in: com.myapp.filter
  [FilterScanner] Found filter: com.myapp.filter.CorsFilter → pattern: /*, order: 1
  → 1 filter(s) loaded

║ Server READY → http://localhost:8080║
```

### Step 5: Test with cURL

```bash
# Simple GET
curl http://localhost:8080/hello?name=World

# Path variable
curl http://localhost:8080/users/42

# POST with query params
curl -X POST "http://localhost:8080/users?name=John"

# PUT
curl -X PUT "http://localhost:8080/users/1?name=Jane"

# DELETE
curl -X DELETE http://localhost:8080/users/1
```

---

## API Reference

### Annotations

| Annotation | Target | Description |
|---|---|---|
| `@MyController` | Class | Marks a class as a controller to be scanned |
| `@MyGetMapping("/path")` | Method | Maps GET requests to a handler method |
| `@MyPostMapping("/path")` | Method | Maps POST requests |
| `@MyPutMapping("/path")` | Method | Maps PUT requests |
| `@MyDeleteMapping("/path")` | Method | Maps DELETE requests |
| `@MyPathVariable("name")` | Parameter | Binds a path segment like `{name}` to a method parameter |
| `@MyRequestParam("name")` | Parameter | Binds a query parameter to a method parameter |
| `@MyFilterMapping(value, order)` | Class | Marks a filter class with a URL pattern and execution order |

### Filter Interface

```java
public interface MyFilter {
    void doFilter(MyHttpRequest request, MyHttpResponse response, FilterChain chain);
}
```

- Call `chain.doFilter(request, response)` to pass to the next filter/servlet
- Skip calling `chain.doFilter()` to block the request (e.g., auth check failed)
- `order` in `@MyFilterMapping` controls execution order (lower = runs first)

### URL Patterns for Filters

| Pattern | Matches |
|---|---|
| `/*` | All requests |
| `/users` | Exact match on `/users` |
| `/api/*` | Any path starting with `/api/` |

### Startup Methods

```java
// Full control: specify both packages and port
Tomcat.start("com.myapp.controller", "com.myapp.filter", 8080);

// Convenience: base package + port (appends .controller and .filter)
Tomcat.run("com.myapp", 8080);
```

---

## Requirements

- Java 21+
- No external dependencies
