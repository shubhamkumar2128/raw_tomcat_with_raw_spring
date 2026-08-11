package RawTomcat.dispatcher;

import RawTomcat.filter.MyFilter;
import RawTomcat.filter.MyFilterMapping;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FilterScanner {


    public static class FilterEntry {
        public final MyFilter filter;
        public final String urlPattern;
        public final int order;

        public FilterEntry(MyFilter filter, String urlPattern, int order) {
            this.filter = filter;
            this.urlPattern = urlPattern;
            this.order = order;
        }
    }

    public static List<FilterEntry> scanForFilters(String basePackage, String sourceRoot) {
        List<FilterEntry> filters = new ArrayList<>();

        // Use classpath-based scanning: find .class files via the classloader
        String packagePath = basePackage.replace('.', '/');

        try {
            URL packageUrl = Thread.currentThread().getContextClassLoader().getResource(packagePath);

            if (packageUrl == null) {
                System.err.println("  [FilterScanner] Package not found on classpath: " + basePackage);
                return filters;
            }

            File directory = new File(packageUrl.toURI());
            System.out.println("  [FilterScanner] Scanning directory: " + directory.getAbsolutePath());

            if (!directory.exists() || !directory.isDirectory()) {
                System.err.println("  [FilterScanner] Directory not found: " + directory.getPath());
                return filters;
            }

            File[] files = directory.listFiles();
            if (files == null) return filters;

            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = basePackage + "." + file.getName().replace(".class", "");

                    try {
                        Class<?> clazz = Class.forName(className);

                        // Check if it has @MyFilterMapping AND implements MyFilter
                        if (clazz.isAnnotationPresent(MyFilterMapping.class)
                                && MyFilter.class.isAssignableFrom(clazz)) {

                            MyFilterMapping annotation = clazz.getAnnotation(MyFilterMapping.class);
                            MyFilter filterInstance = (MyFilter) clazz.getDeclaredConstructor().newInstance();

                            filters.add(new FilterEntry(filterInstance, annotation.value(), annotation.order()));
                            System.out.println("  [FilterScanner] Found filter: " + className +
                                    " → pattern: " + annotation.value() + ", order: " + annotation.order());
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("  [FilterScanner] Cannot load: " + className);
                    } catch (Exception e) {
                        System.err.println("  [FilterScanner] Cannot instantiate: " + className);
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  [FilterScanner] Error scanning package: " + basePackage);
            e.printStackTrace();
        }

        filters.sort(Comparator.comparingInt(f -> f.order));

        return filters;
    }
}
