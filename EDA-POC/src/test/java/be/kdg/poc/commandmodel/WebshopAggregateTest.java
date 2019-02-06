package be.kdg.poc.commandmodel;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.webshop.command.AddProductCommand;
import be.kdg.poc.webshop.command.CreateShopCommand;
import be.kdg.poc.webshop.command.DeleteShopCommand;
import be.kdg.poc.webshop.command.RemoveProductCommand;
import be.kdg.poc.webshop.event.ProductAddedEvent;
import be.kdg.poc.webshop.event.ProductRemovedEvent;
import be.kdg.poc.webshop.event.ShopCreatedEvent;
import be.kdg.poc.webshop.event.ShopDeletedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.UUID;

/**
 * @author Cédric Goffin
 * 06/02/2019 16:08
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WebshopAggregateTest {
    private FixtureConfiguration<WebshopAggregate> fixture;

    private String testShopId;
    private String testShopName;
    private String testProductId;
    private String testProductName;

    @Before
    public void init() {
        // Initialize test fixture
        fixture = new AggregateTestFixture<>(WebshopAggregate.class);

        // Initialize test shop data
        testShopId = UUID.randomUUID().toString();
        testShopName = "Test shop";

        // Initialize test product data
        testProductId = UUID.randomUUID().toString();
        testProductName = "Test product";
    }

    @Test
    public void createShop() {
        fixture.given()
                .when(new CreateShopCommand(testShopId, testShopName))
                .expectResultMessageMatching(Matchers.messageWithPayload(Matchers.equalTo(testShopId)))
                .expectEvents(new ShopCreatedEvent(testShopId, testShopName, 0));
    }

    @Test
    public void deleteShop() {
        fixture.given(new ShopCreatedEvent(testShopId, testShopName, 0))
                .when(new DeleteShopCommand(testShopId))
                .expectResultMessageMatching(Matchers.messageWithPayload(Matchers.nothing()))
                .expectEvents(new ShopDeletedEvent(testShopId));
    }

    @Test
    public void addProduct() {
        Product product = new Product(
                testProductId,
                testProductName,
                10,
                0,
                5
        );

        fixture.given(new ShopCreatedEvent(testShopId, testShopName, 0))
                .when(new AddProductCommand(testShopId, product))
                .expectResultMessageMatching(Matchers.messageWithPayload(Matchers.equalTo(testProductId)))
                .expectEvents(new ProductAddedEvent(testShopId, product));
    }

    @Test
    public void removeProduct() {
        fixture.given(
                new ShopCreatedEvent(testShopId, testShopName, 0),
                new ProductAddedEvent(testShopId, new Product(
                        testProductId,
                        testProductName,
                        10,
                        0,
                        5
                )))
                .when(new RemoveProductCommand(testShopId, testProductId))
                .expectResultMessageMatching(Matchers.messageWithPayload(Matchers.nothing()))
                .expectEvents(new ProductRemovedEvent(testShopId, testProductId));
    }

    @Test
    public void buyProduct() {
        // TODO
    }
}
