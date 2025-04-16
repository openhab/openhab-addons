/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface that represents the definition of a snapshot, which includes information about
 * the power status and online state of a device.
 *
 * <p>
 * This interface is typically used to provide a representation of the current state of a
 * device in the LG ThinQ service. It allows retrieving and updating the power status and online
 * state of the device.
 * </p>
 *
 * <p>
 * Implementations of this interface should provide the actual data and logic for managing
 * the power state and online status of a device.
 * </p>
 *
 * <p>
 * Usage Example:
 *
 * <pre>
 * SnapshotDefinition snapshot = new MySnapshotImplementation();
 * snapshot.setPowerStatus(DevicePowerState.ON);
 * snapshot.setOnline(true);
 * </pre>
 * </p>
 *
 * @author Nemer Daud - Initial contribution
 * @version 1.0
 */
@NonNullByDefault
public interface SnapshotDefinition {

    /**
     * Gets the power status of the device.
     *
     * @return the power status of the device as an instance of {@link DevicePowerState}
     */
    DevicePowerState getPowerStatus();

    /**
     * Sets the power status of the device.
     *
     * @param value the power status to set as an instance of {@link DevicePowerState}
     */
    void setPowerStatus(DevicePowerState value);

    /**
     * Checks if the device is online.
     *
     * @return true if the device is online, false otherwise
     */
    boolean isOnline();

    /**
     * Sets the online status of the device.
     *
     * @param online true if the device is online, false if it is offline
     */
    void setOnline(boolean online);
}
