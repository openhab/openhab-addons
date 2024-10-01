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
package org.openhab.binding.salus.internal.handler;

import java.util.Optional;
import java.util.SortedSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public interface CloudApi {
    /**
     * Finds all devices from cloud
     * 
     * @return all devices from cloud
     */
    SortedSet<Device> findDevices() throws SalusApiException, AuthSalusApiException;

    /**
     * Find a device by DSN
     * 
     * @param dsn of the device to find
     * @return a device with given DSN (or empty if no found)
     */
    Optional<Device> findDevice(String dsn) throws SalusApiException, AuthSalusApiException;

    /**
     * Sets value for a property
     * 
     * @param dsn of the device
     * @param propertyName property name
     * @param value value to set
     * @return if value was properly set
     */
    boolean setValueForProperty(String dsn, String propertyName, Object value)
            throws SalusApiException, AuthSalusApiException;

    /**
     * Finds all properties for a device
     * 
     * @param dsn of the device
     * @return all properties of the device
     */
    SortedSet<DeviceProperty<?>> findPropertiesForDevice(String dsn) throws SalusApiException, AuthSalusApiException;

    boolean isReadOnly();
}
