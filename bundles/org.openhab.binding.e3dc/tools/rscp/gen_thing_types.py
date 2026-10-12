"""Generates the OH-INF thing metadata from the verified channel model.

Channel ids, groups and item types are derived from the tables in E3DCChannelSpec.java so the XML and
the Java implementation cannot drift apart. Labels and descriptions live here because they are
user-facing text, not implementation data.

usage: python gen_thing_types.py <E3DCChannelSpec.java> <outDir>
"""
import os
import re
import sys

spec_file, out_dir = sys.argv[1], sys.argv[2]
command_file = sys.argv[3] if len(sys.argv) > 3 else None
text = open(spec_file, encoding='utf-8').read()

# ---------------------------------------------------------------- extract the model
groups = {}
pattern = re.compile(r'(?:new Registry\(\)\.)?group\((E3DCBindingConstants\.GROUP_[A-Z_]+),\s*Map\.ofEntries\((.*?)\)\)\);',
                     re.DOTALL)
for match in pattern.finditer(text):
    group_const = match.group(1).split('.')[-1]
    body = match.group(2)
    channels = {}
    for entry in re.finditer(r'entry\("([^"]+)",\s*(.*?)\)(?=,\s*(?://[^\n]*)?\s*entry\(|$)', body, re.DOTALL):
        channels[entry.group(1)] = ' '.join(entry.group(2).split())
    groups[group_const] = channels

# ---------------------------------------------------------------- writable channels
# derived from the command registry so the XML cannot drift from the implementation
commands = {}
if command_file:
    command_text = open(command_file, encoding='utf-8').read()
    # Walk the builder chain positionally: every group(...) call owns the channels that follow it
    # until the next group(...) call. A single regex cannot do this reliably because the entries
    # themselves contain nested parentheses.
    boundaries = [(m.start(), m.group(1).split('.')[-1]) for m in
                  re.finditer(r'group\((E3DCBindingConstants\.GROUP_[A-Z_]+)\s*,', command_text)]
    for i, (start, group_const) in enumerate(boundaries):
        end = boundaries[i + 1][0] if i + 1 < len(boundaries) else len(command_text)
        segment = command_text[start:end]
        channels = commands.setdefault(group_const, [])
        for entry in re.finditer(r'"(set-[a-z0-9-]+)"\s*,\s*E3DCCommandSpec\.', segment):
            channels.append(entry.group(1))

