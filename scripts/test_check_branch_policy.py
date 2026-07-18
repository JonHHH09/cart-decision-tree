#!/usr/bin/env python3
"""Tests for protected-branch pull-request provenance policy."""

import unittest

from scripts.check_branch_policy import validate_pull_request


class BranchPolicyTests(unittest.TestCase):
    repository = "owner/cart-decision-tree"

    @staticmethod
    def event(base: str, head: str, head_repository: str) -> dict:
        return {
            "pull_request": {
                "base": {"ref": base},
                "head": {"ref": head, "repo": {"full_name": head_repository}},
            },
            "sender": {"login": "contributor"},
        }

    def test_accepts_same_repository_development_promotion(self) -> None:
        event = self.event("main", "development", self.repository)
        self.assertIsNone(validate_pull_request(event, self.repository))

    def test_rejects_fork_named_development_promotion(self) -> None:
        event = self.event("main", "development", "attacker/cart-decision-tree")
        self.assertIsNotNone(validate_pull_request(event, self.repository))

    def test_accepts_issue_linked_feature_from_fork(self) -> None:
        event = self.event("development", "contributor/open-102-fix-policy", "contributor/cart-decision-tree")
        self.assertIsNone(validate_pull_request(event, self.repository))


if __name__ == "__main__":
    unittest.main()
