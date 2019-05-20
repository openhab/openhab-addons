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
package org.openhab.binding.snmp.internal;

import static org.openhab.binding.snmp.internal.SnmpBindingConstants.THING_TYPE_TARGET;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests cases for {@link SnmpTargetHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class SnmpTargetHandlerTest {

    private @Mock SnmpService snmpService;
    private Thing thing;
    private SnmpTargetHandler thingHandler;
    private Map<String, Object> thingConfiguration = new HashMap<>();
    private Map<String, String> thingProperties = new HashMap<>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        /*
         * thingConfiguration.put(CONFIG_ID, TEST_ID);
         */
        thing = ThingBuilder.create(THING_TYPE_TARGET, "testthing").withLabel("Test thing")
                .withConfiguration(new Configuration(thingConfiguration)).withProperties(thingProperties).build();

        thingHandler = new SnmpTargetHandler(thing, snmpService);
    }

    @Test
    public void testInitializationEndsWithUnknown() {
        /*
         * Mockito.doAnswer(answer -> {
         * return OwSensorType.DS2401;
         * }).when(secondBridgeHandler).getType(any());
         *
         * thingHandler.initialize();
         *
         * waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thingHandler.getThing().getStatusInfo().getStatus()));
         */ }

    @Test
    public void testRefreshAnalog() {
        /*
         * Mockito.doAnswer(answer -> {
         * return OwSensorType.DS18B20;
         * }).when(secondBridgeHandler).getType(any());
         *
         * thingHandler.initialize();
         * waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thingHandler.getThing().getStatusInfo().getStatus()));
         *
         * thingHandler.refresh(bridgeHandler, System.currentTimeMillis());
         *
         * inOrder.verify(bridgeHandler, times(1)).checkPresence(new SensorId(TEST_ID));
         * inOrder.verify(bridgeHandler, times(1)).readDecimalType(eq(new SensorId(TEST_ID)), any());
         *
         * inOrder.verifyNoMoreInteractions();
         */
    }
}