# ---------------------------------------------------------------- item types and text
CHANNEL_TEXT = {
    'solar-power': ('Number:Power', 'Solar Power', 'Photovoltaic power currently produced'),
    'battery-power': ('Number:Power', 'Battery Power', 'Power flowing into or out of the battery'),
    'house-power': ('Number:Power', 'House Power', 'Power currently consumed by the household'),
    'grid-power': ('Number:Power', 'Grid Power', 'Power drawn from or fed into the grid'),
    'additional-power': ('Number:Power', 'Additional Power', 'Power of an additional producer or consumer'),
    'autarky': ('Number:Dimensionless', 'Autarky', 'Share of consumption covered by the power plant'),
    'self-consumption': ('Number:Dimensionless', 'Self Consumption', 'Share of production used on site'),
    'soc': ('Number:Dimensionless', 'State of Charge', 'Battery state of charge'),
    'rsoc': ('Number:Dimensionless', 'Real State of Charge', 'State of charge the battery reports itself'),
    'remaining-charge-power': ('Number:Power', 'Remaining Charge Power', 'Charge power still available'),
    'remaining-discharge-power': ('Number:Power', 'Remaining Discharge Power', 'Discharge power still available'),
    'max-charge-power': ('Number:Power', 'Maximum Charge Power', 'Configured limit for battery charging'),
    'max-discharge-power': ('Number:Power', 'Maximum Discharge Power', 'Configured limit for battery discharging'),
    'discharge-start-power': ('Number:Power', 'Discharge Start Power', 'Power above which the battery is discharged'),
    'active-phases': ('Number', 'Active Phases', 'Number of phases currently in use'),
    'ac-power': ('Number:Power', 'AC Power', 'Alternating current power of the inverter'),
    'ac-voltage': ('Number:ElectricPotential', 'AC Voltage', 'Alternating current voltage of the inverter'),
    'ac-current': ('Number:ElectricCurrent', 'AC Current', 'Alternating current of the inverter'),
    'ac-frequency': ('Number:Frequency', 'AC Frequency', 'Grid frequency measured by the inverter'),
    'dc-power': ('Number:Power', 'DC Power', 'Direct current power of the inverter'),
    'dc-voltage': ('Number:ElectricPotential', 'DC Voltage', 'Direct current voltage of the inverter'),
    'dc-current': ('Number:ElectricCurrent', 'DC Current', 'Direct current of the inverter'),
    'temperature': ('Number:Temperature', 'Temperature', 'Temperature of the inverter'),
    'on-grid': ('Switch', 'On Grid', 'Whether the inverter is connected to the grid'),
    'string-count': ('Number', 'String Count', 'Number of photovoltaic strings in use'),
    'energy-total': ('Number:Energy', 'Total Energy', 'Energy produced since commissioning'),
    'energy-day': ('Number:Energy', 'Energy Today', 'Energy produced today'),
    'software-version': ('String', 'Software Version', 'Firmware version of the device'),
    'serial-number': ('String', 'Serial Number', 'Serial number of the device'),
    'production-date': ('String', 'Production Date', 'Manufacturing date of the device'),
    'time-zone': ('String', 'Time Zone', 'Time zone configured on the device'),
    'installed-peak-power': ('Number:Power', 'Installed Peak Power', 'Peak power of the installed photovoltaic system'),
    'inverter-count': ('Number', 'Inverter Count', 'Number of inverters in the system'),
    'battery-count': ('Number', 'Battery Count', 'Number of batteries in the system'),
    'mode': ('Number', 'Mode', 'Operating mode of the energy management system'),
    'coupling-mode': ('Number', 'Coupling Mode', 'How the power plant is coupled to the grid'),
    'emergency-power-status': ('Number', 'Emergency Power Status', 'State of the emergency power supply'),
    'charging-lock': ('Switch', 'Charging Lock', 'Battery charging is currently blocked'),
    'discharging-lock': ('Switch', 'Discharging Lock', 'Battery discharging is currently blocked'),
    'emergency-power-available': ('Switch', 'Emergency Power Available', 'Emergency power reserve is available'),
    'charging-throttled': ('Switch', 'Charging Throttled', 'Battery charging is currently limited'),
    'grid-in-limit': ('Switch', 'Grid Limit Active', 'Grid consumption is currently limited'),
    'charging-time-lock': ('Switch', 'Charging Time Lock', 'Battery charging is blocked by a time window'),
    'discharging-time-lock': ('Switch', 'Discharging Time Lock', 'Battery discharging is blocked by a time window'),
    'weather-regulated-charge': ('Switch', 'Weather Regulated Charge', 'Charging follows the weather forecast'),
    'power-limits-used': ('Switch', 'Power Limits Used', 'Configured power limits are active'),
    'powersave': ('Switch', 'Power Save', 'Power saving mode is active'),
    'power-l1': ('Number:Power', 'Power L1', 'Power on phase L1'),
    'power-l2': ('Number:Power', 'Power L2', 'Power on phase L2'),
    'power-l3': ('Number:Power', 'Power L3', 'Power on phase L3'),
    'voltage-l1': ('Number:ElectricPotential', 'Voltage L1', 'Voltage on phase L1'),
    'voltage-l2': ('Number:ElectricPotential', 'Voltage L2', 'Voltage on phase L2'),
    'voltage-l3': ('Number:ElectricPotential', 'Voltage L3', 'Voltage on phase L3'),
    'energy-l1': ('Number:Energy', 'Energy L1', 'Energy measured on phase L1'),
    'energy-l2': ('Number:Energy', 'Energy L2', 'Energy measured on phase L2'),
    'energy-l3': ('Number:Energy', 'Energy L3', 'Energy measured on phase L3'),
    'max-phase-power': ('Number:Power', 'Maximum Phase Power', 'Highest power measured on one phase'),
    'energy-solar': ('Number:Energy', 'Solar Energy', 'Energy taken from the photovoltaic system'),
    'min-charge-current': ('Number:ElectricCurrent', 'Minimum Charge Current', 'Lowest charging current allowed'),
    'max-charge-current': ('Number:ElectricCurrent', 'Maximum Charge Current', 'Highest charging current allowed'),
    'number-phases': ('Number', 'Number of Phases', 'Number of phases used for charging'),
    'auto-phase-switch': ('Switch', 'Automatic Phase Switch',
                          'Whether the wallbox changes the number of phases by itself'),
    'key-state': ('Switch', 'Key State', 'State of the wallbox key switch'),
    'plugged': ('Switch', 'Plugged', 'A vehicle is connected'),
    'locked': ('Switch', 'Locked', 'The connector is locked'),
    'charging': ('Switch', 'Charging', 'A charging session is running'),
    'suspended': ('Switch', 'Suspended', 'Charging is currently suspended'),
    'sun-mode': ('Switch', 'Sun Mode', 'Charging uses surplus photovoltaic power'),
}

