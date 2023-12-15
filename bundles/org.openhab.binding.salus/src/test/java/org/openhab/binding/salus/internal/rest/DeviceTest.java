package org.openhab.binding.salus.internal.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("EqualsWithItself")
class DeviceTest {

    // Returns true if 'connection_status' property exists and is set to 'online'
    @Test
    @DisplayName("Returns true if 'connection_status' property exists and is set to 'online'")
    public void test_returns_true_if_connection_status_property_exists_and_is_set_to_online() {
        // Given
        var properties = new HashMap<String, Object>();
        properties.put("connection_status", "online");
        var device = new Device("dsn", "name", properties);

        // When
        var result = device.isConnected();

        // Then
        assertThat(result).isTrue();
    }

    // Returns false if 'connection_status' property exists and is not set to 'online'
    @Test
    @DisplayName("Returns false if 'connection_status' property exists and is not set to 'online'")
    public void test_returns_false_if_connection_status_property_exists_and_is_not_set_to_online() {
        // Given
        var properties = new HashMap<String, Object>();
        properties.put("connection_status", "offline");
        var device = new Device("dsn", "name", properties);

        // When
        var result = device.isConnected();

        // Then
        assertThat(result).isFalse();
    }

    // Returns false if 'connection_status' property does not exist
    @Test
    @DisplayName("Returns false if 'connection_status' property does not exist")
    public void test_returns_false_if_connection_status_property_does_not_exist() {
        // Given
        var properties = new HashMap<String, Object>();
        var device = new Device("dsn", "name", properties);

        // When
        var result = device.isConnected();

        // Then
        assertThat(result).isFalse();
    }

    // Returns false if 'properties' parameter does not contain 'connection_status' key
    @Test
    @DisplayName("Returns false if 'properties' parameter does not contain 'connection_status' key")
    public void test_returns_false_if_properties_parameter_does_not_contain_connection_status_key() {
        // Given
        var properties = new HashMap<String, Object>();
        var device = new Device("dsn", "name", properties);

        // When
        var result = device.isConnected();

        // Then
        assertThat(result).isFalse();
    }

    // Returns false if 'connection_status' property is null
    @Test
    @DisplayName("Returns false if 'connection_status' property is null")
    public void test_returns_false_if_connection_status_property_is_null() {
        // Given
        var properties = new HashMap<String, Object>();
        properties.put("connection_status", null);
        var device = new Device("dsn", "name", properties);

        // When
        var result = device.isConnected();

        // Then
        assertThat(result).isFalse();
    }

    // Returns false if 'connection_status' property is not a string
    @Test
    @DisplayName("Returns false if 'connection_status' property is not a string")
    public void test_returns_false_if_connection_status_property_is_not_a_string() {
        // Given
        var properties = new HashMap<String, Object>();
        properties.put("connection_status", 123);
        var device = new Device("dsn", "name", properties);

        // When
        var result = device.isConnected();

        // Then
        assertThat(result).isFalse();
    }

    // Creating a new Device object with valid parameters should succeed.
    @Test
    @DisplayName("Creating a new Device object with valid parameters should succeed")
    public void test_creating_new_device_with_valid_parameters_should_succeed() {
        // Given
        String dsn = "123456";
        String name = "Device 1";
        Map<String, Object> properties = Map.of("connection_status", "online");

        // When
        Device device = new Device(dsn, name, properties);

        // Then
        assertThat(device).isNotNull();
        assertThat(device.dsn()).isEqualTo(dsn);
        assertThat(device.name()).isEqualTo(name);
        assertThat(device.properties()).isEqualTo(properties);
    }

    // Two Device objects with the same DSN should be considered equal.
    @Test
    @DisplayName("Two Device objects with the same DSN should be considered equal")
    public void test_two_devices_with_same_dsn_should_be_equal() {
        // Given
        String dsn = "123456";
        String name1 = "Device 1";
        String name2 = "Device 2";
        Map<String, Object> properties = Map.of("connection_status", "online");

        Device device1 = new Device(dsn, name1, properties);
        Device device2 = new Device(dsn, name2, properties);

        // When
        boolean isEqual = device1.equals(device2);

        // Then
        assertThat(isEqual).isTrue();
    }

    // The compareTo method should correctly compare two Device objects based on their DSNs.
    @Test
    @DisplayName("The compareTo method should correctly compare two Device objects based on their DSNs")
    public void test_compare_to_method_should_correctly_compare_devices_based_on_dsn() {
        // Given
        String dsn1 = "123456";
        String dsn2 = "654321";
        String name = "Device";
        Map<String, Object> properties = Map.of("connection_status", "online");

        Device device1 = new Device(dsn1, name, properties);
        Device device2 = new Device(dsn2, name, properties);

        // When
        int result1 = device1.compareTo(device2);
        int result2 = device2.compareTo(device1);
        int result3 = device1.compareTo(device1);

        // Then
        assertThat(result1).isNegative();
        assertThat(result2).isPositive();
        assertThat(result3).isZero();
    }

    // The isConnected method should return true if the connection_status property is "online".
    @Test
    @DisplayName("The isConnected method should return true if the connection_status property is \"online\"")
    public void test_is_connected_method_should_return_true_if_connection_status_is_online() {
        // Given
        String dsn = "123456";
        String name = "Device";
        Map<String, Object> properties1 = Map.of("connection_status", "online");
        Map<String, Object> properties2 = Map.of("connection_status", "offline");

        Device device1 = new Device(dsn, name, properties1);
        Device device2 = new Device(dsn, name, properties2);

        // When
        boolean isConnected1 = device1.isConnected();
        boolean isConnected2 = device2.isConnected();

        // Then
        assertThat(isConnected1).isTrue();
        assertThat(isConnected2).isFalse();
    }

    // The toString method should return a string representation of the Device object with its DSN and name.
    @Test
    @DisplayName("The toString method should return a string representation of the Device object with its DSN and name")
    public void test_to_string_method_should_return_string_representation_with_dsn_and_name() {
        // Given
        String dsn = "123456";
        String name = "Device";
        Map<String, Object> properties = Map.of("connection_status", "online");

        Device device = new Device(dsn, name, properties);

        // When
        String result = device.toString();

        // Then
        assertThat(result).isEqualTo("Device{dsn='123456', name='Device'}");
    }
}
