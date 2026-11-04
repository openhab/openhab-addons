"""Load pinned profiles with separately attributed binding metadata channels."""

import json


def load_profile(path):
    original = json.loads(path.read_text(encoding="utf-8"))
    supplement = path.parent / "metadata" / path.name
    additions = json.loads(supplement.read_text(encoding="utf-8")) if supplement.exists() else []
    assert all(s["source"] == "binding:deye_hybrid" and not s["writable"] for s in additions)
    combined = original + additions
    assert len({s["id"] for s in combined}) == len(combined), "Duplicate profile channel ID"
    return combined
