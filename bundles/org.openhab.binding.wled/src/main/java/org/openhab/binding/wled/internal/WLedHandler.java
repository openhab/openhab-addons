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
package org.openhab.binding.wled.internal;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedHandler} is responsible for handling commands and states, which are
 * sent to one of the channels or http replies back.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class WLedHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpClient httpClient;
    private final WledDynamicStateDescriptionProvider stateDescriptionProvider;
    private @Nullable ScheduledFuture<?> pollingFuture = null;
    private BigDecimal masterBrightness = BigDecimal.ZERO;
    private HSBType primaryColor = new HSBType();
    private BigDecimal primaryWhite = BigDecimal.ZERO;
    private HSBType secondaryColor = new HSBType();
    private BigDecimal secondaryWhite = BigDecimal.ZERO;
    private boolean hasWhite = false;
    private WLedConfiguration config;

    public WLedHandler(Thing thing, HttpClient httpClient,
            WledDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;
        config = getConfigAs(WLedConfiguration.class);
    }

    private void sendGetRequest(String url) {
        Request request = httpClient.newRequest(config.address + url);
        request.timeout(3, TimeUnit.SECONDS);
        request.method(HttpMethod.GET);
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip");
        logger.trace("Sending WLED GET:{}", url);
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() == 200) {
                processState(contentResponse.getContentAsString());
                return;
            } else {
                errorReason = String.format("WLED request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
            }
        } catch (TimeoutException e) {
            errorReason = "TimeoutException: WLED was not reachable on your network";
        } catch (ExecutionException e) {
            errorReason = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorReason = String.format("InterruptedException: %s", e.getMessage());
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorReason);
    }

    private HSBType parseToHSBType(String message, String element) {
        int startIndex = message.indexOf(element);
        if (startIndex == -1) {
            return new HSBType();
        }
        int endIndex = message.indexOf("<", startIndex + element.length());
        int r = 0, g = 0, b = 0;
        try {
            r = Integer.parseInt(message.substring(startIndex + element.length(), endIndex));
            // look for second element
            startIndex = message.indexOf(element, endIndex);
            if (startIndex == -1) {
                return new HSBType();
            }
            endIndex = message.indexOf("<", startIndex + element.length());
            g = Integer.parseInt(message.substring(startIndex + element.length(), endIndex));
            // look for third element called <cl>
            startIndex = message.indexOf(element, endIndex);
            if (startIndex == -1) {
                return new HSBType();
            }
            endIndex = message.indexOf("<", startIndex + element.length());
            b = Integer.parseInt(message.substring(startIndex + element.length(), endIndex));
        } catch (NumberFormatException e) {
            logger.warn("NumberFormatException when parsing the WLED color fields.");
        }
        return HSBType.fromRGB(r, g, b);
    }

    private void parseColours(String message) {
        primaryColor = parseToHSBType(message, "<cl>");
        updateState(CHANNEL_PRIMARY_COLOR, primaryColor);
        secondaryColor = parseToHSBType(message, "<cs>");
        updateState(CHANNEL_SECONDARY_COLOR, secondaryColor);
        try {
            primaryWhite = new BigDecimal(getValue(message, "<wv>", "<"));
            if (primaryWhite.intValue() > -1) {
                hasWhite = true;
                updateState(CHANNEL_PRIMARY_WHITE,
                        new PercentType(primaryWhite.divide(new BigDecimal(2.55), RoundingMode.HALF_UP)));
                secondaryWhite = new BigDecimal(getValue(message, "<ws>", "<"));
                updateState(CHANNEL_SECONDARY_WHITE,
                        new PercentType(secondaryWhite.divide(new BigDecimal(2.55), RoundingMode.HALF_UP)));
            }
        } catch (NumberFormatException e) {
            logger.warn("NumberFormatException when parsing the WLED colour and white fields.");
        }
    }

    /**
     *
     * This function should prevent the need to keep updating the binding as more FX and Palettes are added to the
     * firmware.
     */
    private void scrapeChannelOptions(String message) {
        List<StateOption> fxOptions = new ArrayList<>();
        List<StateOption> palleteOptions = new ArrayList<>();
        int counter = 0;
        for (String value : getValue(message, "\"effects\":[", "]").replace("\"", "").split(",")) {
            fxOptions.add(new StateOption(Integer.toString(counter++), value));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_FX), fxOptions);
        counter = 0;
        for (String value : (getValue(message, "\"palettes\":[", "]").replace("\"", "")).split(",")) {
            palleteOptions.add(new StateOption("" + counter++, value));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_PALETTES), palleteOptions);
    }

    private void processState(String message) {
        logger.trace("WLED states are:{}", message);
        if (thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            sendGetRequest("/json"); // fetch FX and Pallete names
        }
        if (message.contains("\"effects\":[")) {// JSON API reply
            scrapeChannelOptions(message);
            return;
        }
        if (message.contains("<ac>0</ac>")) {
            updateState(CHANNEL_MASTER_CONTROLS, OnOffType.OFF);
        } else {
            masterBrightness = new BigDecimal(getValue(message, "<ac>", "<"));
            updateState(CHANNEL_MASTER_CONTROLS,
                    new PercentType(masterBrightness.divide(new BigDecimal(2.55), RoundingMode.HALF_UP)));
        }
        if (message.contains("<ix>0</ix>")) {
            updateState(CHANNEL_INTENSITY, OnOffType.OFF);
        } else {
            BigDecimal bigTemp = new BigDecimal(getValue(message, "<ix>", "<")).divide(new BigDecimal(2.55),
                    RoundingMode.HALF_UP);
            updateState(CHANNEL_INTENSITY, new PercentType(bigTemp));
        }
        if (message.contains("<cy>1</cy>")) {
            updateState(CHANNEL_PRESET_CYCLE, OnOffType.ON);
        } else {
            updateState(CHANNEL_PRESET_CYCLE, OnOffType.OFF);
        }
        if (message.contains("<nl>1</nl>")) {
            updateState(CHANNEL_SLEEP, OnOffType.ON);
        } else {
            updateState(CHANNEL_SLEEP, OnOffType.OFF);
        }
        if (message.contains("<fx>")) {
            updateState(CHANNEL_FX, new StringType(getValue(message, "<fx>", "<")));
        }
        if (message.contains("<sx>")) {
            BigDecimal bigTemp = new BigDecimal(getValue(message, "<sx>", "<")).divide(new BigDecimal(2.55),
                    RoundingMode.HALF_UP);
            updateState(CHANNEL_SPEED, new PercentType(bigTemp));
        }
        if (message.contains("<fp>")) {
            updateState(CHANNEL_PALETTES, new StringType(getValue(message, "<fp>", "<")));
        }
        parseColours(message);
    }

    private void sendWhite() {
        if (hasWhite) {
            sendGetRequest("/win&TT=1000&FX=0&CY=0&CL=hFF000000" + "&A=" + masterBrightness);
        } else {
            sendGetRequest("/win&TT=1000&FX=0&CY=0&CL=hFFFFFF" + "&A=" + masterBrightness);
        }
    }

    /**
     *
     * @param hsb
     * @return WLED needs the letter h followed by 2 digit HEX code for RRGGBB
     */
    private String createColorHex(HSBType hsb) {
        return String.format("h%06X", hsb.getRGB());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_MASTER_CONTROLS:
                    sendGetRequest("/win");
            }
            return;// no need to check for refresh below
        }
        logger.debug("command {} sent to {}", command, channelUID.getId());
        switch (channelUID.getId()) {
            case CHANNEL_PRIMARY_WHITE:
                if (command instanceof PercentType) {
                    sendGetRequest("/win&W=" + new BigDecimal(command.toString()).multiply(new BigDecimal(2.55)));
                }
                break;
            case CHANNEL_SECONDARY_WHITE:
                if (command instanceof PercentType) {
                    sendGetRequest("/win&W2=" + new BigDecimal(command.toString()).multiply(new BigDecimal(2.55)));
                }
                break;
            case CHANNEL_MASTER_CONTROLS:
                if (command instanceof OnOffType) {
                    if (OnOffType.OFF.equals(command)) {
                        sendGetRequest("/win&TT=250&T=0");
                    } else {
                        sendGetRequest("/win&TT=1000&T=1");
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        if (masterBrightness.intValue() < 240) {
                            sendGetRequest("/win&TT=1000&A=~15"); // 255 divided by 15 = 17 different levels
                        } else {
                            sendGetRequest("/win&TT=1000&A=255");
                        }
                    } else {
                        if (masterBrightness.intValue() > 15) {
                            sendGetRequest("/win&TT=1000&A=~-15");
                        } else {
                            sendGetRequest("/win&TT=1000&A=0");
                        }
                    }
                } else if (command instanceof HSBType) {
                    if ((((HSBType) command).getBrightness()) == PercentType.ZERO) {
                        sendGetRequest("/win&TT=500&T=0");
                    }
                    masterBrightness = new BigDecimal((((HSBType) command).getBrightness()).toString())
                            .multiply(new BigDecimal(2.55));
                    primaryColor = new HSBType(command.toString());
                    if (primaryColor.getSaturation().intValue() < config.saturationThreshold) {
                        sendWhite();
                    } else if (primaryColor.getSaturation().intValue() == 32 && primaryColor.getHue().intValue() == 36
                            && hasWhite) {
                        // Google sends this when it wants white
                        sendWhite();
                    } else {
                        sendGetRequest(
                                "/win&TT=1000&FX=0&CY=0&CL=" + createColorHex(primaryColor) + "&A=" + masterBrightness);
                    }
                } else {// should only be PercentType left
                    masterBrightness = new BigDecimal(command.toString()).multiply(new BigDecimal(2.55));
                    sendGetRequest("/win&TT=1000&A=" + masterBrightness);
                }
                return;
            case CHANNEL_PRIMARY_COLOR:
                if (command instanceof OnOffType) {
                    return;
                } else if (command instanceof HSBType) {
                    primaryColor = new HSBType(command.toString());
                    sendGetRequest("/win&CL=" + createColorHex(primaryColor));
                } else if (command instanceof IncreaseDecreaseType) {
                    return;
                } else {// Percentype
                    primaryColor = new HSBType(primaryColor.getHue().toString() + ","
                            + primaryColor.getSaturation().toString() + ",command");
                    sendGetRequest("/win&CL=" + createColorHex(primaryColor));
                }
                return;
            case CHANNEL_SECONDARY_COLOR:
                if (command instanceof OnOffType) {
                    return;
                } else if (command instanceof HSBType) {
                    secondaryColor = new HSBType(command.toString());
                    sendGetRequest("/win&C2=" + createColorHex(secondaryColor));
                } else if (command instanceof IncreaseDecreaseType) {
                    return;
                } else {// Percentype
                    secondaryColor = new HSBType(secondaryColor.getHue().toString() + ","
                            + secondaryColor.getSaturation().toString() + ",command");
                    sendGetRequest("/win&C2=" + createColorHex(secondaryColor));
                }
                return;
            case CHANNEL_PALETTES:
                sendGetRequest("/win&FP=" + command);
                break;
            case CHANNEL_FX:
                sendGetRequest("/win&FX=" + command);
                break;
            case CHANNEL_SPEED:
                BigDecimal bigTemp = new BigDecimal(command.toString());
                if (OnOffType.OFF.equals(command)) {
                    bigTemp = BigDecimal.ZERO;
                } else if (OnOffType.ON.equals(command)) {
                    bigTemp = new BigDecimal(255);
                } else {
                    bigTemp = new BigDecimal(command.toString()).multiply(new BigDecimal(2.55));
                }
                sendGetRequest("/win&SX=" + bigTemp);
                break;
            case CHANNEL_INTENSITY:
                if (OnOffType.OFF.equals(command)) {
                    bigTemp = BigDecimal.ZERO;
                } else if (OnOffType.ON.equals(command)) {
                    bigTemp = new BigDecimal(255);
                } else {
                    bigTemp = new BigDecimal(command.toString()).multiply(new BigDecimal(2.55));
                }
                sendGetRequest("/win&IX=" + bigTemp);
                break;
            case CHANNEL_SLEEP:
                if (OnOffType.ON.equals(command)) {
                    sendGetRequest("/win&ND");
                } else {
                    sendGetRequest("/win&NL=0");
                }
                break;
            case CHANNEL_PRESETS:
                sendGetRequest("/win&PL=" + command);
                break;
            case CHANNEL_PRESET_DURATION:
                if (OnOffType.OFF.equals(command)) {
                    bigTemp = BigDecimal.ZERO;
                } else if (OnOffType.ON.equals(command)) {
                    bigTemp = new BigDecimal(255);
                } else {
                    bigTemp = new BigDecimal(command.toString()).multiply(new BigDecimal(600)).add(new BigDecimal(500));
                }
                sendGetRequest("/win&PT=" + bigTemp);
                break;
            case CHANNEL_TRANS_TIME:
                if (OnOffType.OFF.equals(command)) {
                    bigTemp = BigDecimal.ZERO;
                } else if (OnOffType.ON.equals(command)) {
                    bigTemp = new BigDecimal(255);
                } else {
                    bigTemp = new BigDecimal(command.toString()).multiply(new BigDecimal(600)).add(new BigDecimal(500));
                }
                sendGetRequest("/win&TT=" + bigTemp);
                break;
            case CHANNEL_PRESET_CYCLE:
                if (OnOffType.ON.equals(command)) {
                    sendGetRequest("/win&CY=1");
                } else {
                    sendGetRequest("/win&CY=0");
                }
                break;
        }
    }

    public void savePreset(int presetIndex) {
        if (presetIndex > 16) {
            logger.warn("Presets above 16 do not exist, and the action sent {}", presetIndex);
            return;
        }
        sendGetRequest("/win&PS=" + presetIndex);
    }

    private void pollLED() {
        sendGetRequest("/win");
    }

    @Override
    public void initialize() {
        config = getConfigAs(WLedConfiguration.class);
        if (!config.address.contains("://")) {
            logger.debug("Address was not entered in correct format, it may be the raw IP so adding http:// to start");
            config.address = "http://" + config.address;
        }
        pollingFuture = scheduler.scheduleWithFixedDelay(this::pollLED, 1, config.pollTime, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pollingFuture != null) {
            pollingFuture.cancel(true);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(WLedActions.class);
    }

    /**
     * @return A string that starts after finding the element and terminates when it finds the first occurrence of the
     *         end string after the element.
     */
    static String getValue(String message, String element, String end) {
        int startIndex = message.indexOf(element);
        if (startIndex != -1) // -1 means "not found"
        {
            int endIndex = message.indexOf(end, startIndex + element.length());
            if (endIndex != -1) {
                return message.substring(startIndex + element.length(), endIndex);
            }
        }
        return "";
    }
}
