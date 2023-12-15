/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * @author Martin Grześlowski - Initial contribution
 */
public class GsonMapperTest {

    // Can serialize login parameters to JSON
    @Test
    public void test_serialize_login_parameters_to_json() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String username = "test@example.com";
        char[] password = "password".toCharArray();
        String expectedJson1 = "{\"user\":{\"email\":\"test@example.com\",\"password\":\"password\"}}";
        String expectedJson2 = "{\"user\":{\"password\":\"password\",\"email\":\"test@example.com\"}}";

        // When
        String json = gsonMapper.loginParam(username, password);

        // Then
        assertThat(json).isIn(expectedJson1, expectedJson2);
    }

    // Can deserialize authentication token from JSON
    @Test
    public void test_deserialize_authentication_token_from_json() {
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
    public void test_parse_list_of_devices_from_json() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String json = "[{\"device\":{\"dsn\":\"123\",\"product_name\":\"Product 1\"}},{\"device\":{\"dsn\":\"456\",\"product_name\":\"Product 2\"}}]";
        List<Device> expectedDevices = List.of(new Device("123", "Product 1", Collections.emptyMap()),
                new Device("456", "Product 2", Collections.emptyMap()));

        // When
        List<Device> devices = gsonMapper.parseDevices(json);

        // Then
        assertThat(devices).isEqualTo(expectedDevices);
    }

    // Returns empty list when parsing invalid JSON for devices
    @Test
    public void test_returns_empty_list_when_parsing_invalid_json_for_devices() {
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
    public void test_returns_empty_list_when_parsing_invalid_json_for_device_properties() {
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
    public void test_returns_empty_optional_when_parsing_invalid_json_for_datapoint_value() {
        // Given
        GsonMapper gsonMapper = GsonMapper.INSTANCE;
        String json = "invalid json";

        // When
        Optional<Object> datapointValue = gsonMapper.datapointValue(json);

        // Then
        assertThat(datapointValue).isEmpty();
    }
}
