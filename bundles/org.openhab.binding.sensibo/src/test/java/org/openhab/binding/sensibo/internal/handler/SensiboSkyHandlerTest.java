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
package org.openhab.binding.sensibo.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openhab.binding.sensibo.internal.SensiboBindingConstants;
import org.openhab.binding.sensibo.internal.SensiboCommunicationException;
import org.openhab.binding.sensibo.internal.WireHelper;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetailsDTO;
import org.openhab.binding.sensibo.internal.handler.SensiboSkyHandler.StateChange;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * @author Arne Seime - Initial contribution
 */
public class SensiboSkyHandlerTest {

    private final WireHelper wireHelper = new WireHelper();

    @Test
    public void testStateChangeValidation() throws IOException, SensiboCommunicationException {
        final PodDetailsDTO rsp = wireHelper.deSerializeResponse("/get_pod_details_response.json", PodDetailsDTO.class);
        SensiboSky sky = new SensiboSky(rsp);
        Thing thing = Mockito.mock(Thing.class);
        SensiboSkyHandler handler = new SensiboSkyHandler(thing);

        // Target temperature
        StateChange stateChangeCheck = handler.checkStateChangeValid(sky, SensiboSkyHandler.TARGET_TEMPERATURE_PROPERTY,
                new DecimalType(123));
        assertFalse(stateChangeCheck.valid);
        assertNotNull(stateChangeCheck.validationMessage);
        assertTrue(handler.checkStateChangeValid(sky, SensiboSkyHandler.TARGET_TEMPERATURE_PROPERTY,
                new DecimalType(10)).valid);

        // Mode
        StateChange stateChangeCheckMode = handler.checkStateChangeValid(sky, "mode", "invalid");
        assertFalse(stateChangeCheckMode.valid);
        assertNotNull(stateChangeCheckMode.validationMessage);
        assertTrue(handler.checkStateChangeValid(sky, "mode", "auto").valid);

        // Swing
        StateChange stateChangeCheckSwing = handler.checkStateChangeValid(sky, "swing", "invalid");
        assertFalse(stateChangeCheckSwing.valid);
        assertNotNull(stateChangeCheckSwing.validationMessage);
        assertTrue(handler.checkStateChangeValid(sky, "swing", "stopped").valid);

        // FanLevel
        StateChange stateChangeCheckFanLevel = handler.checkStateChangeValid(sky, "fanLevel", "invalid");
        assertFalse(stateChangeCheckFanLevel.valid);
        assertNotNull(stateChangeCheckFanLevel.validationMessage);
        assertTrue(handler.checkStateChangeValid(sky, "fanLevel", "high").valid);
    }

    @Test
    public void testTemperatureConversion() throws IOException {
        final PodDetailsDTO rsp = wireHelper.deSerializeResponse("/get_pod_details_response.json", PodDetailsDTO.class);
        SensiboSky sky = new SensiboSky(rsp);
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getUID()).thenReturn(new ThingUID("sensibo:account:thinguid"));

        Map<String, Object> config = new HashMap<>();
        config.put("macAddress", sky.getMacAddress());
        Mockito.when(thing.getConfiguration()).thenReturn(new Configuration(config));

        SensiboSkyHandler handler = Mockito.spy(new SensiboSkyHandler(thing));
        handler.initialize();

        SensiboModel model = new SensiboModel(0);
        model.addPod(sky);

        // Once with Celcius argument
        handler.handleCommand(new ChannelUID(thing.getUID(), SensiboBindingConstants.CHANNEL_TARGET_TEMPERATURE),
                new QuantityType<>(50, ImperialUnits.FAHRENHEIT), model);

        // Once with Fahrenheit
        handler.handleCommand(new ChannelUID(thing.getUID(), SensiboBindingConstants.CHANNEL_TARGET_TEMPERATURE),
                new QuantityType<>(10, SIUnits.CELSIUS), model);

        // Once with Decimal directly
        handler.handleCommand(new ChannelUID(thing.getUID(), SensiboBindingConstants.CHANNEL_TARGET_TEMPERATURE),
                new DecimalType(10), model);

        ArgumentCaptor<DecimalType> valueCapture = ArgumentCaptor.forClass(DecimalType.class);
        Mockito.verify(handler, Mockito.times(3)).updateAcState(ArgumentMatchers.eq(sky), ArgumentMatchers.anyString(),
                valueCapture.capture());
        assertEquals(new DecimalType(10), valueCapture.getValue());
    }

    @Test
    public void testAddDynamicChannelsMarco() throws IOException, SensiboCommunicationException {
        testAddDynamicChannels("/get_pod_details_response_marco.json");
    }

    @Test
    public void testAddDynamicChannels() throws IOException, SensiboCommunicationException {
        testAddDynamicChannels("/get_pod_details_response.json");
    }

    private void testAddDynamicChannels(String podDetailsResponse) throws IOException, SensiboCommunicationException {
        final PodDetailsDTO rsp = wireHelper.deSerializeResponse(podDetailsResponse, PodDetailsDTO.class);
        SensiboSky sky = new SensiboSky(rsp);
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getUID()).thenReturn(new ThingUID("sensibo:account:thinguid"));
        SensiboSkyHandler handler = Mockito.spy(new SensiboSkyHandler(thing));
        List<Channel> dynamicChannels = handler.createDynamicChannels(sky);
        assertTrue(!dynamicChannels.isEmpty());
    }
}
