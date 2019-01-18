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
package org.openhab.binding.dsmr.internal.discovery;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.dsmr.internal.meter.DSMRMeterType.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.openhab.binding.dsmr.internal.TelegramReaderUtil;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;
import org.openhab.binding.dsmr.internal.handler.DSMRBridgeHandler;
import org.openhab.binding.dsmr.internal.handler.DSMRMeterHandler;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;

/**
 * Test class for {@link DSMRMeterDiscoveryService}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class DSMRMeterDiscoveryServiceTest {

    private static final String EXPECTED_CONFIGURED_TELEGRAM = "dsmr_50";
    private static final String UNREGISTERED_METER_TELEGRAM = "unregistered_meter";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DSMRBridgeHandler bridge;
    @Mock
    private Thing thing;
    @Mock
    private DSMRMeterHandler meterHandler;

    @Before
    public void setUp() throws PortInUseException, TooManyListenersException {
        initMocks(this);
    }

    /**
     * Test if discovery reports when the user has incorrectly configured the binding with the wrong meter types.
     * Some meters are a subset of other meters so it won't generates errors in usage, but some values will not be
     * available to the user with the subset meter.
     */
    @Test
    public void testInvalidConfiguredMeters() {
        P1Telegram expected = TelegramReaderUtil.readTelegram(EXPECTED_CONFIGURED_TELEGRAM, TelegramState.OK);
        AtomicReference<List<DSMRMeterType>> invalidConfiguredRef = new AtomicReference<>();
        AtomicReference<List<DSMRMeterType>> unconfiguredRef = new AtomicReference<>();
        DSMRMeterDiscoveryService service = new DSMRMeterDiscoveryService(bridge) {
            @Override
            protected void reportConfigurationValidationResults(List<DSMRMeterType> invalidConfigured,
                    List<DSMRMeterType> unconfiguredMeters) {
                super.reportConfigurationValidationResults(invalidConfigured, unconfiguredMeters);
                invalidConfiguredRef.set(invalidConfigured);
                unconfiguredRef.set(unconfiguredMeters);
            }
        };

        // Mock the invalid configuration by reading a telegram that is valid for a meter that is a subset of the
        // expected meter.
        List<DSMRMeterDescriptor> invalidConfiguredMeterDescriptors = EnumSet.of(DEVICE_V5, ELECTRICITY_V4_2, M3_V5_0)
                .stream().map(mt -> new DSMRMeterDescriptor(mt, 0)).collect(Collectors.toList());
        List<Thing> things = invalidConfiguredMeterDescriptors.stream().map(m -> thing).collect(Collectors.toList());
        AtomicReference<Iterator<DSMRMeterDescriptor>> detectMetersRef = new AtomicReference<>();
        when((meterHandler).getMeterDescriptor()).then(a -> {
            if (detectMetersRef.get() == null || !detectMetersRef.get().hasNext()) {
                detectMetersRef.set(invalidConfiguredMeterDescriptors.iterator());
            }
            return detectMetersRef.get().next();
        });
        when(thing.getHandler()).thenReturn(meterHandler);
        when(bridge.getThing().getThings()).thenReturn(things);

        service.telegramReceived(expected);
        assertNotNull("Should have invalid configured meters", invalidConfiguredRef.get());
        assertTrue("Should have found specific invalid meter",
                invalidConfiguredRef.get().contains(DSMRMeterType.ELECTRICITY_V4_2));
        assertNotNull("Should have undetected meters", unconfiguredRef.get());
        assertTrue("Should have found specific undetected meter",
                unconfiguredRef.get().contains(DSMRMeterType.ELECTRICITY_V5_0));
    }

    /**
     * Test if discovery correctly reports if a meter was detected that has not been registered with the energy
     * provider. This meter doesn't report all values in telegram and therefore is not recognized as a specific
     * meter. But reports with an empty equipment identifier.
     */
    @Test
    public void testUnregisteredMeters() {
        P1Telegram telegram = TelegramReaderUtil.readTelegram(UNREGISTERED_METER_TELEGRAM, TelegramState.OK);
        AtomicBoolean unregisteredMeter = new AtomicBoolean(false);
        DSMRMeterDiscoveryService service = new DSMRMeterDiscoveryService(bridge) {
            @Override
            protected void reportUnregisteredMeters() {
                super.reportUnregisteredMeters();
                unregisteredMeter.set(true);
            }
        };
        when(bridge.getThing().getThings()).thenReturn(Collections.emptyList());

        service.telegramReceived(telegram);
        assertTrue("Should have found an unregistered meter", unregisteredMeter.get());
    }
}
