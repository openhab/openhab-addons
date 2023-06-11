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
package org.openhab.binding.serial.internal.channel;

import java.util.IllegalFormatException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.serial.internal.transform.ValueTransformation;
import org.openhab.binding.serial.internal.transform.ValueTransformationProvider;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceChannel} is the abstract class for handling a channel. Provides
 * the ability to transform the device data into the channel state.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public abstract class DeviceChannel {
    protected final Logger logger = LoggerFactory.getLogger(DeviceChannel.class);

    protected final ChannelConfig config;

    private final ValueTransformation stateTransform;
    private final ValueTransformation commandTransform;

    protected DeviceChannel(final ValueTransformationProvider valueTransformationProvider, final ChannelConfig config) {
        this.config = config;
        stateTransform = valueTransformationProvider.getValueTransformation(config.stateTransformation);
        commandTransform = valueTransformationProvider.getValueTransformation(config.commandTransformation);
    }

    /**
     * Map the supplied command into the data to send to the device by
     * applying a format followed by a transform.
     * 
     * @param command the command to map
     * @return the mapped data if the mapping produced a result.
     */
    public Optional<String> mapCommand(final Command command) {
        final Optional<String> result = transformCommand(formatCommand(command));

        logger.debug("Mapped command is '{}'", result.orElse(null));

        return result;
    }

    /**
     * Transform the data using the configured transform
     * 
     * @param data the data to transform
     * @return the transformed data if the transform produced a result.
     */
    public Optional<String> transformData(final String data) {
        return stateTransform.apply(data);
    }

    /**
     * Transform the data using the configured command transform
     * 
     * @param data the command to transform
     * @return the transformed data if the transform produced a result.
     */
    protected Optional<String> transformCommand(final String data) {
        return commandTransform.apply(data);
    }

    /**
     * Format the commnd using the configured format
     * 
     * @param data the command to transform
     * @return the formatted data. The orginal data is returned if there is no format string
     *         or if there is an error performing the format.
     */
    protected String formatCommand(final Command command) {
        String data;

        final String commandFormat = config.commandFormat;
        if (commandFormat != null) {
            try {
                data = command.format(commandFormat);
            } catch (final IllegalFormatException e) {
                logger.warn("Couldn't format commmand because format string '{}' is invalid", commandFormat);
                data = command.toFullString();
            }
        } else {
            data = command.toFullString();
        }

        return data;
    }
}
