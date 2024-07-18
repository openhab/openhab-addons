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
package org.openhab.binding.insteon.internal.handler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.insteon.internal.InsteonBindingLegacyConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * Tests for the HomeWizard Handler
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class InsteonNetworkHandlerTest {

    private static final Configuration CONFIG = createConfig();

    private static Configuration createConfig() {
        final Configuration config = new Configuration();
        config.put("port", "1234");
        config.put("devicePollIntervalSeconds", "2");
        return config;
    }

    private static Bridge mockBridge() {
        final Bridge thing = mock(Bridge.class);
        when(thing.getUID())
                .thenReturn(new ThingUID(InsteonBindingLegacyConstants.NETWORK_THING_TYPE, "insteon-test-network"));
        when(thing.getConfiguration()).thenReturn(CONFIG);

        final List<Channel> channelList = Arrays
                .asList(mockChannel(thing.getUID(), InsteonBindingLegacyConstants.LIGHT_DIMMER));

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    private static InsteonNetworkHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing, boolean createNullSerial) {
        final SerialPortManager serialPortManager = mock(SerialPortManager.class);

        SerialPortIdentifier spi = mock(SerialPortIdentifier.class);
        SerialPort sp = mock(SerialPort.class);
        try {
            when(spi.open(anyString(), anyInt())).thenReturn(sp);
        } catch (PortInUseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        when(serialPortManager.getIdentifier(anyString())).thenReturn(spi);

        final InsteonNetworkHandlerMock handler = spy(
                new InsteonNetworkHandlerMock((Bridge) thing, createNullSerial ? null : serialPortManager));

        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    private static State getState(final int input) {
        return new DecimalType(input);
    }

    private static State getState(final int input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    private static State getState(final double input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    @Test
    public void testNullSerialPortManager() {
        final Thing thing = mockBridge();
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final InsteonNetworkHandlerMock handler = createAndInitHandler(callback, thing, true);
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNoConnection() {
        final Thing thing = mockBridge();
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final InsteonNetworkHandlerMock handler = createAndInitHandler(callback, thing, false);
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUpdateChannels() {
        // FIX-ME Fails due to not able to load xml file, while it is present ?! this has not changed
        // https://github.com/openhab/openhab-addons/blob/12a1f76c3ce9da812a26458ecfddfa36b360274d/bundles/org.openhab.binding.insteon/src/main/java/org/openhab/binding/insteon/internal/message/Msg.java#L125
        final Thing thing = mockBridge();
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final InsteonNetworkHandlerMock handler = createAndInitHandler(callback, thing, false);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(new ChannelUID(thing.getUID(), InsteonBindingLegacyConstants.LIGHT_DIMMER),
                    getState(567.0, Units.AMPERE));
        } finally {
            handler.dispose();
        }
    }
}
