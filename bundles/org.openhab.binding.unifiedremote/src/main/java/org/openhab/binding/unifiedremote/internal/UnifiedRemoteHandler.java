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
package org.openhab.binding.unifiedremote.internal;

import java.text.ParseException;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link UnifiedRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@NonNullByDefault
public class UnifiedRemoteHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(UnifiedRemoteHandler.class);

    private @Nullable UnifiedRemoteConfiguration config;

    private @Nullable UnifiedRemoteConnection connection;
    private @Nullable ScheduledFuture<?> connectionCheckerSchedule;

    public UnifiedRemoteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // TODO: nothing to do for now
            return;
        }
        String channelId = channelUID.getId();
        if (!isLinked(channelId))
            return;
        logger.debug("handle channel: {}", channelId);
        String[] channelIdSegments = Arrays.stream(channelId.split("#")).reduce((first, second) -> second).get()
                .split("-", 2);
        String stringCommand = command.toFullString();
        logger.debug("handle command: {}", stringCommand);
        JsonArray values = null;
        if (stringCommand.length() > 0 && !stringCommand.trim().equalsIgnoreCase("NULL")) {
            values = parseCommandValues(stringCommand);
        }
        try {
            if (connection != null) {
                ContentResponse response = connection.execRemoteAction(getRemoteId(channelIdSegments[0]),
                        channelIdSegments[1], values);
                if (isErrorResponse(response)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Session expired");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection not initialized");
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Server request fail: " + e.getMessage());
        }
    }

    private JsonArray parseCommandValues(String command) {
        JsonArray values = new JsonArray();
        String[] commandSegments = command.split(",");
        for (String segment : commandSegments) {
            JsonObject value = new JsonObject();
            value.addProperty("Value", segment);
            values.add(value);
        }
        return values;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);
        initConnectionChecker();
    }

    private UnifiedRemoteConnection getNewConnection() throws Exception {
        config = getConfigAs(UnifiedRemoteConfiguration.class);
        if (config == null) {
            throw new ParseException("Unable to parse thing config", 0);
        }
        return new UnifiedRemoteConnection(config.host);
    }

    private void initConnectionChecker() {
        stopConnectionChecker();
        connectionCheckerSchedule = scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (this.connection == null || thing.getStatus() == ThingStatus.OFFLINE) {
                    if (this.connection != null) {
                        this.connection.close();
                        this.connection = null;
                    }
                    UnifiedRemoteConnection connection = getNewConnection();
                    if (connection.authenticate()) {
                        this.connection = connection;
                        updateStatus(ThingStatus.ONLINE);
                    } else if (thing.getStatus() != ThingStatus.OFFLINE) {
                        updateStatus(ThingStatus.OFFLINE);
                        connection.close();
                    }
                } else if (thing.getStatus() == ThingStatus.ONLINE) {
                    if (isErrorResponse(connection.keepAlive())) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Keep alive failed");
                    }

                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to connect: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void stopConnectionChecker() {
        if (connectionCheckerSchedule != null && !connectionCheckerSchedule.isCancelled()) {
            connectionCheckerSchedule.cancel(true);
        }
        connectionCheckerSchedule = null;
    }

    @Override
    public void dispose() {
        stopConnectionChecker();
        if (connection != null) {
            connection.close();
            connection = null;
        }
        super.dispose();
    }

    private boolean isErrorResponse(ContentResponse response) {
        return response.getStatus() != 200 || response.getContentAsString().contains("Not a valid connection");
    }

    private String getRemoteId(String remoteAlias) {
        switch (remoteAlias) {
            case "relmtech_basic__input":
                return "Relmtech.Basic Input";
            case "unified_navigation":
                return "Unified.Navigation";
            case "unified_power":
                return "Unified.Power";
            case "unified_media":
                return "Unified.Media";
            case "unified_monitor":
                return "Unified.Monitor";
            default:
                return "";
        }
    }
}
