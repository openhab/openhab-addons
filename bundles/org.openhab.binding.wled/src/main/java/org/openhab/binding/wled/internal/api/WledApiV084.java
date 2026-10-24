/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.wled.internal.api;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.wled.internal.WLedHelper;
import org.openhab.binding.wled.internal.WledState;
import org.openhab.binding.wled.internal.WledState.InfoResponse;
import org.openhab.binding.wled.internal.WledState.JsonResponse;
import org.openhab.binding.wled.internal.WledState.LedInfo;
import org.openhab.binding.wled.internal.WledState.StateResponse;
import org.openhab.binding.wled.internal.handlers.WLedBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WledApiV084} is the json Api methods for firmware version 0.8.4 and newer
 * as newer firmwares come out with breaking changes, extend this class into a newer firmware version class.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WledApiV084 implements WledApi {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Gson gson = new Gson();
    protected final HttpClient httpClient;
    protected final WLedBridgeHandler handler;
    protected final String address;
    protected WledState state = new WledState();
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
    private int version = 0;

    public WledApiV084(WLedBridgeHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.address = handler.config.address;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() throws ApiException {
        state.jsonResponse = getJson();
        state.infoResponse = getInfo();
        @Nullable
        LedInfo localLedInfo = gson.fromJson(state.infoResponse.leds.toString(), LedInfo.class);
        if (localLedInfo != null) {
            state.ledInfo = localLedInfo;
        }
        handler.hasWhite = state.ledInfo.rgbw;
    }

    @Override
    public String sendGetRequest(String url) throws ApiException {
        Request request = httpClient.newRequest(address + url);
        request.timeout(3, TimeUnit.SECONDS);
        request.method(HttpMethod.GET);
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip");
        logger.trace("Sending WLED GET:{}", url);
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() == 200) {
                return contentResponse.getContentAsString();
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
        throw new ApiException(errorReason);
    }

    protected String postState(String json) throws ApiException {
        return sendPostRequest("/json/state", json);
    }

    protected String sendPostRequest(String url, String json) throws ApiException {
        logger.debug("Sending WLED POST:{} Message:{}", url, json);
        Request request = httpClient.POST(address + url);
        request.timeout(3, TimeUnit.SECONDS);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.content(new StringContentProvider(json), "application/json");
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() == 200) {
                return contentResponse.getContentAsString();
            } else {
                errorReason = String.format("WLED request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
            }
        } catch (InterruptedException e) {
            errorReason = String.format("InterruptedException: %s", e.getMessage());
        } catch (TimeoutException e) {
            errorReason = "TimeoutException: WLED was not reachable on your network";
        } catch (ExecutionException e) {
            errorReason = String.format("ExecutionException: %s", e.getMessage());
        }
        throw new ApiException(errorReason);
    }

    protected void updateStateFromReply(String jsonState) {
        try {
            StateResponse response = gson.fromJson(jsonState, StateResponse.class);
            if (response == null) {
                throw new ApiException("Reply back from WLED when command was made is not valid JSON");
            }
            state.stateResponse = response;
            state.unpackJsonObjects();
            processBridgeStates();
            for (int count = 0; count < state.stateResponse.seg.length; count++) {
                processState(count);
            }
        } catch (JsonSyntaxException | ApiException e) {
            logger.debug("Reply back when a command was sent triggered an exception:{}", jsonState);
        }
    }

    protected StateResponse getState() throws ApiException {
        try {
            String returnContent = sendGetRequest("/json/state");
            StateResponse response = gson.fromJson(returnContent, StateResponse.class);
            if (response == null) {
                throw new ApiException("Could not GET:/json/state");
            }
            logger.trace("json/state:{}", returnContent);
            return response;
        } catch (JsonSyntaxException e) {
            throw new ApiException("JsonSyntaxException:{}", e);
        }
    }

    protected InfoResponse getInfo() throws ApiException {
        try {
            String returnContent = sendGetRequest("/json/info");
            InfoResponse response = gson.fromJson(returnContent, InfoResponse.class);
            if (response == null) {
                throw new ApiException("Could not GET:/json/info");
            }
            logger.trace("/json/info:{}", returnContent);
            return response;
        } catch (JsonSyntaxException e) {
            throw new ApiException("JsonSyntaxException:{}", e);
        }
    }

    protected JsonResponse getJson() throws ApiException {
        try {
            String returnContent = sendGetRequest("/json");
            JsonResponse response = gson.fromJson(returnContent, JsonResponse.class);
            if (response == null) {
                throw new ApiException("Could not GET:/json");
            }
            return response;
        } catch (JsonSyntaxException e) {
            throw new ApiException("JsonSyntaxException:{}", e);
        }
    }

    @Override
    public void update() throws ApiException {
        state.stateResponse = getState();
        state.unpackJsonObjects();
        processBridgeStates();
        for (int count = 0; count < state.stateResponse.seg.length; count++) {
            processState(count);
        }
    }

    @Override
    public List<StateOption> getUpdatedFxList() {
        List<StateOption> fxOptions = new ArrayList<>();
        int counter = 0;
        List<String> responseEffects = state.jsonResponse.effects;
        if (responseEffects != null) {
            for (String value : responseEffects) {
                fxOptions.add(new StateOption(Integer.toString(counter++), value));
            }
            if (handler.config.sortEffects) {
                fxOptions.sort(Comparator.comparing(o -> "0".equals(o.getValue()) ? "" : o.getLabel()));
            }
        } else {
            logger.debug(
                    "Effects in JSON response are missing (either bridge isn't initialized properly (yet) or WLED firmware does not return them)");
        }
        return fxOptions;
    }

    @Override
    public List<StateOption> getUpdatedPaletteList() {
        List<StateOption> palleteOptions = new ArrayList<>();
        int counter = 0;
        List<String> responsePalettes = state.jsonResponse.palettes;
        if (responsePalettes != null) {
            for (String value : responsePalettes) {
                palleteOptions.add(new StateOption(Integer.toString(counter++), value));
            }
            if (handler.config.sortPalettes) {
                palleteOptions.sort(Comparator.comparing(o -> "0".equals(o.getValue()) ? "" : o.getLabel()));
            }
        } else {
            logger.debug(
                    "Palettes in JSON response are missing (either bridge isn't initialized properly (yet) or WLED firmware does not return them)");
        }
        return palleteOptions;
    }

    @Override
    public int getFirmwareVersion() throws ApiException {
        state.infoResponse = getInfo();
        String temp = state.infoResponse.ver;
        logger.debug("Firmware for WLED is ver:{}", temp);
        // Only use the leading numbers, so versions like 0.14.0-b1 or 0.15.0-beta do not break the parsing
        Matcher matcher = VERSION_PATTERN.matcher(temp);
        if (!matcher.find()) {
            throw new ApiException("Could not understand the WLED firmware version:" + temp);
        }
        String patch = matcher.group(3);
        String digits = matcher.group(1) + matcher.group(2) + (patch == null ? "0" : patch);
        if (digits.length() > 4) {
            digits = digits.substring(0, 4);
        }
        version = Integer.parseInt(digits);
        handler.firmwareVersion = version;
        return version;
    }

    protected void processBridgeStates() throws ApiException {
        if (!state.stateResponse.on) {
            handler.update(CHANNEL_GLOBAL_BRIGHTNESS, OnOffType.OFF);
        } else {
            handler.update(CHANNEL_GLOBAL_BRIGHTNESS, new PercentType(
                    new BigDecimal(state.stateResponse.bri).divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
        }
        handler.update(CHANNEL_LIVE_OVERRIDE, new StringType(Integer.toString(state.stateResponse.lor)));
        handler.update(CHANNEL_PRESETS, new StringType(Integer.toString(state.stateResponse.ps)));
        if (state.nightLightState.on) {
            handler.update(CHANNEL_SLEEP, OnOffType.ON);
        } else {
            handler.update(CHANNEL_SLEEP, OnOffType.OFF);
        }
        if (state.udpnState.recv) {
            handler.update(CHANNEL_SYNC_RECEIVE, OnOffType.ON);
        } else {
            handler.update(CHANNEL_SYNC_RECEIVE, OnOffType.OFF);
        }
        if (state.udpnState.send) {
            handler.update(CHANNEL_SYNC_SEND, OnOffType.ON);
        } else {
            handler.update(CHANNEL_SYNC_SEND, OnOffType.OFF);
        }
        handler.update(CHANNEL_SYNC_SEND_GROUPS, new DecimalType(state.udpnState.sgrp));
        handler.update(CHANNEL_SYNC_RECEIVE_GROUPS, new DecimalType(state.udpnState.rgrp));
        handler.update(CHANNEL_MAIN_SEGMENT, new DecimalType(state.stateResponse.mainseg));
        if (state.stateResponse.pl == 0) {
            handler.update(CHANNEL_PRESET_CYCLE, OnOffType.ON);
        } else {
            handler.update(CHANNEL_PRESET_CYCLE, OnOffType.OFF);
        }
        handler.update(CHANNEL_TRANS_TIME, new QuantityType<>(
                new BigDecimal(state.stateResponse.transition).divide(BigDecimal.TEN), Units.SECOND));
        handler.update(CHANNEL_SLEEP_DURATION,
                new QuantityType<>(new BigDecimal(state.nightLightState.dur), Units.MINUTE));
        handler.update(CHANNEL_SLEEP_BRIGHTNESS, new PercentType(
                new BigDecimal(state.nightLightState.tbri).divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
        handler.update(CHANNEL_SLEEP_MODE, new StringType(Integer.toString(state.nightLightState.mode)));
        // rem is -1 when the sleep timer is not running
        handler.update(CHANNEL_SLEEP_REMAINING,
                new QuantityType<>(Math.max(0, state.nightLightState.rem), Units.SECOND));
    }

    protected void processState(int segmentIndex) throws ApiException {
        if (state.stateResponse.seg.length <= segmentIndex) {
            throw new ApiException(
                    "Segment " + segmentIndex + " is not currently setup correctly in the WLED firmware");
        }
        if (handler.handlerMissing(segmentIndex)) {
            // There is no thing setup for this segmentIndex.
            return;
        }
        HSBType tempHSB = WLedHelper.parseToHSBType(state.stateResponse.seg[segmentIndex].col[0].toString());
        handler.update(segmentIndex, CHANNEL_PRIMARY_COLOR, tempHSB);
        handler.update(segmentIndex, CHANNEL_SECONDARY_COLOR,
                WLedHelper.parseToHSBType(state.stateResponse.seg[segmentIndex].col[1].toString()));
        handler.update(segmentIndex, CHANNEL_THIRD_COLOR,
                WLedHelper.parseToHSBType(state.stateResponse.seg[segmentIndex].col[2].toString()));
        if (state.ledInfo.rgbw) {
            handler.update(segmentIndex, CHANNEL_PRIMARY_WHITE,
                    WLedHelper.parseWhitePercent(state.stateResponse.seg[segmentIndex].col[0].toString()));
            handler.update(segmentIndex, CHANNEL_SECONDARY_WHITE,
                    WLedHelper.parseWhitePercent(state.stateResponse.seg[segmentIndex].col[1].toString()));
            handler.update(segmentIndex, CHANNEL_THIRD_WHITE,
                    WLedHelper.parseWhitePercent(state.stateResponse.seg[segmentIndex].col[2].toString()));
        }
        // Global OFF or Segment OFF needs to be treated as OFF
        if (!state.stateResponse.seg[segmentIndex].on || !state.stateResponse.on) {
            handler.update(segmentIndex, CHANNEL_MASTER_CONTROLS, OnOffType.OFF);
            handler.update(segmentIndex, CHANNEL_SEGMENT_BRIGHTNESS, OnOffType.OFF);
        } else {
            handler.update(segmentIndex, CHANNEL_MASTER_CONTROLS, tempHSB);
            handler.update(segmentIndex, CHANNEL_SEGMENT_BRIGHTNESS,
                    new PercentType(new BigDecimal(state.stateResponse.seg[segmentIndex].bri).divide(BIG_DECIMAL_2_55,
                            RoundingMode.HALF_UP)));
        }
        if (state.stateResponse.seg[segmentIndex].mi) {
            handler.update(segmentIndex, CHANNEL_MIRROR, OnOffType.ON);
        } else {
            handler.update(segmentIndex, CHANNEL_MIRROR, OnOffType.OFF);
        }
        if (state.stateResponse.seg[segmentIndex].rev) {
            handler.update(segmentIndex, CHANNEL_REVERSE, OnOffType.ON);
        } else {
            handler.update(segmentIndex, CHANNEL_REVERSE, OnOffType.OFF);
        }
        handler.update(segmentIndex, CHANNEL_FX,
                new StringType(Integer.toString(state.stateResponse.seg[segmentIndex].fx)));
        handler.update(segmentIndex, CHANNEL_PALETTES,
                new StringType(Integer.toString(state.stateResponse.seg[segmentIndex].pal)));
        handler.update(segmentIndex, CHANNEL_SPEED,
                new PercentType(new BigDecimal(state.stateResponse.seg[segmentIndex].sx).divide(BIG_DECIMAL_2_55,
                        RoundingMode.HALF_UP)));
        handler.update(segmentIndex, CHANNEL_INTENSITY,
                new PercentType(new BigDecimal(state.stateResponse.seg[segmentIndex].ix).divide(BIG_DECIMAL_2_55,
                        RoundingMode.HALF_UP)));
        handler.update(segmentIndex, CHANNEL_GROUPING, new DecimalType(state.stateResponse.seg[segmentIndex].grp));
        handler.update(segmentIndex, CHANNEL_SPACING, new DecimalType(state.stateResponse.seg[segmentIndex].spc));
        handler.update(segmentIndex, CHANNEL_SELECTED, OnOffType.from(state.stateResponse.seg[segmentIndex].sel));
        handler.update(segmentIndex, CHANNEL_MAPPING,
                new StringType(Integer.toString(state.stateResponse.seg[segmentIndex].m12)));
        handler.update(segmentIndex, CHANNEL_SOUND_SIMULATION,
                new StringType(Integer.toString(state.stateResponse.seg[segmentIndex].si)));
        handler.update(segmentIndex, CHANNEL_SEGMENT_START,
                new DecimalType(state.stateResponse.seg[segmentIndex].start));
        handler.update(segmentIndex, CHANNEL_SEGMENT_STOP, new DecimalType(state.stateResponse.seg[segmentIndex].stop));
        String segmentName = state.stateResponse.seg[segmentIndex].n;
        if (segmentName != null) {
            handler.update(segmentIndex, CHANNEL_SEGMENT_NAME, new StringType(segmentName));
        }
    }

    @Override
    public void setGlobalOn(boolean bool) throws ApiException {
        updateStateFromReply(postState("{\"on\":" + bool + ",\"v\":true,\"tt\":2}"));
    }

    @Override
    public void setMasterOn(boolean bool, int segmentIndex) throws ApiException {
        updateStateFromReply(
                postState("{\"v\":true,\"tt\":2,\"seg\":[{\"id\":" + segmentIndex + ",\"on\":" + bool + "}]}"));
    }

    @Override
    public void setGlobalBrightness(PercentType percent) throws ApiException {
        if (percent.equals(PercentType.ZERO)) {
            updateStateFromReply(postState("{\"on\":false,\"v\":true}"));
            return;
        }
        updateStateFromReply(postState("{\"on\":true,\"v\":true,\"tt\":2,\"bri\":"
                + percent.toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "}"));
    }

    @Override
    public void setMasterBrightness(PercentType percent, int segmentIndex) throws ApiException {
        if (percent.equals(PercentType.ZERO)) {
            updateStateFromReply(postState("{\"v\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"on\":false}]}"));
            return;
        }
        updateStateFromReply(postState("{\"tt\":2,\"v\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"on\":true,\"bri\":"
                + percent.toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "}]}"));
    }

    @Override
    public void setMasterHSB(HSBType hsbType, int segmentIndex) throws ApiException {
        int[] rgb = ColorUtil.hsbToRgb(hsbType);
        if (hsbType.getBrightness().toBigDecimal().equals(BigDecimal.ZERO)) {
            updateStateFromReply(postState("{\"tt\":2,\"v\":true,\"seg\":[{\"on\":false,\"id\":" + segmentIndex
                    + ",\"fx\":0,\"col\":[[" + rgb[0] + "," + rgb[1] + "," + rgb[2] + "]]}]}"));
            return;
        }
        updateStateFromReply(postState("{\"tt\":2,\"v\":true,\"seg\":[{\"on\":true,\"id\":" + segmentIndex
                + ",\"fx\":0,\"col\":[[" + rgb[0] + "," + rgb[1] + "," + rgb[2] + "]]}]}"));
    }

    @Override
    public void setEffect(String string, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"fx\":" + string + "}]}");
    }

    @Override
    public void setPreset(String string) throws ApiException {
        updateStateFromReply(postState("{\"ps\":" + string + ",\"v\":true}"));
    }

    @Override
    public void setPalette(String string, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"pal\":" + string + "}]}");
    }

    @Override
    public void setFxIntencity(PercentType percentType, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"ix\":"
                + percentType.toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "}]}");
    }

    @Override
    public void setFxSpeed(PercentType percentType, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"sx\":"
                + percentType.toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "}]}");
    }

    @Override
    public void setSleep(boolean bool) throws ApiException {
        postState("{\"nl\":{\"on\":" + bool + "}}");
    }

    @Override
    public void setUdpSend(boolean bool) throws ApiException {
        postState("{\"udpn\":{\"send\":" + bool + "}}");
    }

    @Override
    public void setUdpReceive(boolean bool) throws ApiException {
        postState("{\"udpn\":{\"recv\":" + bool + "}}");
    }

    @Override
    public void setTransitionTime(BigDecimal time) throws ApiException {
        postState("{\"transition\":" + time + "}");
    }

    @Override
    public void setPresetCycle(boolean bool) throws ApiException {
        if (bool) {
            postState("{\"pl\":0}");
        } else {
            postState("{\"pl\":-1}");
        }
    }

    @Override
    public void setPrimaryColor(HSBType hsbType, int segmentIndex) throws ApiException {
        int[] rgb = ColorUtil.hsbToRgb(hsbType);
        postState("{\"on\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"col\":[[" + rgb[0] + "," + rgb[1] + "," + rgb[2]
                + "],[],[]]}]}");
    }

    @Override
    public void setSecondaryColor(HSBType hsbType, int segmentIndex) throws ApiException {
        int[] rgb = ColorUtil.hsbToRgb(hsbType);
        postState("{\"on\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"col\":[[],[" + rgb[0] + "," + rgb[1] + ","
                + rgb[2] + "],[]]}]}");
    }

    @Override
    public void setTertiaryColor(HSBType hsbType, int segmentIndex) throws ApiException {
        int[] rgb = ColorUtil.hsbToRgb(hsbType);
        postState("{\"on\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"col\":[[],[],[" + rgb[0] + "," + rgb[1] + ","
                + rgb[2] + "]]}]}");
    }

    @Override
    public void setWhiteOnly(PercentType percentType, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"on\":true,\"id\":" + segmentIndex + ",\"fx\":0,\"col\":[[0,0,0,"
                + percentType.toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "]]}]}");
    }

    @Override
    public void setMirror(boolean bool, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"mi\":" + bool + "}]}");
    }

    @Override
    public void setReverse(boolean bool, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"rev\":" + bool + "}]}");
    }

    @Override
    public void savePreset(int position, String presetName) throws ApiException {
        // named presets not supported in older firmwares, and max of 16.
        if (position > 16 || position < 1) {
            logger.warn("Preset position {} is not supported in this firmware version", position);
            return;
        }
        try {
            sendGetRequest("/win&PS=" + position);
        } catch (ApiException e) {
            logger.warn("Preset failed to save:{}", e.getMessage());
        }
    }

    @Override
    public void setWhiteTemperature(PercentType percent, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"cct\":" + WLedHelper.fromPercent(percent, 255) + "}]}");
    }

    @Override
    public void setCustomSlider(int sliderNumber, PercentType percent, int segmentIndex) throws ApiException {
        if (sliderNumber < 1 || sliderNumber > 3) {
            throw new ApiException("Custom slider " + sliderNumber + " does not exist");
        }
        // the third slider only has 32 steps
        int value = WLedHelper.fromPercent(percent, sliderNumber == 3 ? 31 : 255);
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"c" + sliderNumber + "\":" + value + "}]}");
    }

    @Override
    public void setOption(int optionNumber, boolean bool, int segmentIndex) throws ApiException {
        if (optionNumber < 1 || optionNumber > 3) {
            throw new ApiException("Option " + optionNumber + " does not exist");
        }
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"o" + optionNumber + "\":" + bool + "}]}");
    }

    @Override
    public void setFreeze(boolean bool, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"frz\":" + bool + "}]}");
    }

    @Override
    public void setSelected(boolean bool, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"sel\":" + bool + "}]}");
    }

    @Override
    public void setSegmentStart(int value, int segmentIndex) throws ApiException {
        updateStateFromReply(postState("{\"v\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"start\":" + value + "}]}"));
    }

    @Override
    public void setSegmentStop(int value, int segmentIndex) throws ApiException {
        updateStateFromReply(postState("{\"v\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"stop\":" + value + "}]}"));
    }

    @Override
    public void setSegmentName(String name, int segmentIndex) throws ApiException {
        updateStateFromReply(
                postState("{\"v\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"n\":" + gson.toJson(name) + "}]}"));
    }

    @Override
    public String getStateJson() throws ApiException {
        return sendGetRequest("/json/state");
    }

    @Override
    public String getInfoJson() throws ApiException {
        return sendGetRequest("/json/info");
    }

    @Override
    public String getConfigJson() throws ApiException {
        return sendGetRequest("/json/cfg");
    }

    @Override
    public String getNodesJson() throws ApiException {
        return sendGetRequest("/json/nodes");
    }

    @Override
    public String getLiveJson() throws ApiException {
        return sendGetRequest("/json/live");
    }

    @Override
    public String getEffectDataJson() throws ApiException {
        return sendGetRequest("/json/fxdata");
    }

    @Override
    public void setConfig(String json) throws ApiException {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (obj == null) {
                throw new ApiException("The JSON to send to WLED is empty");
            }
            sendPostRequest("/json/cfg", obj.toString());
        } catch (JsonSyntaxException e) {
            throw new ApiException("The JSON to send to WLED is not valid:" + e.getMessage());
        }
    }

    @Override
    public void setTime(long epochSeconds) throws ApiException {
        postState("{\"time\":" + epochSeconds + "}");
    }

    @Override
    public void setSyncSendGroups(int groups) throws ApiException {
        postState("{\"udpn\":{\"sgrp\":" + Math.max(0, Math.min(255, groups)) + "}}");
    }

    @Override
    public void setSyncReceiveGroups(int groups) throws ApiException {
        postState("{\"udpn\":{\"rgrp\":" + Math.max(0, Math.min(255, groups)) + "}}");
    }

    @Override
    public void setMainSegment(int segmentIndex) throws ApiException {
        postState("{\"mainseg\":" + segmentIndex + "}");
    }

    @Override
    public void setLedMap(int ledMap) throws ApiException {
        postState("{\"ledmap\":" + ledMap + "}");
    }

    @Override
    public void setAudioReactive(boolean bool) throws ApiException {
        setConfig("{\"um\":{\"AudioReactive\":{\"enabled\":" + bool + "}}}");
    }

    @Override
    public void setAudioSyncMode(String mode) throws ApiException {
        try {
            int value = Integer.parseInt(mode.trim());
            setConfig("{\"um\":{\"AudioReactive\":{\"sync\":{\"mode\":" + value + "}}}}");
        } catch (NumberFormatException e) {
            throw new ApiException("The audio sync mode must be a number:" + mode);
        }
    }

    @Override
    public void setMapping(String mapping, int segmentIndex) throws ApiException {
        try {
            postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"m12\":" + Integer.parseInt(mapping.trim()) + "}]}");
        } catch (NumberFormatException e) {
            throw new ApiException("The mapping must be a number:" + mapping);
        }
    }

    @Override
    public void setSoundSimulation(String type, int segmentIndex) throws ApiException {
        try {
            postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"si\":" + Integer.parseInt(type.trim()) + "}]}");
        } catch (NumberFormatException e) {
            throw new ApiException("The sound simulation must be a number:" + type);
        }
    }

    @Override
    public String getPresetsJson() throws ApiException {
        throw new ApiException("Reading presets is not supported in this firmware version");
    }

    @Override
    public String getPresetsText() throws ApiException {
        throw new ApiException("Reading presets is not supported in this firmware version");
    }

    @Override
    public void savePresetState(int position, String presetName, String stateJson) throws ApiException {
        throw new ApiException("Saving a preset from JSON is not supported in this firmware version");
    }

    @Override
    public void savePlaylist(int position, String playlistName, List<Integer> presets, List<Double> durationsSeconds,
            List<Double> transitionsSeconds, int repeat, int endPreset) throws ApiException {
        throw new ApiException("Saving a playlist is not supported in this firmware version");
    }

    @Override
    public void deletePreset(int position) throws ApiException {
        logger.warn("Deleting presets is not supported in this firmware version");
    }

    @Override
    public void reboot() throws ApiException {
        logger.warn("Rebooting is not supported in this firmware version");
    }

    @Override
    public void sendState(String json) throws ApiException {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (obj == null) {
                throw new ApiException("The JSON to send to WLED is empty");
            }
            updateStateFromReply(postState(obj.toString()));
        } catch (JsonSyntaxException e) {
            throw new ApiException("The JSON to send to WLED is not valid:" + e.getMessage());
        }
    }

    @Override
    public void setLiveOverride(String value) throws ApiException {
        postState("{\"lor\":" + value + "}");
    }

    @Override
    public void setGrouping(int value, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"grp\":" + value + "}]}");
    }

    @Override
    public void setSpacing(int value, int segmentIndex) throws ApiException {
        postState("{\"seg\":[{\"id\":" + segmentIndex + ",\"spc\":" + value + "}]}");
    }

    @Override
    public List<String> getSegmentNames() {
        List<String> segmentNames = new ArrayList<>(state.stateResponse.seg.length);
        for (int count = 0; count < state.stateResponse.seg.length; count++) {
            segmentNames.add("Segment " + count);
        }
        return segmentNames;
    }

    @Override
    public void setSleepMode(String value) throws ApiException {
        // Binding requires firmware 0.11.0 and newer
    }

    @Override
    public void setSleepDuration(BigDecimal time) throws ApiException {
        postState("{\"nl\":{\"dur\":" + time + "}}");
    }

    @Override
    public void setSleepTargetBrightness(PercentType percent) throws ApiException {
        postState("{\"nl\":{\"tbri\":" + percent.toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "}}");
    }

    @Override
    public void setLegacyWhite(String whiteChannel, PercentType brightness, int segmentIndex) throws ApiException {
        // only API>0.9.0 supports segments, so we have to set the value for all stripes here
        sendGetRequest(whiteChannel + brightness.toBigDecimal().multiply(BIG_DECIMAL_2_55));
    }
}
