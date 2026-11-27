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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
import org.openhab.core.types.UnDefType;

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
        verify(callback).stateUpdated(channel("work-type"), new StringType("AUTOMATIC"));
    }

    @ParameterizedTest
    @CsvSource({ "AUTOMATIC,0,AUTOMATIC", "AUTO,0,AUTOMATIC", "TEMPORARY_MANUAL,1,TEMPORARY_MANUAL", "MANUAL,2,MANUAL",
            "OFF,7,OFF" })
    void setsAndRefreshesOperatingMode(String mode, long expected, String reportedMode) throws Exception {
        handler.handleCommand(channel("work-type"), new StringType(mode));
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:SetHoldType", expected);
        when(bridgeHandler.findPropertiesForDevice("device-123"))
                .thenReturn(new TreeSet<>(GsonMapper.INSTANCE.parseAwsDeviceProperties(
                        PROPERTIES.replace("\"ep9:sIT600TH:HoldType\":0", "\"ep9:sIT600TH:HoldType\":" + expected))));
        handler.handleCommand(channel("work-type"), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel("work-type"), new StringType(reportedMode));
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

    private ChannelUID channel(String id) {
        return new ChannelUID(thing.getUID(), id);
    }

    @ParameterizedTest
    @CsvSource({ "cooling-setpoint,CoolingSetpoint_x100,2400,24", "temperature-offset,TemperatureOffset,-150,-1.5",
            "frost-temperature,FrostSetpoint_x100,750,7.5", "minimum-setpoint,MinHeatSetpoint_x100,1500,15",
            "maximum-setpoint,MaxHeatSetpoint_x100,2500,25" })
    void writesAndReadsTemperatureSettings(String channel, String property, long raw, String temperature)
            throws Exception {
        report("\"ep9:sIT600TH:" + property + "\":" + raw);
        handler.handleCommand(channel(channel), new DecimalType(temperature));
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:Set" + property, raw);
        handler.handleCommand(channel(channel), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel(channel), QuantityType.valueOf(temperature + " °C"));
    }

    @ParameterizedTest
    @CsvSource({ "keypad-lock,LockKey", "allow-local-unlock,AllowUnlockFromDevice", "valve-protection,ValveProtection",
            "display-off,ShutOffDisplay" })
    void writesAndReadsSwitchSettings(String channel, String property) throws Exception {
        report("\"ep9:sIT600TH:" + property + "\":1");
        handler.handleCommand(channel(channel), OnOffType.ON);
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:Set" + property, 1L);
        handler.handleCommand(channel(channel), OnOffType.OFF);
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:Set" + property, 0L);
        handler.handleCommand(channel(channel), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel(channel), OnOffType.ON);
    }

    @ParameterizedTest
    @CsvSource({ "system-mode,SystemMode,HEAT,4", "system-mode,SystemMode,COOL,3", "system-mode,SystemMode,OFF,0",
            "display-unit,TemperatureDisplayMode,CELSIUS,0", "display-unit,TemperatureDisplayMode,FAHRENHEIT,1",
            "clock-format,TimeFormat24Hour,12H,0", "clock-format,TimeFormat24Hour,24H,1" })
    void writesAndReadsEnumSettings(String channel, String property, String command, long raw) throws Exception {
        report("\"ep9:sIT600TH:" + property + "\":" + raw);
        handler.handleCommand(channel(channel), new StringType(command));
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:Set" + property, raw);
        handler.handleCommand(channel(channel), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel(channel), new StringType(command));
    }

    @Test
    void convertsAbsoluteAndRelativeTemperatureUnits() throws Exception {
        report("\"ep9:sIT600TH:CoolingSetpoint_x100\":2400,\"ep9:sIT600TH:TemperatureOffset\":0");
        handler.handleCommand(channel("cooling-setpoint"), QuantityType.valueOf("77 °F"));
        handler.handleCommand(channel("temperature-offset"), QuantityType.valueOf("-2.7 °F"));
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:SetCoolingSetpoint_x100", 2500L);
        verify(bridgeHandler).setValueForProperty("device-123", "ep9:sIT600TH:SetTemperatureOffset", -150L);
    }

    @Test
    void rejectsInvalidSettingsAndInvertedLimits() throws Exception {
        report("""
                "ep9:sIT600TH:TemperatureOffset":0,"ep9:sIT600TH:FrostSetpoint_x100":500,
                "ep9:sIT600TH:MinHeatSetpoint_x100":1000,"ep9:sIT600TH:MaxHeatSetpoint_x100":3000,
                "ep9:sIT600TH:CoolingSetpoint_x100":2400,"ep9:sIT600TH:MinCoolSetpoint_x100":1800,
                "ep9:sIT600TH:MaxCoolSetpoint_x100":2800,"ep9:sIT600TH:SystemMode":4,
                "ep9:sIT600TH:TemperatureDisplayMode":0,"ep9:sIT600TH:TimeFormat24Hour":1,"ep9:sIT600TH:LockKey":0
                """);
        handler.handleCommand(channel("temperature-offset"), new DecimalType("3.5"));
        handler.handleCommand(channel("temperature-offset"), new DecimalType("0.25"));
        handler.handleCommand(channel("temperature-offset"), QuantityType.valueOf("5 m"));
        handler.handleCommand(channel("frost-temperature"), new DecimalType("18"));
        handler.handleCommand(channel("frost-temperature"), new DecimalType("7"));
        handler.handleCommand(channel("minimum-setpoint"), new DecimalType("31"));
        handler.handleCommand(channel("maximum-setpoint"), new DecimalType("9"));
        handler.handleCommand(channel("cooling-setpoint"), new DecimalType("17"));
        handler.handleCommand(channel("cooling-setpoint"), new DecimalType("29"));
        handler.handleCommand(channel("cooling-setpoint"), new DecimalType("24.001"));
        handler.handleCommand(channel("system-mode"), new StringType("AUTOMATIC"));
        handler.handleCommand(channel("display-unit"), new StringType("KELVIN"));
        handler.handleCommand(channel("clock-format"), new StringType("13H"));
        handler.handleCommand(channel("keypad-lock"), new DecimalType(1));
        verify(bridgeHandler, never()).setValueForProperty(anyString(), anyString(), any());
    }

    @Test
    void missingOptionalPropertiesDoNotWriteOrTakeThermostatOffline() throws Exception {
        handler.handleCommand(channel("keypad-lock"), OnOffType.ON);
        handler.handleCommand(channel("temperature-offset"), new DecimalType("1"));
        for (var id : new String[] { "keypad-lock", "temperature-offset", "system-mode", "rssi", "lqi",
                "active-errors" }) {
            handler.handleCommand(channel(id), RefreshType.REFRESH);
            verify(callback).stateUpdated(channel(id), UnDefType.UNDEF);
        }
        verify(bridgeHandler, never()).setValueForProperty(anyString(), anyString(), any());
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    void doesNotInterpretUnknownValuesAsValidStates() throws Exception {
        report("""
                "ep9:sIT600TH:LockKey":2,"ep9:sIT600TH:SystemMode":99,"ep9:sIT600TH:TemperatureOffset":32768,
                "ep9:sIT600TH:TimeFormat24Hour":2,"ep9:sIT600TH:TemperatureDisplayMode":2
                """);
        for (var id : new String[] { "keypad-lock", "system-mode", "temperature-offset", "clock-format",
                "display-unit" }) {
            handler.handleCommand(channel(id), RefreshType.REFRESH);
            verify(callback).stateUpdated(channel(id), UnDefType.UNDEF);
        }
    }

    @Test
    void reportsAndClearsErrorsWithoutConfusingTrvIndexWithAnError() throws Exception {
        report("\"ep9:sIT600TH:Error07TRVIndex\":3,\"ep9:sIT600TH:Error07\":1,\"ep9:sIT600TH:Error02\":1");
        handler.handleCommand(channel("active-errors"), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel("active-errors"), new StringType("Error02, Error07"));
        report("\"ep9:sIT600TH:Error02\":0,\"ep9:sIT600TH:Error07\":0");
        handler.handleCommand(channel("active-errors"), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel("active-errors"), new StringType("NONE"));
        report("\"ep9:sIT600TH:Error02\":2");
        handler.handleCommand(channel("active-errors"), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel("active-errors"), UnDefType.UNDEF);
    }

    @Test
    void reportsRadioDiagnosticsAndIgnoresWrites() throws Exception {
        report("\"ep9:sIT600I:LastMessageRSSI_d\":-75,\"ep9:sIT600I:LastMessageLQI_d\":255");
        handler.handleCommand(channel("rssi"), RefreshType.REFRESH);
        handler.handleCommand(channel("lqi"), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel("rssi"), QuantityType.valueOf("-75 dBm"));
        verify(callback).stateUpdated(channel("lqi"), new DecimalType(255));
        handler.handleCommand(channel("rssi"), new DecimalType(-50));
        handler.handleCommand(channel("lqi"), new DecimalType(200));
        handler.handleCommand(channel("active-errors"), new StringType("NONE"));
        verify(bridgeHandler, never()).setValueForProperty(anyString(), anyString(), any());
    }

    @Test
    void optionalSettingsRetainLegacyEndpointSpelling() throws Exception {
        report("\"ep_9:sIT600TH:TemperatureOffset\":0");
        handler.handleCommand(channel("temperature-offset"), new DecimalType("-1"));
        verify(bridgeHandler).setValueForProperty("device-123", "ep_9:sIT600TH:SetTemperatureOffset", -100L);
    }

    @Test
    void optionalRefreshRecoversAfterCommunicationFailure() throws Exception {
        when(bridgeHandler.findPropertiesForDevice("device-123")).thenThrow(new SalusApiException("Unavailable"));
        handler.handleCommand(channel("keypad-lock"), RefreshType.REFRESH);
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        report("\"ep9:sIT600TH:LockKey\":0");
        handler.handleCommand(channel("keypad-lock"), RefreshType.REFRESH);
        verify(callback).stateUpdated(channel("keypad-lock"), OnOffType.OFF);
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    private void report(String properties) throws Exception {
        var json = "{\"state\":{\"reported\":{\"11\":{\"properties\":{" + properties + "}}}}}";
        doReturn(new TreeSet<>(GsonMapper.INSTANCE.parseAwsDeviceProperties(json))).when(bridgeHandler)
                .findPropertiesForDevice("device-123");
    }
}
