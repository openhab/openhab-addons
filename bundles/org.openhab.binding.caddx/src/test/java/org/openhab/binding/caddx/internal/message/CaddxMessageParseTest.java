/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.caddx.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageReaderUtil;

/**
 * Test class for CaddxMessage.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxMessageParseTest {

    // @formatter:off
    public static final List<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "zone_status_message", "zone_number", "4", },
            { "interface_configuration_message", "panel_firmware_version", "5.37", },
            { "interface_configuration_message", "panel_interface_configuration_message", "true", },

        });
    }
    // @formatter:on

    @ParameterizedTest
    @MethodSource("data")
    public void testParsing(String messageName, String property, String value) {
        CaddxMessage message = CaddxMessageReaderUtil.readCaddxMessage(messageName);

        assertNotNull(message, "Should not be null");
        assertEquals(value, message.getPropertyById(property), property + " should be: " + value);
    }
}
