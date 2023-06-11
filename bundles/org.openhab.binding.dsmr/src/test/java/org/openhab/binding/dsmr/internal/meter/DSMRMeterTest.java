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
package org.openhab.binding.dsmr.internal.meter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;

/**
 * Test class for {@link DSMRMeter}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DSMRMeterTest {

    /**
     * Test if method {@link DSMRMeter#filterMeterValues(List)} correctly filters values.
     */
    @Test
    public void testFilterMeterValues() {
        final List<CosemObject> cosemObjects = TelegramReaderUtil.readTelegram("dsmr_50", TelegramState.OK)
                .getCosemObjects();

        assertMeterValues(cosemObjects, DSMRMeterType.DEVICE_V5, DSMRMeterConstants.UNKNOWN_CHANNEL, 3);
        assertMeterValues(cosemObjects, DSMRMeterType.ELECTRICITY_V5_0, 0, 29);
        assertMeterValues(cosemObjects, DSMRMeterType.M3_V5_0, 1, 3);
    }

    private void assertMeterValues(List<CosemObject> cosemObjects, DSMRMeterType type, int channel, int expected) {
        final DSMRMeterDescriptor descriptor = new DSMRMeterDescriptor(type, channel);
        final DSMRMeter meter = new DSMRMeter(descriptor);
        final List<CosemObject> filterMeterValues = meter.filterMeterValues(cosemObjects, channel);

        assertEquals(expected, filterMeterValues.size(), "Filter should return all required objects");
    }
}
