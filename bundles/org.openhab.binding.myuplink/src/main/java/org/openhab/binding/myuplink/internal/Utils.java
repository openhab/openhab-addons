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
package org.openhab.binding.myuplink.internal;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myuplink.internal.model.ConfigurationException;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * some helper methods.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public final class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * only static methods no instance needed
     */
    private Utils() {
    }

    // TODO: check which methods are needed, delete unused

    // /**
    // * parses a date string in easee format to ZonedDateTime which is used by Openhab.
    // *
    // * @param date
    // * @return
    // */
    // public static ZonedDateTime parseDate(String date) throws DateTimeParseException {
    // DateTimeFormatter formatter;
    // if (date.length() == 24) {
    // formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    // } else {
    // formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
    // }
    // LOGGER.trace("parsing: {}", date);
    // return ZonedDateTime.parse(date, formatter);
    // }

    // /**
    // * get element as JsonObject.
    // *
    // * @param jsonObject
    // * @param key
    // * @return
    // */
    // public static @Nullable JsonObject getAsJsonObject(@Nullable JsonObject jsonObject, String key) {
    // JsonElement element = jsonObject == null ? null : jsonObject.get(key);
    // return (element instanceof JsonObject) ? element.getAsJsonObject() : null;
    // }

    // /**
    // * get element as String.
    // *
    // * @param jsonObject
    // * @param key
    // * @return
    // */
    // public static @Nullable String getAsString(@Nullable JsonObject jsonObject, String key) {
    // JsonElement element = jsonObject == null ? null : jsonObject.get(key);
    // String text = null;
    // if (element != null) {
    // if (element instanceof JsonPrimitive) {
    // text = element.getAsString();
    // } else if (element instanceof JsonObject || element instanceof JsonArray) {
    // text = element.toString();
    // }
    // }
    // return text;
    // }

    // /**
    // * get element as int.
    // *
    // * @param jsonObject
    // * @param key
    // * @return
    // */
    // public static int getAsInt(@Nullable JsonObject jsonObject, String key) {
    // JsonElement element = jsonObject == null ? null : jsonObject.get(key);
    // return (element instanceof JsonPrimitive) ? element.getAsInt() : 0;
    // }

    // /**
    // * get element as boolean.
    // *
    // * @param jsonObject
    // * @param key
    // * @return
    // */
    // public static @Nullable Boolean getAsBool(@Nullable JsonObject jsonObject, String key) {
    // JsonElement json = jsonObject == null ? null : jsonObject.get(key);
    // return (json == null || json instanceof JsonNull) ? null : json.getAsBoolean();
    // }

    /**
     * retrieves typeID of a channel.
     *
     * @param channel
     * @return typeID or empty string if typeUID is null.
     */
    public static String getChannelTypeId(Channel channel) {
        ChannelTypeUID typeUID = channel.getChannelTypeUID();
        if (typeUID == null) {
            return "";
        }
        return typeUID.getId();
    }

    /**
     * retrieves the validation expression which is assigned to this channel, fallback to a public static, if no
     * validation
     * is
     * defined.
     *
     * @param channel
     * @return the validation expression
     */
    public static String getValidationExpression(Channel channel) {
        String expr = getPropertyOrParameter(channel, PARAMETER_NAME_VALIDATION_REGEXP);
        if (expr == null) {
            throw new ConfigurationException(
                    "channel (" + channel.getUID().getId() + ") does not have a validation expression configured");
        }
        return expr;
    }

    /**
     * retrieves the write API url suffix which is assigned to this channel.
     *
     * @param channel
     * @return the url suffix
     */
    public static String getWriteCommand(Channel channel) {
        String command = getPropertyOrParameter(channel, PARAMETER_NAME_WRITE_COMMAND);
        if (command == null) {
            throw new ConfigurationException(
                    "channel (" + channel.getUID().getId() + ") does not have a write command configured");
        }
        return command;
    }

    /**
     * internal utiliy method which returns a property (if found) or a config parameter (if found) otherwise null
     *
     * @param channel
     * @param name
     * @return
     */
    public static @Nullable String getPropertyOrParameter(Channel channel, String name) {
        String value = channel.getProperties().get(name);
        // also eclipse says this cannot be null, it definitely can!
        if (value == null || value.isEmpty()) {
            Object obj = channel.getConfiguration().get(name);
            value = obj == null ? null : obj.toString();
        }
        return value;
    }
}
