# Governance

The repository maintainer is the final decision maker for scope, architecture, security, releases, and the code of conduct. Decisions favor correctness, reproducibility, bounded resource use, privacy, and maintainability.

All product changes enter through an issue-linked feature branch based on current `origin/development`, then a reviewed pull request to `development`. Stable promotion is a reviewed `development` → `main` pull request. Repository rules protect both long-lived branches from direct pushes, force pushes, and deletion.

Material changes to public APIs, artifact identity, dataset contracts, security boundaries, or release policy require an issue describing compatibility and migration impact. Maintainers may reject changes whose operational or supply-chain cost exceeds their demonstrated value.
