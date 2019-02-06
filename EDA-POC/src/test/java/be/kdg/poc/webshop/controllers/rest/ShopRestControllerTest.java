package be.kdg.poc.webshop.controllers.rest;

import be.kdg.poc.commandmodel.WebshopAggregate;
import be.kdg.poc.product.dom.Product;
import be.kdg.poc.product.dto.ProductDTO;
import be.kdg.poc.webshop.event.ProductAddedEvent;
import be.kdg.poc.webshop.event.ShopCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * @author Cédric Goffin
 * 02/02/2019 15:07
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ShopRestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ObjectMapper objectMapper;

    private FixtureConfiguration<WebshopAggregate> fixture = new AggregateTestFixture<>(WebshopAggregate.class);


    @Test
    public void testEvents() throws Exception {
        String shopId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        fixture.given(
                new ShopCreatedEvent(shopId, "test", 0),
                new ProductAddedEvent(
                        shopId,
                        new Product(
                                productId,
                                10,
                                5
                        )
                )
        );

//        String shopId = initializeShop();
//
//        String productId = UUID.randomUUID().toString();
//        ProductDTO productDTO = new ProductDTO(productId, "Test_Product_1", 10.0, 5.0);
//        mockMvc.perform(put("/api/shop/addProduct?shopId=" + shopId + "&productId=" + productId)
//                .contentType(APPLICATION_JSON_UTF8)
//                .content(objectMapper.writeValueAsString(productDTO)))
//                .andDo(print())
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(result -> Assert.assertEquals(productId, result.getAsyncResult().toString()));

        String url = "/api/shop/stockAmount?shopId=" + shopId + "&productId=" + productId;
        mockMvc.perform(get(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> Assert.assertEquals(0, Integer.valueOf(result.getResponse().getContentAsString()).intValue()));
    }

    @Test
    public void createShop() throws Exception {
        String url = "/api/shop/create?name=Test_Shop";
        mockMvc.perform(post(url))
                .andDo(result -> System.out.println(result.getAsyncResult()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    // If invalid, will throw IllegalArgumentException
                    UUID.fromString(result.getAsyncResult().toString());
                });
    }

    @Test
    public void deleteShop() throws Exception {
        String shopId = initializeShop();

        String deleteUrl = "/api/shop/delete?shopId=" + shopId;
        mockMvc.perform(delete(deleteUrl))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void addProduct() throws Exception {
        String shopId = initializeShop();
        String productId = UUID.randomUUID().toString();

        ProductDTO productDTO = new ProductDTO("Test_Product_1", 10.0, 5.0);

        String url = "/api/shop/addProduct?shopId=" + shopId + "&productId=" + productId;
        mockMvc.perform(put(url)
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> Assert.assertEquals(productId, result.getAsyncResult().toString()));
        ;
    }

    @Test
    public void removeProduct() throws Exception {
        String shopId = initializeShop();
        String productId = UUID.randomUUID().toString();

        ProductDTO productDTO = new ProductDTO("Test_Product_1", 10.0, 5.0);

        mockMvc.perform(put("/api/shop/addProduct?shopId=" + shopId)
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(productDTO)));


        String url = "/api/shop/removeProduct?shopId=" + shopId + "&productId=" + productId;
        mockMvc.perform(put(url))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //    @Test
    public void buyProduct() {
    }

    @Test
    public void getCurrentStockAmount() throws Exception {
        String shopId = initializeShop();
        String productId = UUID.randomUUID().toString();

        fixture.given(new ProductAddedEvent(
                shopId,
                new Product(
                        productId,
                        10,
                        5
                )
        ));

        String url = "/api/shop/stockAmount?shopId=" + shopId + "&productId=" + productId;
        mockMvc.perform(get(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> Assert.assertEquals(0, Integer.valueOf(result.getResponse().getContentAsString()).intValue()));
    }

    @Test
    public void getCurrentBalance() throws Exception {
        String shopId = initializeShop();

        String url = "/api/shop/currentBalance?shopId=" + shopId;
        mockMvc.perform(get(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    Assert.assertEquals(0d, Double.valueOf(result.getResponse().getContentAsString()), 0.0);
                });
    }

    private String initializeShop() throws Exception {
        return initializeShop("Test_Shop");
    }

    private String initializeShop(String name) throws Exception {
        String url = "/api/shop/create?name=" + name;
        return String.valueOf(mockMvc.perform(post(url)).andReturn().getAsyncResult());
    }
}
