#!/usr/bin/env python3
"""Pick the builds a workflow run needs, so unchanged versions are never built.

  plan_matrix.py build   [--before SHA] [--sha SHA]   builds whose folder changed in the push
  plan_matrix.py release --tag vYYYY.M.N              builds whose own version equals the tag

Writes `matrix=<json>` and `any=true|false` to $GITHUB_OUTPUT (and prints them).
"""
import argparse
import json
import os
import re
import subprocess

ALL = [
    ("forge-26.3", "versions/mc26.3/forge", 25),
    ("neoforge-26.3", "versions/mc26.3/neoforge", 25),
    ("forge-26.2", "versions/mc26.2/forge", 25),
    ("neoforge-26.2", "versions/mc26.2/neoforge", 25),
    ("forge-26.1.2", "versions/mc26.1.2/forge", 25),
    ("neoforge-26.1.2", "versions/mc26.1.2/neoforge", 25),
    ("forge-1.21.1", "versions/mc1.21.1/forge", 21),
    ("neoforge-1.21.1", "versions/mc1.21.1/neoforge", 21),
    ("forge-1.20.1", "versions/mc1.20.1/forge", 17),
    ("neoforge-1.20.1", "versions/mc1.20.1/neoforge", 17),
]
ZERO = "0" * 40


def changed_files(before, sha):
    if not before or before == ZERO:
        # first push of a branch or a manual run: compare with the parent commit
        before = sha + "^"
    out = subprocess.run(["git", "diff", "--name-only", before, sha], capture_output=True, text=True)
    if out.returncode != 0:
        return None  # cannot tell: build everything
    return [line.strip() for line in out.stdout.splitlines() if line.strip()]


def module_version(folder):
    try:
        text = open(os.path.join(folder, "build.gradle"), encoding="utf-8").read()
    except OSError:
        return None
    m = re.search(r"^version = '([^']+)'", text, re.M)
    return m.group(1) if m else None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("mode", choices=["build", "release"])
    ap.add_argument("--before")
    ap.add_argument("--sha", default="HEAD")
    ap.add_argument("--tag")
    args = ap.parse_args()

    if args.mode == "release":
        version = (args.tag or "").lstrip("v")
        if not re.match(r"^\d+\.\d+\.\d+$", version):
            # a manual run on a branch: release the newest version found
            found = sorted({module_version(d) for _, d, _ in ALL if module_version(d)}, key=lambda v: [int(x) for x in v.split(".")])
            version = found[-1] if found else ""
        picked = [m for m in ALL if module_version(m[1]) == version]
    else:
        files = changed_files(args.before, args.sha)
        if files is None:
            picked = list(ALL)
        else:
            picked = [m for m in ALL if any(f.startswith(m[1] + "/") for f in files)]

    matrix = {"include": [{"module": m, "dir": d, "java": j} for m, d, j in picked]}
    lines = [f"matrix={json.dumps(matrix)}", f"any={'true' if picked else 'false'}"]
    print("\n".join(lines))
    print("builds:", ", ".join(m for m, _, _ in picked) or "none")
    out = os.environ.get("GITHUB_OUTPUT")
    if out:
        with open(out, "a", encoding="utf-8") as fh:
            fh.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()
