# Release Policy

Stable releases are automated after a verified `development` → `main` promotion merge.

1. Required pull-request checks validate the exact source head.
2. The trusted `main` push workflow verifies that the associated merged pull request has `development` as head and `main` as base.
3. The workflow reruns the full gate, builds one versioned Spring Boot JAR, and records its SHA-256 checksum.
4. Per-platform `linux/amd64` and `linux/arm64` images are built from that exact JAR, smoke-tested, exported, and then published without rebuilding.
5. The workflow creates the next patch SemVer tag, a GitHub Release, an OCI index in GHCR, SBOMs, checksums, signatures, and provenance attestations.

The first automated release is `v0.1.0`; subsequent promotions increment the patch component. Major/minor changes require a release-policy pull request before promotion. Tags are immutable. A failed release is never repaired by moving a tag; fix through the normal branch flow and publish the next version.

`latest` is a convenience alias. Deploy immutable `ghcr.io/jonhhh09/cart-decision-tree@sha256:<digest>` references.
