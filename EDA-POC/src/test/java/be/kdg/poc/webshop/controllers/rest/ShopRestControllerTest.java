package be.kdg.poc.webshop.controllers.rest;

import be.kdg.poc.commandmodel.WebshopAggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    private FixtureConfiguration<WebshopAggregate> fixture = new AggregateTestFixture<>(WebshopAggregate.class);

//    @Test
    public void getCurrentStockAmount() {
    }

//    @Test
    public void getCurrentBalance() {
    }

    @Test
    public void createShop() throws Exception {
        String url = "/api/shop/create?name=Eerste_shop";
        mockMvc.perform(post(url))
                .andDo(result -> System.out.println(result.getAsyncResult()));
    }

    @Test
    public void deleteShop() throws Exception {
        String shopId = initializeShop();

        String deleteUrl = "/api/shop/delete/" + shopId;
        mockMvc.perform(delete(deleteUrl));

//        String id = UUID.randomUUID().toString();
//
//        fixture.givenCommands(new CreateShopCommand(id, "first_shop"))
//                .when(new DeleteShopCommand(id))
//                .expectEvents(new ShopDeletedEvent(id));
    }

//    @Test
    public void buyProduct() {
    }

//    @Test
    public void addProduct() {
    }

//    @Test
    public void removeProduct() {
    }

    private String initializeShop() throws Exception {
        String url = "/api/shop/create?name=Eerste_shop";
        return String.valueOf(mockMvc.perform(post(url)).andReturn().getAsyncResult());
    }
}
