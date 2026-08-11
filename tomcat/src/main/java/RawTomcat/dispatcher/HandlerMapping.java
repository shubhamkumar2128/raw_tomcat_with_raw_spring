package RawTomcat.dispatcher;

import RawTomcat.dispatcher.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerMapping {

    private final Map<String, HandlerMethod> exactHandlerMap = new HashMap<>();

    private final Map<String, HandlerMethod> patternHandlerMap = new HashMap<>();
    public void scanControllers(List<Class<?>> controllerClasses) {

        for (Class<?> clazz : controllerClasses) {

            if (!clazz.isAnnotationPresent(MyController.class)) {
                System.out.println("  [HandlerMapping] Skipping " + clazz.getName() + " (no @MyController)");
                continue;
            }

            try {
                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

                for (Method method : clazz.getDeclaredMethods()) {
                    registerIfAnnotated(method, controllerInstance, clazz);
                }

            } catch (Exception e) {
                System.err.println("  [HandlerMapping] Failed to scan: " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

    private void registerIfAnnotated(Method method, Object controller, Class<?> clazz) {

        if (method.isAnnotationPresent(MyGetMapping.class)) {
            String path = method.getAnnotation(MyGetMapping.class).value();
            register("GET", path, method, controller, clazz);
        }
        if (method.isAnnotationPresent(MyPostMapping.class)) {
            String path = method.getAnnotation(MyPostMapping.class).value();
            register("POST", path, method, controller, clazz);
        }
        if (method.isAnnotationPresent(MyPutMapping.class)) {
            String path = method.getAnnotation(MyPutMapping.class).value();
            register("PUT", path, method, controller, clazz);
        }
        if (method.isAnnotationPresent(MyDeleteMapping.class)) {
            String path = method.getAnnotation(MyDeleteMapping.class).value();
            register("DELETE", path, method, controller, clazz);
        }
    }

    private void register(String httpMethod, String path, Method method, Object controller, Class<?> clazz) {
        String key = httpMethod + " " + path;
        HandlerMethod handlerMethod = new HandlerMethod(controller, method, path);

        if (path.contains("{")) {
            patternHandlerMap.put(key, handlerMethod);
        } else {
            exactHandlerMap.put(key, handlerMethod);
        }

        System.out.println("  [HandlerMapping] Mapped: " + key + " → " +
                clazz.getSimpleName() + "." + method.getName() + "()");
    }
    public HandlerMethod getHandler(String httpMethod, String path) {
        String key = httpMethod.toUpperCase() + " " + path;

        HandlerMethod handler = exactHandlerMap.get(key);
        if (handler != null) return handler;

        for (Map.Entry<String, HandlerMethod> entry : patternHandlerMap.entrySet()) {
            String patternKey = entry.getKey();
            String[] parts = patternKey.split(" ", 2);
            String patternMethod = parts[0];
            String patternPath = parts[1];

            if (!patternMethod.equalsIgnoreCase(httpMethod)) continue;

            Map<String, String> pathVariables = matchPath(patternPath, path);
            if (pathVariables != null) {
                HandlerMethod matched = entry.getValue();
                matched.setPathVariables(pathVariables);
                return matched;
            }
        }

        return null;
    }

    private Map<String, String> matchPath(String pattern, String actual) {
        List<String> varNames = new ArrayList<>();
        String regex = pattern;

        java.util.regex.Matcher varMatcher = java.util.regex.Pattern.compile("\\{([^}]+)\\}").matcher(pattern);
        while (varMatcher.find()) {
            varNames.add(varMatcher.group(1));
        }

        regex = regex.replaceAll("\\{[^}]+\\}", "([^/]+)");
        regex = "^" + regex + "$";

        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = compiledPattern.matcher(actual);

        if (matcher.matches()) {
            Map<String, String> variables = new HashMap<>();
            for (int i = 0; i < varNames.size(); i++) {
                variables.put(varNames.get(i), matcher.group(i + 1));
            }
            return variables;
        }

        return null;
    }

    public static class HandlerMethod {
        private final Object controller;
        private final Method method;
        private final String pathPattern;
        private Map<String, String> pathVariables = new HashMap<>();

        public HandlerMethod(Object controller, Method method, String pathPattern) {
            this.controller = controller;
            this.method = method;
            this.pathPattern = pathPattern;
        }

        public void setPathVariables(Map<String, String> pathVariables) {
            this.pathVariables = pathVariables;
        }

        public String invoke(Map<String, String> queryParams) throws Exception {
            Parameter[] parameters = method.getParameters();

            if (parameters.length == 0) {
                Object result = method.invoke(controller);
                return (result != null) ? result.toString() : "";
            }

            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];

                if (param.isAnnotationPresent(MyPathVariable.class)) {
                    String varName = param.getAnnotation(MyPathVariable.class).value();
                    args[i] = pathVariables.getOrDefault(varName, "");

                } else if (param.isAnnotationPresent(MyRequestParam.class)) {
                    String paramName = param.getAnnotation(MyRequestParam.class).value();
                    args[i] = queryParams.getOrDefault(paramName, "");

                } else {
                    args[i] = null;
                }
            }

            Object result = method.invoke(controller, args);
            return (result != null) ? result.toString() : "";
        }

        public String invoke() throws Exception {
            return invoke(new HashMap<>());
        }
    }
}
