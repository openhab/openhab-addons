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
package org.openhab.binding.salus.internal;

import java.util.SortedSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

/**
 * The SalusApi class is responsible for interacting with a REST API to perform various operations related to the Salus
 * system. It handles authentication, token management, and provides methods to retrieve and manipulate device
 * information and properties.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public interface SalusApi {
    /**
     * Finds all available devices.
     *
     * @return A sorted set of Device objects representing the discovered devices.
     * @throws SalusApiException if an error occurs during device discovery.
     */
    SortedSet<Device> findDevices() throws SalusApiException, AuthSalusApiException;

    /**
     * Retrieves the properties of a specific device.
     *
     * @param dsn The Device Serial Number (DSN) identifying the device.
     * @return A sorted set of DeviceProperty objects representing the properties of the device.
     * @throws SalusApiException if an error occurs while retrieving device properties.
     */
    SortedSet<DeviceProperty<?>> findDeviceProperties(String dsn) throws SalusApiException, AuthSalusApiException;

    /**
     * Sets the value for a specific property of a device.
     *
     * @param dsn The Device Serial Number (DSN) identifying the device.
     * @param propertyName The name of the property to set.
     * @param value The new value for the property.
     * @return An Object representing the result of setting the property value.
     * @throws SalusApiException if an error occurs while setting the property value.
     */
    Object setValueForProperty(String dsn, String propertyName, Object value)
            throws SalusApiException, AuthSalusApiException;
}
