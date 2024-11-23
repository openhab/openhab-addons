/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
public class StatusTest {
    @Test
    public void testParseStatusWithUnknownRawValue() {
        // given:
        String json = "{ \"key_localized\": \"State\", \"value_raw\": 99, \"value_localized\": \"Booting\" }";

        // when:
        Status status = new Gson().fromJson(json, Status.class);

        // then:
        assertNotNull(status);
        assertEquals("State", status.getKeyLocalized().get());
        assertEquals(Integer.valueOf(99), status.getValueRaw().get());
        assertEquals("Booting", status.getValueLocalized().get());
    }

    @Test
    public void testParseStatusWithKnownRawValue() {
        // given:
        String json = "{ \"key_localized\": \"State\", \"value_raw\": 1, \"value_localized\": \"Off\" }";

        // when:
        Status status = new Gson().fromJson(json, Status.class);

        // then:
        assertNotNull(status);
        assertEquals("State", status.getKeyLocalized().get());
        assertEquals(Integer.valueOf(StateType.OFF.getCode()), status.getValueRaw().get());
        assertEquals("Off", status.getValueLocalized().get());
    }
}
