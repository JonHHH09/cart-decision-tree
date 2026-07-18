package cart.decisiontree.adapter.out.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import cart.decisiontree.PostgreSqlTestImage;
import cart.decisiontree.application.port.out.FruitDatasetPort;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
class JdbcFruitDatasetAdapterTests {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(PostgreSqlTestImage.NAME)
            .withDatabaseName("cart_decision_tree")
            .withUsername("cart")
            .withUrlParam("sslmode", "disable")
            .withPassword("cart");

    static JdbcFruitDatasetAdapter adapter;
    static JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void migrateAndCreateAdapter() {
        Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()).load().migrate();
        var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        jdbcTemplate = new JdbcTemplate(dataSource);
        adapter = new JdbcFruitDatasetAdapter(JdbcClient.create(jdbcTemplate));
    }

    @Test
    void loadsBothDatasetsInStableIdentifierOrder() {
        var training = adapter.trainingExamples();
        var test = adapter.testExamples();

        assertThat(training).hasSize(24);
        assertThat(test).hasSize(11);
        assertThat(training.getFirst().fruit()).isEqualTo("apple");
        assertThat(training.getLast().fruit()).isEqualTo("grape");
        assertThat(test.getFirst().weightGrams()).isEqualTo(190);
    }

    @Test
    void capsMaterializedRowsAtOneBeyondTheApplicationLimit() {
        try {
            jdbcTemplate.update("""
                    INSERT INTO training_data.fruit_examples (color, shape, weight_grams, skin, fruit)
                    SELECT 'red', 'round', 100, 'smooth', 'apple'
                    FROM generate_series(25, 600)
                    """);

            assertThat(adapter.trainingExamples()).hasSize(FruitDatasetPort.MAX_EXAMPLES + 1);
        } finally {
            jdbcTemplate.update("DELETE FROM training_data.fruit_examples WHERE id > 24");
        }
    }
}
