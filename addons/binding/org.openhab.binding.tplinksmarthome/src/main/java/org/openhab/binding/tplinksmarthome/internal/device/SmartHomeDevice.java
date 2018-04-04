/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.device;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.model.ErrorResponse;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;

/**
 * Abstract class as base for Smart Home device implementations.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public abstract class SmartHomeDevice {

    protected Commands commands = new Commands();

    /**
     * Checks if the response object contains errors and if so throws an {@link IOException} when an error code was set.
     *
     * @param response The response to check for errors.
     * @throws IOException if an error code was set in the response object
     */
    protected void checkErrors(@Nullable HasErrorResponse response) throws IOException {
        ErrorResponse errorResponse = response == null ? null : response.getErrorResponse();

        if (errorResponse != null && errorResponse.getErrorCode() != 0) {
            throw new IOException("Error (" + errorResponse.getErrorCode() + "): " + errorResponse.getErrorMessage());
        }
    }

    /**
     * @return the json string to send to the device to get the state of the device.
     */
    public abstract String getUpdateCommand();

    /**
     * Handle the command for the given channel
     *
     * @param channelID The channel the command is for
     * @param connection The connection to the device
     * @param command The command to be send to the device
     * @param configuration The global configuration
     * @return Returns true if the commands successfully was send to the device
     * @throws IOException In case of communications error or the device returned an error
     */
    public abstract boolean handleCommand(String channelID, Connection connection, Command command,
            TPLinkSmartHomeConfiguration configuration) throws IOException;

    /**
     * Returns the {@link State} value for the given value extracted from the deviceState data.
     *
     * @param channelId channel to get state for
     * @param deviceState state object containing the state
     * @return {@link State} value for the given channel
     */
    public abstract State updateChannel(String channelId, DeviceState deviceState);

}
