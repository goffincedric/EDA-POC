package be.kdg.poc.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cédric Goffin
 * 08/02/2019 13:54
 */
@Configuration
public class WebshopConfiguration {
    public static int LOW_STOCK_TRIGGER;
    public static int INITIAL_PRODUCT_STOCK;
    public static int RESTOCK_AMOUNT;
    public static double INITIAL_BALANCE;
    public static double BASE_DISCOUNT;

    @Value("${webshop.product.stock.low_stock_trigger}")
    public void setLowStockTrigger(int lowStockTrigger) {
        LOW_STOCK_TRIGGER = lowStockTrigger;
    }

    @Value("${webshop.product.stock.initial}")
    public void setInitialProductStock(int initialProductStock) {
        INITIAL_PRODUCT_STOCK = initialProductStock;
    }

    @Value("${webshop.product.stock.restock_amount}")
    public void setRestockAmount(int restockAmount) {
        RESTOCK_AMOUNT = restockAmount;
    }

    @Value("${webshop.initial.balance}")
    public void setInitialBalance(double initialBalance) {
        INITIAL_BALANCE = initialBalance;
    }

    @Value("${webshop.base_discount}")
    public void setBaseDiscount(double baseDiscount) {
        BASE_DISCOUNT = baseDiscount;
    }
}