# writable channels that start an action instead of setting a value
COMMAND_TEXT = {
    'set-manual-charge': ('Number:Power', 'Set Manual Charge Power', 'Start a manual battery charge'),
}

GROUP_LABEL = {
    'GROUP_POWER': 'Power',
    'GROUP_BATTERY': 'Battery',
    'GROUP_GRID': 'Grid',
    'GROUP_INVERTER': 'Inverter',
    'GROUP_SYSTEM': 'System',
    'GROUP_WALLBOX': 'Wallbox',
    'GROUP_METER': 'Power Meter',
}

read_ids = set(sum((list(c.keys()) for c in groups.values()), []))
command_ids = set(c for v in commands.values() for c in v)
used = sorted(read_ids | command_ids)
missing = [c for c in used if c not in CHANNEL_TEXT and c not in COMMAND_TEXT]
if missing:
    print('ERROR: no label defined for: %s' % ', '.join(missing))
    sys.exit(1)

# every group that needs a channel type file or a channel group entry
all_groups = sorted(set(groups) | set(commands))

os.makedirs(out_dir, exist_ok=True)

HEADER = ['<?xml version="1.0" encoding="UTF-8"?>',
          '<thing:thing-descriptions bindingId="e3dc"',
          '\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"',
          '\txmlns:thing="https://openhab.org/schemas/thing-description/v1.0.0"',
          '\txsi:schemaLocation="https://openhab.org/schemas/thing-description/v1.0.0 https://openhab.org/schemas/thing-description-1.0.0.xsd">',
          '']


def group_slug(group_const):
    return group_const.replace('GROUP_', '').lower().replace('_', '-')


def channel_type_id(group_const, channel_id):
    return '%s-%s' % (group_slug(group_const), channel_id)


def xml_escape(value):
    return value.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')


# ---------------------------------------------------------------- channel types per group
for group_const in all_groups:
    channels = list(groups.get(group_const, {}).keys())
    writable = commands.get(group_const, [])
    if not channels and not writable:
        continue
    lines = list(HEADER)
    for channel_id in channels:
        item_type, label, description = CHANNEL_TEXT[channel_id]
        lines.append('\t<channel-type id="%s">' % channel_type_id(group_const, channel_id))
        lines.append('\t\t<item-type>%s</item-type>' % item_type)
        lines.append('\t\t<label>%s</label>' % xml_escape(label))
        lines.append('\t\t<description>%s</description>' % xml_escape(description))
        lines.append('\t</channel-type>')
        lines.append('')
    for channel_id in writable:
        item_type, label, description = COMMAND_TEXT[channel_id]
        lines.append('\t<channel-type id="%s">' % channel_type_id(group_const, channel_id))
        lines.append('\t\t<item-type>%s</item-type>' % item_type)
        lines.append('\t\t<label>%s</label>' % xml_escape(label))
        lines.append('\t\t<description>%s</description>' % xml_escape(description))
        lines.append('\t</channel-type>')
        lines.append('')
    lines.append('</thing:thing-descriptions>')
    name = '%s-channel-types.xml' % group_slug(group_const)
    open(os.path.join(out_dir, name), 'w', encoding='utf-8', newline='\n').write('\n'.join(lines) + '\n')
    print('wrote', name)

# ---------------------------------------------------------------- channel groups
for group_const in all_groups:
    channels = list(groups.get(group_const, {}).keys()) + commands.get(group_const, [])
    if not channels:
        continue
    lines = list(HEADER)
    lines.append('\t<channel-group-type id="%s">' % group_slug(group_const))
    lines.append('\t\t<label>%s</label>' % GROUP_LABEL[group_const])
    lines.append('\t\t<channels>')
    for channel_id in channels:
        lines.append('\t\t\t<channel id="%s" typeId="%s"/>' % (channel_id, channel_type_id(group_const, channel_id)))
    lines.append('\t\t</channels>')
    lines.append('\t</channel-group-type>')
    lines.append('')
    lines.append('</thing:thing-descriptions>')
    name = '%s-channel-groups.xml' % group_slug(group_const)
    open(os.path.join(out_dir, name), 'w', encoding='utf-8', newline='\n').write('\n'.join(lines) + '\n')
    print('wrote', name)

print('groups: %s' % ', '.join(all_groups))
print('writable channels: %d' % sum(len(v) for v in commands.values()))
