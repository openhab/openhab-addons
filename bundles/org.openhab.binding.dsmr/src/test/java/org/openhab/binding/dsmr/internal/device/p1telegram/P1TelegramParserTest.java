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
package org.openhab.binding.dsmr.internal.device.p1telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;

/**
 * Test class for {@link P1TelegramParser}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class P1TelegramParserTest {

    // @formatter:off
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "ace4000", 59, 0},
            { "dsmr_40", 39, 0},
            { "dsmr_42", 39, 0},
            { "dsmr_50", 41, 0},
            { "dsmr_50_austria", 18, 0},
            { "flu5", 21, 0},
            { "flu5_extra", 31, 0},
            { "flu5_invalid_gasmeter", 19, 1},
            { "Iskra_AM550", 41, 0},
            { "Landis_Gyr_E350", 10, 0},
            { "Landis_Gyr_ZCF110", 25, 0},
            { "Sagemcom_XS210", 41, 0},
            { "smarty", 28, 0},
            { "smarty_with_units", 23, 0},
        });
    }
    // @formatter:on

    @ParameterizedTest
    @MethodSource("data")
    public void testParsing(final String telegramName, final int numberOfCosemObjects, final int unknownObjects) {
        final P1Telegram telegram = TelegramReaderUtil.readTelegram(telegramName);
        assertEquals(unknownObjects, telegram.getUnknownCosemObjects().size(),
                "Should not have other than " + unknownObjects + " unknown cosem objects");
        assertEquals(numberOfCosemObjects,
                telegram.getCosemObjects().stream().mapToInt(co -> co.getCosemValues().size()).sum(),
                "Expected number of objects");
    }
}
