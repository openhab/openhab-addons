/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.intellicenter2.internal.model;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.intellicenter2.internal.protocol.ICProtocol.GSON;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class GetHardwareDefinitionTest {

    private static final String GET_HARDWARE_DEFINITION_RESPONSE_NO_ARGS = "{\"command\":\"SendQuery\",\"messageID\":\"88a4331a-ccdb-4ab3-b940-0566244c465f\",\"queryName\":\"GetHardwareDefinition\",\"description\":\"Created on: 2000-01-20 00:14:31\",\"response\":\"200\",\"answer\":[{\"objnam\":\"PNL01\",\"params\":{\"OBJTYP\":\"PANEL\",\"SUBTYP\":\"OCP\",\"HNAME\":\"PNL01\",\"SNAME\":\"Panel 1\",\"PANID\":\"SHARE\",\"LISTORD\":\"1\",\"VER\":\"VER\",\"OBJLIST\":[{\"objnam\":\"M0101\",\"params\":{\"OBJTYP\":\"MODULE\",\"SUBTYP\":\"I5P\",\"SNAME\":\"M0101\",\"LISTORD\":\"LISTORD\",\"PARENT\":\"PNL01\",\"PORT\":\"1\",\"VER\":\"10.001\",\"BADGE\":\"BADGE\",\"CIRCUITS\":[{\"objnam\":\"B1101\",\"params\":{\"OBJTYP\":\"BODY\",\"SUBTYP\":\"POOL\",\"SNAME\":\"Spa\",\"LISTORD\":\"1\",\"HITMP\":\"100\",\"LOTMP\":\"80\",\"HTSRC\":\"H0001\",\"SHARE\":\"B1202\",\"PRIM\":\"65535\",\"SEC\":\"65535\",\"ACT1\":\"65535\",\"ACT2\":\"65535\",\"ACT3\":\"65535\",\"ACT4\":\"65535\",\"VOL\":\"3000\",\"MANHT\":\"00000\",\"MODE\":\"2\"}},{\"objnam\":\"GRP01\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"CIRCGRP\",\"HNAME\":\"GRP01\",\"SNAME\":\"UV Group\",\"COVER\":\"OFF\",\"LISTORD\":\"1\",\"PARENT\":\"00000\",\"BODY\":\"00000\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"OFF\"}}]}},{\"objnam\":\"M0102\",\"params\":{\"OBJTYP\":\"MODULE\",\"SUBTYP\":\"I5PS\",\"SNAME\":\"M0102\",\"LISTORD\":\"LISTORD\",\"PARENT\":\"PNL01\",\"PORT\":\"1\",\"VER\":\"10.001\",\"BADGE\":\"BADGE\",\"CIRCUITS\":[{\"objnam\":\"B1202\",\"params\":{\"OBJTYP\":\"BODY\",\"SUBTYP\":\"SPA\",\"SNAME\":\"Pool\",\"LISTORD\":\"2\",\"HITMP\":\"103\",\"LOTMP\":\"100\",\"HTSRC\":\"00000\",\"SHARE\":\"B1101\",\"PRIM\":\"65535\",\"SEC\":\"65535\",\"ACT1\":\"65535\",\"ACT2\":\"65535\",\"ACT3\":\"65535\",\"ACT4\":\"65535\",\"VOL\":\"1000\",\"MANHT\":\"00000\",\"MODE\":\"1\"}},{\"objnam\":\"GRP01\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"CIRCGRP\",\"HNAME\":\"GRP01\",\"SNAME\":\"UV Group\",\"COVER\":\"OFF\",\"LISTORD\":\"1\",\"PARENT\":\"00000\",\"BODY\":\"00000\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"OFF\"}}]}}]}}]}";
    private static final String GET_HARDWARE_DEFINITION_RESPONSE_CIRCUITS = "{\"command\":\"SendQuery\",\"messageID\":\"276A3DAE-126D-4714-A793-1759633222E1\",\"queryName\":\"GetHardwareDefinition\",\"description\":\"Created on: 2000-02-01 05:50:04\",\"response\":\"200\",\"answer\":[{\"objnam\":\"PNL01\",\"params\":{\"OBJTYP\":\"PANEL\",\"SUBTYP\":\"OCP\",\"HNAME\":\"PNL01\",\"SNAME\":\"Panel 1\",\"PANID\":\"SHARE\",\"LISTORD\":\"1\",\"VER\":\"VER\",\"OBJLIST\":[{\"objnam\":\"M0101\",\"params\":{\"OBJTYP\":\"MODULE\",\"SUBTYP\":\"I5P\",\"SNAME\":\"M0101\",\"LISTORD\":\"LISTORD\",\"PARENT\":\"PNL01\",\"PORT\":\"1\",\"VER\":\"10.001\",\"BADGE\":\"BADGE\",\"CIRCUITS\":[{\"objnam\":\"B1101\",\"params\":{\"OBJTYP\":\"BODY\",\"SUBTYP\":\"POOL\",\"SNAME\":\"Spa\",\"LISTORD\":\"1\",\"HITMP\":\"100\",\"LOTMP\":\"80\",\"HTSRC\":\"H0001\",\"SHARE\":\"B1202\",\"PRIM\":\"65535\",\"SEC\":\"65535\",\"ACT1\":\"65535\",\"ACT2\":\"65535\",\"ACT3\":\"65535\",\"ACT4\":\"65535\",\"VOL\":\"3000\",\"MANHT\":\"00000\",\"MODE\":\"2\"}},{\"objnam\":\"C0002\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"GENERIC\",\"HNAME\":\"AUX 1\",\"SNAME\":\"Air Blower\",\"LISTORD\":\"2\",\"PARENT\":\"M0101\",\"BODY\":\"BODY\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"ON\"}},{\"objnam\":\"C0003\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"GENERIC\",\"HNAME\":\"AUX 2\",\"SNAME\":\"Ultraviolet\",\"LISTORD\":\"3\",\"PARENT\":\"M0101\",\"BODY\":\"BODY\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"ON\"}},{\"objnam\":\"C0004\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"INTELLI\",\"HNAME\":\"AUX 3\",\"SNAME\":\"Spa Light\",\"LISTORD\":\"4\",\"PARENT\":\"M0101\",\"BODY\":\"BODY\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"ON\"}},{\"objnam\":\"C0005\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"GENERIC\",\"HNAME\":\"AUX 4\",\"SNAME\":\"AUX 4\",\"LISTORD\":\"5\",\"PARENT\":\"M0101\",\"BODY\":\"BODY\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"OFF\"}},{\"objnam\":\"C0006\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"POOL\",\"HNAME\":\"Pool\",\"SNAME\":\"Spa\",\"LISTORD\":\"6\",\"PARENT\":\"M0101\",\"BODY\":\"BODY\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"OFF\"}},{\"objnam\":\"GRP01\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"CIRCGRP\",\"HNAME\":\"GRP01\",\"SNAME\":\"UV Group\",\"COVER\":\"OFF\",\"LISTORD\":\"1\",\"PARENT\":\"00000\",\"BODY\":\"00000\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"OFF\"}}]}},{\"objnam\":\"M0102\",\"params\":{\"OBJTYP\":\"MODULE\",\"SUBTYP\":\"I5PS\",\"SNAME\":\"M0102\",\"LISTORD\":\"LISTORD\",\"PARENT\":\"PNL01\",\"PORT\":\"1\",\"VER\":\"10.001\",\"BADGE\":\"BADGE\",\"CIRCUITS\":[{\"objnam\":\"B1202\",\"params\":{\"OBJTYP\":\"BODY\",\"SUBTYP\":\"SPA\",\"SNAME\":\"Pool\",\"LISTORD\":\"2\",\"HITMP\":\"103\",\"LOTMP\":\"100\",\"HTSRC\":\"00000\",\"SHARE\":\"B1101\",\"PRIM\":\"65535\",\"SEC\":\"65535\",\"ACT1\":\"65535\",\"ACT2\":\"65535\",\"ACT3\":\"65535\",\"ACT4\":\"65535\",\"VOL\":\"1000\",\"MANHT\":\"00000\",\"MODE\":\"1\"}},{\"objnam\":\"C0001\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"SPA\",\"HNAME\":\"Spa\",\"SNAME\":\"Pool\",\"LISTORD\":\"1\",\"PARENT\":\"M0102\",\"BODY\":\"BODY\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"OFF\"}},{\"objnam\":\"GRP01\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"CIRCGRP\",\"HNAME\":\"GRP01\",\"SNAME\":\"UV Group\",\"COVER\":\"OFF\",\"LISTORD\":\"1\",\"PARENT\":\"00000\",\"BODY\":\"00000\",\"FREEZE\":\"OFF\",\"VER\":\"VER\",\"TIMZON\":\"TIMZON\",\"TIME\":\"720\",\"RLY\":\"RLY\",\"DNTSTP\":\"OFF\",\"FEATR\":\"OFF\"}}]}}]}}]}\n";

    public GetHardwareDefinitionTest() {
    }

    @Test
    public void testGetHardwareDefinitionNoArguments() throws Exception {
        var response = GSON.fromJson(GET_HARDWARE_DEFINITION_RESPONSE_NO_ARGS, ICResponse.class);
        assertNotNull(response);
        var defn = new GetHardwareDefinition(response);

        var bodies = defn.getPanels().get(0).getBodies();
        assertEquals(2, bodies.size());

        var names = bodies.stream().map(Body::getObjectName).collect(toSet());

        assertTrue(names.contains("B1101"));
        assertTrue(names.contains("B1202"));

        System.err.println(bodies);
    }

    @Test
    public void testGetHardwareDefinitionCircuits() throws Exception {
        var response = GSON.fromJson(GET_HARDWARE_DEFINITION_RESPONSE_CIRCUITS, ICResponse.class);
        assertNotNull(response);
        var defn = new GetHardwareDefinition(response);

        var circuits = defn.getPanels().get(0).getCircuits();
        assertEquals(8, circuits.size());
    }
}
