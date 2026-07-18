#!/usr/bin/env python3
"""Validate pull-request direction and issue-linked branch naming."""

from __future__ import annotations

import json
import os
import re
import sys
from pathlib import Path
from typing import Any, Optional


ISSUE_BRANCH = re.compile(r"^(?:[a-z0-9][a-z0-9._-]*/)?(?:open|job)-\d+-[a-z0-9][a-z0-9-]*$", re.I)


def validate_pull_request(event: dict[str, Any], repository: str) -> Optional[str]:
    pull_request = event["pull_request"]
    base = pull_request["base"]["ref"]
    head = pull_request["head"]["ref"]
    actor = event.get("sender", {}).get("login", "")

    if base == "main":
        head_repository = pull_request.get("head", {}).get("repo", {}).get("full_name", "")
        if head != "development" or head_repository != repository:
            return "Only this repository's development branch may open a pull request to main."
    elif base == "development":
        if head in {"main", "development"}:
            return "Feature changes require a dedicated branch."
        if not ISSUE_BRANCH.fullmatch(head) and actor != "dependabot[bot]":
            return "Feature branch must include an OPEN/JOB issue identifier."
    else:
        return "Pull requests must target development or main."
    return None


def main() -> int:
    event_path = os.environ.get("GITHUB_EVENT_PATH")
    if not event_path:
        print("GITHUB_EVENT_PATH is required", file=sys.stderr)
        return 2

    event = json.loads(Path(event_path).read_text(encoding="utf-8"))
    pull_request = event.get("pull_request")
    if not pull_request:
        print("No pull request in event; branch rules own direct-push enforcement.")
        return 0

    repository = event.get("repository", {}).get("full_name") or os.environ.get("GITHUB_REPOSITORY", "")
    if not repository:
        print("Repository identity is required", file=sys.stderr)
        return 2

    error = validate_pull_request(event, repository)
    if error:
        print(error, file=sys.stderr)
        return 1

    base = pull_request["base"]["ref"]
    head = pull_request["head"]["ref"]
    print(f"accepted pull-request direction: {head} -> {base}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
