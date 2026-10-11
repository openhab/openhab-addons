"""Extracts the rscp2mqtt default mapping table into a TSV.

The table drives which RSCP data points the tool polls and how it scales them, so it is the evidence
base for the openHAB channel set. Only constants are extracted (tag names, topics, units, divisors) -
no implementation code is copied.

usage: python extract_mapping.py <RscpMqttMapping.h> <RscpTags.h> <out.tsv>
"""
import re
import sys

mapping_file, tags_file, out_file = sys.argv[1], sys.argv[2], sys.argv[3]

tags = {}
for name, hexval in re.findall(r'^#define\s+TAG_([A-Za-z0-9_]+)\s+(0x[0-9A-Fa-f]{8})',
                               open(tags_file, encoding='utf-8', errors='replace').read(),
                               re.MULTILINE):
    tags[name] = hexval

text = open(mapping_file, encoding='utf-8', errors='replace').read()

match = re.search(r'cache_t\s+cache\[\]\s*=\s*\{(.*?)\n\};', text, re.DOTALL)
if not match:
    sys.exit('cache[] initialiser not found')
body = match.group(1)

rows = []
for line in body.splitlines():
    line = line.strip()
    if not line.startswith('{'):
        continue
    line = line.strip('{},').strip()
    fields = [f.strip() for f in line.split(',')]
    if len(fields) < 8:
        continue
    container, tag, index, topic, payload, fmt, unit, divisor = fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]

    def tag_id(token):
        token = token.strip()
        if token in ('0', ''):
            return '0x00000000'
        if token.startswith('TAG_'):
            return tags.get(token[4:], 'UNKNOWN')
        return token

    rows.append({
        'container': tag_id(container),
        'tag': tag_id(tag),
        'index': index,
        'topic': topic.strip('"'),
        'unit': unit,
        'divisor': divisor,
    })

with open(out_file, 'w', encoding='utf-8', newline='\n') as f:
    f.write('container\ttag\tindex\ttopic\tunit\tdivisor\n')
    for r in rows:
        f.write('%s\t%s\t%s\t%s\t%s\t%s\n' % (r['container'], r['tag'], r['index'], r['topic'], r['unit'], r['divisor']))

print('mapping rows extracted: %d -> %s' % (len(rows), out_file))
