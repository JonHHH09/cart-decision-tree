# Contributing

Contributions are accepted through issue-linked pull requests. By contributing, you agree that your contribution is licensed under this repository's MIT License.

## Workflow

1. Search existing issues, then create or claim a Linear/GitHub issue before implementation.
2. Fetch the repository and synchronize the development base:
   ```sh
   git fetch origin development
   git switch development
   git pull --ff-only origin development
   ```
3. Create a focused branch from `origin/development`. Use `<user>/<issue>-<slug>`; for example, `contributor/open-123-fix-split-validation`.
4. Add tests first for behavior changes. Keep fixtures synthetic and free of credentials or personal data.
5. Run the local gate:
   ```sh
   ./gradlew --no-daemon clean check bootJar
   python3 scripts/publication_audit.py --tree HEAD
   ```
6. Use isolated conventional commits (`feat:`, `fix:`, `test:`, `docs:`, `build:`, `ci:`, or `chore:`) and reference the issue in the commit body.
7. Open a pull request to `development`. Only the `development` branch may open a promotion pull request to `main`.

Direct pushes, force pushes, and deletion of `development` or `main` are prohibited. Pull requests require passing checks and resolved conversations. The sole-maintainer configuration does not require self-approval; raise the approval count when an independent maintainer is available.

## Engineering standards

- Preserve the ports-and-adapters boundary described in `README.md`.
- Keep input materialization, tree depth/node count, and serialization bounded.
- Keep dataset/model fingerprints deterministic; document any identity-format change.
- Sanitize protocol errors and logs; never retain raw secrets or personal data.
- Do not add network-fetched frontend assets at runtime.
- Add dependencies only when the maintenance and supply-chain cost is justified.

## Reporting security defects

Do not open a public issue. Follow `SECURITY.md`.
