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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
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
    private static final int DEFAULT_REFRESH_PERIOD_SEC = 10;

    // pre-define the POST body for status update calls
    private static final Fields PST_POST_CMD = new Fields();
    static {
        PST_POST_CMD.add("cCMD_PST.x", "100");
        PST_POST_CMD.add("cCMD_PST.y", "100");
    }

    private static final Fields STATUS_POST_CMD = new Fields();
    static {
        STATUS_POST_CMD.add("cCMD_GET_STATUS.x", "100");
        STATUS_POST_CMD.add("cCMD_GET_STATUS.y", "100");
    }

    private final Logger logger = LoggerFactory.getLogger(PanasonicHandler.class);
    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> refreshJob;

    private String urlStr = "http://%host%/WAN/dvdr/dvdr_ctrl.cgi";
    private int refreshInterval = DEFAULT_REFRESH_PERIOD_SEC;
    private String playMode = "";
    private String timeCode = "0";

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

        if (host != null && !host.equals("")) {
            urlStr = urlStr.replace("%host%", host);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host Name must be specified");
            return;
        }

        if (config.refresh >= 10)
            refreshInterval = config.refresh;

        updateStatus(ThingStatus.UNKNOWN);
        startAutomaticRefresh();
    }

    /**
     * Start the job to periodically get a status update from the player
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                final String[] statusLines = sendCommand(null, PST_POST_CMD).split(CRLF);

                // a valid response will have at least two lines
                if (statusLines.length >= 2) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

                    // statusLines second line: 1,1543,0,00000000 (play mode, current time, ?, ?)
                    final String statusArr[] = statusLines[1].split(COMMA);

                    if (statusArr.length >= 2) {
                        // update play mode if different
                        if (!playMode.equals(statusArr[0])) {
                            playMode = statusArr[0];

                            switch (playMode) {
                                case ZERO:
                                    updateState(PLAY_MODE, new StringType(STOP));
                                    updateState(TIME_ELAPSED, UnDefType.UNDEF);
                                    updateState(TIME_TOTAL, UnDefType.UNDEF);
                                    updateState(CHAPTER_CURRENT, UnDefType.UNDEF);
                                    updateState(CHAPTER_TOTAL, UnDefType.UNDEF);
                                    // update cached time code with current time code so update below will not occur
                                    // necessary because the player does not clear reported time code when stopped
                                    timeCode = statusArr[1];
                                    break;
                                case ONE:
                                    updateState(PLAY_MODE, new StringType(PLAY));
                                    break;
                                case TWO:
                                    updateState(PLAY_MODE, new StringType(PAUSE));
                                    break;
                                default:
                                    logger.debug("Unknown playMode type: {}", playMode);
                                    updateState(PLAY_MODE, new StringType(UNKNOWN));
                                    updateState(TIME_ELAPSED, UnDefType.UNDEF);
                                    updateState(TIME_TOTAL, UnDefType.UNDEF);
                                    updateState(CHAPTER_CURRENT, UnDefType.UNDEF);
                                    updateState(CHAPTER_TOTAL, UnDefType.UNDEF);
                                    return;
                            }
                        }

                        // update time code and playback status if time code changes
                        // it stops changing when paused or stopped, preventing the second http call running needlessly
                        if (!timeCode.equals(statusArr[1])) {
                            timeCode = statusArr[1];
                            updateState(TIME_ELAPSED, new QuantityType<>(Integer.parseInt(timeCode), API_SECONDS_UNIT));
                            updatePlaybackStatus();
                        }
                    }
                }
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
            logger.debug("Unsupported refresh command: {}", command.toString());
        } else if (channelUID.getId().equals(BUTTON)) {
            sendCommand(command.toString(), null);
        } else {
            logger.warn("Unsupported command: {}", command.toString());
        }
    }

    /**
     * Sends a command to the player (must send one or the other parameters, not both)
     *
     * @param the command to be sent to the player
     * @param a pre-built post body to send to the player
     * @return the response string from the player
     */
    private String sendCommand(@Nullable String command, @Nullable Fields fields) {
        String output = "";

        // if we were not sent the fields to post, build them from the string
        if (fields == null) {
            fields = new Fields();
            fields.add("cCMD_" + command + ".x", "100");
            fields.add("cCMD_" + command + ".y", "100");
        }
        logger.debug("Blu-ray command: {}", command != null ? command : fields.getNames().iterator().next());

        try {
            ContentResponse response = httpClient.POST(urlStr).agent(USER_AGENT).method(HttpMethod.POST)
                    .content(new FormContentProvider(fields)).send();

            output = response.getContentAsString();
            logger.debug("Blu-ray response: {}", output);

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

    // secondary call to get additional playback status info
    private void updatePlaybackStatus() {
        final String[] statusLines = sendCommand(null, STATUS_POST_CMD).split(CRLF);

        // get the second line of the status message
        // 1,0,0,1,5999,61440,500,1,16,00000000 (?, ?, ?, cur time, total time, title#?, ?, chapt #, total chapt, ?)
        if (statusLines.length >= 2) {
            final String statusArr[] = statusLines[1].split(COMMA);
            if (statusArr.length >= 10) {
                updateState(TIME_TOTAL, new QuantityType<>(Integer.parseInt(statusArr[4]), API_SECONDS_UNIT));
                updateState(CHAPTER_CURRENT, new DecimalType(Integer.parseInt(statusArr[7])));
                updateState(CHAPTER_TOTAL, new DecimalType(Integer.parseInt(statusArr[8])));
            }
        }
    }
}
