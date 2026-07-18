#!/usr/bin/env python3
"""Verify immutable repository inputs and workflow action references."""

from __future__ import annotations

import hashlib
import json
import re
import sys
from pathlib import Path


EXPECTED = {
    Path("gradle/wrapper/gradle-wrapper.jar"): "497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7",
    Path("src/main/resources/static/vendor/htmx.min.js"): "57d9191515339922bd1356d7b2d80b1ee3b29f1b3a2c65a078bb8b2e8fd9ae5f",
    Path("src/main/resources/static/vendor/HTMX-LICENSE.txt"): "d3d2456f76414f2456104660ebd65aff1c04cd7966b942bdabd63f3cdb316a38",
}

USES = re.compile(r"^\s*-?\s*uses:\s*[^#\s]+@([^\s#]+)", re.MULTILINE)
FULL_SHA = re.compile(r"[0-9a-f]{40}")
IMAGE_DIGEST = re.compile(r"@sha256:[0-9a-f]{64}$")


def main() -> int:
    failures: list[str] = []
    for path, expected in EXPECTED.items():
        actual = hashlib.sha256(path.read_bytes()).hexdigest()
        if actual != expected:
            failures.append(f"checksum mismatch: {path}")

    for workflow in sorted(Path(".github/workflows").glob("*.y*ml")):
        text = workflow.read_text(encoding="utf-8")
        for reference in USES.findall(text):
            if not FULL_SHA.fullmatch(reference):
                failures.append(f"mutable action reference: {workflow}")

    dockerfile = Path("Dockerfile").read_text(encoding="utf-8")
    for line in dockerfile.splitlines():
        if line.startswith("FROM ") and not IMAGE_DIGEST.search(line.split()[1]):
            failures.append("Dockerfile base image is not digest-pinned")

    for compose in (Path("compose.yaml"), Path("compose.release.yaml")):
        for line in compose.read_text(encoding="utf-8").splitlines():
            stripped = line.strip()
            if stripped.startswith("image:"):
                image = stripped.removeprefix("image:").strip()
                if not image.startswith("${") and not IMAGE_DIGEST.search(image):
                    failures.append(f"mutable container image: {compose}")

    release_compose = Path("compose.release.yaml").read_text(encoding="utf-8")
    if "image: ${CART_IMAGE:?" not in release_compose:
        failures.append("release Compose image must be explicitly supplied")

    if not Path("gradle.lockfile").is_file():
        failures.append("missing Gradle dependency lock")

    for ruleset in sorted(Path(".github/rulesets").glob("*.json")):
        policy = json.loads(ruleset.read_text(encoding="utf-8"))
        rule_types = {rule["type"] for rule in policy["rules"]}
        required = {"deletion", "non_fast_forward", "pull_request", "required_status_checks"}
        if policy.get("enforcement") != "active" or not required.issubset(rule_types):
            failures.append(f"incomplete branch ruleset: {ruleset}")
        status_rule = next(rule for rule in policy["rules"] if rule["type"] == "required_status_checks")
        for check in status_rule["parameters"]["required_status_checks"]:
            if check.get("integration_id") != 15368:
                failures.append(f"required check is not bound to GitHub Actions: {ruleset}")

    if failures:
        print("\n".join(failures), file=sys.stderr)
        return 1
    print(f"verified {len(EXPECTED)} checksums and immutable workflow action references")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
