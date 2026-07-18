# Security Policy

## Supported versions

Only the latest GitHub Release is supported with security updates. The `development` branch is pre-release code.

## Private reporting

Report vulnerabilities through **GitHub Security Advisories → Report a vulnerability** for this repository. Do not include exploits, credentials, personal data, or private infrastructure details in public issues, discussions, or pull requests.

Include the affected release/tag, reproduction prerequisites, impact, and the smallest safe proof of concept. Maintainers will acknowledge a complete report within seven days, coordinate remediation and disclosure, and credit reporters who request attribution.

## Scope

Security-sensitive boundaries include MCP and HTTP input validation, PostgreSQL access, generated tree/model identity, release artifacts, container execution, GitHub Actions, and third-party assets. Local development credentials in `compose.yaml` are synthetic defaults and must not be reused in deployed environments.

## Release integrity

Verify release checksums and GHCR digests before deployment. Treat mutable tags such as `latest` as discovery aliases; pin production deployments to an immutable digest.
