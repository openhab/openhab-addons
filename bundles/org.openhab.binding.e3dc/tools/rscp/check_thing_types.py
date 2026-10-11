"""Cross checks the generated OH-INF thing metadata without the XML schema.

Verifies that every typeId referenced from a channel group is defined exactly once in that group's
channel type file, that every channel type is referenced, and that the writable channels declared by
the binding sources appear in the metadata.

usage: python check_thing_types.py <thingDir> <commandSpec.java>
"""
import os
import re
import sys
from collections import Counter

thing_dir, command_spec = sys.argv[1], sys.argv[2]

type_ids = Counter()
for name in sorted(os.listdir(thing_dir)):
    if not name.endswith('-channel-types.xml'):
        continue
    text = open(os.path.join(thing_dir, name), encoding='utf-8').read()
    for match in re.finditer(r'<channel-type id="([^"]+)"', text):
        type_ids[match.group(1)] += 1

referenced = Counter()
for name in sorted(os.listdir(thing_dir)):
    if not name.endswith('-channel-groups.xml'):
        continue
    text = open(os.path.join(thing_dir, name), encoding='utf-8').read()
    for match in re.finditer(r'<channel id="([^"]+)" typeId="([^"]+)"', text):
        referenced[match.group(2)] += 1

problems = []
for type_id, count in referenced.items():
    if type_id not in type_ids:
        problems.append('referenced but not defined: %s' % type_id)
for type_id, count in type_ids.items():
    if count > 1:
        problems.append('defined %d times: %s' % (count, type_id))
    if type_id not in referenced:
        problems.append('defined but never referenced: %s' % type_id)

# writable channels declared in the Java source must show up in the metadata
source = open(command_spec, encoding='utf-8').read()
declared = sorted(set(re.findall(r'"(set-[a-z0-9-]+)"', source)))
all_metadata = ''
for name in sorted(os.listdir(thing_dir)):
    if name.endswith('-channel-groups.xml'):
        all_metadata += open(os.path.join(thing_dir, name), encoding='utf-8').read()
for channel_id in declared:
    if 'id="%s"' % channel_id not in all_metadata:
        problems.append('writable channel missing from metadata: %s' % channel_id)

print('channel types defined : %d' % len(type_ids))
print('channel type refs     : %d' % sum(referenced.values()))
print('writable channels     : %d (%s)' % (len(declared), ', '.join(declared)))
if problems:
    print('PROBLEMS:')
    for problem in problems:
        print('  ' + problem)
    sys.exit(1)
print('THING TYPES CONSISTENT')
