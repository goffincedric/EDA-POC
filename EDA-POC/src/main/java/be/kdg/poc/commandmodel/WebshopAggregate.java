package be.kdg.poc.commandmodel;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.webshop.command.*;
import be.kdg.poc.webshop.dom.Webshop;
import be.kdg.poc.webshop.event.*;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.Assert;

import java.util.Optional;

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
    private Webshop webshop;

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

    @CommandHandler
    protected void handle(DeleteShopCommand deleteShopCommand) {
        System.out.println("Delete shop");
        AggregateLifecycle.apply(new ShopDeletedEvent(deleteShopCommand.getShopId()));
    }

    @CommandHandler
    protected String handle(AddProductCommand addProductCommand) {
        System.out.println("Add product");
        Assert.isTrue(
                !this.webshop.productExists(addProductCommand.getProduct().getId()),
                "Product already in shop"
        );
        AggregateLifecycle.apply(new ProductAddedEvent(addProductCommand.getShopId(), addProductCommand.getProduct()));
        return addProductCommand.getProduct().getId();
    }

    @CommandHandler
    protected void handle(RemoveProductCommand removeProductCommand) {
        Assert.isTrue(
                this.webshop.productExists(removeProductCommand.getProductId()),
                "Product not found"
        );
        AggregateLifecycle.apply(new ProductRemovedEvent(removeProductCommand.getShopId(), removeProductCommand.getProductId()));
    }

    @CommandHandler
    protected String handle(BuyProductCommand buyProductCommand) {
        System.out.println("Buy product");
        Assert.isTrue(
                this.webshop.productExists(buyProductCommand.getProductId()),
                "Product not found"
        );
        Assert.isTrue(
                this.webshop.getInventoryAmount(buyProductCommand.getProductId()).get() - 1 >= 0,
                "Insufficient amount of product in inventory"
        );

        // Buy product event
        AggregateLifecycle.apply(new ProductBoughtEvent(buyProductCommand.getShopId(), buyProductCommand.getProductId()));

        // Check for low stock
        Optional<Integer> optionalAmount = webshop.getInventoryAmount(buyProductCommand.getProductId());
        if (optionalAmount.isPresent() && optionalAmount.get() - 1 < LOW_STOCK_TRIGGER) {
            // Restock product
            AggregateLifecycle.apply(new LowStockEvent(this.id, buyProductCommand.getProductId()));
        }
        return buyProductCommand.getProductId();
    }

    @EventSourcingHandler
    protected void on(ShopCreatedEvent shopCreatedEvent) {
        System.out.println("Shop created");
        this.id = shopCreatedEvent.getId();
        this.webshop = new Webshop(
                shopCreatedEvent.getId(),
                shopCreatedEvent.getName(),
                shopCreatedEvent.getBalance()
        );
    }

    @EventSourcingHandler
    protected void on(ShopDeletedEvent shopDeletedEvent) {
        System.out.println("Shop deleted");
        AggregateLifecycle.markDeleted();
    }

    @EventSourcingHandler
    protected void on(ProductAddedEvent productAddedEvent) {
        System.out.println("Product added");
        this.webshop.getInventory().put(productAddedEvent.getProduct(), 0);
    }

    @EventSourcingHandler
    protected void on(ProductRemovedEvent productRemovedEvent) {
        System.out.println("Product Removed");
        Product product = this.webshop.getProduct(productRemovedEvent.getProductId()).get();
        this.webshop.getInventory().remove(product);
    }

    @EventSourcingHandler
    protected void on(ProductBoughtEvent productBoughtEvent) {
        System.out.println("Product bought");
        // Get product and stock from inventory
        Product product = this.webshop.getProduct(productBoughtEvent.getProductId()).get();
        // Lower stock by one
        this.webshop.getInventory().put(product, this.webshop.getInventory().get(product) - 1);
        // Add retailprice to current balance
        this.webshop.setBalance(this.webshop.getBalance() + product.getRetailPrice());
    }

    @EventSourcingHandler
    protected void on(LowStockEvent lowStockEvent) {
        // TODO: Implement buying new stock, checking if possible with current balance, if balance lower than set limit declare shop bankrupt and initiate delete shop...
    }

    @Override
    public String toString() {
        return "Webshop '" + this.webshop.getName() + "' (id: '" + id + "') has balance of €" + String.format("%.2f", this.webshop.getBalance());
    }
}
