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

import static org.openhab.binding.unifiedremote.internal.UnifiedRemoteBindingConstants.MOUSE_CHANNEL;
import static org.openhab.binding.unifiedremote.internal.UnifiedRemoteBindingConstants.SEND_KEY_CHANNEL;

import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UnifiedRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@NonNullByDefault
public class UnifiedRemoteHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(UnifiedRemoteHandler.class);

    private @Nullable UnifiedRemoteConfiguration config;

    private @Nullable UnifiedRemoteConnection connection;
    private @Nullable ScheduledFuture<?> connectionCheckerSchedule;
    private HttpClient httpClient;

    public UnifiedRemoteHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        if (!isLinked(channelId))
            return;
        String stringCommand = command.toFullString();
        try {
            if (connection != null) {
                ContentResponse response;
                switch (channelId) {
                    case MOUSE_CHANNEL:
                        response = connection.mouseMove(stringCommand);
                        break;
                    case SEND_KEY_CHANNEL:
                        response = connection.sendKey(stringCommand);
                        break;
                    default:
                        return;
                }
                if (isErrorResponse(response)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Session expired");
                    connection.authenticate();
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection not initialized");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (e.getCause() instanceof ConnectException) {
                // we assume thing is offline
                updateStatus(ThingStatus.OFFLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        connection = getNewConnection();
        initConnectionChecker();
    }

    private UnifiedRemoteConnection getNewConnection() {
        config = getConfigAs(UnifiedRemoteConfiguration.class);
        return new UnifiedRemoteConnection(this.httpClient, config.host);
    }

    private void initConnectionChecker() {
        stopConnectionChecker();
        connectionCheckerSchedule = scheduler.scheduleWithFixedDelay(() -> {
            try {
                ThingStatus status = thing.getStatus();
                if ((status == ThingStatus.OFFLINE || status == ThingStatus.UNKNOWN) && connection != null) {
                    connection.authenticate();
                    updateStatus(ThingStatus.ONLINE);
                } else if (status == ThingStatus.ONLINE) {
                    if (isErrorResponse(connection.keepAlive())) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Keep alive failed");
                    }
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                if (e.getCause() instanceof ConnectException) {
                    // we assume thing is offline
                    updateStatus(ThingStatus.OFFLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unexpected exception: " + e.getMessage());
                }
            }
        }, 0, 40, TimeUnit.SECONDS);
    }

    private void stopConnectionChecker() {
        if (connectionCheckerSchedule != null) {
            connectionCheckerSchedule.cancel(true);
        }
        connectionCheckerSchedule = null;
    }

    @Override
    public void dispose() {
        stopConnectionChecker();
        super.dispose();
    }

    private boolean isErrorResponse(ContentResponse response) {
        return response.getStatus() != 200 || response.getContentAsString().contains("Not a valid connection");
    }
}
