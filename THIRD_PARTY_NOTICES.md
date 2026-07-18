# Third-Party Notices

This project is MIT-licensed, but its application JAR and container redistribute third-party components under their own licenses. Those licenses are not replaced by the project license.

## Vendored browser asset

- **HTMX 2.0.9** — Zero-Clause BSD (`0BSD`). The exact upstream source, checksums, and update procedure are recorded in `src/main/resources/static/vendor/HTMX-ASSET.md`; the license text is retained in `src/main/resources/static/vendor/HTMX-LICENSE.txt`.

## Runtime component families

The runtime dependency graph includes components from the following license families:

- Spring Boot, Spring Framework, Spring AI, Model Context Protocol SDK, Flyway, Thymeleaf, Jackson, Micrometer, Reactor, Tomcat, HikariCP, and supporting libraries — predominantly Apache License 2.0.
- PostgreSQL JDBC — BSD-2-Clause.
- SLF4J and supporting libraries — MIT-family licenses.
- Logback — EPL-2.0 or LGPL-2.1 dual-license terms.
- Jakarta Annotation API — GPL-2.0 with Classpath Exception 2.0.
- Additional supporting libraries — BSD, EPL, MIT-0, CC0, and public-domain terms as declared by their upstream artifacts.

The complete version-specific component and license inventory is the SPDX JSON SBOM attached to every GitHub Release. Original dependency JARs retain their `META-INF/LICENSE*` and `META-INF/NOTICE*` files inside the Spring Boot archive.

## Build and test dependencies

Gradle, JUnit, AssertJ, Mockito, Testcontainers, JaCoCo, and related tooling are used to build or verify the project and are not copied into the production runtime image unless identified by the release SBOM.

When adding or updating dependencies, regenerate `gradle.lockfile`, inspect declared licenses, and verify the release SBOM before distribution.
