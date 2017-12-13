/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.handler;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Tests cases for {@link SmartHomeHandler} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@SuppressWarnings("null")
public class SmartHomeHandlerTest {

    private static final String CHANNEL_PREFIX = "binding:tplinksmarthome:1234:";

    private ThingHandler handler;

    @Mock
    private Connection connection;
    @Mock
    private ThingHandlerCallback callback;
    @Mock
    private Thing thing;
    @Mock
    private SmartHomeDevice smartHomeDevice;

    private final Configuration configuration = new Configuration();

    @Before
    public void setUp() throws IOException {
        initMocks(this);
        configuration.put(TPLinkSmartHomeBindingConstants.CONFIG_IP, "localhost");
        configuration.put(TPLinkSmartHomeBindingConstants.CONFIG_REFRESH, 300);
        when(thing.getConfiguration()).thenReturn(configuration);
        when(smartHomeDevice.getUpdateCommand()).thenReturn(Commands.getSysinfo());
        when(connection.sendCommand(Commands.getSysinfo()))
                .thenReturn(ModelTestUtil.readJson("plug_get_sysinfo_response"));
        handler = new SmartHomeHandler(thing, smartHomeDevice) {
            @Override
            Connection createConnection(TPLinkSmartHomeConfiguration config) {
                return connection;
            }
        };
        when(smartHomeDevice.handleCommand(eq(CHANNEL_SWITCH), eq(connection), any(), any())).thenReturn(true);
        handler.setCallback(callback);
        handler.initialize();
    }

    @After
    public void after() {
        handler.dispose();
    }

    @Test
    public void testInitializeShouldCallTheCallback() throws InterruptedException {
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        assertEquals("Device should be unknown", ThingStatus.UNKNOWN, thingStatusInfo.getStatus());
    }

    @Test
    public void testHhandleCommandRefreshType() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_PREFIX + CHANNEL_RSSI);
        handler.handleCommand(channelUID, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channelUID), stateCaptor.capture());
        assertEquals("State of RSSI channel should be set", new DecimalType(-53), stateCaptor.getValue());
    }

    @Test
    public void testHhandleCommandOther() throws InterruptedException {
        ChannelUID channelUID = new ChannelUID(CHANNEL_PREFIX + CHANNEL_SWITCH);
        Mockito.doReturn(OnOffType.ON).when(smartHomeDevice).updateChannel(eq(channelUID.getId()), any());
        handler.handleCommand(channelUID, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channelUID), stateCaptor.capture());
        assertSame("State of channel switch should be set", OnOffType.ON, stateCaptor.getValue());
    }
}
