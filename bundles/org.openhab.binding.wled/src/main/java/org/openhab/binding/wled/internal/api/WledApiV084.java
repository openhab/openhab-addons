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
package org.openhab.binding.wled.internal.api;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.wled.internal.WLedConfiguration;
import org.openhab.binding.wled.internal.WLedHandler;
import org.openhab.binding.wled.internal.WLedHelper;
import org.openhab.binding.wled.internal.WledState;
import org.openhab.binding.wled.internal.WledState.InfoResponse;
import org.openhab.binding.wled.internal.WledState.JsonResponse;
import org.openhab.binding.wled.internal.WledState.StateResponse;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WledApiV084} is the json Api methods for firmware version 0.8.4 and newer
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WledApiV084 implements WledApi {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Gson gson = new Gson();
    protected final HttpClient httpClient;
    protected final WLedConfiguration config;
    protected final WLedHandler handler;
    protected WledState state = new WledState();
    private int version = 0;

    public WledApiV084(WLedHandler handler, WLedConfiguration config, HttpClient httpClient) {
        this.handler = handler;
        this.config = config;
        this.httpClient = httpClient;
    }

    protected String sendGetRequest(String url) throws ApiException {
        Request request = httpClient.newRequest(config.address + url);
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

    protected void postState(String json) throws ApiException {
        sendPostRequest("/json/state", json);
    }

    protected void sendPostRequest(String url, String json) throws ApiException {
        logger.debug("Sending WLED POST:{} Message:{}", url, json);
        Request request = httpClient.POST(config.address + url);
        request.timeout(3, TimeUnit.SECONDS);
        // request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.content(new StringContentProvider(json), "application/json");
        String errorReason = "";
        try {
            request.send();
            return;
        } catch (InterruptedException e) {
            errorReason = String.format("InterruptedException: %s", e.getMessage());
        } catch (TimeoutException e) {
            errorReason = "TimeoutException: WLED was not reachable on your network";
        } catch (ExecutionException e) {
            errorReason = String.format("ExecutionException: %s", e.getMessage());
        }
        throw new ApiException(errorReason);
    }

    protected StateResponse getState() throws ApiException {
        try {
            String returnContent = sendGetRequest("/json/state");
            StateResponse response = gson.fromJson(returnContent, StateResponse.class);
            if (response == null) {
                throw new ApiException("Could not GET:/json/state");
            }
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
        handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_FX),
                fxOptions);
    }

    protected void getUpdatedPaletteList() {
        List<StateOption> palleteOptions = new ArrayList<>();
        int counter = 0;
        for (String value : state.jsonResponse.palettes) {
            palleteOptions.add(new StateOption(Integer.toString(counter++), value));
        }
        handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_PALETTES),
                palleteOptions);
    }

    @Override
    public void initialize() throws ApiException {
        state.jsonResponse = getJson();
        getUpdatedFxList();
        getUpdatedPaletteList();
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

    protected void processState() {
        if (!state.stateResponse.on) {
            handler.update(CHANNEL_MASTER_CONTROLS, OnOffType.OFF);
        } else {
            handler.update(CHANNEL_MASTER_CONTROLS, new PercentType(
                    new BigDecimal(state.stateResponse.bri).divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
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
        handler.update(CHANNEL_TRANS_TIME, new PercentType(
                new BigDecimal(state.stateResponse.transition).divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
        handler.update(CHANNEL_PRESETS, new StringType("" + state.stateResponse.ps));
        handler.update(CHANNEL_FX, new StringType("" + state.stateResponse.seg[handler.config.segmentIndex].fx));
        handler.update(CHANNEL_PALETTES, new StringType("" + state.stateResponse.seg[handler.config.segmentIndex].pal));
        handler.update(CHANNEL_SPEED,
                new PercentType(new BigDecimal(state.stateResponse.seg[handler.config.segmentIndex].sx)
                        .divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));
        handler.update(CHANNEL_INTENSITY,
                new PercentType(new BigDecimal(state.stateResponse.seg[handler.config.segmentIndex].ix)
                        .divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP)));

        handler.update(CHANNEL_PRIMARY_COLOR,
                WLedHelper.parseToHSBType(state.stateResponse.seg[handler.config.segmentIndex].col[0].toString()));
        handler.update(CHANNEL_SECONDARY_COLOR,
                WLedHelper.parseToHSBType(state.stateResponse.seg[handler.config.segmentIndex].col[1].toString()));
        handler.update(CHANNEL_THIRD_COLOR,
                WLedHelper.parseToHSBType(state.stateResponse.seg[handler.config.segmentIndex].col[2].toString()));
        // white LEDs for RGBW strings
        handler.update(CHANNEL_PRIMARY_WHITE,
                WLedHelper.parseWhitePercent(state.stateResponse.seg[handler.config.segmentIndex].col[0].toString()));
        handler.update(CHANNEL_SECONDARY_WHITE,
                WLedHelper.parseWhitePercent(state.stateResponse.seg[handler.config.segmentIndex].col[1].toString()));
        handler.update(CHANNEL_THIRD_WHITE,
                WLedHelper.parseWhitePercent(state.stateResponse.seg[handler.config.segmentIndex].col[2].toString()));
    }

    @Override
    public void setMasterOn(boolean bool) throws ApiException {
        postState("{\"on\":" + bool + "}");
    }

    @Override
    public void setMasterBrightness(PercentType percent) throws ApiException {
        if (percent.equals(PercentType.ZERO)) {
            postState("{\"on\":false}");
            return;
        }
        postState("{\"on\":true,\"bri\":" + percent.toBigDecimal().multiply(BIG_DECIMAL_2_55) + "}");
    }

    @Override
    public void setMasterHSB(HSBType hsbType) throws ApiException {
        /*
         * primaryColor = (HSBType) command;
         * hue65535 = primaryColor.getHue().toBigDecimal().multiply(BIG_DECIMAL_182_04);
         * saturation255 = primaryColor.getSaturation().toBigDecimal().multiply(BIG_DECIMAL_2_55);
         * masterBrightness255 = primaryColor.getBrightness().toBigDecimal().multiply(BIG_DECIMAL_2_55);
         * if (primaryColor.getSaturation().intValue() < config.saturationThreshold) {
         * sendWhite();
         * } else if (primaryColor.getSaturation().intValue() == 32 && primaryColor.getHue().intValue() == 36
         * && hasWhite) {
         * // Google sends this when it wants white
         * sendWhite();
         * } else {
         * if (config.segmentIndex == -1) {
         * sendGetRequest(
         * "/win&TT=1000&FX=0&CY=0&HU=" + hue65535 + "&SA=" + saturation255 + "&A=" + masterBrightness255);
         * } else {
         * sendGetRequest(
         * "/win&TT=1000&FX=0&CY=0&CL=" + createColorHex(primaryColor) + "&A=" + masterBrightness255);
         * }
         * }
         */
        if (hsbType.getBrightness().equals(PercentType.ZERO)) {
            postState("{\"on\":false}");
            return;
        }
        postState("{\"on\":true,\"seg\":[{\"col\":[["
                + hsbType.getRed().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getGreen().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + ","
                + hsbType.getBlue().toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "]]}]}");
    }

    @Override
    public void setPreset(String string) throws ApiException {
        postState("{\"seg\":[{\"fx\":" + string + "}]}");
    }

    @Override
    public void setPalette(String string) throws ApiException {
        postState("{\"seg\":[{\"pal\":" + string + "}]}");
    }

    @Override
    public void setFxIntencity(PercentType percentType) throws ApiException {
        postState("{\"seg\":[{\"ix\":" + percentType.toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "}]}");
    }

    @Override
    public void setFxSpeed(PercentType percentType) throws ApiException {
        postState("{\"seg\":[{\"sx\":" + percentType.toBigDecimal().multiply(BIG_DECIMAL_2_55).intValue() + "}]}");
    }

    @Override
    public void setSleep(boolean b) throws ApiException {
        postState("{\"nl\":{\"on\":" + b + "}}");
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
    public void setTransitionTime(int milliseconds) throws ApiException {
        postState("{\"transition\":" + milliseconds + "}");
    }

    @Override
    public void setPresetCycle(boolean bool) throws ApiException {
        if (bool) {
            postState("{\"pl\":0}");
        } else {
            postState("{\"pl\":-1}");
        }
    }
}
