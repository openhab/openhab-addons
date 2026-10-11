"""Generates the binding's RscpTag catalogue.

The catalogue is derived from the RSCP interface definitions and is deliberately limited to the data
points the binding actually uses rather than all ~3700 known tags: tags that are not listed are still
decoded from a response, they simply render as their numeric id.

Included:
  * every value tag referenced by the rscp2mqtt default mapping (the evidence base for capabilities)
  * the _REQ_, _RES_ and plain counterparts of those tags
  * every tag referenced by the binding sources passed as extra arguments
  * the handshake and control tags listed in EXPLICIT

usage: python gen_rscp_tag.py <rscp_tags.tsv> <rscp_mapping.tsv> <out.java> [extraSource.java ...]
"""
import re
import sys

tags_file, mapping_file, out_file = sys.argv[1], sys.argv[2], sys.argv[3]
extra_sources = sys.argv[4:]

tags = []
with open(tags_file, encoding='utf-8') as f:
    next(f, None)
    for line in f:
        parts = line.rstrip('\n').split('\t')
        if len(parts) == 2:
            tags.append((parts[0].replace('TAG_', ''), parts[1]))

by_id = dict((int(i, 16), n) for n, i in tags)
by_name = dict((n, i) for n, i in tags)

# value tags used by the default mapping; resolved through their hex id because the names in the C
# mapping literals are truncated to the field width
wanted = set()
with open(mapping_file, encoding='utf-8') as f:
    next(f, None)
    for line in f:
        parts = line.rstrip('\n').split('\t')
        if len(parts) >= 4 and parts[1].startswith('0x') and parts[1] != '0x00000000':
            canonical = by_id.get(int(parts[1], 16))
            wanted.add(canonical if canonical else parts[1])

# the _REQ_ counterpart of every value tag, and the _RES_ and plain counterpart of every _REQ_ tag
for name in list(wanted):
    if '_REQ_' not in name:
        candidate = name.replace('_', '_REQ_', 1)
        if candidate in by_name:
            wanted.add(candidate)
for name in list(wanted):
    if '_REQ_' in name:
        for variant in (name.replace('_REQ_', '_'), name.replace('_REQ_', '_RES_')):
            if variant in by_name:
                wanted.add(variant)

# handshake, control and error tags; every name here is verified against the catalogue
EXPLICIT = [
    'RSCP_REQ_AUTHENTICATION', 'RSCP_AUTHENTICATION_USER', 'RSCP_AUTHENTICATION_PASSWORD',
    'RSCP_AUTHENTICATION', 'RSCP_GENERAL_ERROR', 'RSCP_REQ_SUPPORTED_PROTOCOL_VERSIONS',
    'RSCP_SUPPORTED_PROTOCOL_VERSIONS', 'RSCP_SET_PROTOCOL_VERSION',
    'EMS_REQ_GET_POWER_SETTINGS', 'EMS_GET_POWER_SETTINGS', 'EMS_REQ_SET_POWER_SETTINGS',
    'EMS_SET_POWER_SETTINGS', 'EMS_REQ_SET_POWER_MODE', 'EMS_REQ_GET_IDLE_PERIODS',
    'EMS_REQ_SET_IDLE_PERIODS', 'EMS_REQ_GET_MANUAL_CHARGE', 'EMS_REQ_GET_SYS_SPECS',
    'EMS_RES_POWER_LIMITS_USED',
    'WB_REQ_DATA', 'WB_REQ_SET_EXTERN', 'WB_REQ_SET_PARAM_1', 'WB_REQ_SET_PARAM_2',
    'WB_INDEX', 'WB_REQ_EXTERN_DATA_SUN', 'WB_REQ_EXTERN_DATA_NET', 'WB_REQ_EXTERN_DATA_ALL',
    'PM_INDEX', 'PM_DATA', 'WB_DATA',
    'HA_REQ_ACTUATOR_STATES', 'DB_REQ_HISTORY_DATA_DAY', 'DB_REQ_HISTORY_DATA_WEEK',
    'DB_REQ_HISTORY_DATA_MONTH', 'DB_REQ_HISTORY_DATA_YEAR',
]
wanted.update(EXPLICIT)

# every tag referenced by the binding sources, so the catalogue always covers the implementation
for source in extra_sources:
    text = open(source, encoding='utf-8').read()
    wanted.update(re.findall(r'RscpTag\.([A-Z][A-Z0-9_]+)', text))

resolved = sorted(((int(by_name[n], 16), n) for n in wanted if n in by_name))

lines = ['''/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.e3dc.internal.rscp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * RSCP tag identifiers used by this binding.
 *
 * <p>
 * This is a deliberate subset of the RSCP interface: the data points the binding reads and writes,
 * together with the tags needed for the handshake and for control. Tags that are not listed here are
 * still decoded from a response, they only render as their numeric id in logs.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class RscpTag {
''']

for value, name in resolved:
    lines.append('    public static final int %s = 0x%08X;' % (name, value))

lines.append('''
    private static final Map<Integer, String> NAMES = new HashMap<>();

    static {''')
for value, name in resolved:
    lines.append('        NAMES.put(%s, "%s");' % (name, name))
lines.append('''    }

    private RscpTag() {
    }

    /** The symbolic name of a tag, or a hex rendering when the tag is not part of this catalogue. */
    public static String nameOf(int tag) {
        String name = NAMES.get(tag);
        return name != null ? name : String.format("UNKNOWN_0x%08X", tag);
    }
}''')

open(out_file, 'w', encoding='utf-8', newline='\n').write('\n'.join(lines) + '\n')
print('wrote %s with %d tags' % (out_file, len(resolved)))
missing = sorted(n for n in wanted if n not in by_name)
if missing:
    print('tags referenced but not in the catalogue (%d): %s' % (len(missing), ', '.join(missing[:20])))
