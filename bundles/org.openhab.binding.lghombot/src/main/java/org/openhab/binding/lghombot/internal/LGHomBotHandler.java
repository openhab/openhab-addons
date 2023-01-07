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
package org.openhab.binding.lghombot.internal;

import static org.openhab.binding.lghombot.internal.LGHomBotBindingConstants.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

    // This is setup in initialize().
    private LGHomBotConfiguration config = new LGHomBotConfiguration();

    private @Nullable ScheduledFuture<?> refreshTimer;

    // State of HomBot
    private String currentState = "";
    private String currentMode = "";
    private String currentNickname = "";
    private String currentSrvMem = "";
    private DecimalType currentBattery = DecimalType.ZERO;
    private DecimalType currentCPULoad = DecimalType.ZERO;
    private OnOffType currentCleanState = OnOffType.OFF;
    private OnOffType currentStartState = OnOffType.OFF;
    private OnOffType currentHomeState = OnOffType.OFF;
    private OnOffType currentTurbo = OnOffType.OFF;
    private OnOffType currentRepeat = OnOffType.OFF;
    private State currentImage = UnDefType.UNDEF;
    private State currentMap = UnDefType.UNDEF;
    private DateTimeType currentLastClean = new DateTimeType();
    private String currentMonday = "";
    private String currentTuesday = "";
    private String currentWednesday = "";
    private String currentThursday = "";
    private String currentFriday = "";
    private String currentSaturday = "";
    private String currentSunday = "";

    private final DateTimeFormatter formatterLG = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/ss");
    private boolean disposed = false;
    private boolean refreshSchedule = false;

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
                case CHANNEL_CLEAN:
                    if (command == OnOffType.ON) {
                        if (currentState.equals(HBSTATE_HOMING)) {
                            sendHomBotCommand("PAUSE");
                        }
                        sendHomBotCommand("CLEAN_START");
                    } else if (command == OnOffType.OFF) {
                        sendHomBotCommand("HOMING");
                    }
                    break;
                case CHANNEL_START:
                    if (command == OnOffType.ON) {
                        sendHomBotCommand("CLEAN_START");
                    }
                    break;
                case CHANNEL_HOME:
                    if (command == OnOffType.ON) {
                        sendHomBotCommand("HOMING");
                    }
                    break;
                case CHANNEL_PAUSE:
                    if (command instanceof OnOffType) {
                        sendHomBotCommand("PAUSE");
                    }
                    break;
                case CHANNEL_TURBO:
                    if (command == OnOffType.ON) {
                        sendHomBotCommand("TURBO", "true");
                    } else if (command == OnOffType.OFF) {
                        sendHomBotCommand("TURBO", "false");
                    }
                    break;
                case CHANNEL_REPEAT:
                    if (command == OnOffType.ON) {
                        sendHomBotCommand("REPEAT", "true");
                    } else if (command == OnOffType.OFF) {
                        sendHomBotCommand("REPEAT", "false");
                    }
                    break;
                case CHANNEL_MODE:
                    if (command instanceof StringType) {
                        switch (command.toString()) {
                            case "SB":
                                sendHomBotCommand("CLEAN_MODE", "CLEAN_SB");
                                break;
                            case "ZZ":
                                sendHomBotCommand("CLEAN_MODE", "CLEAN_ZZ");
                                break;
                            case "SPOT":
                                sendHomBotCommand("CLEAN_MODE", "CLEAN_SPOT");
                                break;
                            case "MACRO_SECTOR":
                                sendHomBotCommand("CLEAN_MODE", "CLEAN_MACRO_SECTOR");
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case CHANNEL_MOVE:
                    if (command instanceof StringType) {
                        String commandString = command.toString();
                        switch (commandString) {
                            case "FORWARD":
                            case "FORWARD_LEFT":
                            case "FORWARD_RIGHT":
                            case "LEFT":
                            case "RIGHT":
                            case "BACKWARD":
                            case "BACKWARD_LEFT":
                            case "BACKWARD_RIGHT":
                            case "RELEASE":
                                sendHomBotJoystick(commandString);
                                break;
                            default:
                                break;
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
        logger.debug("Initializing handler for LG HomBot");
        config = getConfigAs(LGHomBotConfiguration.class);

        setupRefreshTimer(0);
    }

    @Override
    public void handleRemoval() {
        ScheduledFuture<?> localTimer = refreshTimer;
        if (localTimer != null) {
            localTimer.cancel(false);
            refreshTimer = null;
        }
        updateStatus(ThingStatus.REMOVED);
    }

    /**
     * Sets up a refresh timer (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *            initiate a refresh.
     */
    private void setupRefreshTimer(int initialWaitTime) {
        ScheduledFuture<?> localTimer = refreshTimer;
        if (localTimer != null) {
            localTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(this::updateAllChannels, initialWaitTime, config.pollingPeriod,
                TimeUnit.SECONDS);
    }

    private String buildHttpAddress(String path) {
        return "http://" + config.ipAddress + ":" + config.port + path;
    }

    private void sendHomBotCommand(String command) {
        String fullCmd = "/json.cgi?" + UrlEncoded.encodeString("{\"COMMAND\":\"" + command + "\"}");
        sendCommand(fullCmd);
    }

    private void sendHomBotCommand(String command, String argument) {
        String fullCmd = "/json.cgi?"
                + UrlEncoded.encodeString("{\"COMMAND\":{\"" + command + "\":\"" + argument + "\"}}");
        sendCommand(fullCmd);
    }

    private void sendHomBotJoystick(String command) {
        String fullCmd = "/json.cgi?" + UrlEncoded.encodeString("{\"JOY\":\"" + command + "\"}");
        sendCommand(fullCmd);
    }

    private @Nullable String sendCommand(String path) {
        String url = buildHttpAddress(path);
        logger.trace("Executing: {}", url);
        String status = null;
        try {
            status = HttpUtil.executeUrl("GET", url, 1000);
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        logger.trace("Status received: {}", status);
        return status;
    }

    private void refreshFromState(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_STATE:
                updateState(channelUID, StringType.valueOf(currentState));
                break;
            case CHANNEL_CLEAN:
                updateState(channelUID, currentCleanState);
                break;
            case CHANNEL_START:
                updateState(channelUID, currentStartState);
                break;
            case CHANNEL_HOME:
                updateState(channelUID, currentHomeState);
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
            case CHANNEL_MAP:
                parseMap();
                updateState(channelUID, currentMap);
                break;
            case CHANNEL_MONDAY:
                updateState(channelUID, StringType.valueOf(currentMonday));
                refreshSchedule = true;
                break;
            case CHANNEL_TUESDAY:
                updateState(channelUID, StringType.valueOf(currentTuesday));
                refreshSchedule = true;
                break;
            case CHANNEL_WEDNESDAY:
                updateState(channelUID, StringType.valueOf(currentWednesday));
                refreshSchedule = true;
                break;
            case CHANNEL_THURSDAY:
                updateState(channelUID, StringType.valueOf(currentThursday));
                refreshSchedule = true;
                break;
            case CHANNEL_FRIDAY:
                updateState(channelUID, StringType.valueOf(currentFriday));
                refreshSchedule = true;
                break;
            case CHANNEL_SATURDAY:
                updateState(channelUID, StringType.valueOf(currentSaturday));
                refreshSchedule = true;
                break;
            case CHANNEL_SUNDAY:
                updateState(channelUID, StringType.valueOf(currentSunday));
                refreshSchedule = true;
                break;
            default:
                logger.warn("Channel refresh for {} not implemented!", channelUID.getId());
        }
    }

    private void updateAllChannels() {
        if (disposed) {
            return;
        }
        if (refreshSchedule) {
            refreshSchedule = false;
            fetchSchedule();
            return;
        }

        String status = sendCommand("/status.txt");
        if (status != null && !status.isEmpty()) {
            boolean parsingOk = true;
            String[] rows = status.split("\\r?\\n");
            for (String row : rows) {
                int idx = row.indexOf('=');
                if (idx == -1) {
                    continue;
                }
                final String key = row.substring(0, idx);
                String value = row.substring(idx + 1).replace("\"", "");
                switch (key) {
                    case "JSON_ROBOT_STATE":
                        if (value.isEmpty()) {
                            value = HBSTATE_UNKNOWN;
                        }
                        if (!value.equals(currentState)) {
                            currentState = value;
                            updateState(CHANNEL_STATE, StringType.valueOf(value));

                            switch (value) {
                                case HBSTATE_WORKING:
                                case HBSTATE_BACKMOVING:
                                case HBSTATE_BACKMOVING_INIT:
                                    currentCleanState = OnOffType.ON;
                                    currentStartState = OnOffType.ON;
                                    currentHomeState = OnOffType.OFF;
                                    break;
                                case HBSTATE_HOMING:
                                case HBSTATE_DOCKING:
                                    currentCleanState = OnOffType.OFF;
                                    currentStartState = OnOffType.OFF;
                                    currentHomeState = OnOffType.ON;
                                    break;
                                default:
                                    currentCleanState = OnOffType.OFF;
                                    currentStartState = OnOffType.OFF;
                                    currentHomeState = OnOffType.OFF;
                                    break;
                            }
                            updateState(CHANNEL_CLEAN, currentCleanState);
                            updateState(CHANNEL_START, currentStartState);
                            updateState(CHANNEL_HOME, currentHomeState);
                        }
                        break;
                    case "JSON_BATTPERC":
                        try {
                            DecimalType battery = DecimalType.valueOf(value);
                            if (!battery.equals(currentBattery)) {
                                currentBattery = battery;
                                updateState(CHANNEL_BATTERY, battery);
                            }
                        } catch (NumberFormatException e) {
                            logger.debug("Couldn't parse Battery Percent.");
                            parsingOk = false;
                        }
                        break;
                    case "CPU_IDLE":
                        if (isLinked(CHANNEL_CPU_LOAD)) {
                            try {
                                DecimalType cpuLoad = new DecimalType(100 - Double.valueOf(value).longValue());
                                if (!cpuLoad.equals(currentCPULoad)) {
                                    currentCPULoad = cpuLoad;
                                    updateState(CHANNEL_CPU_LOAD, cpuLoad);
                                }
                            } catch (NumberFormatException e) {
                                logger.debug("Couldn't parse CPU Idle.");
                                parsingOk = false;
                            }
                        }
                        break;
                    case "LGSRV_MEMUSAGE":
                        if (!value.equals(currentSrvMem)) {
                            currentSrvMem = value;
                            updateState(CHANNEL_SRV_MEM, StringType.valueOf(value));
                        }
                        break;
                    case "JSON_TURBO":
                        OnOffType turbo = OnOffType.from("true".equalsIgnoreCase(value));
                        if (!turbo.equals(currentTurbo)) {
                            currentTurbo = turbo;
                            updateState(CHANNEL_TURBO, turbo);
                        }
                        break;
                    case "JSON_REPEAT":
                        OnOffType repeat = OnOffType.from("true".equalsIgnoreCase(value));
                        if (!repeat.equals(currentRepeat)) {
                            currentRepeat = repeat;
                            updateState(CHANNEL_REPEAT, repeat);
                        }
                        break;
                    case "JSON_MODE":
                        if (!value.equals(currentMode)) {
                            currentMode = value;
                            updateState(CHANNEL_MODE, StringType.valueOf(value));
                        }
                        break;
                    case "JSON_NICKNAME":
                        if (!value.equals(currentNickname)) {
                            currentNickname = value;
                            updateState(CHANNEL_NICKNAME, StringType.valueOf(value));
                        }
                        break;
                    case "CLREC_LAST_CLEAN":
                        if (value.length() < 19) {
                            logger.debug("Couldn't parse Last Clean from: String length: {}", value.length());
                            parsingOk = false;
                            break;
                        }
                        final String stringDate = value.substring(0, 19);
                        try {
                            LocalDateTime localDateTime = LocalDateTime.parse(stringDate, formatterLG);
                            ZonedDateTime date = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
                            DateTimeType lastClean = new DateTimeType(date);
                            if (!lastClean.equals(currentLastClean)) {
                                currentLastClean = lastClean;
                                updateState(CHANNEL_LAST_CLEAN, lastClean);
                            }
                        } catch (DateTimeException e) {
                            logger.debug("Couldn't parse Last Clean from: {}", stringDate);
                            parsingOk = false;
                        }
                        break;
                    default:
                        break;
                }
            }
            if (!parsingOk) {
                logger.debug("Couldn't parse status response;\n {}", status);
            }
        }
    }

    private void fetchSchedule() {
        String status = sendCommand("/.../usr/data/htdocs/timer.txt");

        if (status != null && !status.isEmpty()) {
            String monday = "";
            String tuesday = "";
            String wednesday = "";
            String thursday = "";
            String friday = "";
            String saturday = "";
            String sunday = "";
            String[] rows = status.split("\\r?\\n");
            for (String row : rows) {
                int idx = row.indexOf('=');
                String name = row.substring(0, idx);
                String state = row.substring(idx + 1);
                switch (name) {
                    case "MONDAY":
                        monday = state;
                        break;
                    case "TUESDAY":
                        tuesday = state;
                        break;
                    case "WEDNESDAY":
                        wednesday = state;
                        break;
                    case "THURSDAY":
                        thursday = state;
                        break;
                    case "FRIDAY":
                        friday = state;
                        break;
                    case "SATURDAY":
                        saturday = state;
                        break;
                    case "SUNDAY":
                        sunday = state;
                        break;
                    default:
                        break;
                }

            }
            if (!currentMonday.equals(monday)) {
                currentMonday = monday;
                updateState(CHANNEL_MONDAY, StringType.valueOf(monday));
            }
            if (!currentTuesday.equals(tuesday)) {
                currentTuesday = tuesday;
                updateState(CHANNEL_TUESDAY, StringType.valueOf(tuesday));
            }
            if (!currentWednesday.equals(wednesday)) {
                currentWednesday = wednesday;
                updateState(CHANNEL_WEDNESDAY, StringType.valueOf(wednesday));
            }
            if (!currentThursday.equals(thursday)) {
                currentThursday = thursday;
                updateState(CHANNEL_THURSDAY, StringType.valueOf(thursday));
            }
            if (!currentFriday.equals(friday)) {
                currentFriday = friday;
                updateState(CHANNEL_FRIDAY, StringType.valueOf(friday));
            }
            if (!currentSaturday.equals(saturday)) {
                currentSaturday = saturday;
                updateState(CHANNEL_SATURDAY, StringType.valueOf(saturday));
            }
            if (!currentSunday.equals(sunday)) {
                currentSunday = sunday;
                updateState(CHANNEL_SUNDAY, StringType.valueOf(sunday));
            }
        }
    }

    private void parseImage() {
        if (!isLinked(CHANNEL_CAMERA)) {
            return;
        }
        final int width = 320;
        final int height = 240;
        final int size = width * height;
        String url = buildHttpAddress("/images/snapshot.yuv");
        RawType rawData = HttpUtil.downloadData(url, null, false, size * 2);
        if (rawData != null) {
            byte[] yuvData = rawData.getBytes();
            currentImage = CameraUtil.parseImageFromBytes(yuvData, width, height);
        } else {
            logger.info("No camera image returned from HomBot.");
        }
    }

    /** Parse the maps.html file to find the black-box filename. */
    private String findBlackBoxFile() {
        String url = buildHttpAddress("/sites/maps.html");
        try {
            String htmlString = HttpUtil.executeUrl("GET", url, 1000);
            int idx = htmlString.indexOf("blkfiles");
            return "/.../usr/data/blackbox/" + htmlString.substring(idx + 13, idx + 50);
        } catch (IOException e1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e1.getMessage());
        }
        return "";
    }

    private void parseMap() {
        if (!isLinked(CHANNEL_MAP)) {
            return;
        }
        final int tileSize = 10;
        final int tileArea = tileSize * tileSize;
        final int rowLength = 100;
        final int scale = 1;

        String blackBox = findBlackBoxFile();
        String url = buildHttpAddress(blackBox);
        RawType dlData = HttpUtil.downloadData(url, null, false, -1);
        if (dlData == null) {
            return;
        }
        byte[] mapData = dlData.getBytes();

        final int tileCount = mapData[32];
        int maxX = 0;
        int maxY = 0;
        int minX = 0x10000;
        int minY = 0x10000;
        int pixPos;

        for (int i = 0; i < tileCount; i++) {
            pixPos = (mapData[52 + i * 16] & 0xFF) + (mapData[52 + 1 + i * 16] << 8);
            int xPos = (pixPos % rowLength) * tileSize;
            int yPos = (pixPos / rowLength) * tileSize;
            if (xPos < minX) {
                minX = xPos;
            }
            if (xPos > maxX) {
                maxX = xPos;
            }
            if (yPos > maxY) {
                maxY = yPos;
            }
            if (yPos < minY) {
                minY = yPos;
            }
        }

        final int width = (tileSize + maxX - minX) * scale;
        final int height = (tileSize + maxY - minY) * scale;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                image.setRGB(j, i, 0xFFFFFF);
            }
        }
        for (int i = 0; i < tileCount; i++) {
            pixPos = (mapData[52 + i * 16] & 0xFF) + (mapData[52 + 1 + i * 16] << 8);
            int xPos = ((pixPos % rowLength) * tileSize - minX) * scale;
            int yPos = (maxY - (pixPos / rowLength) * tileSize) * scale;
            int indexTab = 16044 + i * tileArea;
            for (int j = 0; j < tileSize; j++) {
                for (int k = 0; k < tileSize; k++) {
                    int p = 0xFFFFFF;
                    if ((mapData[indexTab] & 0xF0) != 0) {
                        p = 0xFF0000;
                    } else if (mapData[indexTab] != 0) {
                        p = 0xBFBFBF;
                    }
                    image.setRGB(xPos + k * scale, yPos + (9 - j) * scale, p);
                    indexTab++;
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(image, "png", baos)) {
                logger.debug("Couldn't find PNG writer.");
            }
        } catch (IOException e) {
            logger.info("IOException creating PNG image.", e);
        }
        byte[] byteArray = baos.toByteArray();
        if (byteArray != null && byteArray.length > 0) {
            currentMap = new RawType(byteArray, "image/png");
        } else {
            currentMap = UnDefType.UNDEF;
        }
    }
}
