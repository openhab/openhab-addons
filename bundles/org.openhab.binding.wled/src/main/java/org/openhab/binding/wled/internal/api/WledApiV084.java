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
import org.openhab.binding.wled.internal.WledState;
import org.openhab.binding.wled.internal.WledState.InfoResponse;
import org.openhab.binding.wled.internal.WledState.JsonResponse;
import org.openhab.binding.wled.internal.WledState.StateResponse;
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

    protected void sendPostRequest(String url, String json) throws ApiException {
        logger.trace("Sending WLED POST:{}", url);
        Request request = httpClient.POST(config.address + url);
        request.timeout(3, TimeUnit.SECONDS);
        request.header(HttpHeader.ACCEPT, "application/json");
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
    public int getFirmwareVersion() throws ApiException {
        state.jsonResponse = getJson();
        state.infoResponse = getInfo();
        getUpdatedFxList();
        getUpdatedPaletteList();
        String temp = state.infoResponse.ver;
        temp = temp.replaceAll("\\.", "");
        if (temp.length() > 4) {
            temp = temp.substring(0, 4);
        }
        int version = Integer.parseInt(temp);
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
        // logger.debug("col 1:{}", state.stateResponse.seg[handler.config.segmentIndex].col[0]);
        // logger.debug("col 2:{}", state.stateResponse.seg[handler.config.segmentIndex].col[1]);
        // logger.debug("col 3:{}", state.stateResponse.seg[handler.config.segmentIndex].col[2]);
    }
}
