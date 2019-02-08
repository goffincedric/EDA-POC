package be.kdg.poc.querymodel;

import be.kdg.poc.configuration.WebshopConfiguration;
import be.kdg.poc.product.dom.Product;
import be.kdg.poc.webshop.dom.Webshop;
import be.kdg.poc.webshop.event.*;
import be.kdg.poc.webshop.query.GetAllProductsQuery;
import be.kdg.poc.webshop.query.GetAllWebshops;
import be.kdg.poc.webshop.query.GetCurrentBalanceQuery;
import be.kdg.poc.webshop.query.GetCurrentStockAmountQuery;
import lombok.NoArgsConstructor;
import org.axonframework.eventhandling.AllowReplay;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Cédric Goffin
 * 03/02/2019 15:45
 */
@Service
@AllowReplay
@NoArgsConstructor
public class WebshopsEventHandler {
    private final Map<String, Webshop> webshops = new HashMap<>();

    @EventSourcingHandler
    public void on(WebshopCreatedEvent shopCreatedEvent) {
        this.webshops.put(
                shopCreatedEvent.getId(),
                new Webshop(
                        shopCreatedEvent.getId(),
                        shopCreatedEvent.getName(),
                        shopCreatedEvent.getBalance()
                )
        );
    }

    @EventSourcingHandler
    public void on(WebshopDeletedEvent shopDeletedEvent) {
        this.webshops.remove(shopDeletedEvent.getId());
    }

    @EventSourcingHandler
    public void on(ProductAddedEvent productAddedEvent) {
        this.webshops.get(productAddedEvent.getShopId()).getInventory().put(productAddedEvent.getProduct(), WebshopConfiguration.INITIAL_PRODUCT_STOCK);
    }

    @EventSourcingHandler
    public void on(ProductRemovedEvent productRemovedEvent) {
        Product product = this.webshops.get(productRemovedEvent.getShopId()).getProduct(productRemovedEvent.getProductId()).get();
        this.webshops.get(productRemovedEvent.getShopId()).getInventory().remove(product);
    }

    @EventSourcingHandler
    public void on(ProductBoughtEvent productBoughtEvent) {
        Webshop webshop = this.webshops.get(productBoughtEvent.getShopId());
        // Get product and stock from inventory
        Product product = webshop.getProduct(productBoughtEvent.getProductId()).get();
        // Lower stock by one
        webshop.getInventory().put(product, webshop.getInventory().get(product) - 1);
        // Add retailprice to current balance
        webshop.setBalance(webshop.getBalance() + product.getRetailPrice());
    }

    @EventSourcingHandler
    public void on(LowStockEvent lowStockEvent) {
        Webshop webshop = this.webshops.get(lowStockEvent.getShopId());

        // Get product
        Product product = webshop.getProduct(lowStockEvent.getProductId()).get();

        // Lower balance
        webshop.setBalance(webshop.getBalance() - (product.getBuyPrice() * WebshopConfiguration.RESTOCK_AMOUNT));

        // Restock product
        webshop.getInventory().compute(product, (key, value) -> ((value == null) ? 0 : value) + WebshopConfiguration.RESTOCK_AMOUNT);

        System.out.println(webshop.getBalance());
        System.out.println(this.webshops.get(lowStockEvent.getShopId()).getBalance());
    }

    @QueryHandler
    protected Optional handle(GetCurrentBalanceQuery getCurrentBalanceQuery) {
        System.out.println("Balance query");
        if (webshops.containsKey(getCurrentBalanceQuery.getShopId())) {
            return Optional.of(webshops.get(getCurrentBalanceQuery.getShopId()).getBalance());
        } else {
            return Optional.empty();
        }
    }

    @QueryHandler
    protected Optional handle(GetCurrentStockAmountQuery getCurrentStockAmountQuery) {
        System.out.println("Amount query");
        if (webshops.containsKey(getCurrentStockAmountQuery.getShopId())) {
            return webshops.get(getCurrentStockAmountQuery.getShopId()).getInventoryAmount(getCurrentStockAmountQuery.getProductId());
        } else {
            return Optional.empty();
        }
    }

    @QueryHandler
    protected List handle(GetAllWebshops getAllWebshops) {
        return new ArrayList<>(webshops.values());
    }

    @QueryHandler
    protected List handle(GetAllProductsQuery getAllProductsQuery) {
        System.out.println("All products query");
        if (webshops.containsKey(getAllProductsQuery.getShopId())) {
            return new ArrayList<>(webshops.get(getAllProductsQuery.getShopId()).getInventory().keySet());
        } else {
            return new ArrayList<>();
        }
    }
}
