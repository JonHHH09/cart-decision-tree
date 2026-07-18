#!/usr/bin/env python3
"""Tests for publication-audit metadata classification."""

import unittest

from scripts.publication_audit import CONTENT_RULES, is_allowed_commit_email


class CommitEmailClassificationTests(unittest.TestCase):
    def test_accepts_github_user_noreply_identity(self) -> None:
        self.assertTrue(is_allowed_commit_email("12345+maintainer@users.noreply.github.com"))

    def test_accepts_github_platform_merge_identity(self) -> None:
        self.assertTrue(is_allowed_commit_email("noreply@github.com"))

    def test_rejects_personal_contact_identity(self) -> None:
        self.assertFalse(is_allowed_commit_email("maintainer@example.com"))

    def test_preserves_empty_metadata_behavior(self) -> None:
        self.assertTrue(is_allowed_commit_email(""))

    def test_content_scan_accepts_github_platform_identity(self) -> None:
        contact_pattern = next(pattern for pattern, category in CONTENT_RULES if category == "personal-contact")
        self.assertIsNone(contact_pattern.search(b"noreply@github.com"))

    def test_content_scan_rejects_personal_contact(self) -> None:
        contact_pattern = next(pattern for pattern, category in CONTENT_RULES if category == "personal-contact")
        candidate = b"maintainer" + b"@" + b"private.invalid"
        self.assertIsNotNone(contact_pattern.search(candidate))


if __name__ == "__main__":
    unittest.main()
