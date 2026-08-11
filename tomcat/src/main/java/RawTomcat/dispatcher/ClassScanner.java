package RawTomcat.dispatcher;

import RawTomcat.dispatcher.annotations.MyController;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ClassScanner {

    public static List<Class<?>> scanForControllers(String basePackage, String sourceRoot) {
        List<Class<?>> controllers = new ArrayList<>();

        // Use classpath-based scanning: find .class files via the classloader
        String packagePath = basePackage.replace('.', '/');

        try {
            URL packageUrl = Thread.currentThread().getContextClassLoader().getResource(packagePath);

            if (packageUrl == null) {
                System.err.println("  [ClassScanner] Package not found on classpath: " + basePackage);
                return controllers;
            }

            File directory = new File(packageUrl.toURI());
            System.out.println("  [ClassScanner] Scanning directory: " + directory.getAbsolutePath());

            if (!directory.exists() || !directory.isDirectory()) {
                System.err.println("  [ClassScanner] Directory not found: " + directory.getPath());
                return controllers;
            }

            File[] files = directory.listFiles();
            if (files == null) return controllers;

            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = basePackage + "." + file.getName().replace(".class", "");

                    try {
                        Class<?> clazz = Class.forName(className);

                        if (clazz.isAnnotationPresent(MyController.class)) {
                            controllers.add(clazz);
                            System.out.println("  [ClassScanner] Found controller: " + className);
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("  [ClassScanner] Cannot load: " + className);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  [ClassScanner] Error scanning package: " + basePackage);
            e.printStackTrace();
        }

        return controllers;
    }
}
