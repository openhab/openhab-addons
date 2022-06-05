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
package org.openhab.binding.panamaxfurman.internal.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public class PowerConditionerChannelTest {
    /**
     * BRAND("powerConditionerInfo#brandInfo", StringType.class, false, true),
     * MODEL("powerConditionerInfo#modelInfo", StringType.class, false, true),
     * FIRMWARE("powerConditionerInfo#firmwareVersionInfo", StringType.class, false, true),
     * POWER("power", OnOffType.class, true, false),;
     *
     */
    @ParameterizedTest
    @CsvSource({ "BRAND, powerConditionerInfo#brandInfo", "MODEL, powerConditionerInfo#modelInfo",
            "POWER, outlet3#power" })
    public void testParse(String expected, String channelString) {
        PowerConditionerChannel result = PowerConditionerChannel.from(channelString);
        assertEquals(expected, result.toString());
    }
}
