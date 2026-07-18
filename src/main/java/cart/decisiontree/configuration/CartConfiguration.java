package cart.decisiontree.configuration;

import cart.decisiontree.application.port.out.FruitDatasetPort;
import cart.decisiontree.application.port.out.TreeModelStore;
import cart.decisiontree.application.service.CartApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CartConfiguration {

    @Bean
    CartApplicationService cartApplicationService(FruitDatasetPort datasets, TreeModelStore models) {
        return new CartApplicationService(datasets, models);
    }
}
