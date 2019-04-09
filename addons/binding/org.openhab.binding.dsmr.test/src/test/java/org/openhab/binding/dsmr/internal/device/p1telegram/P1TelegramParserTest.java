/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;

/**
 * Test class for {@link P1TelegramParser}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@RunWith(value = Parameterized.class)
public class P1TelegramParserTest {

    // @formatter:off
    @Parameters(name = "{0}")
    public static final List<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "ace4000", 57, },
            { "dsmr_40", 39, },
            { "dsmr_42", 39, },
            { "dsmr_50", 41, },
            { "Iskra_AM550", 41, },
            { "Landis_Gyr_E350", 10, },
            { "Landis_Gyr_ZCF110", 25, },
            { "Sagemcom_XS210", 41, },
            { "smarty", 24, },
        });
    }
    // @formatter:on

    @Parameter(0)
    public String telegramName;

    @Parameter(1)
    public int numberOfCosemObjects;

    @Test
    public void testParsing() {
        P1Telegram telegram = TelegramReaderUtil.readTelegram(telegramName, TelegramState.OK);
        assertEquals("Should not have any unknown cosem objects", 0, telegram.getUnknownCosemObjects().size());
        assertEquals("Expected number of objects", numberOfCosemObjects,
                telegram.getCosemObjects().stream().mapToInt(co -> co.getCosemValues().size()).sum());
    }
}
