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
package org.openhab.binding.salus.internal.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.salus.internal.cloud.rest.AuthToken;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

import com.google.gson.JsonParser;

/**
 * @author Martin Grześlowski - Initial contribution
 * @author Martin Grześlowski - AWS command mapping tests
 */
@NonNullByDefault
public class GsonMapperTest {

    // Can serialize login parameters to JSON
    @Test
    public void testSerializeLoginParametersToJson() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String username = "test@example.com";
        byte[] password = "password".getBytes(UTF_8);
        String expectedJson1 = "{\"user\":{\"email\":\"test@example.com\",\"password\":\"password\"}}";
        String expectedJson2 = "{\"user\":{\"password\":\"password\",\"email\":\"test@example.com\"}}";

        // When
        String json = gsonMapper.loginParam(username, password);

        // Then
        assertThat(json).isIn(expectedJson1, expectedJson2);
    }

    // Can deserialize authentication token from JSON
    @Test
    public void testDeserializeAuthenticationTokenFromJson() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String json = "{\"access_token\":\"token\",\"refresh_token\":\"refresh\",\"expires_in\":3600,\"role\":\"admin\"}";
        AuthToken expectedAuthToken = new AuthToken("token", "refresh", 3600L, "admin");

        // When
        AuthToken authToken = gsonMapper.authToken(json);

        // Then
        assertThat(authToken).isEqualTo(expectedAuthToken);
    }

    // Can parse list of devices from JSON
    @Test
    public void testParseListOfDevicesFromJson() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String json = "[{\"device\":{\"dsn\":\"123\",\"product_name\":\"Product 1\"}},{\"device\":{\"dsn\":\"456\",\"product_name\":\"Product 2\"}}]";
        List<Device> expectedDevices = List.of(new Device("123", "Product 1", true, Collections.emptyMap()),
                new Device("456", "Product 2", true, Collections.emptyMap()));

        // When
        List<Device> devices = gsonMapper.parseDevices(json);

        // Then
        assertThat(devices).isEqualTo(expectedDevices);
    }

    // Returns empty list when parsing invalid JSON for devices
    @Test
    public void testReturnsEmptyListWhenParsingInvalidJsonForDevices() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String json = "invalid json";

        // When
        List<Device> devices = gsonMapper.parseDevices(json);

        // Then
        assertThat(devices).isEmpty();
    }

    // Returns empty list when parsing invalid JSON for device properties
    @Test
    public void testReturnsEmptyListWhenParsingInvalidJsonForDeviceProperties() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String json = "invalid json";

        // When
        List<DeviceProperty<?>> deviceProperties = gsonMapper.parseDeviceProperties(json);

        // Then
        assertThat(deviceProperties).isEmpty();
    }

    // Returns empty optional when parsing invalid JSON for datapoint value
    @Test
    public void testReturnsEmptyOptionalWhenParsingInvalidJsonForDatapointValue() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String json = "invalid json";

        // When
        Optional<Object> datapointValue = gsonMapper.datapointValue(json);

        // Then
        assertThat(datapointValue).isEmpty();
    }

    @Test
    void exposesVerifiedAwsCommandsWithoutMakingReadbacksWritable() {
        var properties = GsonMapper.INSTANCE.parseAwsDeviceProperties("""
                {"state":{"reported":{"11":{"properties":{
                  "ep9:sIT600TH:HeatingSetpoint_x100":2100,
                  "ep9:sIT600TH:CoolingSetpoint_x100":2400,
                  "ep9:sIT600TH:HoldType":0,
                  "ep9:sIT600TH:LockKey":0,
                  "ep9:sIT600TH:LocalTemperature_x100":2050,
                  "ep9:sIT600TH:RunningState":1,
                  "ep9:sIT600TH:SetFactoryDefaultReset":0,
                  "ep9:sIT600TH:Unavailable":null
                }}}}}
                """);
        assertThat(properties.stream().filter(p -> !p.getReadOnly()).map(DeviceProperty::getName)).containsExactly(
                "ep9:sIT600TH:SetHeatingSetpoint_x100", "ep9:sIT600TH:SetCoolingSetpoint_x100",
                "ep9:sIT600TH:SetHoldType", "ep9:sIT600TH:SetLockKey");
        assertThat(properties)
                .allSatisfy(p -> assertThat(p.getDirection()).isEqualTo(p.getReadOnly() ? "output" : "input"));
        assertThat(properties).hasSize(11);
    }

    @Test
    void preservesEndpointAndAvoidsDuplicateAwsCommands() {
        var properties = GsonMapper.INSTANCE.parseAwsDeviceProperties("""
                {"state":{"reported":{"11":{"properties":{
                  "ep_9:sIT600TH:HeatingSetpoint_x100":2100,
                  "ep_9:sIT600TH:SetHeatingSetpoint_x100":2200,
                  "ep9:sIT600TH:HeatingSetpoint_x100_a":2300
                }}}}}
                """);
        assertThat(properties).hasSize(3);
        assertThat(properties.stream().filter(p -> !p.getReadOnly()).map(DeviceProperty::getName))
                .containsExactly("ep_9:sIT600TH:SetHeatingSetpoint_x100");
    }

    @Test
    void serializesAwsThermostatCommandsAsNumericDesiredState() throws Exception {
        var json = GsonMapper.INSTANCE.awsShadowUpdate("ep9:sIT600TH:SetHoldType", 2L);
        assertThat(JsonParser.parseString(json)).isEqualTo(JsonParser.parseString("""
                {"state":{"desired":{"11":{"properties":{"ep9:sIT600TH:SetHoldType":2}}}}}
                """));
    }

    @Test
    void rejectsUnsupportedOrMalformedAwsCommands() {
        var mapper = GsonMapper.INSTANCE;
        for (var property : List.of("ep9:sIT600TH:HeatingSetpoint_x100", "ep9:sIT600TH:SetLocalTemperature_x100",
                "ep9:sOther:SetHoldType", "ep9:sIT600TH:SetHoldType_a")) {
            assertThatThrownBy(() -> mapper.awsShadowUpdate(property, 1L)).isInstanceOf(SalusApiException.class);
        }
        for (var value : List.of("2150", true, 2.5, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThatThrownBy(() -> mapper.awsShadowUpdate("ep9:sIT600TH:SetHeatingSetpoint_x100", value))
                    .isInstanceOf(SalusApiException.class);
        }
        assertThatThrownBy(() -> mapper.awsShadowUpdate("ep9:sIT600TH:SetHoldType", 3))
                .isInstanceOf(SalusApiException.class);
        assertThatThrownBy(() -> mapper.awsShadowUpdate("ep9:sIT600TH:SetLockKey", 2))
                .isInstanceOf(SalusApiException.class);
    }

    @ParameterizedTest
    @CsvSource({ "TemperatureOffset,-300", "TemperatureOffset,300", "FrostSetpoint_x100,500", "FrostSetpoint_x100,1700",
            "MinHeatSetpoint_x100,500", "MaxHeatSetpoint_x100,3500", "AllowUnlockFromDevice,1", "ValveProtection,0",
            "ShutOffDisplay,1", "TemperatureDisplayMode,1", "TimeFormat24Hour,1", "SystemMode,0", "SystemMode,3",
            "SystemMode,4" })
    void exposesAndSerializesAdditionalSettings(String suffix, long value) throws Exception {
        var property = "ep9:sIT600TH:" + suffix;
        var setter = "ep9:sIT600TH:Set" + suffix;
        var properties = GsonMapper.INSTANCE.parseAwsDeviceProperties("""
                {"state":{"reported":{"11":{"properties":{"%s":%d}}}}}
                """.formatted(property, value));
        assertThat(properties.stream().filter(p -> !p.getReadOnly()).map(DeviceProperty::getName))
                .containsExactly(setter);
        var json = GsonMapper.INSTANCE.awsShadowUpdate(setter, value);
        assertThat(JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("state").getAsJsonObject("desired")
                .getAsJsonObject("11").getAsJsonObject("properties").get(setter).getAsLong()).isEqualTo(value);
    }

    @ParameterizedTest
    @CsvSource({ "TemperatureOffset,-350", "TemperatureOffset,350", "TemperatureOffset,125", "FrostSetpoint_x100,499",
            "FrostSetpoint_x100,1750", "MinHeatSetpoint_x100,450", "MaxHeatSetpoint_x100,3550",
            "MinHeatSetpoint_x100,525", "AllowUnlockFromDevice,2", "ValveProtection,-1", "ShutOffDisplay,2",
            "TemperatureDisplayMode,2", "TimeFormat24Hour,24", "SystemMode,1", "SystemMode,7" })
    void rejectsInvalidAdditionalSettings(String suffix, long value) {
        assertThatThrownBy(() -> GsonMapper.INSTANCE.awsShadowUpdate("ep9:sIT600TH:Set" + suffix, value))
                .isInstanceOf(SalusApiException.class);
    }
}
