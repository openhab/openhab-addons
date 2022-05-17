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
package org.openhab.binding.easee.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class provides all methods which deal with channels
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public final class ChannelUtil {
    private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);

    /**
     * only static methods no instance needed
     */
    private ChannelUtil() {
    }

    /**
     * retrieves typeID of a channel.
     *
     * @param channel
     * @return typeID or empty string if typeUID is null.
     */
    public static String getChannelId(Channel channel) {
        ChannelTypeUID typeUID = channel.getChannelTypeUID();
        if (typeUID == null) {
            return "";
        }
        return typeUID.getId();
    }

    // /**
    // * retrieves the validation expression which is assigned to this channel, fallback to a default, if no validation
    // is
    // * defined.
    // *
    // * @param channel
    // * @return the validation expression
    // */
    // public static String getValidationExpression(Channel channel) {
    // String expr = getPropertyOrParameter(channel, NibeUplinkBindingConstants.PARAMETER_NAME_VALIDATION_REGEXP);
    // if (expr == null) {
    // logger.info("Channel {} does not have a validation expression configured", channel.getUID().getId());
    // throw new ConfigurationException(
    // "channel (" + channel.getUID().getId() + ") does not have a validation expression configured");
    // }
    // return expr;
    // }
    //
    // /**
    // * retrieves the write API url suffix which is assigned to this channel.
    // *
    // * @param channel
    // * @return the url suffix
    // */
    // public static String getWriteApiUrlSuffix(Channel channel) {
    // String suffix = getPropertyOrParameter(channel, NibeUplinkBindingConstants.PARAMETER_NAME_WRITE_API_URL);
    // if (suffix == null) {
    // logger.info("channel {} does not have a write api url suffix configured", channel.getUID().getId());
    // throw new ConfigurationException(
    // "channel (" + channel.getUID().getId() + ") does not have a write api url suffix configured");
    // }
    // return suffix;
    // }

    private static @Nullable String getPropertyOrParameter(Channel channel, String name) {
        String value = channel.getProperties().get(name);
        // also eclipse says this cannot be null, it definitely can!
        if (value == null || value.isEmpty()) {
            Object obj = channel.getConfiguration().get(name);
            value = obj == null ? null : obj.toString();
        }
        return value;
    }
}
