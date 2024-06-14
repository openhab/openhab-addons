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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@SuppressWarnings("EqualsWithItself")
@NonNullByDefault
class DeviceTest {

    // Returns true if 'connection_status' property exists and is set to 'online'
    @Test
    @DisplayName("Returns true if 'connection_status' property exists and is set to 'online'")
    public void testReturnsTrueIfConnectionStatusPropertyExistsAndIsSetToOnline() {
        // Given
        var properties = new HashMap<String, @Nullable Object>();
        properties.put("connection_status", "online");
        var device = new Device("dsn", "name", true, properties);

        // When
        var result = device.connected();

        // Then
        assertThat(result).isTrue();
    }

    // Returns false if 'connection_status' property exists and is not set to 'online'
    @Test
    @DisplayName("Returns false if 'connection_status' property exists and is not set to 'online'")
    public void testReturnsFalseIfConnectionStatusPropertyExistsAndIsNotSetToOnline() {
        // Given
        var properties = new HashMap<String, @Nullable Object>();
        properties.put("connection_status", "offline");
        var device = new Device("dsn", "name", false, properties);

        // When
        var result = device.connected();

        // Then
        assertThat(result).isFalse();
    }

    // Returns false if 'connection_status' property does not exist
    @Test
    @DisplayName("Returns false if 'connection_status' property does not exist")
    public void testReturnsFalseIfConnectionStatusPropertyDoesNotExist() {
        // Given
        var properties = new HashMap<String, @Nullable Object>();
        var device = new Device("dsn", "name", false, properties);

        // When
        var result = device.connected();

        // Then
        assertThat(result).isFalse();
    }

    // Returns false if 'properties' parameter does not contain 'connection_status' key
    @Test
    @DisplayName("Returns false if 'properties' parameter does not contain 'connection_status' key")
    public void testReturnsFalseIfPropertiesParameterDoesNotContainConnectionStatusKey() {
        // Given
        var properties = new HashMap<String, @Nullable Object>();
        var device = new Device("dsn", "name", false, properties);

        // When
        var result = device.connected();

        // Then
        assertThat(result).isFalse();
    }

    // Returns false if 'connection_status' property is null
    @Test
    @DisplayName("Returns false if 'connection_status' property is null")
    public void testReturnsFalseIfConnectionStatusPropertyIsNull() {
        // Given
        var properties = new HashMap<String, @Nullable Object>();
        properties.put("connection_status", null);
        var device = new Device("dsn", "name", false, properties);

        // When
        var result = device.connected();

        // Then
        assertThat(result).isFalse();
    }

    // Returns false if 'connection_status' property is not a string
    @Test
    @DisplayName("Returns false if 'connection_status' property is not a string")
    public void testReturnsFalseIfConnectionStatusPropertyIsNotAString() {
        // Given
        var properties = new HashMap<String, @Nullable Object>();
        properties.put("connection_status", 123);
        var device = new Device("dsn", "name", false, properties);

        // When
        var result = device.connected();

        // Then
        assertThat(result).isFalse();
    }

    // Creating a new Device object with valid parameters should succeed.
    @Test
    @DisplayName("Creating a new Device object with valid parameters should succeed")
    public void testCreatingNewDeviceWithValidParametersShouldSucceed() {
        // Given
        String dsn = "123456";
        String name = "Device 1";
        Map<String, @Nullable Object> properties = Map.of("connection_status", "online");

        // When
        Device device = new Device(dsn, name, true, properties);

        // Then
        assertThat(device).isNotNull();
        assertThat(device.dsn()).isEqualTo(dsn);
        assertThat(device.name()).isEqualTo(name);
        assertThat(device.properties()).isEqualTo(properties);
    }

    // Two Device objects with the same DSN should be considered equal.
    @Test
    @DisplayName("Two Device objects with the same DSN should be considered equal")
    public void testTwoDevicesWithSameDsnShouldBeEqual() {
        // Given
        String dsn = "123456";
        String name1 = "Device 1";
        String name2 = "Device 2";
        Map<String, @Nullable Object> properties = Map.of("connection_status", "online");

        Device device1 = new Device(dsn, name1, true, properties);
        Device device2 = new Device(dsn, name2, true, properties);

        // When
        boolean isEqual = device1.equals(device2);

        // Then
        assertThat(isEqual).isTrue();
    }

    // The compareTo method should correctly compare two Device objects based on their DSNs.
    @Test
    @DisplayName("The compareTo method should correctly compare two Device objects based on their DSNs")
    public void testCompareToMethodShouldCorrectlyCompareDevicesBasedOnDsn() {
        // Given
        String dsn1 = "123456";
        String dsn2 = "654321";
        String name = "Device";
        Map<String, @Nullable Object> properties = Map.of("connection_status", "online");

        Device device1 = new Device(dsn1, name, true, properties);
        Device device2 = new Device(dsn2, name, true, properties);

        // When
        int result1 = device1.compareTo(device2);
        int result2 = device2.compareTo(device1);
        int result3 = device1.compareTo(device1);

        // Then
        assertThat(result1).isNegative();
        assertThat(result2).isPositive();
        assertThat(result3).isZero();
    }

    // The connected method should return true if the connection_status property is "online".
    @Test
    @DisplayName("The connected method should return true if the connection_status property is \"online\"")
    public void testconnectedMethodShouldReturnTrueIfConnectionStatusIsOnline() {
        // Given
        String dsn = "123456";
        String name = "Device";
        Map<String, @Nullable Object> properties1 = Map.of("connection_status", "online");
        Map<String, @Nullable Object> properties2 = Map.of("connection_status", "offline");

        Device device1 = new Device(dsn, name, true, properties1);
        Device device2 = new Device(dsn, name, false, properties2);

        // When
        boolean connected1 = device1.connected();
        boolean connected2 = device2.connected();

        // Then
        assertThat(connected1).isTrue();
        assertThat(connected2).isFalse();
    }

    // The toString method should return a string representation of the Device object with its DSN and name.
    @Test
    @DisplayName("The toString method should return a string representation of the Device object with its DSN and name")
    public void testToStringMethodShouldReturnStringRepresentationWithDsnAndName() {
        // Given
        String dsn = "123456";
        String name = "Device";
        Map<String, @Nullable Object> properties = Map.of("connection_status", "online");

        Device device = new Device(dsn, name, true, properties);

        // When
        String result = device.toString();

        // Then
        assertThat(result).isEqualTo("Device{dsn='123456', name='Device'}");
    }
}
