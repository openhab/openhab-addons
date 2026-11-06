"""Compare bundled legacy YAML and pinned Sunsynk metadata without changing runtime resources.

Requires PyYAML in the development environment; no binding dependency is added.
Address matches are review candidates, never proof of equivalent device semantics.
"""

import csv
import hashlib
import json
import re
from collections import Counter
from decimal import Decimal
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
PAIRS = [
    ("single_phase", ["deye_hybrid"], True),
    ("single_phase_16kw", ["deye_hybrid"], True),
    ("three_phase_lv", ["deye_sg04lp3"], True),
    ("three_phase_hv", ["deye_sg01hp3"], True),
    ("micro_hybrid_ess", ["deye_2mppt", "deye_4mppt"], False),
]


def escape(value):
    return re.sub(r"[ .()/\\&_]+", lambda match: "-" * len(match[0]), value.strip().replace("+", "plus").lower())


def legacy_items(name):
    data = yaml.safe_load((ROOT / "src/main/resources/definitions" / (name + ".yaml")).read_text(encoding="utf-8"))
    items = []
    for group in data["parameters"]:
        for item in group["items"]:
            items.append(dict(item, definition=name, channel=escape(group["group"]) + "-" + escape(item["name"])))
    return items


def number(value):
    return Decimal(str(value))


def numeric_signature(item, advanced):
    if advanced:
        if item["kind"] not in ("Sensor", "NumberRWSensor", "TempSensor") or item.get("mask", 0):
            return None
        factor = number(item["factor"])
        signed = factor < 0 and item["kind"] != "TempSensor"
        intercept = -number(item.get("offset", 100)) if item["kind"] == "TempSensor" else Decimal(0)
        return (signed, abs(factor), intercept, (item.get("unit") or "").casefold())
    if item.get("rule", 1) not in (1, 2, 3, 4) or item.get("lookup") or item.get("isstr"):
        return None
    scale = number(item.get("scale", 1))
    return (item.get("rule", 1) in (2, 4), scale, -number(item.get("offset", 0)) * scale,
            (item.get("uom") or "").casefold())


def run():
    rows, summaries, inputs = [], [], {}
    for profile, legacy_names, comparable in PAIRS:
        resource = ROOT / "src/main/resources/sunsynk" / (profile + ".json")
        sensors = json.loads(resource.read_text(encoding="utf-8"))
        old = [item for name in legacy_names for item in legacy_items(name)]
        for source in [resource] + [ROOT / "src/main/resources/definitions" / (name + ".yaml") for name in legacy_names]:
            inputs[source.relative_to(ROOT).as_posix()] = hashlib.sha256(source.read_bytes()).hexdigest()
        old_addresses = {reg for item in old for reg in item.get("registers", [])}
        counts = Counter()
        unique = set()
        unmatched_write = 0
        for sensor in sensors:
            registers = tuple(sensor["registers"])
            candidates = [item for item in old if tuple(item.get("registers", [])) == registers]
            partial = [item for item in old if set(registers).intersection(item.get("registers", []))]
            computed = sensor["kind"] in ("PVDynamicTotalSensor", "MathSensor", "Constant")
            signature = numeric_signature(sensor, True)
            numeric = [item for item in candidates if signature is not None and signature == numeric_signature(item, False)]
            if not comparable:
                category = "different_device_family_context_only"
            elif computed:
                category = "computed_or_constant"
            elif numeric:
                category = "same_tuple_numeric_candidate"
            elif candidates:
                category = "same_tuple_semantic_review"
            elif partial:
                category = "partial_tuple_review"
            else:
                category = "no_shared_yaml_address"
            counts[category] += 1
            writable = sensor.get("writable", False)
            if comparable and writable and not any(item.get("isReadOnly") is False for item in candidates):
                unmatched_write += 1
            # Preserve all control/conversion semantics when deduplicating names/IDs.
            unique.add(json.dumps({k: v for k, v in sensor.items() if k not in ("id", "name")}, sort_keys=True))
            notes = []
            if sensor.get("mask"):
                notes.append("masked control/decoding")
            if sensor["kind"] == "Sensor16":
                notes.append("history-dependent 16/32-bit interpretation")
            if writable and any(isinstance(sensor.get(key), dict) for key in ("min", "max")):
                notes.append("dependent write limits")
            if writable and not any(item.get("isReadOnly") is False for item in candidates):
                notes.append("no same-tuple explicitly writable YAML entry")
            rows.append({"profile": profile, "advanced_id": sensor["id"], "kind": sensor["kind"],
                         "registers": list(registers), "unit": sensor.get("unit", ""),
                         "factor": sensor.get("factor"), "mask": sensor.get("mask", 0),
                         "writable": writable, "min": sensor.get("min"), "max": sensor.get("max"),
                         "category": category, "legacy_candidates": [item["definition"] + ":" + item["channel"] for item in candidates],
                         "legacy_numeric_candidates": [item["definition"] + ":" + item["channel"] for item in numeric],
                         "legacy_models": [{"channel": item["channel"], "rule": item.get("rule", 1),
                                            "scale": item.get("scale", 1), "offset": item.get("offset", 0),
                                            "unit": item.get("uom", ""), "writable": item.get("isReadOnly") is False,
                                            "lookup": item.get("lookup", [])} for item in candidates],
                         "notes": notes})
        direct_sensors = [sensor for sensor in sensors if sensor["kind"] not in ("PVDynamicTotalSensor", "MathSensor", "Constant")]
        advanced_tuples = {tuple(sensor["registers"]) for sensor in direct_sensors}
        summaries.append({"profile": profile, "legacy_definitions": legacy_names, "comparable_family": comparable,
                          "legacy_entries": len(old), "legacy_writable": sum(item.get("isReadOnly") is False for item in old),
                          "advanced_entries": len(sensors), "advanced_writable": sum(sensor.get("writable", False) for sensor in sensors),
                          "distinct_definition_bodies_excluding_id_name": len(unique), "categories": dict(counts),
                          "writable_entries_without_same_tuple_writable_legacy": unmatched_write if comparable else None,
                          "legacy_entries_without_exact_advanced_direct_tuple": [item["channel"] for item in old if tuple(item.get("registers", [])) not in advanced_tuples] if comparable else None})
        assert sum(counts.values()) == len(sensors)
    output = {"baseline": "openhab-addons main 821bb618754b6a0973ae4a11a28ac93133be278c definitions; pinned Python f1ecbc28e3ccf2b9f87b1ccee85a41628567da43",
              "method": "Ordered address tuples, numeric sign/factor/intercept/unit candidates, masks, kinds, write access and declared limits. No name-based equivalence or hardware qualification is inferred. Numeric candidates do not establish identical rounding, Item types, limits, lookup semantics or whole-channel compatibility.",
              "input_sha256": inputs, "summary": summaries, "rows": rows}
    out = ROOT / "docs/profile-comparison.json"
    out.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    with (ROOT / "docs/profile-comparison.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=[key for key in rows[0] if key != "legacy_models"])
        writer.writeheader()
        for row in rows:
            writer.writerow({key: json.dumps(value, ensure_ascii=False) if isinstance(value, (list, dict)) else value
                             for key, value in row.items() if key != "legacy_models"})
    assert len(rows) == 925
    print(json.dumps(summaries, indent=2))


if __name__ == "__main__":
    run()
