package be.kdg.poc.webshop.controllers.rest;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.product.dto.ProductDTO;
import be.kdg.poc.webshop.command.*;
import be.kdg.poc.webshop.query.GetCurrentBalanceQuery;
import be.kdg.poc.webshop.query.GetCurrentStockAmountQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.queryhandling.QueryGateway;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:43
 */

@RestController
@RequestMapping("/api/shop")
public class ShopRestController {
    private final ModelMapper modelMapper;
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public ShopRestController(ModelMapper modelMapper, CommandGateway commandGateway, QueryGateway queryGateway) {
        this.modelMapper = modelMapper;
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @GetMapping("/currentBalance")
    public ResponseEntity<Double> getCurrentBalance(@RequestParam(value = "shopId") String shopId) throws ExecutionException, InterruptedException {
        Optional<Double> optionalBalance = (Optional<Double>) queryGateway.query(new GetCurrentBalanceQuery(shopId), Optional.class).get();
        return optionalBalance.map(balance -> new ResponseEntity<>(
                balance,
                HttpStatus.OK
        )).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping("/stockAmount")
    public ResponseEntity<Integer> getCurrentStockAmount(@RequestParam(value = "shopId") String shopId, @RequestParam(value = "productId") String productId) throws ExecutionException, InterruptedException {
        Optional<Integer> optionalAmount = (Optional<Integer>) queryGateway.query(new GetCurrentStockAmountQuery(shopId, productId), Optional.class).get();
        return optionalAmount.map(amount -> new ResponseEntity<>(
                amount,
                HttpStatus.OK
        )).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    // Returns id of created shop
    @PostMapping("/create")
    public CompletableFuture<String> createShop(@RequestParam(value = "name") String name) {
        String id = UUID.randomUUID().toString();

        CompletableFuture<String> future = commandGateway.send(new CreateShopCommand(id, name));
        return future;
    }

    @DeleteMapping("/delete")
    public CompletableFuture<Object> deleteShop(@RequestParam(value = "shopId") String shopId) {
        CompletableFuture<Object> future = commandGateway.send(new DeleteShopCommand(shopId));
        return future;
    }

    @PutMapping("/addProduct")
    public ResponseEntity<String> addProduct(@RequestParam(value = "shopId") String shopId, @RequestBody ProductDTO productDTO) throws ExecutionException, InterruptedException {
        Product product = modelMapper.map(productDTO, Product.class);
        String productId = UUID.randomUUID().toString();
        product.setId(productId);

        // Blocks on get
        commandGateway.send(new AddProductCommand(shopId, product)).get();
        return new ResponseEntity<>(
                productId,
                HttpStatus.OK
        );
    }

    @PutMapping("/removeProduct")
    public CompletableFuture<Object> removeProduct(@RequestParam(value = "shopId") String shopId, @RequestParam(value = "productId") String productId) {
        CompletableFuture<Object> future = commandGateway.send(new RemoveProductCommand(shopId, productId));
        return future;
    }

    @PutMapping("/buy")
    public CompletableFuture<Object> buyProduct(@RequestParam(value = "shopId") String shopId, @RequestParam(value = "productId") String productId) {
        CompletableFuture<Object> future = commandGateway.send(new BuyProductCommand(shopId, productId));
        return future;
    }


    // When aggregate cannot be found
    // TODO: MOVE TO CONTROLLERADVICE?
    @ExceptionHandler(AggregateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }
}
