package be.kdg.poc.webshop.controllers.rest;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.product.dto.ProductDTO;
import be.kdg.poc.webshop.command.*;
import be.kdg.poc.webshop.query.GetCurrentBalanceCommand;
import be.kdg.poc.webshop.query.GetCurrentStockAmountCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:43
 */

@RestController
@RequestMapping("/api/shop")
public class ShopRestController {
    private final ModelMapper modelMapper;
    private final CommandGateway commandGateway;

    @Autowired
    public ShopRestController(ModelMapper modelMapper, CommandGateway commandGateway) {
        this.modelMapper = modelMapper;
        this.commandGateway = commandGateway;
    }

    @GetMapping("currentBalance/{shopId}")
    public CompletableFuture<Object> getCurrentStockAmount(@PathVariable String shopId) {
        CompletableFuture<Object> future = commandGateway.send(new GetCurrentBalanceCommand(shopId));
        return future;
    }

    @GetMapping("/stockAmount")
    public CompletableFuture<Object> getCurrentBalance(@RequestParam(value = "shopId") String shopId, @RequestParam(value = "productId") String productId) {
        CompletableFuture<Object> future = commandGateway.send(new GetCurrentStockAmountCommand(shopId, productId));
        return future;
    }

    // Returns id of created shop
    @PostMapping("/create")
    public CompletableFuture<String> createShop(@RequestParam(value = "name") String name) {
        String id = UUID.randomUUID().toString();

        CompletableFuture<String> future = commandGateway.send(new CreateShopCommand(id, name));
        return future;
    }

    @DeleteMapping("delete/{id}")
    public CompletableFuture<Object> deleteShop(@PathVariable String id) {
        CompletableFuture<Object> future = commandGateway.send(new DeleteShopCommand(id));
        return future;
    }

    @PutMapping("buy/{shopId}/{productId}")
    public CompletableFuture<Object> buyProduct(@PathVariable String shopId, @PathVariable String productId) {
        CompletableFuture<Object> future = commandGateway.send(new BuyProductCommand(shopId, productId));
        return future;
    }

    @PutMapping("add/{shopId}/{productId}")
    public CompletableFuture<Object> addProduct(@PathVariable String shopId, @RequestBody ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);

        CompletableFuture<Object> future = commandGateway.send(new AddProductCommand(shopId,  product));
        return future;
    }

    @PutMapping("remove/{shopId}/{productId}")
    public CompletableFuture<Object> removeProduct(@PathVariable String shopId, @PathVariable String productId) {
        CompletableFuture<Object> future = commandGateway.send(new RemoveProductCommand(shopId, productId));
        return future;
    }


    // When aggregate cannot be found
    // TODO: MOVE TO CONTROLLERADVICE?
    @ExceptionHandler(AggregateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }
}
