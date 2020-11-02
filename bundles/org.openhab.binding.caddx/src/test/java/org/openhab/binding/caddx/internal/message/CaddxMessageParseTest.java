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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageReaderUtil;

/**
 * Test class for {@link P1TelegramParser}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@RunWith(value = Parameterized.class)
@NonNullByDefault
public class CaddxMessageParseTest {

    // @formatter:off
    @Parameters(name = "{0}")
    public static final List<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "zone_status_message", "zone_number", "4", },
            { "interface_configuration_message", "panel_firmware_version", "5.37", },
            { "interface_configuration_message", "panel_interface_configuration_message", "true", },

        });
    }
    // @formatter:on

    @Parameter(0)
    public String messageName = "";

    @Parameter(1)
    public String property = "";

    @Parameter(2)
    public String value = "";

    @Test
    public void testParsing() {
        CaddxMessage message = CaddxMessageReaderUtil.readCaddxMessage(messageName);

        assertNotNull("Should not be null", message);
        /*
         * assertEquals(property + " should have length: " + value.length(), value.length(),
         * message.getPropertyById(property).length());
         */
        assertEquals(property + " should be: " + value, value, message.getPropertyById(property));

        /*
         * assertEquals("Should not have any unknown cosem objects", 0, telegram.getUnknownCosemObjects().size());
         * assertEquals("Expected number of objects", numberOfCosemObjects,
         * telegram.getCosemObjects().stream().mapToInt(co -> co.getCosemValues().size()).sum());
         */
    }
}
