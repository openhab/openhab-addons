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
package org.openhab.binding.amazonechocontrol.internal.channelhandler;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ChannelHandler} is the base class for all channel handlers
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public abstract class ChannelHandler {

    public abstract boolean tryHandleCommand(Device device, Connection connection, String channelId, Command command)
            throws IOException, URISyntaxException, InterruptedException;

    protected final IAmazonThingHandler thingHandler;
    protected final Gson gson;
    private final Logger logger;

    protected ChannelHandler(IAmazonThingHandler thingHandler, Gson gson) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.thingHandler = thingHandler;
        this.gson = gson;
    }

    protected <T> @Nullable T tryParseJson(String json, Class<T> type) {
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            logger.debug("Json parse error", e);
            return null;
        }
    }

    protected <T> @Nullable T parseJson(String json, Class<T> type) throws JsonSyntaxException {
        return gson.fromJson(json, type);
    }
}
