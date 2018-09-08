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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
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

    private LGHomBotConfiguration config = getConfigAs(LGHomBotConfiguration.class);

    @Nullable
    private ScheduledFuture<?> refreshTimer;

    private String currentState = "";
    private String currentMode = "";
    private String currentNickname = "";
    private String currentSrvMem = "";
    private DecimalType currentBattery = DecimalType.ZERO;
    private DecimalType currentCPULoad = DecimalType.ZERO;
    private OnOffType currentTurbo = OnOffType.OFF;
    private OnOffType currentRepeat = OnOffType.OFF;
    private State currentImage = UnDefType.UNDEF;
    private DateTimeType currentLastClean = new DateTimeType();

    private final DateTimeFormatter formatterLG = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/ss");
    private boolean disposed = false;
    private int refreshCounter = 0;

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
            refreshFromState(channelUID);
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_START:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            if (currentState.equals(HBSTATE_HOMING)) {
                                sendHomBotCommand("PAUSE");
                            }
                            sendHomBotCommand("CLEAN_START");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendHomBotCommand("PAUSE");
                        }
                    }
                    break;
                case CHANNEL_HOME:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            sendHomBotCommand("HOMING");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendHomBotCommand("PAUSE");
                        }
                    }
                    break;
                case CHANNEL_STOP:
                    if (command instanceof OnOffType) {
                        sendHomBotCommand("PAUSE");
                    }
                    break;
                case CHANNEL_TURBO:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            sendHomBotCommand("TURBO", "true");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendHomBotCommand("TURBO", "false");
                        }
                    }
                    break;
                case CHANNEL_REPEAT:
                    if (command instanceof OnOffType) {
                        if (((OnOffType) command) == OnOffType.ON) {
                            sendHomBotCommand("REPEAT", "true");
                        } else if (((OnOffType) command) == OnOffType.OFF) {
                            sendHomBotCommand("REPEAT", "false");
                        }
                    }
                    break;
                case CHANNEL_MODE:
                    if (command instanceof StringType) {
                        if (((StringType) command).toString().equals("SB")) {
                            sendHomBotCommand("CLEAN_MODE", "CLEAN_SB");
                        } else if (((StringType) command).toString().equals("ZZ")) {
                            sendHomBotCommand("CLEAN_MODE", "CLEAN_ZZ");
                        } else if (((StringType) command).toString().equals("SPOT")) {
                            sendHomBotCommand("CLEAN_MODE", "CLEAN_SPOT");
                        } else if (((StringType) command).toString().equals("MACRO_SECTOR")) {
                            sendHomBotCommand("CLEAN_MODE", "CLEAN_MACRO_SECTOR");
                        }
                    }
                    break;
                case CHANNEL_MOVE:
                    if (command instanceof StringType) {
                        if (((StringType) command).toString().equals("FORWARD")) {
                            sendHomBotJoystick("FORWARD");
                        } else if (((StringType) command).toString().equals("FORWARD_LEFT")) {
                            sendHomBotJoystick("FORWARD_LEFT");
                        } else if (((StringType) command).toString().equals("FORWARD_RIGHT")) {
                            sendHomBotJoystick("FORWARD_RIGHT");
                        } else if (((StringType) command).toString().equals("LEFT")) {
                            sendHomBotJoystick("LEFT");
                        } else if (((StringType) command).toString().equals("RIGHT")) {
                            sendHomBotJoystick("RIGHT");
                        } else if (((StringType) command).toString().equals("BACKWARD")) {
                            sendHomBotJoystick("BACKWARD");
                        } else if (((StringType) command).toString().equals("BACKWARD_LEFT")) {
                            sendHomBotJoystick("BACKWARD_LEFT");
                        } else if (((StringType) command).toString().equals("BACKWARD_RIGHT")) {
                            sendHomBotJoystick("BACKWARD_RIGHT");
                        } else if (((StringType) command).toString().equals("RELEASE")) {
                            sendHomBotJoystick("RELEASE");
                        }
                    }
                    break;
                default:
                    logger.debug("Command received for unknown channel {}: {}", channelUID.getId(), command);
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        disposed = false;
        logger.debug("Initializing handler for LG-HomBot");

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateAllChannels();
        setupRefreshTimer(config.getPollingPeriod());
        updateStatus(ThingStatus.ONLINE);

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

    private void sendHomBotCommand(String command) {
        String fullCmd = "/json.cgi?%7B%22COMMAND%22:%22" + command + "%22%7D";
        sendCommand(fullCmd);
    }

    private void sendHomBotCommand(String command, String argument) {
        String fullCmd = "/json.cgi?%7B%22COMMAND%22:%7B%22" + command + "%22:%22" + argument + "%22%7D%7D";
        sendCommand(fullCmd);
    }

    private void sendHomBotJoystick(String command) {
        String fullCmd = "/json.cgi?%7B%22JOY%22:%22" + command + "%22%7D";
        sendCommand(fullCmd);
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

    private void refreshFromState(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_STATE:
                updateState(channelUID, StringType.valueOf(currentState));
                break;
            case CHANNEL_BATTERY:
                updateState(channelUID, currentBattery);
                break;
            case CHANNEL_CPU_LOAD:
                updateState(channelUID, currentCPULoad);
                break;
            case CHANNEL_SRV_MEM:
                updateState(channelUID, StringType.valueOf(currentSrvMem));
                break;
            case CHANNEL_TURBO:
                updateState(channelUID, currentTurbo);
                break;
            case CHANNEL_REPEAT:
                updateState(channelUID, currentRepeat);
                break;
            case CHANNEL_MODE:
                updateState(channelUID, StringType.valueOf(currentMode));
                break;
            case CHANNEL_NICKNAME:
                updateState(channelUID, StringType.valueOf(currentNickname));
                break;
            case CHANNEL_CAMERA:
                parseImage();
                updateState(channelUID, currentImage);
                break;
            case CHANNEL_LAST_CLEAN:
                updateState(channelUID, currentLastClean);
                break;
            default:
                logger.warn("Channel refresh for {} not implemented!", channelUID.getId());
        }
    }

    private void updateAllChannels() {
        if (disposed) {
            return;
        }
        if (refreshCounter > 0) {
            refreshCounter--;
            if (refreshCounter % 5 == 0) {
                parseImage();
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_CAMERA), currentImage);
                return;
            }
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
            for (String row : rows) {
                if (row.startsWith("JSON_ROBOT_STATE=")) {
                    String state = row.substring(17).replace("\"", "");
                    if (!state.equals(currentState)) {
                        if (state.isEmpty()) {
                            state = "ERROR";
                        }
                        currentState = state;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_STATE);
                        updateState(channel, StringType.valueOf(state));

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
                    DecimalType battery = DecimalType.valueOf(row.substring(14).replace("\"", ""));
                    if (!battery.equals(currentBattery)) {
                        currentBattery = battery;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_BATTERY);
                        updateState(channel, battery);
                    }
                } else if (row.startsWith("CPU_IDLE=")) {
                    DecimalType cpuLoad = new DecimalType(
                            100 - Double.valueOf(row.substring(9).replace("\"", "")).intValue());
                    if (!cpuLoad.equals(currentCPULoad)) {
                        currentCPULoad = cpuLoad;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_CPU_LOAD);
                        updateState(channel, cpuLoad);
                    }
                } else if (row.startsWith("LGSRV_MEMUSAGE=")) {
                    String srvMem = row.substring(15).replace("\"", "");
                    if (!srvMem.equals(currentSrvMem)) {
                        currentSrvMem = srvMem;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_SRV_MEM);
                        updateState(channel, StringType.valueOf(srvMem));
                    }
                } else if (row.startsWith("JSON_TURBO=")) {
                    OnOffType turbo = row.substring(11).replace("\"", "").equalsIgnoreCase("true") ? OnOffType.ON
                            : OnOffType.OFF;
                    if (!turbo.equals(currentTurbo)) {
                        currentTurbo = turbo;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_TURBO);
                        updateState(channel, turbo);
                    }
                } else if (row.startsWith("JSON_REPEAT=")) {
                    OnOffType repeat = row.substring(12).replace("\"", "").equalsIgnoreCase("true") ? OnOffType.ON
                            : OnOffType.OFF;
                    if (!repeat.equals(currentRepeat)) {
                        currentRepeat = repeat;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_REPEAT);
                        updateState(channel, repeat);
                    }
                } else if (row.startsWith("JSON_MODE=")) {
                    String mode = row.substring(10).replace("\"", "");
                    if (!mode.equals(currentMode)) {
                        currentMode = mode;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_MODE);
                        updateState(channel, StringType.valueOf(mode));
                    }
                } else if (row.startsWith("JSON_NICKNAME=")) {
                    String nickname = row.substring(14).replace("\"", "");
                    if (!nickname.equals(currentNickname)) {
                        currentNickname = nickname;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_NICKNAME);
                        updateState(channel, StringType.valueOf(nickname));
                    }
                } else if (row.startsWith("CLREC_LAST_CLEAN=")) {
                    final String stringDate = row.substring(17, 37).replace("\"", "");
                    ZonedDateTime date = ZonedDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
                    try {
                        LocalDateTime localDateTime = LocalDateTime.parse(stringDate, formatterLG);
                        date = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
                    } catch (Exception e) {
                        logger.info("Couldn't parse DateTime {}", e);
                    }
                    DateTimeType lastClean = new DateTimeType(date);
                    if (!lastClean.equals(currentLastClean)) {
                        currentLastClean = lastClean;
                        channel = new ChannelUID(getThing().getUID(), CHANNEL_LAST_CLEAN);
                        updateState(channel, lastClean);
                    }
                }
            }
        }
    }

    private void parseImage() {
        final int width = 320;
        final int height = 240;
        final int size = width * height;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        String url = buildHttpAddress("/images/snapshot.yuv");
        byte[] yuvData = HttpUtil.downloadData(url, null, false, size * 2).getBytes();

        for (int i = 0; i < size; i++) {
            double y = yuvData[i] & 0xFF;
            double u = yuvData[size + i / 2] & 0xFF;
            double v = yuvData[(int) (size * 1.5 + i / 2.0)] & 0xFF;

            int r = Math.min(Math.max((int) (y + 1.371 * (v - 128)), 0), 255); // red
            int g = Math.min(Math.max((int) (y - 0.336 * (u - 128) - 0.698 * (v - 128)), 0), 255); // green
            int b = Math.min(Math.max((int) (y + 1.732 * (u - 128)), 0), 255); // blue

            int p = (r << 16) | (g << 8) | b; // pixel
            image.setRGB(i % width, i / width, p);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", baos);
        } catch (IOException e) {
            logger.error("IOException {}", e);
        }
        byte[] byteArray = baos.toByteArray();
        if (byteArray != null && byteArray.length > 0) {
            currentImage = new RawType(byteArray, "image/jpeg");
        } else {
            currentImage = UnDefType.UNDEF;
        }
    }
}
