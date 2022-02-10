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
package org.openhab.binding.tplinksmarthome.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_SWITCH;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeDiscoveryService;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * Tests cases for {@link SmartHomeHandler} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
public class SmartHomeHandlerTest {

    private @NonNullByDefault({}) SmartHomeHandler handler;

    private @Mock @NonNullByDefault({}) Connection connection;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) SmartHomeDevice smartHomeDevice;
    private @Mock @NonNullByDefault({}) TPLinkSmartHomeDiscoveryService discoveryService;

    private final Configuration configuration = new Configuration();

    @BeforeEach
    public void setUp() throws IOException {
        configuration.put(CONFIG_IP, "localhost");
        configuration.put(CONFIG_REFRESH, 1);
        when(thing.getConfiguration()).thenReturn(configuration);
        lenient().when(smartHomeDevice.getUpdateCommand()).thenReturn(Commands.getSysinfo());
        lenient().when(connection.sendCommand(Commands.getSysinfo()))
                .thenReturn(ModelTestUtil.readJson("plug_get_sysinfo_response"));
        handler = new SmartHomeHandler(thing, smartHomeDevice, TPLinkSmartHomeThingType.HS100, discoveryService) {
            @Override
            Connection createConnection(TPLinkSmartHomeConfiguration config) {
                return connection;
            }
        };
        lenient().when(smartHomeDevice.handleCommand(eq(CHANNEL_UID_SWITCH), any())).thenReturn(true);
        lenient().when(callback.isChannelLinked(any())).thenReturn(true);
        handler.setCallback(callback);
    }

    @AfterEach
    public void after() {
        handler.dispose();
    }

    @Test
    public void testInitializeShouldCallTheCallback() throws InterruptedException {
        handler.initialize();
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        assertEquals(ThingStatus.UNKNOWN, thingStatusInfo.getStatus(), "Device should be unknown");
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
        ChannelUID channelUID = ChannelUIDConstants.CHANNEL_UID_RSSI;
        handler.handleCommand(channelUID, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channelUID), stateCaptor.capture());
        assertEquals(new QuantityType<>(expectedRssi + " dBm"), stateCaptor.getValue(),
                "State of RSSI channel should be set");
    }

    @Test
    public void testHandleCommandOther() throws InterruptedException {
        handler.initialize();
        ChannelUID channelUID = ChannelUIDConstants.CHANNEL_UID_SWITCH;
        Mockito.doReturn(OnOffType.ON).when(smartHomeDevice).updateChannel(eq(channelUID), any());
        handler.handleCommand(channelUID, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channelUID), stateCaptor.capture());
        assertSame(OnOffType.ON, stateCaptor.getValue(), "State of channel switch should be set");
    }

    @Test
    public void testRefreshChannels() {
        handler.initialize();
        handler.refreshChannels();
    }
}
