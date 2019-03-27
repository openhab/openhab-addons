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
package org.openhab.binding.dsmr.internal.meter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
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
        DSMRMeterDescriptor descriptor = new DSMRMeterDescriptor(DSMRMeterType.DEVICE_V5, 0);
        DSMRMeter meter = new DSMRMeter(descriptor);

        List<CosemObject> filterMeterValues = meter
                .filterMeterValues(TelegramReaderUtil.readTelegram("dsmr_50", TelegramState.OK).getCosemObjects());
        assertEquals("Filter should return all required objects", DSMRMeterType.DEVICE_V5.requiredCosemObjects.length,
                filterMeterValues.size());
    }
}
