#!/usr/bin/env python3
"""Tests for protected-branch pull-request provenance policy."""

import unittest

from scripts.check_branch_policy import validate_pull_request


class BranchPolicyTests(unittest.TestCase):
    repository = "owner/cart-decision-tree"

    @staticmethod
    def event(
        base: str,
        head: str,
        head_repository: str,
        *,
        sender: str = "contributor",
        author: str = "contributor",
    ) -> dict:
        return {
            "pull_request": {
                "base": {"ref": base},
                "head": {"ref": head, "repo": {"full_name": head_repository}},
                "user": {"login": author},
            },
            "sender": {"login": sender},
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

    def test_accepts_same_repository_dependabot_pr_after_branch_synchronization(self) -> None:
        event = self.event(
            "development",
            "dependabot/gradle/development/gradle-wrapper-9.6.1",
            self.repository,
            sender="github-actions[bot]",
            author="dependabot[bot]",
        )
        self.assertIsNone(validate_pull_request(event, self.repository))

    def test_rejects_forked_dependabot_pr(self) -> None:
        event = self.event(
            "development",
            "dependabot/gradle/development/gradle-wrapper-9.6.1",
            "attacker/cart-decision-tree",
            sender="github-actions[bot]",
            author="dependabot[bot]",
        )
        self.assertIsNotNone(validate_pull_request(event, self.repository))


if __name__ == "__main__":
    unittest.main()
