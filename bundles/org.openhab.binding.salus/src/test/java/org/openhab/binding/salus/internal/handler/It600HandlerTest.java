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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.salus.internal.aws.handler.AwsCloudBridgeHandler;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class It600HandlerTest {
    private static final String PROPERTIES = """
            {"state":{"reported":{"11":{"properties":{
              "ep9:sIT600TH:LocalTemperature_x100":2050,
              "ep9:sIT600TH:HeatingSetpoint_x100":2100,
              "ep9:sIT600TH:HoldType":0,
              "ep9:sIT600TH:RunningState":1
            }}}}}
            """;
    private final AwsCloudBridgeHandler bridgeHandler = mock(AwsCloudBridgeHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final ThingUID bridgeUID = new ThingUID("salus", "salus-aws-bridge", "test");
    private final Thing thing = ThingBuilder.create(new ThingTypeUID("salus", "salus-it600-device"), "thermostat")
            .withBridge(bridgeUID).withConfiguration(new Configuration(Map.of("dsn", "device-123"))).build();
    private final It600Handler handler = new It600Handler(thing);

    @BeforeEach
    void initialize() throws Exception {
        var bridge = mock(Bridge.class);
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(bridgeHandler.channelPrefix()).thenReturn("ep9");
        when(bridgeHandler.findDevice("device-123"))
                .thenReturn(Optional.of(new Device("device-123", "Thermostat", true, Map.of())));
        when(bridgeHandler.findPropertiesForDevice("device-123"))
                .thenReturn(new TreeSet<>(GsonMapper.INSTANCE.parseAwsDeviceProperties(PROPERTIES)));
        when(bridgeHandler.setValueForProperty(anyString(), anyString(), any())).thenReturn(true);
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback, bridgeHandler);
    }

    static Stream<Arguments> temperatures() {
        return Stream.of(Arguments.of(new DecimalType("21.5"), 2150L),
                Arguments.of(QuantityType.valueOf("21.5 °C"), 2150L),
                Arguments.of(QuantityType.valueOf("68 °F"), 2000L));
    }

    @ParameterizedTest
    @MethodSource("temperatures")
    void setsTemperatureEvenWithoutReportedSetter(Command command, long expected) throws Exception {
        handler.handleCommand(channel("expected-temperature"), command);
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:SetHeatingSetpoint_x100", expected);
        handler.handleCommand(channel("work-type"), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel("work-type"), new StringType("AUTO"));
    }

    @ParameterizedTest
    @CsvSource({ "AUTO,0", "TEMPORARY_MANUAL,1", "MANUAL,2", "OFF,7" })
    void setsOperatingMode(String mode, long expected) throws Exception {
        handler.handleCommand(channel("work-type"), new StringType(mode));
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:SetHoldType", expected);
    }

    @Test
    void refreshesTemperatureWithUnitWithoutWriting() throws Exception {
        handler.handleCommand(channel("temperature"), RefreshType.REFRESH);
        handler.handleCommand(channel("expected-temperature"), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel("temperature"), QuantityType.valueOf("20.5 °C"));
        verify(callback).stateUpdated(channel("expected-temperature"), QuantityType.valueOf("21 °C"));
        verify(bridgeHandler, never()).setValueForProperty(anyString(), anyString(), any());
    }

    @Test
    void rejectsIncompatibleUnitsAndInvalidMode() throws Exception {
        handler.handleCommand(channel("expected-temperature"), QuantityType.valueOf("5 m"));
        handler.handleCommand(channel("work-type"), new StringType("UNKNOWN"));
        handler.handleCommand(channel("temperature"), new DecimalType(22));
        verify(bridgeHandler, never()).setValueForProperty(anyString(), anyString(), any());
    }

    @Test
    void doesNotRefreshTemperatureFromSetterOrAttributeVariant() throws Exception {
        when(bridgeHandler.findPropertiesForDevice("device-123"))
                .thenReturn(new TreeSet<>(GsonMapper.INSTANCE.parseAwsDeviceProperties("""
                        {"state":{"reported":{"11":{"properties":{
                          "ep1:sIT600TH:SetHeatingSetpoint_x100":2300,
                          "ep1:sIT600TH:HeatingSetpoint_x100_a":2400
                        }}}}}
                        """)));
        handler.handleCommand(channel("expected-temperature"), RefreshType.REFRESH);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void acceptsLegacyEndpointSpelling() throws Exception {
        when(bridgeHandler.findPropertiesForDevice("device-123")).thenReturn(
                new TreeSet<>(GsonMapper.INSTANCE.parseAwsDeviceProperties(PROPERTIES.replace("ep9:", "ep_9:"))));
        handler.handleCommand(channel("expected-temperature"), new DecimalType("21.5"));
        verify(bridgeHandler).setValueForProperty("device-123", "ep_9:sIT600TH:SetHeatingSetpoint_x100", 2150L);
    }

    @Test
    void recoversAfterFailedWrite() throws Exception {
        when(bridgeHandler.setValueForProperty(anyString(), anyString(), any()))
                .thenThrow(new SalusApiException("Write failed")).thenReturn(true);
        handler.handleCommand(channel("expected-temperature"), new DecimalType("21.5"));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        handler.handleCommand(channel("expected-temperature"), new DecimalType("21.5"));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void awsBridgeAllowsCommands() {
        var bridge = new AwsCloudBridgeHandler(mock(Bridge.class), mock(HttpClientFactory.class));
        assertThat(bridge.isReadOnly()).isFalse();
    }

    private ChannelUID channel(String id) {
        return new ChannelUID(thing.getUID(), id);
    }
}
