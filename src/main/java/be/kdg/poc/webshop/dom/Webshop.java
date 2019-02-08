package be.kdg.poc.webshop.dom;

import be.kdg.poc.product.dom.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Cédric Goffin
 * 04/02/2019 16:11
 */
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Webshop {
    private String id;
    private String name;

    @JsonIgnore
    private Map<Product, Integer> inventory = new HashMap<>();

    private double balance;

    public Webshop(String id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public boolean productExists(String productId) {
        return this.inventory.keySet().stream().anyMatch(product -> product.getId().equals(productId));
    }

    public Optional<Product> getProduct(String productId) {
        return this.inventory.keySet().stream().filter(p -> p.getId().equals(productId)).findFirst();
    }

    public Optional<Integer> getInventoryAmount(String productId) {
        return inventory.entrySet().stream().filter(entry -> entry.getKey().getId().equals(productId)).map(Map.Entry::getValue).findFirst();
    }

    @Override
    public String toString() {
        return "Id: '" + id + "', Name: '" + name + "', Balance: " + balance;
    }
}
