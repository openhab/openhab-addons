/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class TypeTest {
    @Test
    public void testParseTypeWithUnknownRawValue() {
        // given:
        String json = "{ \"key_localized\": \"Devicetype\", \"value_raw\": 99, \"value_localized\": \"Car Vaccuum Robot\" }";

        // when:
        Type type = new Gson().fromJson(json, Type.class);

        // then:
        assertNotNull(type);
        assertEquals("Devicetype", type.getKeyLocalized().get());
        assertEquals(DeviceType.UNKNOWN, type.getValueRaw());
        assertEquals("Car Vaccuum Robot", type.getValueLocalized().get());
    }

    @Test
    public void testParseTypeWithKnownRawValue() {
        // given:
        String json = "{ \"key_localized\": \"Devicetype\", \"value_raw\": 1, \"value_localized\": \"Washing Machine\" }";

        // when:
        Type type = new Gson().fromJson(json, Type.class);

        // then:
        assertNotNull(type);
        assertEquals("Devicetype", type.getKeyLocalized().get());
        assertEquals(DeviceType.WASHING_MACHINE, type.getValueRaw());
        assertEquals("Washing Machine", type.getValueLocalized().get());
    }
}
