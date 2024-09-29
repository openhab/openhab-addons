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
package org.openhab.binding.dsmr.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.DEVICE_V5;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.ELECTRICITY_V4_2;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.M3_V5_0;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.handler.DSMRBridgeHandler;
import org.openhab.binding.dsmr.internal.handler.DSMRMeterHandler;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Test class for {@link DSMRMeterDiscoveryService}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class DSMRMeterDiscoveryServiceTest {

    private static final String EXPECTED_CONFIGURED_TELEGRAM = "dsmr_50";
    private static final String UNREGISTERED_METER_TELEGRAM = "unregistered_meter";

    private @NonNullByDefault({}) @Mock(answer = Answers.RETURNS_DEEP_STUBS) DSMRBridgeHandler bridge;
    private @NonNullByDefault({}) @Mock Thing thing;
    private @NonNullByDefault({}) @Mock DSMRMeterHandler meterHandler;

    /**
     * Test if discovery reports when the user has incorrectly configured the binding with the wrong meter types.
     * Some meters are a subset of other meters so it won't generates errors in usage, but some values will not be
     * available to the user with the subset meter.
     */
    @Test
    public void testInvalidConfiguredMeters() {
        final P1Telegram expected = TelegramReaderUtil.readTelegram(EXPECTED_CONFIGURED_TELEGRAM);
        final AtomicReference<List<DSMRMeterType>> invalidConfiguredRef = new AtomicReference<>();
        final AtomicReference<List<DSMRMeterType>> unconfiguredRef = new AtomicReference<>();
        final DSMRMeterDiscoveryService service = new DSMRMeterDiscoveryService() {
            @Override
            protected void reportConfigurationValidationResults(final List<DSMRMeterType> invalidConfigured,
                    final List<DSMRMeterType> unconfiguredMeters) {
                super.reportConfigurationValidationResults(invalidConfigured, unconfiguredMeters);
                invalidConfiguredRef.set(invalidConfigured);
                unconfiguredRef.set(unconfiguredMeters);
            }
        };
        service.setThingHandler(bridge);

        // Mock the invalid configuration by reading a telegram that is valid for a meter that is a subset of the
        // expected meter.
        final List<DSMRMeterDescriptor> invalidConfiguredMeterDescriptors = EnumSet
                .of(DEVICE_V5, ELECTRICITY_V4_2, M3_V5_0).stream().map(mt -> new DSMRMeterDescriptor(mt, 0))
                .collect(Collectors.toList());
        final List<Thing> things = invalidConfiguredMeterDescriptors.stream().map(m -> thing)
                .collect(Collectors.toList());
        final AtomicReference<Iterator<DSMRMeterDescriptor>> detectMetersRef = new AtomicReference<>();

        when((meterHandler).getMeterDescriptor()).then(a -> {
            if (detectMetersRef.get() == null || !detectMetersRef.get().hasNext()) {
                detectMetersRef.set(invalidConfiguredMeterDescriptors.iterator());
            }
            return detectMetersRef.get().next();
        });
        when(thing.getHandler()).thenReturn(meterHandler);
        when(bridge.getThing().getUID()).thenReturn(new ThingUID("dsmr:dsmrBridge:22e5393c"));
        when(bridge.getThing().getThings()).thenReturn(things);

        service.telegramReceived(expected);
        assertNotNull(invalidConfiguredRef.get(), "Should have invalid configured meters");
        assertTrue(invalidConfiguredRef.get().contains(DSMRMeterType.ELECTRICITY_V4_2),
                "Should have found specific invalid meter");
        assertNotNull(unconfiguredRef.get(), "Should have undetected meters");
        assertTrue(unconfiguredRef.get().contains(DSMRMeterType.ELECTRICITY_V5_0),
                "Should have found specific undetected meter");
    }

    /**
     * Test if discovery correctly reports if a meter was detected that has not been registered with the energy
     * provider. This meter doesn't report all values in telegram and therefore is not recognized as a specific
     * meter. But reports with an empty equipment identifier.
     */
    @Test
    public void testUnregisteredMeters() {
        final P1Telegram telegram = TelegramReaderUtil.readTelegram(UNREGISTERED_METER_TELEGRAM);
        final AtomicBoolean unregisteredMeter = new AtomicBoolean(false);
        final DSMRMeterDiscoveryService service = new DSMRMeterDiscoveryService() {
            @Override
            protected void reportUnregisteredMeters() {
                super.reportUnregisteredMeters();
                unregisteredMeter.set(true);
            }
        };
        service.setThingHandler(bridge);
        when(bridge.getThing().getUID()).thenReturn(new ThingUID("dsmr:dsmrBridge:22e5393c"));
        when(bridge.getThing().getThings()).thenReturn(Collections.emptyList());

        service.telegramReceived(telegram);
        assertTrue(unregisteredMeter.get(), "Should have found an unregistered meter");
    }
}
