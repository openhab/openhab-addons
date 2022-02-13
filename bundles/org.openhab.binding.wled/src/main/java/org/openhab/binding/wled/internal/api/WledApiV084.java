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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.wled.internal.WLedHandler;
import org.openhab.binding.wled.internal.WLedHelper;
import org.openhab.binding.wled.internal.WledState;
import org.openhab.binding.wled.internal.WledState.InfoResponse;
import org.openhab.binding.wled.internal.WledState.JsonResponse;
import org.openhab.binding.wled.internal.WledState.LedInfo;
import org.openhab.binding.wled.internal.WledState.StateResponse;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
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
    protected final WLedHandler handler;
    protected final String address;
    protected WledState state = new WledState();
    private int version = 0;

    public WledApiV084(WLedHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.address = handler.config.address;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() throws ApiException {
        state.jsonResponse = getJson();
        getUpdatedFxList();
        getUpdatedPaletteList();

        @Nullable
        LedInfo localLedInfo = gson.fromJson(state.infoResponse.leds.toString(), LedInfo.class);
        if (localLedInfo != null) {
            state.ledInfo = localLedInfo;
        }

        handler.hasWhite = state.ledInfo.rgbw;
        ArrayList<Channel> removeChannels = new ArrayList<>();
        if (!state.ledInfo.rgbw) {
            logger.debug("WLED is not setup to use RGBW, so removing un-needed white channels");
            Channel channel = handler.getThing().getChannel(CHANNEL_PRIMARY_WHITE);
            if (channel != null) {
                removeChannels.add(channel);
            }
            channel = handler.getThing().getChannel(CHANNEL_SECONDARY_WHITE);
            if (channel != null) {
                removeChannels.add(channel);
            }
            channel = handler.getThing().getChannel(CHANNEL_THIRD_WHITE);
            if (channel != null) {
                removeChannels.add(channel);
            }
        }
        handler.removeChannels(removeChannels);
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
            processState();
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
        processState();
    }

    protected void getUpdatedFxList() {
        List<StateOption> fxOptions = new ArrayList<>();
        int counter = 0;
        for (String value : state.jsonResponse.effects) {
            fxOptions.add(new StateOption(Integer.toString(counter++), value));
        }
        if (handler.config.sortEffects) {
            fxOptions.sort(Comparator.comparing(o -> o.getValue().equals("0") ? "" : o.getLabel()));
        }
        handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_FX),
                fxOptions);
    }

    protected void getUpdatedPaletteList() {
        List<StateOption> palleteOptions = new ArrayList<>();
        int counter = 0;
        for (String value : state.jsonResponse.palettes) {
            palleteOptions.add(new StateOption(Integer.toString(counter++), value));
        }
        if (handler.config.sortPalettes) {
            palleteOptions.sort(Comparator.comparing(o -> o.getValue().equals("0") ? "" : o.getLabel()));
        }
        handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_PALETTES),
                palleteOptions);
    }

    @Override
    public int getFirmwareVersion() throws ApiException {
        state.infoResponse = getInfo();
        String temp = state.infoResponse.ver;
        logger.debug("Firmware for WLED is ver:{}", temp);
        temp = temp.replaceAll("\\.", "");
        if (temp.length() > 4) {
            temp = temp.substring(0, 4);
        }
        version = Integer.parseInt(temp);
        return version;
    }

    protected void processState() throws ApiException {
        if (state.stateResponse.seg.length <= handler.config.segmentIndex) {
            throw new ApiException("Segment " + handler.config.segmentIndex
                    + " is not currently setup correctly in the WLED firmware");
        }
        HSBType tempHSB = WLedHelper
                .parseToHSBType(state.stateResponse.seg[handler.config.segmentIndex].col[0].toString());
        handler.update(CHANNEL_PRIMARY_COLOR, tempHSB);
        handler.update(CHANNEL_SECONDARY_COLOR,
                WLedHelper.parseToHSBType(state.stateResponse.seg[handler.config.segmentIndex].col[1].toString()));
        handler.update(CHANNEL_THIRD_COLOR,
                WLedHelper.parseToHSBType(state.stateResponse.seg[handler.config.segmentIndex].col[2].toString()));
        if (state.ledInfo.rgbw) {
            handler.update(CHANNEL_PRIMARY_WHITE, WLedHelper
                    .parseWhitePercent(state.stateResponse.seg[handler.config.segmentIndex].col[0].toString()));
            handler.update(CHANNEL_SECONDARY_WHITE, WLedHelper
                    .parseWhitePercent(state.stateResponse.seg[handler.config.segmentIndex].col[1].toString()));
            handler.update(CHANNEL_THIRD_WHITE, WLedHelper
                    .parseWhitePercent(state.stateResponse.seg[handler.config.segmentIndex].col[2].toString()));
        }
        // Global OFF or Segment OFF needs to be treated as OFF
        if (!state.stateResponse.seg[handler.config.segmentIndex].on || !state.stateResponse.on) {
            handler.update(CHANNEL_MASTER_CONTROLS, OnOffType.OFF);
            handler.update(CHANNEL_SEGMENT_BRIGHTNESS, OnOffType.OFF);
        } else {
            handler.update(CHANNEL_MASTER_CONTROLS, tempHSB);
            handler.update(CHANNEL_SEGMENT_BRIGHTNESS,
                    new PercentType(new BigDecimal(state.stateResponse.seg[handler.config.segmentIndex].bri)
                            .divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
        }
        if (state.nightLightState.on) {
            handler.update(CHANNEL_SLEEP, OnOffType.ON);
        } else {
            handler.update(CHANNEL_SLEEP, OnOffType.OFF);
        }
        if (state.stateResponse.pl == 0) {
            handler.update(CHANNEL_PRESET_CYCLE, OnOffType.ON);
        } else {
            handler.update(CHANNEL_PRESET_CYCLE, OnOffType.OFF);
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
        if (state.stateResponse.seg[handler.config.segmentIndex].mi) {
            handler.update(CHANNEL_MIRROR, OnOffType.ON);
        } else {
            handler.update(CHANNEL_MIRROR, OnOffType.OFF);
        }
        if (state.stateResponse.seg[handler.config.segmentIndex].rev) {
            handler.update(CHANNEL_REVERSE, OnOffType.ON);
        } else {
            handler.update(CHANNEL_REVERSE, OnOffType.OFF);
        }
        handler.update(CHANNEL_TRANS_TIME, new QuantityType<>(
                new BigDecimal(state.stateResponse.transition).divide(BigDecimal.TEN), Units.SECOND));
        handler.update(CHANNEL_PRESETS, new StringType(Integer.toString(state.stateResponse.ps)));
        handler.update(CHANNEL_FX,
                new StringType(Integer.toString(state.stateResponse.seg[handler.config.segmentIndex].fx)));
        handler.update(CHANNEL_PALETTES,
                new StringType(Integer.toString(state.stateResponse.seg[handler.config.segmentIndex].pal)));
        handler.update(CHANNEL_SPEED,
                new PercentType(new BigDecimal(state.stateResponse.seg[handler.config.segmentIndex].sx)
                        .divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
        handler.update(CHANNEL_INTENSITY,
                new PercentType(new BigDecimal(state.stateResponse.seg[handler.config.segmentIndex].ix)
                        .divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
        handler.update(CHANNEL_LIVE_OVERRIDE, new StringType(Integer.toString(state.stateResponse.lor)));
        handler.update(CHANNEL_GROUPING, new DecimalType(state.stateResponse.seg[handler.config.segmentIndex].grp));
        handler.update(CHANNEL_SPACING, new DecimalType(state.stateResponse.seg[handler.config.segmentIndex].spc));
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
        if (hsbType.getBrightness().toBigDecimal().equals(BigDecimal.ZERO)) {
            updateStateFromReply(postState("{\"tt\":2,\"v\":true,\"seg\":[{\"on\":false,\"id\":" + segmentIndex
                    + ",\"fx\":0,\"col\":[[" + hsbType.getRed().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue()
                    + "," + hsbType.getGreen().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                    + hsbType.getBlue().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "]]}]}"));
            return;
        }
        updateStateFromReply(postState("{\"tt\":2,\"v\":true,\"seg\":[{\"on\":true,\"id\":" + segmentIndex
                + ",\"fx\":0,\"col\":[[" + hsbType.getRed().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getGreen().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getBlue().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "]]}]}"));
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
    public void setUdpRecieve(boolean bool) throws ApiException {
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
        postState("{\"on\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"col\":[["
                + hsbType.getRed().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getGreen().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getBlue().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "],[],[]]}]}");
    }

    @Override
    public void setSecondaryColor(HSBType hsbType, int segmentIndex) throws ApiException {
        postState("{\"on\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"col\":[[],["
                + hsbType.getRed().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getGreen().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getBlue().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "],[]]}]}");
    }

    @Override
    public void setTertiaryColor(HSBType hsbType, int segmentIndex) throws ApiException {
        postState("{\"on\":true,\"seg\":[{\"id\":" + segmentIndex + ",\"col\":[[],[],["
                + hsbType.getRed().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getGreen().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getBlue().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "]]}]}");
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
}
