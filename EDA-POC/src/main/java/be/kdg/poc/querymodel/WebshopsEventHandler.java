package be.kdg.poc.querymodel;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.webshop.dom.Webshop;
import be.kdg.poc.webshop.event.*;
import be.kdg.poc.webshop.query.GetCurrentBalanceQuery;
import be.kdg.poc.webshop.query.GetCurrentStockAmountQuery;
import lombok.NoArgsConstructor;
import org.axonframework.eventhandling.AllowReplay;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public void on(ShopCreatedEvent shopCreatedEvent) {
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
    public void on(ShopDeletedEvent shopDeletedEvent) {
        this.webshops.remove(shopDeletedEvent.getId());
    }

    @EventSourcingHandler
    public void on(ProductAddedEvent productAddedEvent) {
        this.webshops.get(productAddedEvent.getShopId()).getInventory().put(productAddedEvent.getProduct(), 0);
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
        // TODO: Implement buying new stock, checking if possible with current balance, if balance lower than set limit declare shop bankrupt and initiate delete shop...
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
}
