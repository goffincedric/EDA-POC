package be.kdg.poc.commandmodel;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.webshop.command.*;
import be.kdg.poc.webshop.event.*;
import be.kdg.poc.webshop.exception.InsufficientStockException;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:06
 */

@Aggregate
@NoArgsConstructor // Required for Axon test fixture
public class WebshopAggregate {
    // TODO: Initialize value from properties?
    private static final int LOW_STOCK_TRIGGER = 5;

    @AggregateIdentifier
    private String id;
    private String name;

    private Map<Product, Integer> inventory;

    private double balance;

    // Constructor/handler for CreateShopCommand
    @CommandHandler
    public WebshopAggregate(CreateShopCommand createShopCommand) {
        System.out.println("Create shop");
        Assert.hasLength(createShopCommand.getId(), "Missing id");
        Assert.hasLength(createShopCommand.getName(), "Missing shop name");

        // TODO: Change default balance to e.g. 10000.00?
        // TODO: Move to properties?
        double defaultBalance = 0;
        AggregateLifecycle.apply(new ShopCreatedEvent(createShopCommand.getId(), createShopCommand.getName(), defaultBalance));
    }

    // Eventhandler for shopCreatedEvent
    @EventSourcingHandler
    protected void on(ShopCreatedEvent shopCreatedEvent) {
        System.out.println("Shop created");
        this.id = shopCreatedEvent.getId();
        this.name = shopCreatedEvent.getName();

        this.inventory = new HashMap<>();

        this.balance = shopCreatedEvent.getBalance();
    }

    @CommandHandler
    protected void handle(DeleteShopCommand deleteShopCommand) {
        System.out.println("Delete shop");
        AggregateLifecycle.apply(new ShopDeletedEvent(deleteShopCommand.getShopId()));
    }

    @EventSourcingHandler
    protected void on(ShopDeletedEvent shopDeletedEvent) {
        System.out.println("Shop deleted");
        AggregateLifecycle.markDeleted();
    }

    @CommandHandler
    protected void handle(AddProductCommand addProductCommand) {
        Assert.isTrue(
                inventory.keySet().stream().noneMatch(product -> product.getId().equals(addProductCommand.getProduct().getId())),
                "Product already in shop"
        );
        AggregateLifecycle.apply(new ProductAddedEvent(addProductCommand.getShopId(), addProductCommand.getProduct()));
    }

    @EventSourcingHandler
    protected void on(ProductAddedEvent productAddedEvent) {
        inventory.put(productAddedEvent.getProduct(), 0);
    }

    @CommandHandler
    protected void handle(RemoveProductCommand removeProductCommand) {
        Assert.isTrue(
                inventory.keySet().stream().anyMatch(product -> product.getId().equals(removeProductCommand.getProductId())),
                "Product not found"
        );
        AggregateLifecycle.apply(new ProductRemovedEvent(removeProductCommand.getShopId(), removeProductCommand.getProductId()));
    }

    @EventSourcingHandler
    protected void on(ProductRemovedEvent productRemovedEvent) {
        Product product = inventory.keySet().stream().filter(p -> p.getId().equals(productRemovedEvent.getProductId())).findFirst().get();
        inventory.remove(product);
    }

    @CommandHandler
    protected void handle(BuyProductCommand buyProductCommand) {
        Assert.isTrue(
                inventory.keySet().stream().anyMatch(p -> p.getId().equals(buyProductCommand.getProductId())),
                "Product not found"
        );
        AggregateLifecycle.apply(new ProductBoughtEvent(buyProductCommand.getShopId(), buyProductCommand.getProductId()));
    }

    @EventSourcingHandler
    protected void on(ProductBoughtEvent productBoughtEvent) throws InsufficientStockException {
        // Get product and stock from inventory
        Product product = inventory.keySet().stream().filter(p -> p.getId().equals(productBoughtEvent.getProductId())).findFirst().get();
        int newStock = inventory.get(product) - 1;

        // Check stock requirements
        if (newStock > 0) {
            // Lower stock by one
            inventory.put(product, --newStock);
            // Add retailprice to current balance
            balance += product.getRetailPrice();

            // Check for low stock
            if (newStock > LOW_STOCK_TRIGGER)
                AggregateLifecycle.apply(new LowStockEvent(this.id, productBoughtEvent.getProductId()));
        } else {
            // If no products left to buy
            throw new InsufficientStockException();
        }
    }

    @EventSourcingHandler
    protected void on(LowStockEvent lowStockEvent) {
        // TODO: Implement buying new stock, checking if possible with current balance, if balance lower than set limit declare shop bankrupt and initiate delete shop...
    }

    @Override
    public String toString() {
        return "Webshop '" + name + "' (id: '" + id + "') has balance of €" + String.format("%.2f", balance);
    }
}
