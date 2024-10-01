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
package org.openhab.binding.salus.internal.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.salus.internal.cloud.rest.AuthToken;

/**
 * @author Martin Grześlowski - Initial contribution
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
}
