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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wled.internal.WLedHelper;
import org.openhab.binding.wled.internal.WledState.InfoResponse;
import org.openhab.binding.wled.internal.WledState.LedInfo;
import org.openhab.binding.wled.internal.WledState.SegmentState;
import org.openhab.binding.wled.internal.WledState.StateResponse;
import org.openhab.binding.wled.internal.WledState.Wifi;
import org.openhab.binding.wled.internal.handlers.WLedBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WledApiV0140} is the json Api methods for firmware version 0.14.0 and newer
 * as newer firmwares come out with breaking changes, extend this class into a newer firmware version class.
 * <p>
 * This version reads the state and the info of the device with one request, which allows the binding to show
 * information like the uptime and the Wi-Fi signal. It also supports the custom sliders, options and the freeze of
 * the effects.
 *
 * @author miloit - Initial contribution
 */
@NonNullByDefault
public class WledApiV0140 extends WledApiV0130 {

    private static final int CONFIG_POLL_INTERVAL = 20;
    private int configCounter = 0;

    public WledApiV0140(WLedBridgeHandler handler, HttpClient httpClient) {
        super(handler, httpClient);
    }

    @Override
    public void initialize() throws ApiException {
        super.initialize();
        handler.hasCct = state.ledInfo.hasCct();
        Map<String, String> properties = new HashMap<>();
        String name = state.infoResponse.name;
        if (name != null && !name.isBlank()) {
            properties.put(PROPERTY_NAME, name);
        }
        String arch = state.infoResponse.arch;
        if (arch != null && !arch.isBlank()) {
            properties.put(PROPERTY_ARCHITECTURE, arch);
        }
        String brand = state.infoResponse.brand;
        if (brand != null && !brand.isBlank()) {
            properties.put(PROPERTY_BRAND, brand);
        }
        properties.put(PROPERTY_LED_COUNT, Integer.toString(state.ledInfo.count));
        properties.put("firmwareVersion", state.infoResponse.ver);
        handler.updateInfoProperties(properties);
    }

    @Override
    public void update() throws ApiException {
        JsonObject root;
        try {
            root = gson.fromJson(sendGetRequest("/json/si"), JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new ApiException("JsonSyntaxException:" + e.getMessage(), e);
        }
        if (root == null || !root.has("state") || !root.has("info")) {
            throw new ApiException("Could not GET:/json/si");
        }
        try {
            StateResponse stateResponse = gson.fromJson(root.get("state"), StateResponse.class);
            InfoResponse infoResponse = gson.fromJson(root.get("info"), InfoResponse.class);
            if (stateResponse == null || infoResponse == null) {
                throw new ApiException("Could not GET:/json/si");
            }
            state.stateResponse = stateResponse;
            state.infoResponse = infoResponse;
            JsonElement leds = root.getAsJsonObject("info").get("leds");
            if (leds != null) {
                LedInfo ledInfo = gson.fromJson(leds, LedInfo.class);
                if (ledInfo != null) {
                    state.ledInfo = ledInfo;
                }
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("JsonSyntaxException:" + e.getMessage(), e);
        }
        state.unpackJsonObjects();
        processBridgeStates();
        processInfo();
        if (configCounter-- <= 0) {
            configCounter = CONFIG_POLL_INTERVAL;
            processConfig();
        }
        refreshUnknownPreset();
        for (int count = 0; count < state.stateResponse.seg.length; count++) {
            processState(count);
        }
    }

    /**
     * The settings of the audio usermod are in the config of the device, so they are only read now and then.
     */
    protected void processConfig() {
        try {
            String audioReactive = WLedHelper.getJsonPath(getConfigJson(), "um.AudioReactive");
            if (audioReactive == null) {
                return;
            }
            String enabled = WLedHelper.getJsonPath(audioReactive, "enabled");
            if (enabled != null) {
                handler.update(CHANNEL_AUDIO_REACTIVE, OnOffType.from(Boolean.parseBoolean(enabled)));
            }
            String mode = WLedHelper.getJsonPath(audioReactive, "sync.mode");
            if (mode != null) {
                handler.update(CHANNEL_AUDIO_SYNC_MODE, new StringType(mode));
            }
        } catch (ApiException | JsonSyntaxException e) {
            logger.debug("Could not read the audio settings:{}", e.getMessage());
        }
    }

    @Override
    public void setAudioReactive(boolean bool) throws ApiException {
        super.setAudioReactive(bool);
        processConfig();
    }

    @Override
    public void setAudioSyncMode(String mode) throws ApiException {
        super.setAudioSyncMode(mode);
        processConfig();
    }

    protected void processInfo() {
        handler.update(CHANNEL_LIVE_ACTIVE, OnOffType.from(state.infoResponse.live));
        String liveMode = state.infoResponse.lm;
        handler.update(CHANNEL_LIVE_MODE, new StringType(liveMode == null ? "" : liveMode));
        String liveSource = state.infoResponse.lip;
        handler.update(CHANNEL_LIVE_SOURCE, new StringType(liveSource == null ? "" : liveSource));
        handler.update(CHANNEL_UPTIME, new QuantityType<>(state.infoResponse.uptime, Units.SECOND));
        handler.update(CHANNEL_FREE_HEAP, new QuantityType<>(state.infoResponse.freeheap, Units.BYTE));
        Wifi wifi = state.infoResponse.wifi;
        if (wifi != null) {
            handler.update(CHANNEL_WIFI_SIGNAL,
                    new QuantityType<>(Math.max(0, Math.min(100, wifi.signal)), Units.PERCENT));
        }
        // pwr is the estimated current in mA
        handler.update(CHANNEL_CURRENT,
                new QuantityType<>(
                        new BigDecimal(state.ledInfo.pwr).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP),
                        Units.AMPERE));
        handler.update(CHANNEL_FPS, new DecimalType(state.ledInfo.fps));
    }

    @Override
    protected void processState(int segmentIndex) throws ApiException {
        super.processState(segmentIndex);
        if (handler.handlerMissing(segmentIndex)) {
            return;
        }
        SegmentState segment = state.stateResponse.seg[segmentIndex];
        handler.update(segmentIndex, CHANNEL_FREEZE, OnOffType.from(segment.frz));
        handler.update(segmentIndex, CHANNEL_OPTION_1, OnOffType.from(segment.o1));
        handler.update(segmentIndex, CHANNEL_OPTION_2, OnOffType.from(segment.o2));
        handler.update(segmentIndex, CHANNEL_OPTION_3, OnOffType.from(segment.o3));
        handler.update(segmentIndex, CHANNEL_CUSTOM_1, WLedHelper.toPercent(segment.c1, 255));
        handler.update(segmentIndex, CHANNEL_CUSTOM_2, WLedHelper.toPercent(segment.c2, 255));
        handler.update(segmentIndex, CHANNEL_CUSTOM_3, WLedHelper.toPercent(segment.c3, 31));
        if (state.ledInfo.hasCct()) {
            handler.update(segmentIndex, CHANNEL_WHITE_TEMPERATURE, WLedHelper.toPercent(segment.cct, 255));
        }
    }
}
