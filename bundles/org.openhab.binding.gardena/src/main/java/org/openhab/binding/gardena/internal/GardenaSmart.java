/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.dto.Device;
import org.openhab.binding.gardena.internal.model.dto.api.DataItem;
import org.openhab.binding.gardena.internal.model.dto.command.GardenaCommand;

/**
 * Describes the methods required for the communication with Gardena smart system.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public interface GardenaSmart {

    /**
     * Disposes Gardena smart system.
     */
    public void dispose();

    /**
     * Returns all devices from all locations.
     */
    public Collection<Device> getAllDevices();

    /**
     * Returns a device with the given id.
     */
    public Device getDevice(String deviceId) throws GardenaDeviceNotFoundException;

    /**
     * Sends a command to Gardena smart system.
     */
    public void sendCommand(DataItem<?> dataItem, GardenaCommand gardenaCommand) throws GardenaException;

    /**
     * Returns the id.
     */
    public String getId();

    /**
     * Restarts all WebSocket.
     */
    public void restartWebsockets();
}
