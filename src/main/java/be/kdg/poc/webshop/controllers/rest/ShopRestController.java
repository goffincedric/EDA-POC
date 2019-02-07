package be.kdg.poc.webshop.controllers.rest;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.product.dto.ProductDTO;
import be.kdg.poc.webshop.command.*;
import be.kdg.poc.webshop.dom.Webshop;
import be.kdg.poc.webshop.query.GetAllProductsQuery;
import be.kdg.poc.webshop.query.GetAllWebshops;
import be.kdg.poc.webshop.query.GetCurrentBalanceQuery;
import be.kdg.poc.webshop.query.GetCurrentStockAmountQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.queryhandling.QueryGateway;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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

    // Returns id of created shop
    @PostMapping("/initializeShop")
    public ResponseEntity<String> initializeShop(@Value(value = "${webshop.initial.name}") String shopName) throws ExecutionException, InterruptedException {
        // Check if any webshops are present
        List<Webshop> webshopList = queryGateway.query(new GetAllWebshops(), List.class).get();
        if (webshopList.isEmpty()) {
            // Create new webshop
            String webshopId = UUID.randomUUID().toString();
            commandGateway.send(new CreateWebshopCommand(webshopId, shopName));

            // Add products to webshop
            List<Product> products = Arrays.asList(
                    new Product(
                            UUID.randomUUID().toString(),
                            "Keyboard vaccuum cleaner 3000",
                            200,
                            0,
                            100
                    ),
                    new Product(
                            UUID.randomUUID().toString(),
                            "Banana slicer",
                            15,
                            0,
                            5
                    ),
                    new Product(
                            UUID.randomUUID().toString(),
                            "YBox Two",
                            150,
                            0,
                            100
                    ),
                    new Product(
                            UUID.randomUUID().toString(),
                            "Stijn's broken HDD",
                            100,
                            0,
                            20
                    )
            );
            products.forEach(product ->
                    commandGateway.send(new AddProductCommand(
                            webshopId,
                            product
                    ))
            );

            return new ResponseEntity<>(
                    "Webshop already initialized",
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    "Webshop already initialized",
                    HttpStatus.OK
            );
        }
    }

    @GetMapping("/getWebshops")
    public ResponseEntity<List<Webshop>> getWebshops() throws ExecutionException, InterruptedException {
        List<Webshop> webshops = queryGateway.query(new GetAllWebshops(), List.class).get();
        return new ResponseEntity<>(
                webshops,
                HttpStatus.OK
        );
    }

    @GetMapping("/getProducts")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(value = "shopId") String shopId) throws ExecutionException, InterruptedException {
        List<Product> products = queryGateway.query(new GetAllProductsQuery(shopId), List.class).get();
        return new ResponseEntity<>(
                products,
                HttpStatus.OK
        );
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
    public ResponseEntity<String> createShop(@RequestParam(value = "name") String name) throws ExecutionException, InterruptedException {
        String id = UUID.randomUUID().toString();

        // Blocks on get
        String result = (String) commandGateway.send(new CreateWebshopCommand(id, name)).get();
        return new ResponseEntity<>(
                result,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity deleteShop(@RequestParam(value = "shopId") String shopId) throws ExecutionException, InterruptedException {
        commandGateway.send(new DeleteWebshopCommand(shopId)).get();
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/addProduct")
    public ResponseEntity<String> addProduct(@RequestParam(value = "shopId") String shopId, @RequestBody ProductDTO productDTO) throws ExecutionException, InterruptedException {
        Product product = modelMapper.map(productDTO, Product.class);
        String productId = UUID.randomUUID().toString();
        product.setId(productId);

        // Blocks on get
        String result = (String) commandGateway.send(new AddProductCommand(shopId, product)).get();
        return new ResponseEntity<>(
                result,
                HttpStatus.OK
        );
    }

    @PutMapping("/removeProduct")
    public ResponseEntity removeProduct(@RequestParam(value = "shopId") String shopId, @RequestParam(value = "productId") String productId) throws ExecutionException, InterruptedException {
        commandGateway.send(new RemoveProductCommand(shopId, productId)).get();
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/buy")
    public ResponseEntity<String> buyProduct(@RequestParam(value = "shopId") String shopId, @RequestParam(value = "productId") String productId) throws ExecutionException, InterruptedException {
        String result = (String) commandGateway.send(new BuyProductCommand(shopId, productId)).get();
        return new ResponseEntity<>(
                HttpStatus.OK
        );
    }


    // When aggregate cannot be found
    // TODO: MOVE TO CONTROLLERADVICE?
    @ExceptionHandler(AggregateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }
}
