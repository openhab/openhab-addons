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
package org.openhab.binding.panasonicbr.internal.handler;

import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.panasonicbr.internal.PanasonicBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.panasonicbr.internal.PanasonicConfiguration;
import org.openhab.binding.panasonicbr.internal.PanasonicHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class PanasonicHandler extends BaseThingHandler {
    private static final int DEFAULT_REFRESH_PERIOD = 10;

    private final Logger logger = LoggerFactory.getLogger(PanasonicHandler.class);
    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> refreshJob;

    private String urlStr = "http://%host%/WAN/dvdr/dvdr_ctrl.cgi";
    private int refreshInterval = DEFAULT_REFRESH_PERIOD;
    private String playerStatus = "";
    private Fields configPostCmd = new Fields();

    public PanasonicHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Panasonic Blu-ray Player handler.");
        PanasonicConfiguration config = getConfigAs(PanasonicConfiguration.class);
        @Nullable
        final String host = config.hostName;

        if (host != null) {
            urlStr = urlStr.replace("%host%", host);
        }

        // pre-define the POST body for status update calls
        configPostCmd.add("cCMD_PST.x", "100");
        configPostCmd.add("cCMD_PST.y", "100");

        this.refreshInterval = config.refresh;

        startAutomaticRefresh();
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Start the job to periodically get a status update from the player
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                final String response = sendCommand(null, configPostCmd);
                logger.debug("Blu-ray status: {}", response);

                final String[] statusLines = response.split(CRLF);

                // get the second line of the status message
                if (statusLines[1] != null) {
                    playerStatus = statusLines[1];
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                }

                // update the channels
                updateStatus();
            };
            this.refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Panasonic Blu-ray Player handler.");

        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateStatus();
        } else if (channelUID.getId().equals(BUTTON)) {
            sendCommand(command.toString(), null);
        } else {
            logger.warn("Unsupported command: {}", command.toString());
        }
    }

    /**
     * Sends a command to the player
     *
     * @param the command to be sent to the player
     * @param a pre-built post body to send to to the player
     * @return the response string from the player
     */
    public String sendCommand(@Nullable String command, @Nullable Fields fields) {
        String output = "";

        // if we were not sent the fields to post, build them from the string
        if (fields == null) {
            fields = new Fields();
            fields.add("cCMD_" + command + ".x", "100");
            fields.add("cCMD_" + command + ".y", "100");
        }

        try {
            ContentResponse response = httpClient.POST(urlStr).agent(USER_AGENT).method(HttpMethod.POST)
                    .content(new FormContentProvider(fields)).send();

            output = response.getContentAsString();

            if (response.getStatus() != OK_200) {
                throw new PanasonicHttpException("Player response: " + response.getStatus() + " - " + output);
            }

        } catch (PanasonicHttpException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error executing player command: {}, {}", command, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Error communicating with the player");
        }

        return output;
    }

    private void updateStatus() {
        if (playerStatus != "") {
            // response string: 0,0,0,00000000
            final String statusArr[] = playerStatus.split(COMMA);

            switch (statusArr[0]) {
                case ZERO:
                    updateState(PLAY_MODE, new StringType(STOP));
                    break;
                case ONE:
                    updateState(PLAY_MODE, new StringType(PLAY));
                    break;
                case TWO:
                    updateState(PLAY_MODE, new StringType(PAUSE));
                    break;
                default:
                    updateState(PLAY_MODE, new StringType(UNKNOWN));
            }

            if (statusArr[1] != null) {
                updateState(TIME_ELAPSED, new QuantityType<>(Integer.parseInt(statusArr[1]), API_SECONDS_UNIT));
            }
        }
    }
}
