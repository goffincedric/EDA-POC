package be.kdg.poc;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.webshop.dom.Webshop;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cédric Goffin
 * 07/02/2019 21:51
 */
@EnableAsync
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EDAApplicationPerformanceTest {
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Autowired
    private ObjectMapper objectMapper;

    private final String URL = "http://eventshop-poc.herokuapp.com/api/shop";

    private Webshop webshop;
    private List<Product> products;

    @Before
    public void initializeShop() throws Exception {
        // Initialize shop, if it already hasn't been
        HttpResponse initializeResponse = HttpClientBuilder.create().build().execute(new HttpPost(URL + "/initializeShop"));
        assertTrue(initializeResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value() || initializeResponse.getStatusLine().getStatusCode() == HttpStatus.I_AM_A_TEAPOT.value());

        // Get webshop(s)
        HttpResponse getWebshopsResponse = HttpClientBuilder.create().build().execute(new HttpGet(URL + "/getWebshops"));
        assertEquals(getWebshopsResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
        List<Webshop> webshops = objectMapper.readValue(
                getWebshopsResponse.getEntity().getContent(),
                new TypeReference<List<Webshop>>() {
                });
        Assert.assertTrue(!webshops.isEmpty());
        webshop = webshops.stream().max(Comparator.comparing(webshop -> webshop.getInventory().size())).get();

        // Get product(s) of first webshop
        HttpResponse getProductsResponse = HttpClientBuilder.create().build().execute(new HttpGet(URL + "/getProducts?shopId=" + webshop.getId()));
        assertEquals(getWebshopsResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
        products = objectMapper.readValue(
                getProductsResponse.getEntity().getContent(),
                new TypeReference<List<Product>>() {
                });
        Assert.assertTrue(!products.isEmpty());
    }

    @Test
    public void stressTestAPIPerformance() throws InterruptedException {
        // Make ~50 threads and buy random product 1200 times each 50ms
        int threads = 50;
        int callAmount = 1200;
        int callDelayMs = 50;

        // Completionservice
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(threads));

        for (int i = 0; i < threads; i++) {
            completionService.submit(() -> {
                // Random for random choice
                Random random = new Random();

                for (int j = 0; j < callAmount; j++) {
                    // Choose random product
                    int index = random.nextInt(products.size());

                    // Call API
                    HttpResponse buyProductResponse = HttpClientBuilder.create().build().execute(new HttpPut(URL + "/buy?shopId=" + webshop.getId() + "&productId=" + products.get(index).getId()));
                    if (buyProductResponse.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                        LOGGER.warning("Request #" + j + " returned status code " + buyProductResponse.getStatusLine().getStatusCode());
                    }

                    // Delay
                    Thread.sleep(callDelayMs);
                }

                // Return async result
                return true;
            });
        }

        // Wait for async tasks to finish
        int received = 0;
        boolean errors = false;

        while (received < threads && !errors) {
            Future<Boolean> resultFuture = completionService.take(); //blocks if none available
            try {
                Boolean result = resultFuture.get();
                received++;
            } catch (Exception e) {
                //log
                errors = true;
            }
        }
    }
}
