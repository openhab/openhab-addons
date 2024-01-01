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
package org.openhab.binding.tplinksmarthome.internal.device;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.model.ErrorResponse;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Abstract class as base for Smart Home device implementations.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public abstract class SmartHomeDevice {

    protected final Commands commands = new Commands();
    protected @NonNullByDefault({}) Connection connection;
    protected @NonNullByDefault({}) TPLinkSmartHomeConfiguration configuration;

    /**
     * Checks if the response object contains errors and if so throws an {@link IOException} when an error code was set.
     *
     * @param response The response to check for errors.
     * @throws IOException if an error code was set in the response object
     */
    protected void checkErrors(@Nullable HasErrorResponse response) throws IOException {
        final ErrorResponse errorResponse = response == null ? null : response.getErrorResponse();

        if (errorResponse != null && errorResponse.getErrorCode() != 0) {
            throw new IOException("Error (" + errorResponse.getErrorCode() + "): " + errorResponse.getErrorMessage());
        }
    }

    /**
     * Sets connection and configuration values.
     *
     * @param connection The connection to the device
     * @param configuration The global configuration
     */
    public void initialize(Connection connection, TPLinkSmartHomeConfiguration configuration) {
        this.connection = connection;
        this.configuration = configuration;
    }

    /**
     * @return the json string to send to the device to get the state of the device.
     */
    public abstract String getUpdateCommand();

    /**
     * Handle the command for the given channel
     *
     * @param channelUID The channel the command is for
     * @param command The command to be send to the device
     * @return Returns true if the commands successfully was send to the device
     * @throws IOException In case of communications error or the device returned an error
     */
    public abstract boolean handleCommand(ChannelUID channelUID, Command command) throws IOException;

    /**
     * Returns the {@link State} value for the given value extracted from the deviceState data.
     *
     * @param channelUid channel to get state for
     * @param deviceState state object containing the state
     * @return {@link State} value for the given channel
     */
    public abstract State updateChannel(ChannelUID channelUid, DeviceState deviceState);

    /**
     * Called with the new device state after the new device state is retrieved from the device.
     *
     * @param deviceState new device state
     */
    public void refreshedDeviceState(@Nullable DeviceState deviceState) {
    }
}
