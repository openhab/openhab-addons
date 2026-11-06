"""Review actual single-phase register definitions and propose private Item changes.

No runtime resources or existing Item files are modified. PyYAML is development-only.
"""

import argparse
import csv
import hashlib
import json
import re
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

from markdown_format import format_markdown
from profile_resources import load_profile

import yaml

from compare_profile_definitions import numeric_signature
from generate_profile_item_examples import escape_name, legacy_type

ROOT = Path(__file__).resolve().parents[1]
LEGACY_PATH = ROOT / "src/main/resources/definitions/deye_hybrid.yaml"
ADVANCED_PATH = ROOT / "src/main/resources/sunsynk/single_phase.json"


def addresses(registers):
    return ", ".join(f"{word} (0x{word:04X})" for word in registers) or "None"


def write_csv(path, rows):
    with path.open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)


def review(source=None, output=None):
    if output is not None:
        output.mkdir(parents=True, exist_ok=True)
    legacy = {}
    for group in yaml.safe_load(LEGACY_PATH.read_text(encoding="utf-8"))["parameters"]:
        for item in group["items"]:
            legacy[escape_name(group["group"]) + "-" + escape_name(item["name"])] = item
    advanced = {item["id"].replace("_", "-"): item for item in load_profile(ADVANCED_PATH)}
    xml = ET.parse(ROOT / "src/main/resources/OH-INF/thing/sunsynk.xml").getroot()
    channel_types = {node.get("id"): node.findtext("item-type") for node in xml.findall("channel-type")}
    thing = next(node for node in xml.findall("thing-type") if node.get("id") == "sunsynk-single-phase")
    types = {node.get("id"): channel_types[node.get("typeId")] for node in thing.findall("./channels/channel")}
    decisions = {}
    for channel, old in legacy.items():
        direct = {key: item for key, item in advanced.items()
                  if item["kind"] not in ("MathSensor", "Constant", "PVDynamicTotalSensor")}
        exact = {key: item for key, item in direct.items() if item["registers"] == old["registers"]}
        partial = {key: item for key, item in direct.items()
                   if set(item["registers"]) & set(old["registers"])} if not exact else {}
        computed = {key: item for key, item in advanced.items() if item["kind"] == "MathSensor"
                    and set(item["registers"]) & set(old["registers"])}
        signature = numeric_signature(old, False)
        matching = [key for key, item in exact.items() if signature is not None
                    and signature == numeric_signature(item, True) and legacy_type(old) == types[key]]
        proposed = ""
        decision = "No direct channel"
        note = "No direct target channel exposes the complete source register tuple. Do not substitute a total or a different profile's register."
        if len(matching) == 1:
            proposed = matching[0]
            decision = "Propose: same numeric read model"
            note = "Ordered registers, signedness, scale, offset, unit and Item type agree. Hardware/firmware qualification and rounding details are separate from metadata agreement."
        elif len(exact) == 1 and old.get("rule") == 9:
            proposed = next(iter(exact))
            decision = "Propose: same time control, new bounds"
            note = "Same HHMM word and HH:mm String. Advanced writes additionally enforce the neighboring program-time bounds; retain source-disabled status."
        elif channel in ("inverter-ac-temperature", "inverter-dc-temperature"):
            proposed = "radiator-temperature" if channel == "inverter-ac-temperature" else "dc-transformer-temperature"
            decision = "Propose with difference: temperature sign"
            note = "Same temperature function, word and formula raw*0.1-100 C. Legacy raw word is signed; advanced raw word unsigned. Results agree for raw 0..32767, including normal operating temperatures; this is not full-domain bit-for-bit equivalence."
        elif channel in ("solar-pv1-power", "solar-pv2-power", "upload-total-load-power", "upload-load-l1-power", "upload-load-l2-power"):
            proposed = next(iter(exact))
            decision = "Propose with difference: power sign"
            note = "Same power function/word and 1 W scale. Legacy unsigned, advanced signed: equal for raw 0..32767; raw 65535 changes from 65535 W to -1 W. The negative Python factor selects signed decoding; it does not reverse the sign. Review rules using direction."
        elif channel == "inverter-current-l1":
            proposed = "inverter-current"
            decision = "Propose with difference: current sign"
            note = "Same word 164 and 0.01 A scale. Legacy signed, advanced unsigned: raw 65535 means -0.01 A versus 655.35 A. This link does not preserve negative-current behavior. If rules depend on negative current, retain the legacy interpretation until the device behavior is resolved."
        elif channel in ("solar-micro-inverter-power", "inverter-gen-power"):
            proposed = "aux-power" if channel.startswith("solar-") else "gen-power"
            decision = "Propose with review: port alias and sign"
            note = "Both target aliases expose port-power word 166. Legacy unsigned, advanced signed. Verify AUX/GEN port configuration; the same port word does not establish that a generator or microinverter is physically attached."
        elif channel in ("inverter-communication-board-version-no-", "inverter-control-board-version-no-", "inverter-inverter-id") and len(exact) == 1:
            proposed = next(iter(exact))
            decision = "Propose with review: binding String metadata"
            note = "Same ordered registers and String Item type. Versions use unsigned decimal text; identity uses high/low byte text with edge padding trimmed. Confirm the legacy display format and external references. These are separately attributed binding additions, not Kellerza definitions."
        elif channel == "inverter-running-status":
            proposed = "overall-state"
            decision = "Propose with difference: state vocabulary"
            note = "Same word 59 and String type, but lookup vocabulary/codes change. Legacy 3=FAULT; advanced 3=alarm, 4=fault, 5=activating. Update String comparisons in rules."
        elif channel in ("inverter-grid-connected-status", "time-of-use-time-of-use"):
            proposed = "grid-connected" if channel.startswith("inverter-") else "use-timer"
            decision = "Proposal requires Item type change"
            note = "Same source word, but legacy String becomes Switch ON/OFF. Preserve the Item name only after reviewing rules, sitemap widgets and persistence. Advanced use-timer is writable; the legacy control was read-only."
        elif channel == "grid-total-grid-production":
            decision = "Hold: energy meaning and sign require validation"
            note = "Words 63/64 and 0.1 kWh exist as total-active-energy, but legacy signed versus advanced unsigned and the old Grid Feed-in label is not established by Kellerza's Total active energy definition. Do not relink automatically or substitute total-grid-export at words 81/82."
        elif channel == "battery-battery-status":
            decision = "Reject: same address, different function"
            note = "Legacy word 189 is a battery-state String lookup; advanced word 189 is PV4 power in W. Sharing the address does not establish a replacement."
        elif channel == "inverter-gen-connected-status":
            decision = "Reject: same address, different function"
            note = "Legacy word 166 is a String status lookup. Advanced gen-power/aux-power is numeric port power, not that status."
        elif channel == "alert-alert":
            decision = "Hold: partial fault coverage and String format change"
            note = "Legacy raw alert covers words 101..106; advanced fault decodes 103..106 into fault labels. Words 101/102 and raw String representation are not preserved. A separate fault Item is an alternative, not a drop-in replacement."
        elif channel == "inverter-work-mode":
            decision = "Hold: one control split into two"
            note = "Legacy combines words 244/247 as one String lookup. Advanced load-limit (244, String) and solar-export (247, mask 1, Switch) expose separate writable controls. No single Item preserves the old representation."
        elif channel.startswith("time-of-use-time-of-use-enable-"):
            decision = "Hold: one raw word split into masked controls"
            note = "Legacy declares mask 1, but the inspected numeric updater does not apply that mask. Advanced charge/mode/sell use masks 3/28/64 and String/String/Switch types. They do not replace the raw Number Item individually."
        if proposed:
            assert advanced[proposed]["registers"] == old["registers"], (channel, proposed)
            if advanced[proposed]["writable"]:
                note += " Advanced limits: " + json.dumps({key: advanced[proposed][key] for key in ("min", "max") if key in advanced[proposed]}) + "."
        decisions[channel] = dict(legacy_channel=channel, legacy_registers=addresses(old["registers"]),
                                  legacy_type=legacy_type(old), legacy_definition=json.dumps(old, ensure_ascii=False),
                                  exact_candidates="; ".join(exact), partial_candidates="; ".join(partial),
                                  computed_inputs_not_replacements="; ".join(computed),
                                  proposed_channel=proposed, proposed_registers=addresses(advanced[proposed]["registers"]) if proposed else "None",
                                  target_definition=json.dumps(advanced[proposed], ensure_ascii=False) if proposed else "",
                                  candidate_definitions=json.dumps({**exact, **partial}, ensure_ascii=False),
                                  decision=decision, notes=note)
    assert len(decisions) == 81
    public = ROOT / "docs/examples/profile-channel-lists"
    write_csv(public / "single-phase-register-review.csv", list(decisions.values()))
    report = ["# Reviewed Single-phase Migration Proposals", "",
              "Current deye_hybrid YAML is compared with the single-phase profile plus its separately attributed binding metadata. All 81 legacy entries are accounted for. The README's generic 0x021D temperature example is not the deye_hybrid address; this pair uses 91 (0x005B).", "",
              "A proposal is separate from automatic linking and hardware qualification. Same-function controls with changed signedness are listed with explicit differences; they are not classified as missing. Partial register tuples, different functions and uncertain energy meanings are held rather than silently linked.", "",
              "The CSV contains complete source/target definitions, exact and partial candidates, and computed inputs that are not replacements. Multi-word values retain declared low-word-first order, including the sparse 78/80 pair. No channel name alone establishes a source register.", "",
              "| Legacy channel | Source registers | Proposed channel | Target registers | Decision | Notes |",
              "| --- | --- | --- | --- | --- | --- |"]
    for row in decisions.values():
        report.append(f"| `{row['legacy_channel']}` | {row['legacy_registers']} | {row['proposed_channel'] or 'None'} | {row['proposed_registers']} | {row['decision']} | {row['notes']} |")
    (public / "single-phase-register-review.md").write_text(format_markdown("\n".join(report)), encoding="utf-8")
    if source is None:
        print("Reviewed all 81 current-main legacy definitions without reading personal Items.")
        return

    rows = []
    proposed_lines = ["// PROPOSAL ONLY for inverterType=sunsynk_single_phase; not deployed or hardware-qualified.",
                      "// Review item-by-item-review.md before use. Existing Item names, Thing UID and source-disabled status are retained.",
                      "// Replace matching declarations; do not load alongside duplicate Item names.",
                      "// Entries held for review retain their Item declarations without a channel link.", ""]
    block = False
    unmatched = 0
    for line_number, line in enumerate(source.read_text(encoding="utf-8-sig").splitlines(), 1):
        text = line.strip()
        if text == "/*":
            block = True
            continue
        if text == "*/":
            unmatched += not block
            block = False
            continue
        match = re.match(r'\s*(//)?\s*(Number(?::\w+)?|String|Switch|DateTime)\s+(\w+)\s+(.*)', line)
        if not match:
            assert not text or text.startswith("//"), line_number
            continue
        comment, item_type, name, body = match.groups()
        disabled = bool(comment) or block
        link = re.search(r'channel="([^"]+)"', body)
        assert link
        prefix, channel = link[1].rsplit(":", 1)
        evidence = decisions.get(channel)
        target = evidence["proposed_channel"] if evidence else ""
        note = evidence["notes"] if evidence else "Source channel is absent from deye_hybrid. No source register can be verified from this declaration, so no link is proposed by name alone."
        decision = evidence["decision"] if evidence else "Unknown source definition"
        timestamp = 'profile="system:timestamp-update"' in body
        if timestamp:
            assert item_type == "DateTime" and channel == "grid-internal-ct-l1-power"
            note += " Preserve system:timestamp-update: the link profile produces DateTime from updates of a Power channel. A Last Update timestamp does not certify an entire polling snapshot."
        if evidence and evidence["legacy_type"] != item_type and not timestamp:
            note += " Supplied Item type " + item_type + " differs from legacy channel metadata " + evidence["legacy_type"] + "; no automatic type conversion is applied."
        if target and types[target] != item_type and not timestamp:
            target = ""
            note += " Item type change is required and is not applied to this declaration."
        proposed_lines.append(f"// Source line {line_number}: {decision}. " + note)
        if target:
            body = body[:link.start()] + link[0].replace(link[1], prefix + ":" + target) + body[link.end():]
        else:
            body = body[:link.start()] + body[link.end():]
            body = re.sub(r'\{\s*,\s*', "{", body)
            body = re.sub(r'\{\s*\}', "", body).rstrip()
        proposed_lines.append(("// " if disabled else "") + item_type + " " + name + " " + body.strip())
        rows.append(dict(source_line=line_number, item=name, source_enabled=not disabled, item_type=item_type,
                         legacy_channel=channel, legacy_registers=evidence["legacy_registers"] if evidence else "Unknown",
                         proposed_channel=target, proposed_registers=addresses(advanced[target]["registers"]) if target else "None",
                         decision=decision, notes=note, timestamp_profile_preserved=timestamp,
                         source_definition=evidence["legacy_definition"] if evidence else "Absent from source profile",
                         target_definition=json.dumps(advanced[target], ensure_ascii=False) if target else ""))
    assert not block and len({row["item"] for row in rows}) == len(rows)
    write_csv(output / "item-by-item-review.csv", rows)
    (output / "sunsynk_single_phase.proposed.items").write_text("\n".join(proposed_lines) + "\n", encoding="utf-8")
    counts = dict(total=len(rows), source_enabled=sum(row["source_enabled"] for row in rows),
                  proposed_links=sum(bool(row["proposed_channel"]) for row in rows),
                  held_or_source_unknown=sum(not row["proposed_channel"] for row in rows),
                  enabled_without_proposed_link=sum(row["source_enabled"] and not row["proposed_channel"] for row in rows),
                  undefined_source_channels=sum(row["decision"] == "Unknown source definition" for row in rows),
                  timestamp_profiles=sum(row["timestamp_profile_preserved"] for row in rows))
    private_report = ["# Item-by-item Migration Proposal", "",
                      "Source: the latest supplied attachment. Profiles are strictly deye_hybrid -> sunsynk_single_phase. Existing generated Item files are not overwritten; the new .proposed.items file is a reviewable proposal only.", "",
                      "Counts: " + json.dumps(counts) + ".", "",
                      "Names, labels, icons, compatible units, Thing UID and explicit source-disabled state are preserved. No commands were sent and nothing was installed. Default autoupdate is unchanged. Source-disabled items stay commented. Held active Items retain names but have no device link, so persisted old states are not fresh observations.", "",
                      "Important: a proposed link with a signedness/vocabulary/bound difference is an explicit functional proposal, not a statement of bit-for-bit equivalence. Review the row before enabling rules. Unknown source channels are held even when a similarly named target function exists.", "",
                      f"The fragment contains {unmatched} unmatched closing block-comment marker(s). These are omitted from the standalone proposal; preserve any outer file comment context when merging. The timestamp link profile is retained rather than incorrectly requiring a DateTime source channel.", "",
                      "The earlier strict automatic converter withheld some usable same-function links. This proposal restores temperature, power, current and time correspondences with explicit differences; it does not change automatic-link eligibility or overwrite prior files.", "",
                      "| Source line / Item | Enabled | Legacy channel / registers | Proposed channel / registers | Decision | Difference or limitation |",
                      "| --- | --- | --- | --- | --- | --- |"]
    for row in rows:
        private_report.append(f"| {row['source_line']} / `{row['item']}` | {row['source_enabled']} | `{row['legacy_channel']}` / {row['legacy_registers']} | {row['proposed_channel'] or 'None'} / {row['proposed_registers']} | {row['decision']} | {row['notes']} |")
    held_summary = ["## Source-enabled Items Without a Proposed Link", "",
                    "These Items remain declared but receive no device observations in the proposal. They are not silently replaced by a total, partial fault String or a register from another device profile.", "",
                    "| Item | Source registers | Reason |", "| --- | --- | --- |"]
    for row in rows:
        if row["source_enabled"] and not row["proposed_channel"]:
            held_summary.append(f"| `{row['item']}` | {row['legacy_registers']} | {row['decision']} |")
    private_report = private_report[:2] + held_summary + ["", "## Complete Item-by-item Evidence", ""] + private_report[2:]
    (output / "item-by-item-review.md").write_text("\n".join(private_report) + "\n", encoding="utf-8")
    verification = dict(counts, generic_legacy_entries=len(decisions), advanced_entries=len(advanced),
                        decision_counts=dict(Counter(row["decision"] for row in decisions.values())),
                        inputs_sha256={"legacy": hashlib.sha256(LEGACY_PATH.read_bytes()).hexdigest(),
                                       "advanced": hashlib.sha256(ADVANCED_PATH.read_bytes()).hexdigest(),
                                       "attachment": hashlib.sha256(source.read_bytes()).hexdigest()})
    (output / "review-verification.json").write_text(json.dumps(verification, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(verification, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--definitions-only", action="store_true")
    parser.add_argument("--items", type=Path)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    if args.definitions_only:
        review()
    else:
        if args.items is None or args.output is None:
            parser.error("Supply --definitions-only or both --items and --output.")
        review(args.items, args.output)
