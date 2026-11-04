"""Generate additive openHAB XML, Java identifiers and a reviewable register catalog."""

import json
import re
from pathlib import Path

from markdown_format import format_markdown
from xml.sax.saxutils import escape, quoteattr

from profile_resources import load_profile

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/org/openhab/binding/solarman/internal/SolarmanBindingConstants.java"
PROFILES = {name: load_profile(path) for path in sorted((ROOT / "src/main/resources/sunsynk").glob("*.json")) if (name := path.stem)}
UNITS = {"%": "Dimensionless", "A": "ElectricCurrent", "Ah": "ElectricCharge", "Hz": "Frequency", "V": "ElectricPotential", "W": "Power", "kWh": "Energy", "kVarh": "Energy", "°C": "Temperature", "s": "Time", "h": "Time", "Hours": "Time", "days": "Time", "mΩ": "ElectricResistance"}


def identifier(text):
    return re.sub(r"[^a-z0-9-]", "-", text.lower().replace("_", "-"))


def item_type(sensor):
    kind = sensor["kind"]
    if kind in ("BinarySensor", "SwitchRWSensor"):
        return "Switch"
    if kind == "SystemTimeRWSensor":
        return "DateTime"
    if kind in ("SelectRWSensor", "TimeRWSensor", "EnumSensor", "FaultSensor", "HVFaultSensor", "InverterStateSensor", "SDStatusSensor", "RegisterStringSensor", "RegisterTextSensor"):
        return "String"
    return "Number" + ((":" + UNITS[sensor["unit"]]) if sensor["unit"] else "")


channel_ids = {s["id"]: identifier(s["id"]) for items in PROFILES.values() for s in items}
assert len(set(channel_ids.values())) == len(channel_ids), "Channel identifier collision"
type_ids = {profile + ":" + s["id"]: "sunsynk-" + identifier(profile) + "-" + channel_ids[s["id"]] for profile, items in PROFILES.items() for s in items}
java = ["    // BEGIN GENERATED SUNSYNK IDENTIFIERS", "    public static final Map<ThingTypeUID, String> SUNSYNK_PROFILES = Map.ofEntries("]
java.append(",\n".join('            Map.entry(new ThingTypeUID(SOLARMAN_BINDING_ID, "sunsynk-' + identifier(p) + '"), "' + p + '")' for p in PROFILES) + ");")
for field, values in [("SUNSYNK_CHANNEL_IDS", channel_ids), ("SUNSYNK_CHANNEL_TYPE_IDS", type_ids)]:
    java.append("    public static final Map<String, String> " + field + " = Map.ofEntries(")
    java.append(",\n".join("            Map.entry(" + json.dumps(k) + ", " + json.dumps(v) + ")" for k, v in sorted(values.items())) + ");")
java.append("    // END GENERATED SUNSYNK IDENTIFIERS")
text = JAVA.read_text(encoding="utf-8")
if "import java.util.Map;" not in text:
    text = text.replace("import org.eclipse", "import java.util.Map;\n\nimport org.eclipse", 1)
text = re.sub(r"    // BEGIN GENERATED SUNSYNK IDENTIFIERS.*?    // END GENERATED SUNSYNK IDENTIFIERS\n?", "", text, flags=re.S)
text = text.rstrip().removesuffix("}").rstrip() + "\n\n" + "\n".join(java) + "\n}\n"
JAVA.write_text(text, encoding="utf-8")
xml = ['<?xml version="1.0" encoding="UTF-8"?>', '<thing:thing-descriptions bindingId="solarman" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:thing="https://openhab.org/schemas/thing-description/v1.0.0" xsi:schemaLocation="https://openhab.org/schemas/thing-description/v1.0.0 https://openhab.org/schemas/thing-description-1.0.0.xsd">']
catalog = ["# Sunsynk register catalog", "", "Generated from the pinned kellerza/sunsynk definitions. Addresses are decimal, zero-based holding registers. Ordered tuples preserve low-word-first significance; aliases are included. Limits may reference another sensor ID. Constant and PVDynamicTotalSensor tuples can contain virtual placeholders such as [0]; these are not direct measurement addresses or additional read requests. MathSensor tuples identify calculation inputs. The three single-phase identification/version channels are separately attributed binding additions from deye_hybrid; other labels and options come from the pinned profile. See README for installation, command types and profile selection.", ""]
for profile, sensors in PROFILES.items():
    label = "Deye/Sunsynk " + profile.replace("_", " ").title().replace("Lv", "LV").replace("Hv", "HV").replace("Ess", "ESS")
    xml += ['<thing-type id="sunsynk-' + identifier(profile) + '" listed="false">', "<label>" + escape(label) + "</label>", "<description>Profile-specific monitoring and asynchronous controls over a Solarman logger.</description>", "<semantic-equipment-tag>Inverter</semantic-equipment-tag>", "<channels>"]
    xml += ['<channel id=' + quoteattr(channel_ids[s["id"]]) + ' typeId=' + quoteattr(type_ids[profile + ":" + s["id"]]) + '/>' for s in sensors]
    xml += ["</channels>", '<config-description-ref uri="thing-type:solarman:sunsynk"/>', "</thing-type>"]
    catalog += ["## " + profile, "", "| Channel | Item type | Registers | Factor | Mask | Access | Bounds |", "| --- | --- | --- | --- | --- | --- | --- |"]
    for s in sensors:
        bounds = str(s.get("min", "")) + " .. " + str(s.get("max", "")) if s["kind"] in ("NumberRWSensor", "TimeRWSensor") else ""
        catalog.append(f'| `{channel_ids[s["id"]]}` | {item_type(s)} | {s["registers"]} | {s["factor"]} | {hex(s["mask"])} | {"Read/write" if s["writable"] else "Read"} | {bounds} |')
        xml += ['<channel-type id=' + quoteattr(type_ids[profile + ":" + s["id"]]) + '>', "<item-type>" + item_type(s) + "</item-type>", "<label>" + escape(s["name"]) + "</label>", "<description>" + escape(s["name"]) + (" (binding metadata, deye_hybrid registers)" if s.get("source") else "") + " for the " + escape(profile.replace("_", " ")) + " profile.</description>"]
        options = s.get("options", {})
        state = '<state readOnly="' + str(not s["writable"]).lower() + '"'
        if item_type(s).startswith("Number:"):
            state += ' pattern="%.2f %unit%"'
        if options:
            xml += [state + ">", "<options>"] + ["<option value=" + quoteattr(v) + ">" + escape(v) + "</option>" for v in options.values()] + ["</options>", "</state>"]
        else:
            xml.append(state + "/>")
        if s["writable"]:
            xml.append("<autoUpdatePolicy>veto</autoUpdatePolicy>")
        xml.append("</channel-type>")
    catalog.append("")
xml.append("</thing:thing-descriptions>")
(ROOT / "src/main/resources/OH-INF/thing/sunsynk.xml").write_text("\n".join(xml) + "\n", encoding="utf-8")
(ROOT / "docs").mkdir(exist_ok=True)
(ROOT / "docs/sunsynk-channels.md").write_text(format_markdown("\n".join(catalog)), encoding="utf-8")
print(f"Generated {len(PROFILES)} Things, {len(type_ids)} channel types and {len(channel_ids)} distinct channel IDs.")
