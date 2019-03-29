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
package org.openhab.binding.tplinksmarthome.internal.handler;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeDiscoveryService;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Tests cases for {@link SmartHomeHandler} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SmartHomeHandlerTest {

    private static final String CHANNEL_PREFIX = "binding:tplinksmarthome:1234:";

    private SmartHomeHandler handler;

    @Mock
    private Connection connection;
    @Mock
    private ThingHandlerCallback callback;
    @Mock
    private Thing thing;
    @Mock
    private SmartHomeDevice smartHomeDevice;
    @Mock
    private TPLinkSmartHomeDiscoveryService discoveryService;

    private @NonNull final Configuration configuration = new Configuration();

    @Before
    public void setUp() throws IOException {
        initMocks(this);
        configuration.put(CONFIG_IP, "localhost");
        configuration.put(CONFIG_REFRESH, 0);
        when(thing.getConfiguration()).thenReturn(configuration);
        when(smartHomeDevice.getUpdateCommand()).thenReturn(Commands.getSysinfo());
        when(connection.sendCommand(Commands.getSysinfo()))
                .thenReturn(ModelTestUtil.readJson("plug_get_sysinfo_response"));
        handler = new SmartHomeHandler(thing, smartHomeDevice, discoveryService) {
            @Override
            Connection createConnection(TPLinkSmartHomeConfiguration config) {
                return connection;
            }
        };
        when(smartHomeDevice.handleCommand(eq(CHANNEL_SWITCH), eq(connection), any(), any())).thenReturn(true);
        handler.setCallback(callback);
    }

    @After
    public void after() {
        handler.dispose();
    }

    @Test
    public void testInitializeShouldCallTheCallback() throws InterruptedException {
        handler.initialize();
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        assertEquals("Device should be unknown", ThingStatus.UNKNOWN, thingStatusInfo.getStatus());
    }

    @Test
    public void testHandleCommandRefreshType() {
        handler.initialize();
        assertHandleCommandRefreshType(-53);
    }

    @Test
    public void testHandleCommandRefreshTypeRangeExtender() throws IOException {
        when(connection.sendCommand(Commands.getSysinfo()))
                .thenReturn(ModelTestUtil.readJson("rangeextender_get_sysinfo_response"));
        handler.initialize();
        assertHandleCommandRefreshType(-70);
    }

    private void assertHandleCommandRefreshType(int expectedRssi) {
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(CHANNEL_PREFIX + CHANNEL_RSSI);
        handler.handleCommand(channelUID, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channelUID), stateCaptor.capture());
        assertEquals("State of RSSI channel should be set", new DecimalType(expectedRssi), stateCaptor.getValue());
    }

    @Test
    public void testHandleCommandOther() throws InterruptedException {
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(CHANNEL_PREFIX + CHANNEL_SWITCH);
        Mockito.doReturn(OnOffType.ON).when(smartHomeDevice).updateChannel(eq(channelUID.getId()), any());
        handler.handleCommand(channelUID, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channelUID), stateCaptor.capture());
        assertSame("State of channel switch should be set", OnOffType.ON, stateCaptor.getValue());
    }

    @Test
    public void testRefreshChannels() {
        handler.initialize();
        handler.refreshChannels();
    }
}
