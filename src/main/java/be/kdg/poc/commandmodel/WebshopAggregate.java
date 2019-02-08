package be.kdg.poc.commandmodel;

import be.kdg.poc.configuration.WebshopConfiguration;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:06
 */

@Aggregate
@NoArgsConstructor // Required for Axon test fixture
public class WebshopAggregate {
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @AggregateIdentifier
    private String id;
    private Webshop webshop;

    @CommandHandler
    public WebshopAggregate(CreateWebshopCommand createShopCommand) {
        Assert.hasLength(createShopCommand.getId(), "Missing id");
        Assert.hasLength(createShopCommand.getName(), "Missing shop name");

        AggregateLifecycle.apply(new WebshopCreatedEvent(createShopCommand.getId(), createShopCommand.getName(), WebshopConfiguration.INITIAL_BALANCE));
    }

    @CommandHandler
    protected void handle(DeleteWebshopCommand deleteShopCommand) {
        AggregateLifecycle.apply(new WebshopDeletedEvent(deleteShopCommand.getShopId()));
    }

    @CommandHandler
    protected String handle(AddProductCommand addProductCommand) {
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
        if (optionalAmount.isPresent() && optionalAmount.get() - 1 < WebshopConfiguration.LOW_STOCK_TRIGGER) {
            // Restock product
            AggregateLifecycle.apply(new LowStockEvent(this.id, buyProductCommand.getProductId()));
        }

        // Recalculate price
        AggregateLifecycle.apply(new PriceDiscountRecalculatedEvent(buyProductCommand.getShopId(), buyProductCommand.getProductId()));

        return buyProductCommand.getProductId();
    }

    @EventSourcingHandler
    protected void on(WebshopCreatedEvent shopCreatedEvent) {
        this.id = shopCreatedEvent.getId();
        this.webshop = new Webshop(
                shopCreatedEvent.getId(),
                shopCreatedEvent.getName(),
                shopCreatedEvent.getBalance()
        );

        LOGGER.info("Shop created: " + webshop);
    }

    @EventSourcingHandler
    protected void on(WebshopDeletedEvent shopDeletedEvent) {
        AggregateLifecycle.markDeleted();

        LOGGER.info("Shop deleted: " + webshop);
    }

    @EventSourcingHandler
    protected void on(ProductAddedEvent productAddedEvent) {
        this.webshop.getInventory().put(productAddedEvent.getProduct(), WebshopConfiguration.INITIAL_PRODUCT_STOCK);

        LOGGER.info("Product added to shop with id '" + id + "': " + productAddedEvent.getProduct());
    }

    @EventSourcingHandler
    protected void on(ProductRemovedEvent productRemovedEvent) {
        Product product = this.webshop.getProduct(productRemovedEvent.getProductId()).get();
        this.webshop.getInventory().remove(product);

        LOGGER.info("Product (" + product + ") added to shop with id '" + id + "'");
    }

    @EventSourcingHandler
    protected void on(ProductBoughtEvent productBoughtEvent) {
        // Get product and stock from inventory
        Product product = this.webshop.getProduct(productBoughtEvent.getProductId()).get();
        // Lower stock by one
        this.webshop.getInventory().put(product, this.webshop.getInventory().get(product) - 1);
        // Add retailprice to current balance
        this.webshop.setBalance(this.webshop.getBalance() + product.getRetailPrice());

        LOGGER.info("Product (" + product + ") bought from shop with id '" + id + "'");
    }

    @EventSourcingHandler
    protected void on(LowStockEvent lowStockEvent) {
        // Get product
        Product product = webshop.getProduct(lowStockEvent.getProductId()).get();

        // Lower balance
        webshop.setBalance(webshop.getBalance() - (product.getBuyPrice() * WebshopConfiguration.RESTOCK_AMOUNT));

        // Restock product
        webshop.getInventory().compute(product, (key, value) -> ((value == null) ? 0 : value) + WebshopConfiguration.RESTOCK_AMOUNT);
    }

    @EventSourcingHandler
    protected void on(PriceDiscountRecalculatedEvent priceDiscountRecalculatedEvent) {
        // Get product
        Product product = webshop.getProduct(priceDiscountRecalculatedEvent.getProductId()).get();
        int inventoryAmount = webshop.getInventory().get(product);

        // Set discount based on leftover stock
        if (inventoryAmount < WebshopConfiguration.RESTOCK_AMOUNT && WebshopConfiguration.RESTOCK_AMOUNT < WebshopConfiguration.INITIAL_PRODUCT_STOCK) {
            product.setDiscountPercentage(0.25);
        } else if (inventoryAmount < WebshopConfiguration.RESTOCK_AMOUNT * 2 && WebshopConfiguration.RESTOCK_AMOUNT * 2 < WebshopConfiguration.INITIAL_PRODUCT_STOCK ) {
            product.setDiscountPercentage(0.15);
        } else if (inventoryAmount < WebshopConfiguration.RESTOCK_AMOUNT * 3 && WebshopConfiguration.RESTOCK_AMOUNT * 3 < WebshopConfiguration.INITIAL_PRODUCT_STOCK ) {
            product.setDiscountPercentage(0.10);
        } else if (inventoryAmount < WebshopConfiguration.RESTOCK_AMOUNT * 4 && WebshopConfiguration.RESTOCK_AMOUNT * 4 < WebshopConfiguration.INITIAL_PRODUCT_STOCK ) {
            product.setDiscountPercentage(0.05);
        } else {
            product.setDiscountPercentage(0);
        }
    }

    @Override
    public String toString() {
        return "Webshop '" + this.webshop.getName() + "' (id: '" + id + "') has balance of €" + String.format("%.2f", this.webshop.getBalance());
    }
}
