package be.kdg.poc;

import be.kdg.poc.product.dom.Product;
import be.kdg.poc.webshop.dom.Webshop;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Cédric Goffin
 * 07/02/2019 21:51
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EDAApplicationPerformanceTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private final String URL = "http://eventshop-poc.herokuapp.com/api/shop";

    private Webshop webshop;
    private List<Product> products;

    @Before
    public void initializeShop() throws Exception {
        // Initialize shop, if it already hasn't been
        /*
        TODO: MockMvc with HttpUriRequest:
                https://www.baeldung.com/integration-testing-a-rest-api*/

        mockMvc.perform(post(URL + "/initializeShop"))
                .andDo(print())
                .andExpect(status().isOk());

        // Get webshop(s)
        mockMvc.perform(get(URL + "/getWebshops"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(result -> {
                    List<Webshop> webshops = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<List<Webshop>>() {
                            });
                    Assert.assertTrue(!webshops.isEmpty());
                    webshop = webshops.stream().max(Comparator.comparing(webshop -> webshop.getInventory().size())).get();
                });

        // Get product(s) of first webshop
        mockMvc.perform(get(URL + "/getProducts?shopId=" + webshop.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(result -> {
                    products = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<List<Product>>() {
                            });
                    Assert.assertTrue(!products.isEmpty());
                });
    }

    @Test
    public void stressTestAPIPerformance() {
        // Make ~50 threads and buy random product each 10ms
//        System.out.println(webshop);
//        System.out.println(products);
    }
}
