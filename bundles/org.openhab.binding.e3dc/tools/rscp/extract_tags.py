"""Extracts the RSCP tag identifiers from the rscp2mqtt header catalogue into a compact TSV.

The tag identifiers are protocol interface facts taken from the E3/DC RSCP interface.
Only the name/number pairs are extracted, no implementation code is copied.

usage: python extract_tags.py <RscpTags.h> <out.tsv>
"""
import re
import sys

src, out = sys.argv[1], sys.argv[2]
data = open(src, 'r', encoding='utf-8', errors='replace').read()

pattern = re.compile(r'^#define\s+(TAG_[A-Za-z0-9_]+)\s+(0x[0-9A-Fa-f]{8})\s*$', re.MULTILINE)
rows = {}
for name, hexval in pattern.findall(data):
    rows[int(hexval, 16)] = (name, hexval)

with open(out, 'w', encoding='utf-8', newline='\n') as f:
    f.write('name\thex\n')
    for value in sorted(rows):
        name, hexval = rows[value]
        f.write('%s\t%s\n' % (name, hexval))

print('tags extracted: %d -> %s' % (len(rows), out))
