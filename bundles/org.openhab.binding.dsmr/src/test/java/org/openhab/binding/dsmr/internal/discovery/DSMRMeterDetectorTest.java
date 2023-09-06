/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.DEVICE_EMUCS_V1_0;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.DEVICE_V2_V3;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.DEVICE_V4;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.DEVICE_V5;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.ELECTRICITY_ACE4000;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.ELECTRICITY_EMUCS_V1_0;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.ELECTRICITY_SMARTY_V1_0;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.ELECTRICITY_SMARTY_V1_0_AUSTRIA;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.ELECTRICITY_V3_0;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.ELECTRICITY_V4_2;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.ELECTRICITY_V5_0;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.GAS_ACE4000;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.GAS_EMUCS_V1_0;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.M3_V4;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.M3_V5_0;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;

/**
 * Test class for {@link DSMRMeterDetector}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DSMRMeterDetectorTest {

    // @formatter:off
    public static final List<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "ace4000", EnumSet.of(ELECTRICITY_ACE4000, GAS_ACE4000)},
            { "dsmr_40", EnumSet.of(DEVICE_V4, ELECTRICITY_V4_2, M3_V5_0)},
            { "dsmr_42", EnumSet.of(DEVICE_V4, ELECTRICITY_V4_2, M3_V5_0)},
            { "dsmr_50", EnumSet.of(DEVICE_V5, ELECTRICITY_V5_0, M3_V5_0)},
            { "dsmr_50_austria", EnumSet.of(ELECTRICITY_SMARTY_V1_0_AUSTRIA)},
            { "flu5", EnumSet.of(DEVICE_EMUCS_V1_0, ELECTRICITY_EMUCS_V1_0, GAS_EMUCS_V1_0)},
            { "flu5_extra", EnumSet.of(DEVICE_EMUCS_V1_0, ELECTRICITY_EMUCS_V1_0, GAS_EMUCS_V1_0)},
            { "Iskra_AM550", EnumSet.of(DEVICE_V5, ELECTRICITY_V5_0, M3_V5_0)},
            { "Landis_Gyr_E350", EnumSet.of(DEVICE_V2_V3, ELECTRICITY_V3_0)},
            { "Landis_Gyr_ZCF110", EnumSet.of(DEVICE_V4, ELECTRICITY_V4_2, M3_V5_0)},
            { "Sagemcom_XS210", EnumSet.of(DEVICE_V4, ELECTRICITY_V4_2)},
            { "smarty", EnumSet.of(DEVICE_V5, ELECTRICITY_SMARTY_V1_0)},
            { "smarty_with_units", EnumSet.of(DEVICE_V5, ELECTRICITY_SMARTY_V1_0, M3_V4)},
        });
    }
    // @formatter:on

    @ParameterizedTest
    @MethodSource("data")
    public void testDetectMeters(final String telegramName, final Set<DSMRMeterType> expectedMeters) {
        final P1Telegram telegram = TelegramReaderUtil.readTelegram(telegramName);
        final DSMRMeterDetector detector = new DSMRMeterDetector();
        final Entry<Collection<DSMRMeterDescriptor>, List<CosemObject>> entry = detector.detectMeters(telegram);
        final Collection<DSMRMeterDescriptor> detectMeters = entry.getKey();

        assertEquals(expectedMeters.size(), detectMeters.size(),
                "Should detect correct number of meters: " + Arrays.toString(detectMeters.toArray()));
        assertEquals(List.of(), entry.getValue(), "Should not have any undetected cosem objects: ");
        assertEquals(List.of(), telegram.getUnknownCosemObjects(), "Should not have any unknown cosem objects");
        for (final DSMRMeterType meter : expectedMeters) {
            assertEquals(1, detectMeters.stream().filter(e -> e.getMeterType() == meter).count(),
                    String.format("Meter '%s' not found: %s", meter,
                            Arrays.toString(detectMeters.toArray(new DSMRMeterDescriptor[0]))));
        }
    }
}
