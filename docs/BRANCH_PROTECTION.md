# Branch Protection Operations

The canonical GitHub ruleset payloads are versioned in `.github/rulesets/`. Apply them only after the workflows have run successfully and the repository plan supports rulesets.

```sh
gh api --method POST repos/JonHHH09/cart-decision-tree/rulesets \
  --input .github/rulesets/development.json
gh api --method POST repos/JonHHH09/cart-decision-tree/rulesets \
  --input .github/rulesets/main.json
gh api repos/JonHHH09/cart-decision-tree/rulesets
```

Read back each returned ruleset by ID and compare its target, enforcement, conditions, rules, required contexts, and empty bypass list with the tracked JSON. Never create duplicate rulesets; update the existing ID when the tracked policy changes.

Rulesets block deletion, force pushes, and non-PR updates. `scripts/check_branch_policy.py` adds the source-direction contract that rulesets cannot express: issue-linked feature branches target `development`, and only `development` targets `main`.

`development` permits merge commits so a released `main` merge can be synchronized back through an issue-linked pull request without losing ancestry. Ordinary feature pull requests should use rebase or squash; lineage synchronization must use a merge commit before the next strict, up-to-date `development` to `main` promotion.

The initial sole-maintainer policy requires zero approving reviews because GitHub does not allow pull-request authors to approve their own changes. Required checks, pull-request-only updates, and conversation resolution remain enforced without bypass actors. Raise `required_approving_review_count` and enable code-owner review when an independent maintainer is available.
