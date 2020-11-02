/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
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

    private final ValueTransformation transform;
    private final ValueTransformation commandTransform;

    protected DeviceChannel(final ValueTransformationProvider valueTransformationProvider, final ChannelConfig config) {
        this.config = config;
        transform = valueTransformationProvider.getValueTransformation(config.stateTransformation);
        commandTransform = valueTransformationProvider.getValueTransformation(config.commandTransformation);
    }

    /**
     * Map the supplied command into the data to send to the device by
     * applying a format followed by a transform.
     * 
     * @param command the command to map
     * @return the mapped data or the orginal data if no mapping found
     */
    public @Nullable String mapCommand(final Command command) {
        String data = formatCommand(command);

        data = transformCommand(data);

        logger.debug("Mapped command is '{}'", data);

        return data;
    }

    /**
     * Transform the data using the configured transform
     * 
     * @param data the data to transform
     * @return the transformed data. The orginal data is returned if no transform is defined or there
     *         is an error performing the transform.
     */
    public @Nullable String transformData(final @Nullable String data) {
        final Optional<String> result = data != null ? transform.apply(data) : Optional.empty();
        return result.isPresent() ? result.get() : null;
    }

    /**
     * Transform the data using the configured command transform
     * 
     * @param data the command to transform
     * @return the transformed data. The orginal data is returned if no transform is defined or there
     *         is an error performing the transform.
     */
    protected @Nullable String transformCommand(final @Nullable String data) {
        final Optional<String> result = data != null ? commandTransform.apply(data) : Optional.empty();
        return result.isPresent() ? result.get() : null;
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
