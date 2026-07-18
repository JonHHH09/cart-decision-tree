package cart.decisiontree;

import org.testcontainers.utility.DockerImageName;

public final class PostgreSqlTestImage {

    public static final DockerImageName NAME = DockerImageName
            .parse("postgres:18-alpine@sha256:9a8afca54e7861fd90fab5fdf4c42477a6b1cb7d293595148e674e0a3181de15")
            .asCompatibleSubstituteFor("postgres");

    private PostgreSqlTestImage() {
    }
}
