package cart.decisiontree.adapter.out.jdbc;

import cart.decisiontree.application.port.out.FruitDatasetPort;
import cart.decisiontree.domain.model.FruitExample;

import static cart.decisiontree.application.port.out.FruitDatasetPort.MAX_EXAMPLES;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("SqlNoDataSourceInspection")
public class JdbcFruitDatasetAdapter implements FruitDatasetPort {

    private static final String SELECT_COLUMNS = "SELECT color, shape, weight_grams, skin, fruit FROM ";

    private final JdbcClient jdbcClient;

    public JdbcFruitDatasetAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<FruitExample> trainingExamples() {
        return load("training_data.fruit_examples");
    }

    @Override
    public List<FruitExample> testExamples() {
        return load("test_data.fruit_examples");
    }

    private List<FruitExample> load(String table) {
        return jdbcClient.sql(SELECT_COLUMNS + table + " ORDER BY id LIMIT " + (MAX_EXAMPLES + 1))
                .query((resultSet, ignored) -> new FruitExample(
                        resultSet.getString("color"),
                        resultSet.getString("shape"),
                        resultSet.getInt("weight_grams"),
                        resultSet.getString("skin"),
                        resultSet.getString("fruit")))
                .list();
    }
}
