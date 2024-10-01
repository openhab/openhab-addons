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
package org.openhab.binding.panasonicbdp.internal.handler;

import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.panasonicbdp.internal.PanaBlurayBindingConstants.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.openhab.binding.panasonicbdp.internal.PanaBlurayConfiguration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanaBlurayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class PanaBlurayHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(PanaBlurayHandler.class);
    private final HttpClient httpClient;
    private static final int REQUEST_TIMEOUT = 5000;

    private @Nullable ScheduledFuture<?> refreshJob;

    private String urlStr = "http://%host%/WAN/dvdr/dvdr_ctrl.cgi";
    private String nonceUrlStr = "http://%host%/cgi-bin/get_nonce.cgi";
    private int refreshInterval = DEFAULT_REFRESH_PERIOD_SEC;
    private String playerMode = EMPTY;
    private String playerKey = EMPTY;
    private boolean debounce = true;
    private boolean authEnabled = false;
    private Object sequenceLock = new Object();
    private ThingTypeUID thingTypeUID = THING_TYPE_BD_PLAYER;

    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final @Nullable Bundle bundle;

    public PanaBlurayHandler(Thing thing, HttpClient httpClient, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(PanaBlurayHandler.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Panasonic Blu-ray Player handler.");
        PanaBlurayConfiguration config = getConfigAs(PanaBlurayConfiguration.class);

        this.thingTypeUID = thing.getThingTypeUID();

        final String host = config.hostName;
        final String playerKey = config.playerKey;

        if (!host.isBlank()) {
            urlStr = urlStr.replace("%host%", host);
            nonceUrlStr = nonceUrlStr.replace("%host%", host);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.hostname");
            return;
        }

        if (!playerKey.isBlank()) {
            if (playerKey.length() != 32) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.keyerror");
                return;
            }
            this.playerKey = playerKey;
            authEnabled = true;
        }
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
            this.refreshJob = scheduler.scheduleWithFixedDelay(this::refreshPlayerStatus, 0, refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Sends commands to the player to get status information and updates the channels
     */
    private void refreshPlayerStatus() {
        final String[] playerStatusRespArr = sendCommand(REVIEW_POST_CMD, urlStr).split(CRLF);

        if (playerStatusRespArr.length == 1 && playerStatusRespArr[0].isBlank()) {
            return;
        } else if (playerStatusRespArr.length >= 2) {
            // CMD_REVIEW response second line, 4th group is the status:
            // F,,,07,00,,8,,,,1,000:00,,05:10,F,FF:FF,0000,0000,0000,0000
            final String playerStatusArr[] = playerStatusRespArr[1].split(COMMA);

            if (playerStatusArr.length >= 4) {
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }

                // update playerMode if different
                if (!playerMode.equals(playerStatusArr[3])) {
                    playerMode = playerStatusArr[3];
                    final String i18nKey = STATUS_MAP.get(playerMode) != null ? STATUS_MAP.get(playerMode) : "unknown";
                    updateState(PLAYER_STATUS, new StringType(translationProvider.getText(bundle, "status." + i18nKey,
                            i18nKey, localeProvider.getLocale())));
                    updateState(CONTROL, PLAY_STATUS.equals(playerMode) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);

                    // playback stopped, tray open, or power switched off, zero out time and chapters
                    if (!isPlayingMode()) {
                        updateState(TIME_ELAPSED, UnDefType.UNDEF);
                        updateState(TIME_TOTAL, UnDefType.UNDEF);
                        updateState(CHAPTER_CURRENT, UnDefType.UNDEF);
                        updateState(CHAPTER_TOTAL, UnDefType.UNDEF);
                    }
                }

                if (debounce) {
                    updateState(POWER, OnOffType.from(!OFF_STATUS.equals(playerMode)));
                }
                debounce = true;
            } else {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/error.polling");
                return;
            }
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/error.polling");
            return;
        }

        // if time/chapter channels are not linked or player is stopped or paused there is no need to make extra calls
        if (isAnyStatusChannelsLinked() && isPlayingMode() && !PAUSE_STATUS.equals(playerMode)) {
            final String[] pstRespArr = sendCommand(PST_POST_CMD, urlStr).split(CRLF);

            if (pstRespArr.length >= 2) {
                // CMD_PST response second line: 1,1543,0,00000000 (play mode, current time, ?, ?)
                final String pstArr[] = pstRespArr[1].split(COMMA);

                if (pstArr.length >= 2) {
                    try {
                        updateState(TIME_ELAPSED, new QuantityType<>(Integer.parseInt(pstArr[1]), API_SECONDS_UNIT));
                    } catch (NumberFormatException nfe) {
                        logger.debug("Error parsing time elapsed integer in CMD_PST message: {}", pstRespArr[1]);
                    }
                }
            }

            // only BD players support the CMD_GET_STATUS command, it returns a 404 error on UHD models
            if (THING_TYPE_BD_PLAYER.equals(thingTypeUID)
                    && (isLinked(TIME_TOTAL) || isLinked(CHAPTER_CURRENT) || isLinked(CHAPTER_TOTAL))) {
                final String[] getStatusRespArr = sendCommand(GET_STATUS_POST_CMD, urlStr).split(CRLF);

                if (getStatusRespArr.length >= 2) {
                    // CMD_GET_STATUS response second line: 1,0,0,1,5999,61440,500,1,16,00000000
                    // (?, ?, ?, cur time, total time, title#?, ?, chapter #, total chapter, ?)
                    final String getStatusArr[] = getStatusRespArr[1].split(COMMA);

                    if (getStatusArr.length >= 10) {
                        try {
                            updateState(TIME_TOTAL,
                                    new QuantityType<>(Integer.parseInt(getStatusArr[4]), API_SECONDS_UNIT));
                            updateState(CHAPTER_CURRENT, new DecimalType(Integer.parseInt(getStatusArr[7])));
                            updateState(CHAPTER_TOTAL, new DecimalType(Integer.parseInt(getStatusArr[8])));
                        } catch (NumberFormatException nfe) {
                            logger.debug("Error parsing integer in CMD_GET_STATUS message: {}", getStatusRespArr[1]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        synchronized (sequenceLock) {
            if (command instanceof RefreshType) {
                logger.debug("Unsupported refresh command: {}", command);
            } else if (POWER.equals(channelUID.getId()) || CONTROL.equals(channelUID.getId())
                    || BUTTON.equals(channelUID.getId())) {
                final String commandStr;
                if (command instanceof OnOffType) {
                    commandStr = CMD_POWER + command; // e.g. POWERON or POWEROFF
                    // if the power is switched on while the polling is running, the switch could bounce back to off,
                    // set this flag to stop the first polling event from changing the state of the switch to give the
                    // player time to start up and report the correct power status on the next poll
                    debounce = false;
                } else if (command instanceof PlayPauseType || command instanceof NextPreviousType
                        || command instanceof RewindFastforwardType) {
                    if (command == PlayPauseType.PLAY) {
                        commandStr = CMD_PLAYBACK;
                    } else if (command == PlayPauseType.PAUSE) {
                        commandStr = CMD_PAUSE;
                    } else if (command == NextPreviousType.NEXT) {
                        commandStr = CMD_SKIPFWD;
                    } else if (command == NextPreviousType.PREVIOUS) {
                        commandStr = CMD_SKIPREV;
                    } else if (command == RewindFastforwardType.FASTFORWARD) {
                        commandStr = CMD_CUE;
                    } else if (command == RewindFastforwardType.REWIND) {
                        commandStr = CMD_REV;
                    } else {
                        logger.debug("Invalid control command: {}", command);
                        return;
                    }
                } else {
                    commandStr = command.toString();
                }

                // build the fields to POST the RC_ command string
                Fields fields = new Fields();
                fields.add("cCMD_RC_" + commandStr + ".x", "100");
                fields.add("cCMD_RC_" + commandStr + ".y", "100");

                // if command authentication enabled, get nonce to create authKey and add it to the POST fields
                if (authEnabled) {
                    final String nonce = sendCommand(GET_NONCE_CMD, nonceUrlStr).trim();
                    if (nonce.isBlank()) {
                        return;
                    } else if (nonce.length() != 32) {
                        logger.debug("Error retrieving nonce, message was: {}", nonce);
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/error.nonce");
                        return;
                    }
                    try {
                        fields.add("cAUTH_FORM", playerKey.substring(0, playerKey.contains("2") ? 2 : 3));
                        fields.add("cAUTH_VALUE", getAuthKey(playerKey + nonce));
                    } catch (NoSuchAlgorithmException e) {
                        logger.debug("Error creating auth key: {}", e.getMessage());
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/error.authkey");
                        return;
                    }
                }

                // send the command to the player
                sendCommand(fields, urlStr);
            } else {
                logger.debug("Unsupported command: {}", command);
            }
        }
    }

    /**
     * Sends a command to the player using a pre-built post body
     *
     * @param fields a pre-built post body to send to the player
     * @param url the url to receive the command
     * @return the response string from the player
     */
    private String sendCommand(Fields fields, String url) {
        try {
            logger.trace("POST url: {}, data: {}", url, fields);
            ContentResponse response = httpClient.POST(url).agent(USER_AGENT).method(HttpMethod.POST)
                    .content(new FormContentProvider(fields)).timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).send();

            final String output = response.getContentAsString();
            logger.trace("Response status: {}, response: {}", response.getStatus(), output);

            if (response.getStatus() != OK_200) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/error.polling");
                return EMPTY;
            } else if (output.startsWith(PLAYER_CMD_ERR)) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/error.invalid");
                return EMPTY;
            }

            return output;
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Error executing command: {}, {}", fields.getNames().iterator().next(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "@text/error.exception");
        } catch (InterruptedException e) {
            logger.debug("InterruptedException executing command: {}, {}", fields.getNames().iterator().next(),
                    e.getMessage());
            Thread.currentThread().interrupt();
        }
        return EMPTY;
    }

    /*
     * Returns true if the player is currently in a playing mode.
     */
    private boolean isPlayingMode() {
        return !(STOP_STATUS.equals(playerMode) || OPEN_STATUS.equals(playerMode) || OFF_STATUS.equals(playerMode));
    }

    /*
     * Returns true if any of the time or chapter channels are linked depending on which thing type is used.
     */
    private boolean isAnyStatusChannelsLinked() {
        if (THING_TYPE_BD_PLAYER.equals(thingTypeUID)) {
            return isLinked(TIME_ELAPSED) || isLinked(TIME_TOTAL) || isLinked(CHAPTER_CURRENT)
                    || isLinked(CHAPTER_TOTAL);
        }
        return isLinked(TIME_ELAPSED);
    }

    @Override
    public boolean isLinked(String channelName) {
        final Channel channel = this.thing.getChannel(channelName);
        return channel != null ? isLinked(channel.getUID()) : false;
    }

    /**
     * Returns a SHA-256 hash of the input string
     *
     * @param input the input string to generate the hash from
     * @return the 256 bit hash string
     */
    private String getAuthKey(String input) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance(SHA_256_ALGORITHM);
        final byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

        // convert byte array into signum representation
        final BigInteger number = new BigInteger(1, hash);

        // convert message digest into hex value
        final StringBuilder hexString = new StringBuilder(number.toString(16));

        // pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, "0");
        }

        return hexString.toString().toUpperCase();
    }
}
