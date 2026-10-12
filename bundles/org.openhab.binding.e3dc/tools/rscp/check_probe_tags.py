"""Checks that every tag the probe uses resolves against the extracted catalogue.

The probe derives its sweep from the catalogue, but the curated direct and container lists are written
by hand. A name that does not resolve would be reported as NOT_IN_CATALOGUE and hide whether the
device supports the data point, so these are checked here.

usage: python check_probe_tags.py <rscp_tags.tsv> <Probe.java>
"""
import re
import sys

tags_file, probe_file = sys.argv[1], sys.argv[2]

index = {}
for line in open(tags_file, encoding='utf-8'):
    parts = line.rstrip('\n').split('\t')
    if len(parts) == 2 and parts[0].startswith('TAG_'):
        index[parts[0][4:]] = int(parts[1], 16)

probe = open(probe_file, encoding='utf-8').read()


def resolve(name):
    """Mirrors Probe.tagFor: exact name first, then the _REQ_ form."""
    bare = name[4:] if name.startswith('TAG_') else name
    if bare in index:
        return index[bare]
    underscore = bare.find('_')
    if underscore > 0 and '_REQ_' not in bare:
        return index.get(bare[:underscore] + '_REQ_' + bare[underscore + 1:], 0)
    return 0


names = []
for match in re.finditer(r'private static final List<String> (DIRECT_TAGS|CONTAINER_TAGS) = List\.of\((.*?)\);',
                         probe, re.DOTALL):
    found = re.findall(r'"([A-Z0-9_]+)"', match.group(2))
    print('%-16s %d names' % (match.group(1), len(found)))
    names.extend(found)

# the request shapes name their tags through the binding catalogue, which has to be the same tag table
catalogue_refs = sorted(set(re.findall(r'RscpTag\.([A-Z][A-Z0-9_]+)', probe)))
print('%-16s %d names (from RscpTag)' % ('container shapes', len(catalogue_refs)))

# the sweep is derived from the catalogue, so it cannot contain unknown names
sweep = [n for n in index if '_REQ_' in n and any(n.startswith(ns + '_REQ_') for ns in
         ('RSCP', 'INFO', 'EMS', 'PVI', 'BAT', 'PM', 'WB', 'DB', 'HA', 'DCDC', 'SGR', 'SE'))]
print('%-16s %d names (derived)' % ('catalogue sweep', len(sweep)))

unresolved = sorted(n for n in names if resolve(n) == 0)
unresolved.extend(sorted(n for n in catalogue_refs if n not in index))
print()
print('catalogue tags : %d' % len(index))
print('hand written   : %d' % len(names))
print('unresolved     : %d' % len(unresolved))
if unresolved:
    for name in unresolved:
        print('  ' + name)
    sys.exit(1)
print('TOTAL PROBE TAGS: %d' % (len(names) + len(sweep)))
print('ALL PROBE TAGS RESOLVE')
