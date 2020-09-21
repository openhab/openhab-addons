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
package org.openhab.binding.dsmr.internal.device.p1telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;

/**
 * Test class for {@link P1TelegramParser}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class P1TelegramParserTest {

    // @formatter:off
    public static final List<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "ace4000", 59, },
            { "dsmr_40", 39, },
            { "dsmr_42", 39, },
            { "dsmr_50", 41, },
            { "flu5", 21, },
            { "Iskra_AM550", 41, },
            { "Landis_Gyr_E350", 10, },
            { "Landis_Gyr_ZCF110", 25, },
            { "Sagemcom_XS210", 41, },
            { "smarty", 28, },
            { "smarty_with_units", 23, },
        });
    }
    // @formatter:on

    @ParameterizedTest
    @MethodSource("data")
    public void testParsing(final String telegramName, final int numberOfCosemObjects) {
        P1Telegram telegram = TelegramReaderUtil.readTelegram(telegramName, TelegramState.OK);
        assertEquals(0, telegram.getUnknownCosemObjects().size(), "Should not have any unknown cosem objects");
        assertEquals(numberOfCosemObjects,
                telegram.getCosemObjects().stream().mapToInt(co -> co.getCosemValues().size()).sum(),
                "Expected number of objects");
    }
}
