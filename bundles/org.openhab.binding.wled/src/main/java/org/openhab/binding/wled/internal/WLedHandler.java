/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.concurrent.Future;
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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
    private BigDecimal hue65535 = BigDecimal.ZERO;
    private BigDecimal saturation255 = BigDecimal.ZERO;
    private BigDecimal masterBrightness255 = BigDecimal.ZERO;
    private HSBType primaryColor = new HSBType();
    private BigDecimal primaryWhite = BigDecimal.ZERO;
    private HSBType secondaryColor = new HSBType();
    private BigDecimal secondaryWhite = BigDecimal.ZERO;
    private boolean hasWhite = false;
    private WLedConfiguration config = new WLedConfiguration();

    public WLedHandler(Thing thing, HttpClient httpClient,
            WledDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    private void sendGetRequest(String url) {
        Request request;
        if (url.contains("json") || config.segmentIndex == -1) {
            request = httpClient.newRequest(config.address + url);
        } else {
            request = httpClient.newRequest(config.address + url + "&SM=" + config.segmentIndex);
        }
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
            logger.warn("NumberFormatException when parsing the WLED color fields:{}", e.getMessage());
        }
        return HSBType.fromRGB(r, g, b);
    }

    private void parseColours(String message) {
        primaryColor = parseToHSBType(message, "<cl>");
        updateState(CHANNEL_PRIMARY_COLOR, primaryColor);
        secondaryColor = parseToHSBType(message, "<cs>");
        updateState(CHANNEL_SECONDARY_COLOR, secondaryColor);
        try {
            primaryWhite = new BigDecimal(WLedHelper.getValue(message, "<wv>", "<"));
            if (primaryWhite.intValue() > -1) {
                hasWhite = true;
                updateState(CHANNEL_PRIMARY_WHITE,
                        new PercentType(primaryWhite.divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
                secondaryWhite = new BigDecimal(WLedHelper.getValue(message, "<ws>", "<"));
                updateState(CHANNEL_SECONDARY_WHITE,
                        new PercentType(secondaryWhite.divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("IllegalArgumentException when parsing the WLED colour and white fields:{}", e.getMessage());
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
        for (String value : WLedHelper.getValue(message, "\"effects\":[", "]").replace("\"", "").split(",")) {
            fxOptions.add(new StateOption(Integer.toString(counter++), value));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_FX), fxOptions);
        counter = 0;
        for (String value : (WLedHelper.getValue(message, "\"palettes\":[", "]").replace("\"", "")).split(",")) {
            palleteOptions.add(new StateOption(Integer.toString(counter++), value));
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
            masterBrightness255 = new BigDecimal(WLedHelper.getValue(message, "<ac>", "<"));
            updateState(CHANNEL_MASTER_CONTROLS,
                    new PercentType(masterBrightness255.divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
        }
        if (message.contains("<ix>0</ix>")) {
            updateState(CHANNEL_INTENSITY, OnOffType.OFF);
        } else {
            BigDecimal bigTemp = new BigDecimal(WLedHelper.getValue(message, "<ix>", "<")).divide(BIG_DECIMAL_2_55,
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
        if (message.contains("<ns>1</ns>")) {
            updateState(CHANNEL_SYNC_SEND, OnOffType.ON);
        } else {
            updateState(CHANNEL_SYNC_SEND, OnOffType.OFF);
        }
        if (message.contains("<nr>1</nr>")) {
            updateState(CHANNEL_SYNC_RECEIVE, OnOffType.ON);
        } else {
            updateState(CHANNEL_SYNC_RECEIVE, OnOffType.OFF);
        }
        if (message.contains("<fx>")) {
            updateState(CHANNEL_FX, new StringType(WLedHelper.getValue(message, "<fx>", "<")));
        }
        if (message.contains("<sx>")) {
            BigDecimal bigTemp = new BigDecimal(WLedHelper.getValue(message, "<sx>", "<")).divide(BIG_DECIMAL_2_55,
                    RoundingMode.HALF_UP);
            updateState(CHANNEL_SPEED, new PercentType(bigTemp));
        }
        if (message.contains("<fp>")) {
            updateState(CHANNEL_PALETTES, new StringType(WLedHelper.getValue(message, "<fp>", "<")));
        }
        parseColours(message);
    }

    private void sendWhite() {
        if (hasWhite) {
            sendGetRequest("/win&TT=1000&FX=0&CY=0&CL=hFF000000" + "&A=" + masterBrightness255);
        } else {
            sendGetRequest("/win&TT=1000&FX=0&CY=0&CL=hFFFFFF" + "&A=" + masterBrightness255);
        }
    }

    /**
     *
     * @param hsb
     * @return WLED needs the letter h followed by 2 digit HEX code for RRGGBB
     */
    private String createColorHex(HSBType hsb) {
        return String.format("h%06X", hsb.getRGB() & 0x00FFFFFF);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BigDecimal bigTemp;
        PercentType localPercentType;
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_MASTER_CONTROLS:
                    sendGetRequest("/win");
            }
            return;// no need to check for refresh below
        }
        logger.debug("command {} sent to {}", command, channelUID.getId());
        switch (channelUID.getId()) {
            case CHANNEL_SYNC_SEND:
                if (OnOffType.OFF.equals(command)) {
                    sendGetRequest("/win&NS=0");
                } else {
                    sendGetRequest("/win&NS=1");
                }
                break;
            case CHANNEL_SYNC_RECEIVE:
                if (OnOffType.OFF.equals(command)) {
                    sendGetRequest("/win&NR=0");
                } else {
                    sendGetRequest("/win&NR=1");
                }
                break;
            case CHANNEL_PRIMARY_WHITE:
                if (command instanceof PercentType) {
                    sendGetRequest("/win&W=" + ((PercentType) command).toBigDecimal().multiply(BIG_DECIMAL_2_55));
                }
                break;
            case CHANNEL_SECONDARY_WHITE:
                if (command instanceof PercentType) {
                    sendGetRequest("/win&W2=" + ((PercentType) command).toBigDecimal().multiply(BIG_DECIMAL_2_55));
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
                        if (masterBrightness255.intValue() < 240) {
                            sendGetRequest("/win&TT=1000&A=~15"); // 255 divided by 15 = 17 different levels
                        } else {
                            sendGetRequest("/win&TT=1000&A=255");
                        }
                    } else {
                        if (masterBrightness255.intValue() > 15) {
                            sendGetRequest("/win&TT=1000&A=~-15");
                        } else {
                            sendGetRequest("/win&TT=1000&A=0");
                        }
                    }
                } else if (command instanceof HSBType) {
                    if ((((HSBType) command).getBrightness()) == PercentType.ZERO) {
                        sendGetRequest("/win&TT=500&T=0");
                    }
                    primaryColor = (HSBType) command;
                    hue65535 = primaryColor.getHue().toBigDecimal().multiply(BIG_DECIMAL_182_04);
                    saturation255 = primaryColor.getSaturation().toBigDecimal().multiply(BIG_DECIMAL_2_55);
                    masterBrightness255 = primaryColor.getBrightness().toBigDecimal().multiply(BIG_DECIMAL_2_55);
                    if (primaryColor.getSaturation().intValue() < config.saturationThreshold) {
                        sendWhite();
                    } else if (primaryColor.getSaturation().intValue() == 32 && primaryColor.getHue().intValue() == 36
                            && hasWhite) {
                        // Google sends this when it wants white
                        sendWhite();
                    } else {
                        if (config.segmentIndex == -1) {
                            sendGetRequest("/win&TT=1000&FX=0&CY=0&HU=" + hue65535 + "&SA=" + saturation255 + "&A="
                                    + masterBrightness255);
                        } else {
                            sendGetRequest("/win&TT=1000&FX=0&CY=0&CL=" + createColorHex(primaryColor) + "&A="
                                    + masterBrightness255);
                        }
                    }
                } else if (command instanceof PercentType) {
                    masterBrightness255 = ((PercentType) command).toBigDecimal().multiply(BIG_DECIMAL_2_55);
                    sendGetRequest("/win&TT=1000&A=" + masterBrightness255);
                }
                return;
            case CHANNEL_PRIMARY_COLOR:
                if (command instanceof HSBType) {
                    primaryColor = (HSBType) command;
                    sendGetRequest("/win&CL=" + createColorHex(primaryColor));
                } else if (command instanceof PercentType) {
                    primaryColor = new HSBType(primaryColor.getHue(), primaryColor.getSaturation(),
                            ((PercentType) command));
                    sendGetRequest("/win&CL=" + createColorHex(primaryColor));
                }
                return;
            case CHANNEL_SECONDARY_COLOR:
                if (command instanceof HSBType) {
                    secondaryColor = (HSBType) command;
                    sendGetRequest("/win&C2=" + createColorHex(secondaryColor));
                } else if (command instanceof PercentType) {
                    secondaryColor = new HSBType(secondaryColor.getHue(), secondaryColor.getSaturation(),
                            ((PercentType) command));
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
                localPercentType = ((State) command).as(PercentType.class);
                if (localPercentType != null) {
                    bigTemp = localPercentType.toBigDecimal().multiply(BIG_DECIMAL_2_55);
                    sendGetRequest("/win&SX=" + bigTemp);
                }
                break;
            case CHANNEL_INTENSITY:
                localPercentType = ((State) command).as(PercentType.class);
                if (localPercentType != null) {
                    bigTemp = localPercentType.toBigDecimal().multiply(BIG_DECIMAL_2_55);
                    sendGetRequest("/win&IX=" + bigTemp);
                }
                break;
            case CHANNEL_SLEEP:
                if (OnOffType.ON.equals(command)) {
                    sendGetRequest("/win&NL=1");
                } else {
                    sendGetRequest("/win&NL=0");
                }
                break;
            case CHANNEL_PRESETS:
                sendGetRequest("/win&PL=" + command);
                break;
            case CHANNEL_PRESET_DURATION:
                if (command instanceof QuantityType) {
                    QuantityType<?> seconds = ((QuantityType<?>) command).toUnit(Units.SECOND);
                    if (seconds != null) {
                        bigTemp = new BigDecimal(seconds.intValue()).multiply(new BigDecimal(1000));
                        sendGetRequest("/win&PT=" + bigTemp.intValue());
                    }
                }
                break;
            case CHANNEL_TRANS_TIME:
                if (command instanceof QuantityType) {
                    QuantityType<?> seconds = ((QuantityType<?>) command).toUnit(Units.SECOND);
                    if (seconds != null) {
                        bigTemp = new BigDecimal(seconds.intValue()).multiply(new BigDecimal(1000));
                        sendGetRequest("/win&TT=" + bigTemp.intValue());
                    }
                }
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
        Future<?> future = pollingFuture;
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(WLedActions.class);
    }
}
