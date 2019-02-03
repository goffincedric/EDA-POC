package be.kdg.poc.webshop.exception;

/**
 * @author Cédric Goffin
 * 02/02/2019 17:45
 */
public class InsufficientStockException extends Exception {
    public InsufficientStockException() {
        super("Insufficient stock to complete purchase");
    }
}
