"""Migrates the nested Registry classes to the new top level registry classes.

Replaces E3DCChannelSpec.Registry with E3DCChannelRegistry and E3DCCommandSpec.Registry with
E3DCCommandRegistry, and fixes the nullable annotation that could not be applied to a nested type.

usage: python migrate_registry.py <bindungRoot>
"""
import os
import re
import sys

root = sys.argv[1]
changed = []

for dirpath, _dirnames, filenames in os.walk(os.path.join(root, 'src')):
    for filename in filenames:
        if not filename.endswith('.java'):
            continue
        path = os.path.join(dirpath, filename)
        text = open(path, encoding='utf-8').read()
        original = text

        text = text.replace('E3DCCommandSpec.@Nullable Registry', '@Nullable E3DCCommandRegistry')
        text = text.replace('E3DCCommandSpec.Registry', 'E3DCCommandRegistry')
        text = text.replace('E3DCChannelSpec.Registry', 'E3DCChannelRegistry')

        if text != original:
            open(path, 'w', encoding='utf-8', newline='\n').write(text)
            changed.append(os.path.relpath(path, root))

print('files changed: %d' % len(changed))
for name in sorted(changed):
    print('  ' + name)
