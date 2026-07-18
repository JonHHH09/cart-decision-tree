package cart.decisiontree.adapter.in.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
@Testcontainers
@SuppressWarnings({"HttpHeaderInspection", "SpringBootApplicationProperties"})
class McpProtocolIntegrationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("cart_decision_tree")
            .withUsername("cart")
            .withUrlParam("sslmode", "disable")
            .withPassword("cart");

    @LocalServerPort
    int port;

    @Test
    void discoversAndInvokesAllToolsOverStreamableHttp() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var initialize = send(client, null, """
                    {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"integration-test","version":"1"}}}
                    """);
            assertThat(initialize.statusCode()).isEqualTo(200);
            var sessionId = initialize.headers().firstValue("Mcp-Session-Id").orElseThrow();

            var initialized = send(client, sessionId,
                    "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
            assertThat(initialized.statusCode()).isBetween(200, 299);

            var tools = send(client, sessionId,
                    "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}");
            assertThat(tools.statusCode()).isEqualTo(200);
            assertThat(tools.body()).contains("cart_dataset_summary", "cart_train_tree", "cart_predict_fruit",
                    "cart_render_tree", "cart_evaluate_tree", "outputSchema");

            var training = send(client, sessionId,
                    "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"cart_train_tree\",\"arguments\":{}}}");
            assertThat(training.statusCode()).isEqualTo(200);
            assertThat(training.body()).contains("structuredContent", "trainingSamples", "datasetFingerprint");

            var prediction = send(client, sessionId, """
                    {"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"cart_predict_fruit","arguments":{"color":"red","shape":"round","weightGrams":180,"skin":"smooth"}}}
                    """);
            assertThat(prediction.statusCode()).isEqualTo(200);
            assertThat(prediction.body()).contains("structuredContent", "predictedFruit");
        }
    }

    private HttpResponse<String> send(HttpClient client, String sessionId, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/mcp"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (sessionId != null) {
            builder.header("Mcp-Session-Id", sessionId);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
