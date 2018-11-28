/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;

/**
 * Test class for {@link DSMRMeterDetector}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@RunWith(value = Parameterized.class)
public class DSMRMeterDetectorTest {

    // @formatter:off
    @Parameters(name = "{0}")
    public static final List<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "dsmr_40", EnumSet.of( DEVICE_V4, ELECTRICITY_V4_2, M3_V5_0)},
            { "dsmr_42", EnumSet.of( DEVICE_V4, ELECTRICITY_V4_2, M3_V5_0)},
            { "dsmr_50", EnumSet.of( DEVICE_V5, ELECTRICITY_V5_0, M3_V5_0)},
            { "Iskra_AM550", EnumSet.of( DEVICE_V5, ELECTRICITY_V5_0, M3_V5_0)},
            { "Landis_Gyr_E350", EnumSet.of( DEVICE_V2_V3, ELECTRICITY_V3_0)},
            { "Landis_Gyr_ZCF110", EnumSet.of( DEVICE_V4, ELECTRICITY_V4_2, M3_V5_0)},
            { "Sagemcom_XS210", EnumSet.of( DEVICE_V4, ELECTRICITY_V4_2)},
        });
    }
    // @formatter:on

    @Parameter(0)
    public String telegramName;

    @Parameter(1)
    public Set<DSMRMeterType> expectedMeters;

    @Test
    public void testDetectMeters() {
        P1Telegram telegram = TelegramReaderUtil.readTelegram(telegramName, TelegramState.OK);
        DSMRMeterDetector detector = new DSMRMeterDetector();
        Collection<DSMRMeterDescriptor> detectMeters = detector.detectMeters(telegram).getKey();
        assertEquals("Should detect correct number of meters", expectedMeters.size(), detectMeters.size());
        for (DSMRMeterType meter : expectedMeters) {
            assertEquals(
                    String.format("Meter '%s' not found: %s", meter,
                            Arrays.toString(detectMeters.toArray(new DSMRMeterDescriptor[0]))),
                    1, detectMeters.stream().filter(e -> e.getMeterType() == meter).count());
        }
    }

}
