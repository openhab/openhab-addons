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

import java.lang.ref.WeakReference;
import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
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
    // RegEx to extract a parse a function String <code>'(.*?)\((.*)\)'</code>
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\)");

    protected final Logger logger = LoggerFactory.getLogger(DeviceChannel.class);

    protected final ChannelConfig config;

    private final BundleContext bundleContext;

    private @Nullable String type;
    private @Nullable String pattern;

    private @Nullable String commandType;
    private @Nullable String commandPattern;

    private WeakReference<@Nullable TransformationService> transformationService = new WeakReference<>(null);
    private WeakReference<@Nullable TransformationService> commandTransformationService = new WeakReference<>(null);

    protected DeviceChannel(final BundleContext bundleContext, final ChannelConfig config) {
        this.bundleContext = bundleContext;
        this.config = config;

        if (config.transform != null) {
            final String[] parts = splitTransformationConfig(config.transform);
            type = parts[0];
            pattern = parts[1];
        }

        if (config.commandTransform != null) {
            final String[] parts = splitTransformationConfig(config.commandTransform);
            commandType = parts[0];
            commandPattern = parts[1];
        }
    }

    /**
     * Map the supplied command into the data to send to the device by
     * applying a format followed by a transform.
     * 
     * @param command the command to map
     * @return the mapped data or the orginal data if no mapping found
     */
    public @Nullable String mapCommand(final Command command) {
        String data = null;

        if (config.commandFormat != null) {
            try {
                data = command.format(config.commandFormat);
            } catch (final IllegalFormatException e) {
                logger.warn("Couldn't map commmand because format string '{}' is invalid", config.commandFormat);
                data = command.toFullString();
            }
        } else {
            data = command.toFullString();
        }

        data = transform(data, config.commandTransform, commandPattern, getCommandTransformationService());

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
    public @Nullable String transformData(final String data) {
        return transform(data, config.transform, pattern, getTransformationService());
    }

    /**
     * Transform the data using the supplied transform parameters
     * 
     * @param data the data to transform
     * @param transform the transform config string
     * @param pattern the transform pattern
     * @param transformationService the transformation service
     * @return the transformed data. The orginal data is returned if no transform is defined or there
     *         is an error performing the transform.
     */
    private @Nullable String transform(final String data, final @Nullable String transform,
            final @Nullable String pattern, final @Nullable TransformationService transformationService) {

        if (transform == null || pattern == null) {
            return data;
        }

        String transformedData;

        try {
            if (transformationService != null) {
                transformedData = transformationService.transform(pattern, data);
            } else {
                transformedData = data;
                logger.warn("Couldn't transform because transformationService of type '{}' is unavailable", type);
            }
        } catch (final TransformationException te) {
            logger.warn("An exception occurred while transforming '{}' with '{}' : '{}'", data, transform,
                    te.getMessage());

            // in case of an error we return the response without any transformation
            transformedData = data;
        }

        logger.debug("Transform result is '{}'", transformedData);
        return transformedData;
    }

    /**
     * Splits a transformation configuration string into its two parts - the
     * transformation type and the function/pattern to apply.
     *
     * @param transformation the string to split
     * @return a string array with exactly two entries for the type and the function
     */
    private String[] splitTransformationConfig(final String transformation) {
        final Matcher matcher = EXTRACT_FUNCTION_PATTERN.matcher(transformation);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("given transformation function '" + transformation
                    + "' does not follow the expected pattern '<function>(<pattern>)'");
        }
        matcher.reset();

        matcher.find();

        final String type = matcher.group(1);
        final String pattern = matcher.group(2);

        return new String[] { type, pattern };
    }

    /**
     * Get the transformation service for transforming data received from the device
     * 
     * @return TransformationService
     */
    private @Nullable TransformationService getTransformationService() {
        final String type = this.type;

        if (type == null) {
            return null;
        }

        TransformationService transformationService = this.transformationService.get();
        if (transformationService == null) {
            transformationService = TransformationHelper.getTransformationService(bundleContext, type);
            this.transformationService = new WeakReference<@Nullable TransformationService>(transformationService);
        }

        return transformationService;
    }

    /**
     * Get the transformation service for transforming the command to send to the device
     * 
     * @return TransformationService
     */
    private @Nullable TransformationService getCommandTransformationService() {
        final String type = this.commandType;

        if (type == null) {
            return null;
        }

        TransformationService transformationService = this.commandTransformationService.get();
        if (transformationService == null) {
            transformationService = TransformationHelper.getTransformationService(bundleContext, type);
            this.commandTransformationService = new WeakReference<@Nullable TransformationService>(
                    transformationService);
        }

        return transformationService;
    }
}
