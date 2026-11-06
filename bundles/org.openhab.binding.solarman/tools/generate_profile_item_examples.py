"""Generate complete generic Item examples and channel lists for two logger profiles.

Requires development-only PyYAML. Does not change runtime resources or personal configuration.
"""

import csv
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

from markdown_format import format_markdown

import yaml

from compare_profile_definitions import legacy_items, numeric_signature
from profile_resources import load_profile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/examples/profile-channel-lists"
DIMENSIONS = {"%": "Dimensionless", "W": "Power", "KW": "Power", "V": "ElectricPotential",
              "A": "ElectricCurrent", "AH": "ElectricCharge", "WH": "Energy", "KWH": "Energy",
              "HZ": "Frequency", "\u00b0C": "Temperature", "S": "Time", "H": "Time",
              "HOURS": "Time", "DAYS": "Time"}


def escape_name(value):
    return re.sub(r"[ .()/\\&_]+", lambda match: "-" * len(match[0]), value.strip().replace("+", "plus").lower())


def legacy_type(item):
    rule = item.get("rule", 1)
    if item.get("lookup") or item.get("isstr") is True or rule in (5, 6, 7, 9):
        return "String"
    if rule == 8:
        return "DateTime"
    unit = (item.get("uom") or "").upper()
    return "Number" + (":" + DIMENSIONS[unit] if unit in DIMENSIONS else "")


def generate_legacy_readme_tables():
    baseline = json.loads((ROOT / "src/test/resources/legacy/main-baseline.json").read_text(encoding="utf-8"))["profiles"]
    readme = ROOT / "README.md"
    text = readme.read_text(encoding="utf-8")
    for profile in ("deye_sg04lp3", "deye_2mppt"):
        include_access = profile == "deye_sg04lp3"
        header = "| Channel | Item type | Access | Description |" if include_access else "| Channel | Item type | Description |"
        separator = "| --- | --- | --- | --- |" if include_access else "| --- | --- | --- |"
        rows = [header, separator]
        definitions = {item["channel"]: item for item in legacy_items(profile)}
        for channel in baseline[profile]["channels"]:
            writable = channel["configuration"].get("readOnly") is False
            if not include_access:
                assert not writable
            original = definitions[channel["id"]]
            description = channel["label"] + " `" + channel["configuration"]["registers"] + "`"
            options = list(dict.fromkeys(entry["value"] for entry in original.get("lookup", []) or []))
            if options:
                description += "; options: " + ", ".join("`" + option + "`" for option in options)
            description = description.replace("|", "\\|")
            cells = ["`" + channel["id"] + "`", channel["itemType"]]
            if include_access:
                cells.append("R/W" if writable else "R")
            rows.append("| " + " | ".join(cells + [description]) + " |")
        marker = profile.upper()
        pattern = r"<!-- BEGIN GENERATED " + marker + r" CHANNELS -->.*?<!-- END GENERATED " + marker + r" CHANNELS -->"
        replacement = "<!-- BEGIN GENERATED " + marker + " CHANNELS -->\n\n" + format_markdown("\n".join(rows)) + "\n<!-- END GENERATED " + marker + " CHANNELS -->"
        text, count = re.subn(pattern, lambda match: replacement, text, flags=re.S)
        assert count == 1, profile
    readme.write_text(text, encoding="utf-8")


