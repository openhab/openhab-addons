/*
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

package org.openhab.binding.emerald.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.emerald.internal.api.EmeraldList;

import com.google.gson.Gson;

public class EmeraldListTest {

    private final Gson gson = new Gson();

    @Test
    public void testSharedPropertyParsingAndLookup() {
        String json = """
                {
                  "code": 200,
                  "message": "success",
                  "info": {
                    "property": [
                      {
                        "id": "prop-primary",
                        "heat_pump": [
                          { "id": "uuid-1", "mac_address": "AA:BB:CC:11:22:33" }
                        ]
                      }
                    ],
                    "shared_property": [
                      {
                        "id": "prop-shared",
                        "heat_pump": [
                          { "id": "uuid-shared-99", "mac_address": "DD:EE:FF:44:55:66" }
                        ]
                      }
                    ]
                  }
                }
                """;

        EmeraldList list = gson.fromJson(json, EmeraldList.class);
        assertNotNull(list);

        // Verify primary property lookup
        EmeraldList.HeatpumpContext primaryCtx = list.findHeatpump("uuid-1");
        assertNotNull(primaryCtx);
        assertEquals("prop-primary", primaryCtx.property().id);

        // Verify shared property lookup
        EmeraldList.HeatpumpContext sharedCtx = list.findHeatpump("uuid-shared-99");
        assertNotNull(sharedCtx);
        assertEquals("prop-shared", sharedCtx.property().id);
        assertEquals("DD:EE:FF:44:55:66", sharedCtx.heatpump().macAddress);

        // Verify unknown UUID returns null
        assertNull(list.findHeatpump("non-existent-uuid"));
    }
}
