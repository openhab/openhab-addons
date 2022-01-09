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
package org.openhab.binding.unifiedremote.internal;

import static org.openhab.binding.unifiedremote.internal.UnifiedRemoteBindingConstants.*;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
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

/**
 * The {@link UnifiedRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@NonNullByDefault
public class UnifiedRemoteHandler extends BaseThingHandler {

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
        if (!isLinked(channelId)) {
            return;
        }
        String stringCommand = command.toFullString();
        UnifiedRemoteConnection urConnection = connection;
        try {
            if (urConnection != null) {
                ContentResponse response;
                switch (channelId) {
                    case MOUSE_CHANNEL:
                        response = urConnection.mouseMove(stringCommand);
                        break;
                    case SEND_KEY_CHANNEL:
                        response = urConnection.sendKey(stringCommand);
                        break;
                    default:
                        return;
                }
                if (isErrorResponse(response)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Session expired");
                    urConnection.authenticate();
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection not initialized");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (isThingOfflineException(e)) {
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
        UnifiedRemoteConfiguration currentConfiguration = getConfigAs(UnifiedRemoteConfiguration.class);
        return new UnifiedRemoteConnection(this.httpClient, currentConfiguration.host);
    }

    private void initConnectionChecker() {
        stopConnectionChecker();
        connectionCheckerSchedule = scheduler.scheduleWithFixedDelay(() -> {
            try {
                UnifiedRemoteConnection urConnection = connection;
                if (urConnection == null) {
                    return;
                }
                ThingStatus status = thing.getStatus();
                if ((status == ThingStatus.OFFLINE || status == ThingStatus.UNKNOWN) && connection != null) {
                    urConnection.authenticate();
                    updateStatus(ThingStatus.ONLINE);
                } else if (status == ThingStatus.ONLINE) {
                    if (isErrorResponse(urConnection.keepAlive())) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Keep alive failed");
                    }
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                if (isThingOfflineException(e)) {
                    // we assume thing is offline
                    updateStatus(ThingStatus.OFFLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unexpected exception: " + e.getMessage());
                }
            }
        }, 0, 40, TimeUnit.SECONDS);
    }

    private boolean isThingOfflineException(Exception e) {
        return e instanceof TimeoutException || e.getCause() instanceof ConnectException
                || e.getCause() instanceof NoRouteToHostException;
    }

    private void stopConnectionChecker() {
        var schedule = connectionCheckerSchedule;
        if (schedule != null) {
            schedule.cancel(true);
            connectionCheckerSchedule = null;
        }
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
