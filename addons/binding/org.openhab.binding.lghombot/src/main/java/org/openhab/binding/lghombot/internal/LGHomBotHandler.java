/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lghombot.internal;

import static org.openhab.binding.lghombot.internal.LGHomBotBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGHomBotHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class LGHomBotHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LGHomBotHandler.class);

    @Nullable
    private LGHomBotConfiguration config;

    @Nullable
    private ScheduledFuture<?> refreshTimer;

    @Nullable
    private String currentState = null;
    @Nullable
    private String currentMode = null;
    @Nullable
    private String currentNickname = null;
    @Nullable
    private Integer currentBattery = null;
    @Nullable
    private Boolean currentTurbo = null;
    @Nullable
    private Boolean currentRepeat = null;
    @Nullable
    private Boolean currentMute = null;

    private boolean disposed = false;

    public LGHomBotHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        super.dispose();
        disposed = true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            updateAllChannels();
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_START:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            if (currentState != null && currentState.equals(HBSTATE_HOMING)) {
                                sendCommand("/json.cgi?%7B%22COMMAND%22:%22PAUSE%22%7D");
                            }
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%22CLEAN_START%22%7D");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%22PAUSE%22%7D");
                        }
                    }
                    break;
                case CHANNEL_HOME:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%22HOMING%22%7D");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%22PAUSE%22%7D");
                        }
                    }
                    break;
                case CHANNEL_STOP:
                    if (command instanceof OnOffType) {
                        sendCommand("/json.cgi?%7B%22COMMAND%22:%22PAUSE%22%7D");
                    }
                    break;
                case CHANNEL_TURBO:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22TURBO%22:%22true%22%7D%7D");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22TURBO%22:%22false%22%7D%7D");
                        }
                    }
                    break;
                case CHANNEL_REPEAT:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22REPEAT%22:%22true%22%7D%7D");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22REPEAT%22:%22false%22%7D%7D");
                        }
                    }
                    break;
                case CHANNEL_MUTE:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22MUTING%22:%22true%22%7D%7D");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22MUTING%22:%22false%22%7D%7D");
                        }
                    }
                    break;
                case CHANNEL_MODE:
                    if (command instanceof StringType) {
                        if (((StringType) command).toString().equals("SB")) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22CLEAN_MODE%22:%22CLEAN_SB%22%7D%7D");
                        } else if (((StringType) command).toString().equals("ZZ")) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22CLEAN_MODE%22:%22CLEAN_ZZ%22%7D%7D");
                        } else if (((StringType) command).toString().equals("SPOT")) {
                            sendCommand("/json.cgi?%7B%22COMMAND%22:%7B%22CLEAN_MODE%22:%22CLEAN_SPOT%22%7D%7D");
                        }
                    }
                    break;
                default:
                    logger.debug("Command received for an unknown channel {}: {}", channelUID.getId(), command);
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        disposed = false;
        logger.debug("Initializing handler for LG-HomBot");
        config = getConfigAs(LGHomBotConfiguration.class);

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateAllChannels();
        updateStatus(ThingStatus.ONLINE);
        setupRefreshTimer(config.getPollingPeriod());

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    /**
     * Sets up a refresh timer (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *                            initiate a refresh.
     */
    private void setupRefreshTimer(int initialWaitTime) {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(() -> updateAllChannels(), initialWaitTime,
                config.getPollingPeriod(), TimeUnit.SECONDS);
    }

    private String buildHttpAddress(String path) {
        return "http://" + config.getIpAddress() + ":" + config.getPort() + path;
    }

    private void sendCommand(String path) {
        String url = buildHttpAddress(path);
        String status = null;
        try {
            status = HttpUtil.executeUrl("GET", url, 1000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        logger.debug("Status received: {}", status);
    }

    private void updateAllChannels() {
        if (disposed) {
            return;
        }
        String status = null;
        String url = buildHttpAddress("/status.txt");
        try {
            status = HttpUtil.executeUrl("GET", url, 1000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        if (status != null && !status.isEmpty()) {
            String[] rows = status.split("\\r?\\n");
            ChannelUID channel;
            State st;
            for (String row : rows) {
                if (row.startsWith("JSON_ROBOT_STATE=")) {
                    String state = row.substring(17).replace("\"", "");
                    if (!state.equals(currentState)) {
                        currentState = state;
                        st = StringType.valueOf(state);
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_STATE);
                        updateState(channel, st);

                        if (state.equals(HBSTATE_WORKING) || state.equals(HBSTATE_BACKMOVING)
                                || state.equals(HBSTATE_BACKMOVING_INIT)) {
                            channel = new ChannelUID(getThing().getUID(), CHANNEL_START);
                            updateState(channel, OnOffType.ON);
                            channel = new ChannelUID(getThing().getUID(), CHANNEL_HOME);
                            updateState(channel, OnOffType.OFF);
                        }
                        if (state.equals(HBSTATE_HOMING) || state.equals(HBSTATE_DOCKING)) {
                            channel = new ChannelUID(getThing().getUID(), CHANNEL_START);
                            updateState(channel, OnOffType.OFF);
                            channel = new ChannelUID(getThing().getUID(), CHANNEL_HOME);
                            updateState(channel, OnOffType.ON);
                        }
                        if (state.equals(HBSTATE_PAUSE) || state.equals(HBSTATE_CHARGING)
                                || state.equals(HBSTATE_DIAGNOSIS) || state.equals(HBSTATE_RESERVATION)) {
                            channel = new ChannelUID(getThing().getUID(), CHANNEL_START);
                            updateState(channel, OnOffType.OFF);
                            channel = new ChannelUID(getThing().getUID(), CHANNEL_HOME);
                            updateState(channel, OnOffType.OFF);
                        }
                    }
                } else if (row.startsWith("JSON_BATTPERC=")) {
                    Integer battery = Integer.valueOf(row.substring(14).replace("\"", ""));
                    if (!battery.equals(currentBattery)) {
                        currentBattery = battery;
                        st = DecimalType.valueOf(row.substring(14).replace("\"", ""));
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_BATTERY);
                        updateState(channel, st);
                    }
                } else if (row.startsWith("JSON_TURBO=")) {
                    Boolean turbo = (row.substring(11).replace("\"", "").equalsIgnoreCase("true"));
                    if (!turbo.equals(currentTurbo)) {
                        currentTurbo = turbo;
                        st = OnOffType.OFF;
                        if (turbo) {
                            st = OnOffType.ON;
                        }
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_TURBO);
                        updateState(channel, st);
                    }
                } else if (row.startsWith("JSON_REPEAT=")) {
                    Boolean repeat = (row.substring(12).replace("\"", "").equalsIgnoreCase("true"));
                    if (!repeat.equals(currentRepeat)) {
                        currentRepeat = repeat;
                        st = OnOffType.OFF;
                        if (repeat) {
                            st = OnOffType.ON;
                        }
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_REPEAT);
                        updateState(channel, st);
                    }
                } else if (row.startsWith("JSON_MODE=")) {
                    String mode = row.substring(10).replace("\"", "");
                    if (!mode.equals(currentMode)) {
                        currentMode = mode;
                        st = StringType.valueOf(mode);
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_MODE);
                        updateState(channel, st);
                    }
                } else if (row.startsWith("JSON_NICKNAME=")) {
                    String nickname = row.substring(14).replace("\"", "");
                    if (!nickname.equals(currentNickname)) {
                        currentNickname = nickname;
                        st = StringType.valueOf(nickname);
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_NICKNAME);
                        updateState(channel, st);
                    }
                }
            }
        }
    }

}
