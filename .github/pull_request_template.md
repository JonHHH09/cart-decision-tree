## Summary

<!-- State the user-visible change and why it belongs in this issue. -->

## Issue

- Linear/GitHub issue:

## MLOps and security impact

- [ ] Input/resource bounds remain explicit and tested.
- [ ] Dataset/model identity remains deterministic or its migration is documented.
- [ ] Logs, errors, fixtures, and artifacts contain no secrets or personal data.
- [ ] Public API, artifact, schema, and release compatibility were assessed.
- [ ] No new runtime network dependency or mutable release input was introduced.

## Verification

- [ ] `./gradlew --no-daemon clean check bootJar`
- [ ] `python3 scripts/publication_audit.py --history`
- [ ] Container/workflow checks, when applicable

## Release and rollback

<!-- State release impact, migration steps, and rollback. Write "None" when not applicable. -->
