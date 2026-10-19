/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal.handler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.salus.internal.aws.handler.AwsCloudBridgeHandler;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class DeviceHandlerTest {
    @ParameterizedTest
    @CsvSource({ "SetCoolingSetpoint,SetCoolingSetpoint_x100,24.5,2450", "SetLockKey,SetLockKey,1,1" })
    void exposesAndWritesAwsThermostatCommands(String channelSuffix, String propertySuffix, String command,
            long expected) throws Exception {
        var bridgeUID = new ThingUID("salus", "salus-aws-bridge", "test");
        var thing = ThingBuilder.create(new ThingTypeUID("salus", "salus-device"), "thermostat").withBridge(bridgeUID)
                .withConfiguration(new Configuration(Map.of("dsn", "device-123"))).build();
        var bridge = mock(Bridge.class);
        var api = mock(AwsCloudBridgeHandler.class);
        var callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(api);
        when(api.findDevice("device-123"))
                .thenReturn(Optional.of(new Device("device-123", "Thermostat", true, Map.of())));
        when(api.findPropertiesForDevice("device-123"))
                .thenReturn(new TreeSet<>(GsonMapper.INSTANCE.parseAwsDeviceProperties("""
                        {"state":{"reported":{"11":{"properties":{
                          "ep9:sIT600TH:CoolingSetpoint_x100":2400,
                          "ep9:sIT600TH:LockKey":0
                        }}}}}
                        """)));
        var handler = new DeviceHandler(thing);
        handler.setCallback(callback);
        handler.initialize();

        var channel = new ChannelUID(thing.getUID(), "ep9_sIT600TH_" + channelSuffix);
        verify(callback)
                .thingUpdated(argThat(updated -> updated.getChannels().stream().anyMatch(c -> c.getUID().equals(channel)
                        && new ChannelTypeUID("salus", "generic-input-number-channel").equals(c.getChannelTypeUID()))));
        handler.handleCommand(channel, new DecimalType(command));
        verify(api).setValueForProperty("device-123", "ep9:sIT600TH:" + propertySuffix, expected);
    }
}
