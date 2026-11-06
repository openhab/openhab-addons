"""Check copyable documentation examples against independent legacy and current advanced metadata.

Uses only development tooling (PyYAML), local fixtures and files. No commands or network I/O.
"""
import csv
import json
import re
import xml.etree.ElementTree as ET
from decimal import Decimal
from pathlib import Path

from profile_resources import load_profile

ROOT = Path(__file__).resolve().parents[1]
EXAMPLES = ROOT / 'docs/examples'
LEGACY = json.loads((ROOT / 'src/test/resources/legacy/main-baseline.json').read_text(encoding='utf-8'))['profiles']
DIFFERENCES = json.loads((ROOT / 'src/test/resources/legacy/main-reviewed-differences.json').read_text(encoding='utf-8'))
XML = ET.parse(ROOT / 'src/main/resources/OH-INF/thing/sunsynk.xml').getroot()
TYPES = {node.attrib['id']: node.findtext('item-type') for node in XML.findall('channel-type')}
ITEM = re.compile(r'^\s*(Group|Number(?::\w+)?|String|Switch|DateTime)\s+(\w+)\b', re.M)
WARNING = "Writing settings changes the inverter's operating configuration. Verify that the selected profile matches your inverter and that the requested settings are appropriate for your installation. Incorrect settings may affect inverter or battery operation. Use write functionality at your own risk."


def metadata(profile):
    if profile in LEGACY:
        return {node['id']: dict(item_type=DIFFERENCES['item_type_changes'].get(profile + '/' + node['id'], {}).get('after', node['itemType']), unit=node['configuration'].get('uom', ''), writable=node['configuration'].get('readOnly') is False) for node in LEGACY[profile]['channels']}
    name = profile.removeprefix('sunsynk_')
    thing = next(node for node in XML.findall('thing-type') if node.attrib['id'] == 'sunsynk-' + name.replace('_', '-'))
    channels = {node.attrib['id']: TYPES[node.attrib['typeId']] for node in thing.findall('./channels/channel')}
    return {sensor['id'].replace('_', '-'): dict(sensor, item_type=channels[sensor['id'].replace('_', '-')]) for sensor in load_profile(ROOT / 'src/main/resources/sunsynk' / (name + '.json'))}


def items(text, profile, custom=None):
    catalog = metadata(profile) | (custom or {})
    declarations = list(ITEM.finditer(text))
    result = {}
    for index, match in enumerate(declarations):
        kind, name = match.groups()
        assert name not in result, ('Duplicate Item', name)
        body = text[match.end():declarations[index + 1].start() if index + 1 < len(declarations) else len(text)]
        if kind == 'Group':
            result[name] = dict(item_type=kind)
            continue
        link = re.search(r'channel\s*=\s*"([^"]+)"', body)
        assert link, ('Missing channel', name)
        channel = link[1].rsplit(':', 1)[-1]
        assert channel in catalog, (profile, name, channel)
        expected = catalog[channel]
        assert kind == expected['item_type'], (profile, name, kind, expected['item_type'])
        unit = re.search(r'\bunit\s*=\s*"([^"]*)"', body)
        if unit:
            actual, declared = unit[1], expected.get('unit', '')
            assert actual == declared or {actual, declared} <= {'Wh', 'kWh'}, (name, actual, declared)
            assert kind.startswith('Number:'), ('Unit on non-quantity Item', name)
        result[name] = dict(expected, channel=channel)
    return result


def sitemap(text, declarations):
    references = re.findall(r'\bitem\s*=\s*(\w+)', text)
    assert set(references) <= declarations.keys(), ('Unknown sitemap Items', set(references) - declarations.keys())
    for line in text.splitlines():
        ref = re.search(r'\bitem\s*=\s*(\w+)', line)
        if not ref:
            continue
        item = declarations[ref[1]]
        widget = line.strip().split()[0]
        if widget in ('Slider', 'Setpoint'):
            assert item['item_type'].startswith('Number') and item['writable'], ref[1]
            values = {key: Decimal(value) for key, value in re.findall(r'\b(minValue|maxValue|step)\s*=\s*([0-9.]+)', line)}
            assert values['minValue'] <= values['maxValue'] and values['step'] > 0
            for boundary, attribute in (('minValue', 'min'), ('maxValue', 'max')):
                bound = item.get(attribute)
                if isinstance(bound, (int, float)):
                    assert values[boundary] >= Decimal(str(bound)) if attribute == 'min' else values[boundary] <= Decimal(str(bound)), (ref[1], boundary)
        if widget == 'Switch':
            assert item['item_type'] == 'Switch' and item['writable'], ref[1]
        if widget == 'Selection':
            assert item['writable'], ref[1]
            mapping = re.search(r'mappings=\[(.*?)\]', line)
            assert mapping
            commands = re.findall(r'"([^"\n]*)"\s*=', mapping[1])
            if item.get('options'):
                assert set(commands) <= set(item['options'].values()), (ref[1], commands)
            elif item.get('kind') == 'TimeRWSensor':
                assert all(re.fullmatch(r'(?:[01]\d|2[0-3]):[0-5]\d', command) for command in commands)
            else:
                raise AssertionError(('Unreviewed selection commands', ref[1]))
    return len(references)


