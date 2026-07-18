package cart.decisiontree;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class FruitDatasetMigrationTests {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("cart_decision_tree")
            .withUsername("cart")
            .withUrlParam("sslmode", "disable")
            .withPassword("cart");

    @Test
    void migratesTrainingAndTestFruitDatasets() throws SQLException {
        var flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);

        try (var connection = postgres.createConnection("")) {
            assertThat(countRows(connection, "training_data.fruit_examples")).isEqualTo(24);
            assertThat(countRows(connection, "test_data.fruit_examples")).isEqualTo(11);
        }
    }

    private static long countRows(java.sql.Connection connection, String table) throws SQLException {
        try (var statement = connection.createStatement();
                var result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next();
            return result.getLong(1);
        }
    }
}
