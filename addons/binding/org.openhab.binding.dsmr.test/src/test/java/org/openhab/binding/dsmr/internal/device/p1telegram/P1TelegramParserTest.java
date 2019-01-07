/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        assertEquals("Expected number of objects", numberOfCosemObjects,
            telegram.getCosemObjects().stream().mapToInt(o -> o.getCosemValues().size()).sum());
    }
}