def block(text, filename):
    section = re.search(r'### `' + re.escape(filename) + r'`\n(.*?)(?=\n### |\n## |\Z)', text, re.S)
    assert section, filename
    match = re.search(r'```[^\n]*\n(.*?)\n```', section[1], re.S)
    assert match, filename
    return match[1] + '\n'


def main():
    readme = (ROOT / 'README.md').read_text(encoding='utf-8')
    assert '> **Warning:** ' + WARNING in readme
    assert all(value in readme for value in ('Johann Kellerman', 'f1ecbc28e3ccf2b9f87b1ccee85a41628567da43', 'Apache-2.0', 'Hardware Test Coverage'))
    legacy_tables = {}
    for profile in ("deye_sg04lp3", "deye_2mppt"):
        section = re.search(r'### `' + profile + r'` Channels\n(.*?)(?=\n### |\n## |\Z)', readme, re.S)
        assert section
        rows = [[cell.strip(' `') for cell in line.split('|')[1:-1]] for line in section[1].splitlines() if line.startswith('|')][2:]
        catalog = metadata(profile)
        assert len(rows) == len(catalog) and {row[0] for row in rows} == catalog.keys(), profile
        for row in rows:
            assert row[1] == catalog[row[0]]['item_type'], row[0]
            if profile == 'deye_sg04lp3':
                assert (row[2] == 'R/W') == catalog[row[0]]['writable'], row[0]
        legacy_tables[profile] = len(rows)
    counts = {}
    sitemap_references = 0
    for prefix, profile in (('solarman', 'deye_sg04lp3'), ('sunsynk', 'sunsynk_single_phase')):
        for extension in ('things', 'items', 'sitemap'):
            path = EXAMPLES / (prefix + '.' + extension)
            assert block(readme, path.name) == path.read_text(encoding='utf-8'), path.name
        thing = (EXAMPLES / (prefix + '.things')).read_text(encoding='utf-8')
        assert f'inverterType="{profile}"' in thing
        assert 'hostname="192.168.1.100"' in thing and 'serialNumber="1234567890"' in thing
        if prefix == 'solarman':
            assert 'Type number : inverter-frequency [scale=0.01, uom="Hz", rule=3, registers="0x027E"]' in thing
            custom = {'inverter-frequency': dict(item_type='Number:Frequency', unit='Hz', writable=False)}
        else:
            assert int(re.search(r'refreshInterval=(\d+)', thing)[1]) >= 5
            custom = None
        declarations = items((EXAMPLES / (prefix + '.items')).read_text(encoding='utf-8'), profile, custom)
        counts[prefix] = len(declarations)
        sitemap_references += sitemap((EXAMPLES / (prefix + '.sitemap')).read_text(encoding='utf-8'), declarations)
    for profile, expected_count in (('deye_hybrid', 81), ('sunsynk_single_phase', 150)):
        path = EXAMPLES / 'profile-channel-lists' / (profile + '.items')
        declarations = items(path.read_text(encoding='utf-8'), profile)
        assert len(declarations) == expected_count
        assert {value['channel'] for value in declarations.values()} == metadata(profile).keys()
        counts[profile] = len(declarations)
    rows = list(csv.DictReader((EXAMPLES / 'profile-channel-lists/channels.csv').open(encoding='utf-8')))
    assert len(rows) == 231
    for row in rows:
        expected = metadata(row['profile'])[row['channel']]
        assert row['item_type'] == expected['item_type']
        assert (row['access'] == 'Read/write') == expected['writable']
    mapping = list(csv.DictReader((EXAMPLES / 'profile-channel-lists/migration-map.csv').open(encoding='utf-8')))
    assert len(mapping) == 81 and {row['legacy_channel'] for row in mapping} == metadata('deye_hybrid').keys()
    broken = []
    for path in ROOT.rglob('*.md'):
        if 'target' in path.parts or path.name == 'main-port-validation.md':
            continue
        for target in re.findall(r'\[[^\]]*\]\(([^)]+)\)', path.read_text(encoding='utf-8')):
            target = target.split('#', 1)[0]
            if target and '://' not in target and not target.startswith(('mailto:', '/')) and not (path.parent / target).exists():
                broken.append((str(path.relative_to(ROOT)), target))
    assert not broken, broken
    print(json.dumps(dict(legacy_readme_table_rows=legacy_tables, item_declarations_by_example=counts, sitemap_references=sitemap_references, combined_csv_rows=len(rows), migration_rows=len(mapping), local_links='passed', selected_warning='passed', scope='Static metadata/example checks; no runtime import or inverter commands'), indent=2))


if __name__ == '__main__':
    main()
