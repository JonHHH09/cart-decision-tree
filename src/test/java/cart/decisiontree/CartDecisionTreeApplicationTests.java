package cart.decisiontree;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Testcontainers
@SuppressWarnings("SpringBootApplicationProperties")
class CartDecisionTreeApplicationTests {

    @Container
    @ServiceConnection(name = "postgres")
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(PostgreSqlTestImage.NAME)
            .withDatabaseName("cart_decision_tree")
            .withUsername("cart")
            .withUrlParam("sslmode", "disable")
            .withPassword("cart");

    @Test
    void contextLoads() {
    }
}
