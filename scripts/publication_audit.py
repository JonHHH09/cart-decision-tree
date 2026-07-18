#!/usr/bin/env python3
"""Scan a Git tree or all reachable Git blobs without printing matched content."""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from collections import defaultdict


PATH_RULES = (
    (re.compile(r"(^|/)(AGENTS\.md|CLAUDE\.md)$", re.I), "agent-config"),
    (re.compile(r"(^|/)\.env($|\.)", re.I), "credential-file"),
    (re.compile(r"(^|/)(id_rsa|id_ed25519|[^/]+\.(pem|key|p12|pfx))$", re.I), "private-key-path"),
    (re.compile(r"^(models?|artifacts?)/|\.(joblib|pickle|pkl|onnx|pt|pth)$", re.I), "model-artifact"),
    (re.compile(r"\.(sqlite|sqlite3|db)$", re.I), "database-artifact"),
    (re.compile(r"\.(pyc|class)$", re.I), "generated-bytecode"),
    (re.compile(r"\.(zip|tar|tgz|7z|rar)$", re.I), "archive-artifact"),
    (re.compile(r"(^|/)\.idea/", re.I), "ide-state"),
)

CONTENT_RULES = (
    (re.compile(rb"-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----"), "private-key"),
    (re.compile(rb"gh[pousr]_[A-Za-z0-9]{20,}"), "github-token"),
    (re.compile(rb"/Users/[A-Za-z0-9._-]+/"), "private-absolute-path"),
    (re.compile(rb"(?i)[A-Z0-9._%+-]+@(?!users\.noreply\.github\.com|example\.(?:com|org|net)|localhost)[A-Z0-9.-]+\.[A-Z]{2,}"), "personal-contact"),
)

BLOCKING = {
    "agent-config",
    "credential-file",
    "private-key-path",
    "generated-bytecode",
    "private-key",
    "github-token",
    "private-absolute-path",
    "personal-contact",
}


def is_allowed_commit_email(email: str) -> bool:
    """Accept privacy-preserving GitHub identities used by people and platform merges."""
    normalized = email.casefold()
    return not normalized or normalized.endswith("@users.noreply.github.com") or normalized == "noreply@github.com"


def git(*arguments: str, text: bool = False):
    result = subprocess.run(
        ["git", *arguments], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=text
    )
    return result.stdout


def objects_for_tree(tree: str) -> dict[str, set[str]]:
    objects: dict[str, set[str]] = defaultdict(set)
    output = str(git("ls-tree", "-r", tree, text=True))
    for line in output.splitlines():
        metadata, path = line.split("\t", 1)
        object_id = metadata.split()[2]
        objects[object_id].add(path)
    return objects


def objects_for_history() -> dict[str, set[str]]:
    objects: dict[str, set[str]] = defaultdict(set)
    output = str(git("rev-list", "--objects", "--all", text=True))
    for line in output.splitlines():
        parts = line.split(" ", 1)
        if len(parts) == 2:
            objects[parts[0]].add(parts[1])
    return objects


def main() -> int:
    parser = argparse.ArgumentParser()
    scope = parser.add_mutually_exclusive_group(required=True)
    scope.add_argument("--tree", metavar="REV")
    scope.add_argument("--history", action="store_true")
    arguments = parser.parse_args()

    objects = objects_for_history() if arguments.history else objects_for_tree(arguments.tree)
    findings: set[tuple[str, str, str]] = set()
    scanned = 0
    metadata_blockers = 0

    for object_id, paths in objects.items():
        if str(git("cat-file", "-t", object_id, text=True)).strip() != "blob":
            continue
        scanned += 1
        size = int(str(git("cat-file", "-s", object_id, text=True)).strip())
        for path in paths:
            for pattern, category in PATH_RULES:
                if pattern.search(path):
                    findings.add((object_id, path, category))
        if size > 20_000_000:
            for path in paths:
                findings.add((object_id, path, "oversized-blob"))
            continue
        content = bytes(git("cat-file", "blob", object_id))
        for pattern, category in CONTENT_RULES:
            if pattern.search(content):
                for path in paths:
                    findings.add((object_id, path, category))

    if arguments.history:
        log = str(git("log", "--all", "--format=%H%x00%aE%x00%cE", text=True))
        affected_commits: set[str] = set()
        for line in log.splitlines():
            commit, author_email, committer_email = line.split("\x00")
            if any(not is_allowed_commit_email(email) for email in (author_email, committer_email)):
                affected_commits.add(commit)
        metadata_blockers = len(affected_commits)

    print(f"scope={'history' if arguments.history else arguments.tree} blobs={scanned} findings={len(findings)}")
    for object_id, path, category in sorted(findings, key=lambda item: (item[2], item[1], item[0])):
        print(f"{category}\t{object_id}\t{path}")

    if metadata_blockers:
        print(f"personal-contact-metadata\tcommits={metadata_blockers}")

    blockers = [finding for finding in findings if finding[2] in BLOCKING]
    if blockers or metadata_blockers:
        print(f"publication blockers={len(blockers) + metadata_blockers}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
