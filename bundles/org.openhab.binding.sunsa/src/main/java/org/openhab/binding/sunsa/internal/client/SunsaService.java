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
package org.openhab.binding.sunsa.internal.client;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sunsa.internal.domain.Device;

/**
 * A service for controlling and getting data for sunsa devices.
 *
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public interface SunsaService {
    /**
     * Returns a list of devices for the user.
     */
    public List<Device> getDevices() throws SunsaException;

    /**
     * Update a device.
     * 
     * @param device An instance of the device with the updated attributes.
     * @return the updated device
     */
    public Device updateDevice(final Device device) throws SunsaException;

    /**
     * Retrieves a device.
     * 
     * @param id - The identifier of the device to retrieve.
     */
    public Device getDevice(final String id) throws SunsaException;

    /**
     * Set the device position.
     *
     * @param deviceId Id of the device who's position is being updated.
     * @param rawPosition Value between -100 to 100.
     * @return The updated position.
     */
    public int setDevicePosition(final String deviceId, int rawPosition) throws SunsaException;;

    public static class SunsaException extends RuntimeException {
        public SunsaException(int errorCode, String message) {
            super(errorCode + ": " + message);
        }
    }

    public static class ServiceException extends SunsaException {
        public ServiceException(int errorCode, String message) {
            super(errorCode, message);
        }
    }

    public static class ClientException extends SunsaException {
        public ClientException(int errorCode, String message) {
            super(errorCode, message);
        }
    }

    public static class UnknownDeviceException extends ClientException {
        public UnknownDeviceException(int errorCode, String message) {
            super(errorCode, message);
        }
    }
}
