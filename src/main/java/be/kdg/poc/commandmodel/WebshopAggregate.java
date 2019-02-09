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
import org.springframework.util.Assert;

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
        AggregateLifecycle.apply(new WebshopDeletedEvent(deleteShopCommand.getShopId(), webshop.getName()));
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

        // Get product from webshop
        Product product = this.webshop.getProduct(removeProductCommand.getProductId()).get();
        AggregateLifecycle.apply(new ProductRemovedEvent(removeProductCommand.getShopId(), removeProductCommand.getProductId(), product.getName()));
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
        Product product = this.webshop.getProduct(buyProductCommand.getProductId()).get();
        AggregateLifecycle.apply(new ProductBoughtEvent(buyProductCommand.getShopId(), buyProductCommand.getProductId(), product.getName()));

        // Check for low stock
        int inventoryAmount = webshop.getInventory().get(product);
        if (inventoryAmount < WebshopConfiguration.LOW_STOCK_TRIGGER) {
            AggregateLifecycle.apply(new LowStockEvent(this.id, buyProductCommand.getProductId(), product.getName()));
        }

        // Recheck stock
        inventoryAmount = webshop.getInventory().get(product);
        if (inventoryAmount > WebshopConfiguration.INITIAL_PRODUCT_STOCK && product.getDiscountPercentage()  > 0) {
            int newDiscount = 0;
            AggregateLifecycle.apply(new PriceDiscountRecalculatedEvent(this.id, buyProductCommand.getProductId(), product.getName(), newDiscount, product.getRetailPrice() * (1 - newDiscount)));
        } else {
            for (int i = 0; i < 5; i++) {
                double discountStock = WebshopConfiguration.INITIAL_PRODUCT_STOCK - (5 * i);
                if (discountStock < 0) {
                    break;
                } else if (inventoryAmount == discountStock) {
                    double newDiscount = WebshopConfiguration.BASE_DISCOUNT * i;
                    AggregateLifecycle.apply(new PriceDiscountRecalculatedEvent(this.id, buyProductCommand.getProductId(), product.getName(), newDiscount, product.getRetailPrice() * (1 - newDiscount)));
                }
            }
        }

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

        LOGGER.info(shopCreatedEvent.toString());
    }

    @EventSourcingHandler
    protected void on(WebshopDeletedEvent shopDeletedEvent) {
        AggregateLifecycle.markDeleted();

        LOGGER.info(shopDeletedEvent.toString());
    }

    @EventSourcingHandler
    protected void on(ProductAddedEvent productAddedEvent) {
        this.webshop.getInventory().put(productAddedEvent.getProduct(), WebshopConfiguration.INITIAL_PRODUCT_STOCK);

        LOGGER.info(productAddedEvent.toString());
    }

    @EventSourcingHandler
    protected void on(ProductRemovedEvent productRemovedEvent) {
        Product product = this.webshop.getProduct(productRemovedEvent.getProductId()).get();
        this.webshop.getInventory().remove(product);

        LOGGER.info(productRemovedEvent.toString());
    }

    @EventSourcingHandler
    protected void on(ProductBoughtEvent productBoughtEvent) {
        // Get product and stock from inventory
        Product product = this.webshop.getProduct(productBoughtEvent.getProductId()).get();
        // Lower stock by one
        this.webshop.getInventory().put(product, this.webshop.getInventory().get(product) - 1);
        // Add retailprice to current balance
        this.webshop.setBalance(this.webshop.getBalance() + product.getRetailPrice());

        LOGGER.info(productBoughtEvent.toString());
    }

    @EventSourcingHandler
    protected void on(LowStockEvent lowStockEvent) {
        // Get product
        Product product = webshop.getProduct(lowStockEvent.getProductId()).get();

        // Lower balance
        webshop.setBalance(webshop.getBalance() - (product.getBuyPrice() * WebshopConfiguration.RESTOCK_AMOUNT));

        // Restock product
        webshop.getInventory().compute(product, (key, value) -> ((value == null) ? 0 : value) + WebshopConfiguration.RESTOCK_AMOUNT);

        LOGGER.info(lowStockEvent.toString());
    }

    @EventSourcingHandler
    protected void on(PriceDiscountRecalculatedEvent priceDiscountRecalculatedEvent) {
        // Get product & set new discount percentage
        Product product = webshop.getProduct(priceDiscountRecalculatedEvent.getProductId()).get();
        product.setDiscountPercentage(priceDiscountRecalculatedEvent.getDiscount());

        LOGGER.info(priceDiscountRecalculatedEvent.toString());
    }

    @Override
    public String toString() {
        return this.webshop.toString();
    }
}
