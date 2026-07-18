#!/usr/bin/env python3
"""Validate pull-request direction and issue-linked branch naming."""

from __future__ import annotations

import json
import os
import re
import sys
from pathlib import Path


ISSUE_BRANCH = re.compile(r"^(?:[a-z0-9][a-z0-9._-]*/)?(?:open|job)-\d+-[a-z0-9][a-z0-9-]*$", re.I)


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

    base = pull_request["base"]["ref"]
    head = pull_request["head"]["ref"]
    actor = event.get("sender", {}).get("login", "")

    if base == "main":
        if head != "development":
            print("Only development may open a pull request to main.", file=sys.stderr)
            return 1
    elif base == "development":
        if head in {"main", "development"}:
            print("Feature changes require a dedicated branch.", file=sys.stderr)
            return 1
        if not ISSUE_BRANCH.fullmatch(head) and actor != "dependabot[bot]":
            print("Feature branch must include an OPEN/JOB issue identifier.", file=sys.stderr)
            return 1
    else:
        print("Pull requests must target development or main.", file=sys.stderr)
        return 1

    print(f"accepted pull-request direction: {head} -> {base}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