def generate():
    OUT.mkdir(parents=True, exist_ok=True)
    data = yaml.safe_load((ROOT / "src/main/resources/definitions/deye_hybrid.yaml").read_text(encoding="utf-8"))
    legacy = []
    for group in data["parameters"]:
        for item in group["items"]:
            legacy.append({"channel": escape_name(group["group"]) + "-" + escape_name(item["name"]),
                           "label": item["name"], "group": group["group"], "item_type": legacy_type(item),
                           "unit": item.get("uom") or "", "writable": item.get("isReadOnly") is False,
                           "registers": item.get("registers", []), "options": [entry["value"] for entry in item.get("lookup", [])]})
    metadata = ET.parse(ROOT / "src/main/resources/OH-INF/thing/sunsynk.xml").getroot()
    types = {node.attrib["id"]: node.findtext("item-type") for node in metadata.findall("channel-type")}
    thing = next(node for node in metadata.findall("thing-type") if node.attrib["id"] == "sunsynk-single-phase")
    channels = {node.attrib["id"]: node.attrib["typeId"] for node in thing.findall("./channels/channel")}
    advanced = []
    for sensor in load_profile(ROOT / "src/main/resources/sunsynk/single_phase.json"):
        channel = re.sub(r"[^a-z0-9-]", "-", sensor["id"].lower().replace("_", "-"))
        advanced.append({"channel": channel, "label": sensor["name"], "group": channel.split("-", 1)[0],
                         "item_type": types[channels[channel]], "unit": sensor.get("unit") or "",
                         "writable": sensor["writable"], "registers": sensor["registers"],
                         "options": list(sensor.get("options", {}).values())})
    all_rows = []
    for profile, prefix, entries, count, writable in [("deye_hybrid", "Legacy", legacy, 81, 18),
                                                      ("sunsynk_single_phase", "Advanced", advanced, 150, 73)]:
        assert len(entries) == count and sum(entry["writable"] for entry in entries) == writable
        names = set()
        lines = ["// Complete example for inverterType=" + profile + ".",
                 "// Replace solarman:logger:example with the actual Thing UID locally.",
                 "// Use only the Item file matching the selected inverterType.",
                 "// Creating Items does not issue write commands.", ""]
        table = ["# " + profile + " Channels", "", f"{count} entries: {count - writable} read-only and {writable} read/write, including aliases.",
                 "", "All names and the Thing UID are generic examples. See [usage instructions](README.md).",
                 "", "| Channel ID | Item name | Item type | Unit | Access | Registers (decimal) |",
                 "| --- | --- | --- | --- | --- | --- |"]
        for entry in entries:
            name = prefix + "".join(part[:1].upper() + part[1:] for part in re.findall(r"[a-zA-Z0-9]+", entry["channel"]))
            assert name not in names, name
            names.add(name)
            uid = "solarman:logger:example:" + entry["channel"]
            attrs = ['channel=' + json.dumps(uid)]
            if entry["item_type"].startswith("Number:") and entry["unit"]:
                attrs.append('unit=' + json.dumps(entry["unit"], ensure_ascii=False))
            if entry["writable"]:
                attrs.append('autoupdate="false"')
            label = json.dumps(entry["label"] + " [%s]", ensure_ascii=False)
            lines.append(entry["item_type"] + " " + name + " " + label + " { " + ", ".join(attrs) + " }")
            access = "Read/write" if entry["writable"] else "Read"
            table.append(f'| `{entry["channel"]}` | `{name}` | `{entry["item_type"]}` | {entry["unit"]} | {access} | {entry["registers"]} |')
            all_rows.append(dict(profile=profile, item=name, item_type=entry["item_type"], channel=entry["channel"],
                                 channel_uid=uid, unit=entry["unit"], access=access,
                                 registers=json.dumps(entry["registers"]), options=json.dumps(entry["options"], ensure_ascii=False)))
        (OUT / (profile + ".items")).write_text("\n".join(lines) + "\n", encoding="utf-8")
        (OUT / (profile + ".md")).write_text(format_markdown("\n".join(table)), encoding="utf-8")
        config = ('// Alternative profile configuration: install only one .things example for this logger.\n'
                  'Thing solarman:logger:example [\n'
                  f'    inverterType="{profile}",\n'
                  '    hostname="192.168.1.100",\n'
                  '    serialNumber="1234567890",\n'
                  '    refreshInterval=60,\n'
                  '    solarmanLoggerMode="V5MODBUS"\n'
                  ']\n')
        (OUT / (profile + ".things")).write_text(config, encoding="utf-8")
    with (OUT / "channels.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(all_rows[0]))
        writer.writeheader()
        writer.writerows(all_rows)
    legacy_definitions = {item["channel"]: item for item in legacy_items("deye_hybrid")}
    advanced_definitions = {item["id"].replace("_", "-"): item for item in load_profile(
        ROOT / "src/main/resources/sunsynk/single_phase.json")}
    computed_kinds = {"MathSensor", "Constant", "PVDynamicTotalSensor"}
    mapping = []
    for old in legacy:
        direct = [new for new in advanced if advanced_definitions[new["channel"]]["kind"] not in computed_kinds]
        exact = [new for new in direct if new["registers"] == old["registers"]]
        partial = [] if exact else [new for new in direct if set(old["registers"]).intersection(new["registers"])]
        candidates = exact or partial
        signature = numeric_signature(legacy_definitions[old["channel"]], False)
        matched_numeric = [new for new in exact if signature is not None
                           and signature == numeric_signature(advanced_definitions[new["channel"]], True)]
        if matched_numeric:
            status = "Matching numeric model candidate"
            notes = "Same ordered registers, signedness, factor/intercept and unit; review access, limits and hardware."
            candidates = matched_numeric
        elif exact and old["item_type"] == "String" and old["channel"].startswith("time-of-use-time-of-use-time-"):
            status = "Time candidate with new limits"
            notes = "HH:mm String; the advanced profile enforces neighboring-period limits."
        elif exact:
            status = "Same registers; semantic review required"
            notes = "Register equality is not equivalence: review signedness, type, unit, lookup, masks and command options."
        elif partial:
            status = "Partial overlap; not a direct replacement"
            notes = "Candidate uses a different register tuple or computation; do not replace the original link automatically."
        else:
            status = "No register-based candidate"
            notes = "No direct mapping was established in the current advanced profile; retain for review rather than invent a link."
        old_definition = legacy_definitions[old["channel"]]
        automatic = len(candidates) == 1 and len(matched_numeric) == 1 and old["item_type"] == candidates[0]["item_type"]
        registers_text = lambda registers: ", ".join(f"{address} (0x{address:04X})" for address in registers)
        legacy_model = json.dumps({"rule": old_definition.get("rule", 1),
                                   "bits": 16 * len(old["registers"]),
                                   "scale": old_definition.get("scale", 1),
                                   "offset_before_scale": old_definition.get("offset", 0),
                                   "signed": old_definition.get("rule", 1) in (2, 4),
                                   "unit": old["unit"], "lookup": old_definition.get("lookup", [])}, ensure_ascii=False)
        advanced_models = []
        for new in candidates:
            definition = advanced_definitions[new["channel"]]
            fields = {key: definition.get(key) for key in ("kind", "factor", "offset", "unit", "mask", "min", "max", "options", "on", "off") if key in definition}
            fields["bits"] = 16 * len(new["registers"])
            fields["word_order"] = "low word first" if len(new["registers"]) > 1 else "single word"
            advanced_models.append(new["channel"] + "=" + json.dumps(fields, ensure_ascii=False))
        if automatic and old["writable"]:
            notes += " Advanced write bounds still require review; read-model agreement does not prove identical commands."
        mapping.append({"legacy_channel": old["channel"], "legacy_type": old["item_type"],
                        "legacy_access": "Read/write" if old["writable"] else "Read",
                        "legacy_registers": json.dumps(old["registers"]),
                        "legacy_registers_decimal_hex": registers_text(old["registers"]),
                        "legacy_model": legacy_model, "status": status,
                        "auto_link_allowed": automatic,
                        "advanced_registers": "; ".join(new["channel"] + "=" + json.dumps(new["registers"]) for new in candidates),
                        "advanced_registers_decimal_hex": "; ".join(new["channel"] + "=" + registers_text(new["registers"]) for new in candidates),
                        "advanced_models": "; ".join(advanced_models),
                        "advanced_candidates": "; ".join(new["channel"] for new in candidates),
                        "advanced_types": "; ".join(new["channel"] + "=" + new["item_type"] for new in candidates),
                        "advanced_access": "; ".join(new["channel"] + "=" + ("Read/write" if new["writable"] else "Read") for new in candidates),
                        "notes": notes})
    with (OUT / "migration-map.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(mapping[0]))
        writer.writeheader()
        writer.writerows(mapping)
    table = ["# deye_hybrid to sunsynk_single_phase Migration Candidates", "",
             "Source profile: deye_hybrid. Target profile: sunsynk_single_phase. One row per legacy channel, matched by ordered register tuple before checking interpretation. Names do not establish equivalence.",
             "Computed sensors are excluded from direct matches; shared input registers do not make a computed value a replacement. See migration-map.csv for complete numeric/control models, masks, limits and auto_link_allowed decisions.",
             "Automatic links require a unique exact register tuple, matching numeric sign/scale/offset/unit and matching Item type. Partial tuples, lookup/type changes, signedness changes and source-undefined channels require review. Even matching read models do not establish identical write bounds or hardware behavior.",
             "Registers are profile-specific: deye_hybrid inverter L1 power uses 173 (0x00AD), while the three-phase legacy profiles use 633 (0x0279); advanced three-phase inverter-l1-power uses 633 and 691. These are not interchangeable addresses on a single-phase device.",
             "Keep existing Item names where the Item type remains compatible. See [usage instructions](README.md).", "",
             "| Legacy channel | Legacy registers (decimal / hex) | Legacy Item type / access | Advanced candidate(s) | Advanced registers (decimal / hex) | Advanced Item type / access | Review / automatic link |",
             "| --- | --- | --- | --- | --- | --- | --- |"]
    for row in mapping:
        table.append("| `" + row["legacy_channel"] + "` | " + row["legacy_registers_decimal_hex"] + " | " + row["legacy_type"] + " / " + row["legacy_access"]
                     + " | " + (row["advanced_candidates"] or "Not established") + " | " + (row["advanced_registers_decimal_hex"] or "None in target profile") + " | "
                     + (row["advanced_types"] + "; " + row["advanced_access"] if row["advanced_candidates"] else "Not established")
                     + " | " + row["status"] + "; auto=" + str(row["auto_link_allowed"]) + " |")
    (OUT / "migration-map.md").write_text(format_markdown("\n".join(table)), encoding="utf-8")
    # A profile-qualified index supports address-first review without equating different device families.
    register_rows = []
    for profile in ("deye_hybrid", "deye_sg04lp3", "deye_sg01hp3"):
        for item in legacy_items(profile):
            for index, address in enumerate(item.get("registers", [])):
                register_rows.append(dict(source="legacy", profile=profile, register_decimal=address,
                                          register_hex=f"0x{address:04X}", channel=item["channel"],
                                          complete_register_tuple=json.dumps(item.get("registers", [])),
                                          register_role="direct value", position=index, kind="rule " + str(item.get("rule", 1)),
                                          unit=item.get("uom") or ""))
    for resource in sorted((ROOT / "src/main/resources/sunsynk").glob("*.json")):
        for item in load_profile(resource):
            if item["kind"] in ("PVDynamicTotalSensor", "Constant"):
                continue
            for index, address in enumerate(item["registers"]):
                register_rows.append(dict(source="advanced", profile="sunsynk_" + resource.stem,
                                          register_decimal=address, register_hex=f"0x{address:04X}",
                                          channel=item["id"].replace("_", "-"),
                                          complete_register_tuple=json.dumps(item["registers"]),
                                          register_role="computed input" if item["kind"] == "MathSensor" else "direct value",
                                          position=index, kind=item["kind"], unit=item.get("unit") or ""))
    register_rows.sort(key=lambda entry: (entry["register_decimal"], entry["source"], entry["profile"], entry["channel"]))
    with (OUT / "register-index.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(register_rows[0]))
        writer.writeheader()
        writer.writerows(register_rows)
    generate_legacy_readme_tables()
    print("Generated 81 legacy Items, 150 advanced Items, complete tables, a 231-row channel CSV and an 81-row migration map.")


if __name__ == "__main__":
    generate()
