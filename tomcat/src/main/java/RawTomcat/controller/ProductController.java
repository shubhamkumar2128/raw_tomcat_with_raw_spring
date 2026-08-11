package RawTomcat.controller;

import RawTomcat.dispatcher.annotations.*;

import java.util.HashMap;
import java.util.Map;

@MyController
public class ProductController {

    private final Map<String, String> products = new HashMap<>();
    private int nextId = 1;

    public ProductController() {
        products.put("1", "iPhone - $999");
        products.put("2", "MacBook - $1499");
        products.put("3", "AirPods - $249");
        nextId = 4;
    }

    @MyGetMapping("/products")
    public String getAllProducts() {
        if (products.isEmpty()) {
            return "No products found.";
        }
        StringBuilder sb = new StringBuilder("All Products:\n");
        for (Map.Entry<String, String> entry : products.entrySet()) {
            sb.append("  [").append(entry.getKey()).append("] ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    @MyGetMapping("/products/{id}")
    public String getProduct(@MyPathVariable("id") String id) {
        String product = products.get(id);
        if (product == null) {
            return "Product not found with id: " + id;
        }
        return "Product [" + id + "]: " + product;
    }

    @MyGetMapping("/products/search")
    public String searchProducts(@MyRequestParam("name") String name) {
        StringBuilder sb = new StringBuilder("Search results for '" + name + "':\n");
        boolean found = false;

        for (Map.Entry<String, String> entry : products.entrySet()) {
            if (entry.getValue().toLowerCase().contains(name.toLowerCase())) {
                sb.append("  [").append(entry.getKey()).append("] ").append(entry.getValue()).append("\n");
                found = true;
            }
        }

        return found ? sb.toString() : "No products matching: " + name;
    }

    @MyPostMapping("/products")
    public String createProduct(@MyRequestParam("name") String name,
                                @MyRequestParam("price") String price) {
        String id = String.valueOf(nextId++);
        products.put(id, name + " - $" + price);
        return "Created product [" + id + "]: " + name + " - $" + price;
    }

    @MyPutMapping("/products/{id}")
    public String updateProduct(@MyPathVariable("id") String id,
                                @MyRequestParam("name") String name,
                                @MyRequestParam("price") String price) {
        if (!products.containsKey(id)) {
            return "Product not found with id: " + id;
        }
        products.put(id, name + " - $" + price);
        return "Updated product [" + id + "]: " + name + " - $" + price;
    }

    @MyDeleteMapping("/products/{id}")
    public String deleteProduct(@MyPathVariable("id") String id) {
        String removed = products.remove(id);
        if (removed == null) {
            return "Product not found with id: " + id;
        }
        return "Deleted product [" + id + "]: " + removed;
    }
}
