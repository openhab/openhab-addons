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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wled.internal.WledState.PresetState;
import org.openhab.binding.wled.internal.handlers.WLedBridgeHandler;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WledApiV0110} is the json Api methods for firmware version 0.11.0 and newer
 * as newer firmwares come out with breaking changes, extend this class into a newer firmware version class.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WledApiV0110 extends WledApiV084 {
    private final Set<String> knownPresetIds = new HashSet<>();
    private int lastUnknownPreset = -1;

    public WledApiV0110(WLedBridgeHandler handler, HttpClient httpClient) {
        super(handler, httpClient);
    }

    @Override
    public void initialize() throws ApiException {
        super.initialize();
        getPresets();
    }

    @Override
    public void update() throws ApiException {
        super.update();
        refreshUnknownPreset();
    }

    /**
     * A preset or playlist that was saved by another app after the binding started is not in the lists yet.
     */
    protected void refreshUnknownPreset() throws ApiException {
        int activePreset = state.stateResponse.ps;
        if (activePreset > 0 && activePreset != lastUnknownPreset
                && !knownPresetIds.contains(Integer.toString(activePreset))) {
            lastUnknownPreset = activePreset;
            try {
                getPresets();
            } catch (JsonSyntaxException e) {
                logger.debug("Could not refresh the presets:{}", e.getMessage());
            }
        }
    }

    protected void getPresets() throws JsonSyntaxException, ApiException {
        List<StateOption> presetsOptions = new ArrayList<>();
        knownPresetIds.clear();
        List<StateOption> playlistsOptions = new ArrayList<>();
        JsonObject obj = gson.fromJson(sendGetRequest("/presets.json"), JsonObject.class);
        if (obj == null) {
            return;
        }
        Set<Entry<String, JsonElement>> set = obj.entrySet();
        int counter = 0;
        for (Entry<String, JsonElement> presetEntry : set) {
            logger.trace("Preset:{} json:{}", presetEntry.getKey(), presetEntry.getValue());
            PresetState preset = gson.fromJson(presetEntry.getValue(), PresetState.class);
            if (preset != null && counter > 0) {
                knownPresetIds.add(presetEntry.getKey());
                if (presetEntry.getValue().toString().contains("playlist")) {
                    playlistsOptions.add(new StateOption(presetEntry.getKey(), preset.n));
                } else {
                    presetsOptions.add(new StateOption(presetEntry.getKey(), preset.n));
                }
            }
            counter++;
        }
        handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_PRESETS),
                presetsOptions);
        handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_PLAYLISTS),
                playlistsOptions);
    }

    @Override
    public String getPresetsJson() throws ApiException {
        return sendGetRequest("/presets.json");
    }

    @Override
    public String getPresetsText() throws ApiException {
        try {
            JsonObject obj = gson.fromJson(getPresetsJson(), JsonObject.class);
            if (obj == null) {
                return "";
            }
            StringBuilder text = new StringBuilder();
            for (Entry<String, JsonElement> entry : obj.entrySet()) {
                // slot 0 is always empty
                if ("0".equals(entry.getKey()) || !entry.getValue().isJsonObject()) {
                    continue;
                }
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(describePreset(entry.getKey(), entry.getValue().getAsJsonObject()));
            }
            return text.toString();
        } catch (JsonSyntaxException e) {
            throw new ApiException("JsonSyntaxException:" + e.getMessage(), e);
        }
    }

    private static int intValue(JsonObject obj, String key, int defaultValue) {
        JsonElement element = obj.get(key);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        return defaultValue;
    }

    /**
     * Turns a list of 0.1 second values into seconds for a text.
     */
    private static String tenthsToSeconds(JsonElement element) {
        if (element.isJsonArray()) {
            List<String> values = new ArrayList<>();
            for (JsonElement value : element.getAsJsonArray()) {
                values.add(tenthsToSeconds(value));
            }
            return String.join(",", values);
        }
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                ? Double.toString(element.getAsDouble() / 10)
                : element.toString();
    }

    private String describePreset(String id, JsonObject preset) {
        StringBuilder line = new StringBuilder(id).append(": ");
        JsonElement name = preset.get("n");
        line.append(name != null && name.isJsonPrimitive() ? name.getAsString() : "(no name)");
        JsonElement playlist = preset.get("playlist");
        if (playlist != null && playlist.isJsonObject()) {
            JsonObject pl = playlist.getAsJsonObject();
            line.append(" [playlist]");
            JsonElement ps = pl.get("ps");
            line.append(" presets=").append(ps == null ? "" : ps.toString().replaceAll("[\\[\\]]", ""));
            JsonElement dur = pl.get("dur");
            line.append(" duration(s)=").append(dur == null ? "" : tenthsToSeconds(dur));
            JsonElement transition = pl.get("transition");
            line.append(" transition(s)=").append(transition == null ? "" : tenthsToSeconds(transition));
            line.append(" repeat=").append(intValue(pl, "repeat", 0));
            line.append(" end=").append(intValue(pl, "end", 0));
            return line.toString();
        }
        line.append(" [preset]");
        if (preset.has("on")) {
            line.append(" on=").append(preset.get("on").toString());
        }
        if (preset.has("bri")) {
            line.append(" bri=").append(intValue(preset, "bri", 0));
        }
        if (preset.has("transition")) {
            line.append(" transition(s)=").append(intValue(preset, "transition", 0) / 10.0);
        }
        JsonElement segments = preset.get("seg");
        if (segments != null && segments.isJsonArray()) {
            line.append(" segments=").append(segments.getAsJsonArray().size());
            for (JsonElement segment : segments.getAsJsonArray()) {
                if (segment.isJsonObject()) {
                    JsonObject seg = segment.getAsJsonObject();
                    line.append(" {id=").append(intValue(seg, "id", 0));
                    line.append(" fx=").append(intValue(seg, "fx", 0));
                    line.append(" pal=").append(intValue(seg, "pal", 0));
                    line.append(" sx=").append(intValue(seg, "sx", 0));
                    line.append(" ix=").append(intValue(seg, "ix", 0));
                    if (seg.has("col")) {
                        line.append(" col=").append(seg.get("col").toString());
                    }
                    line.append('}');
                }
            }
        }
        return line.toString();
    }

    @Override
    public void savePresetState(int position, String presetName, String stateJson) throws ApiException {
        if (position < 1) {
            throw new ApiException("Preset position " + position + " is not supported");
        }
        JsonObject obj;
        try {
            obj = gson.fromJson(stateJson, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new ApiException("The JSON for the preset is not valid:" + e.getMessage());
        }
        if (obj == null) {
            obj = new JsonObject();
        }
        obj.addProperty("psave", position);
        obj.addProperty("n", presetName.isEmpty() ? "Preset " + position : presetName);
        // o means the supplied JSON is saved as it is, instead of the current state of the lights
        obj.addProperty("o", true);
        postState(obj.toString());
        refreshPresets();
    }

    @Override
    public void savePlaylist(int position, String playlistName, List<Integer> presets, List<Double> durationsSeconds,
            List<Double> transitionsSeconds, int repeat, int endPreset) throws ApiException {
        if (position < 1) {
            throw new ApiException("Preset position " + position + " is not supported");
        }
        if (presets.isEmpty()) {
            throw new ApiException("A playlist needs at least one preset");
        }
        JsonObject playlist = new JsonObject();
        playlist.add("ps", gson.toJsonTree(presets));
        playlist.add("dur", toTenthsArray(durationsSeconds, presets.size(), "duration"));
        playlist.add("transition", toTenthsArray(transitionsSeconds, presets.size(), "transition"));
        playlist.addProperty("repeat", repeat);
        playlist.addProperty("end", endPreset);
        JsonObject obj = new JsonObject();
        obj.addProperty("psave", position);
        obj.addProperty("n", playlistName.isEmpty() ? "Playlist " + position : playlistName);
        obj.addProperty("o", true);
        obj.add("playlist", playlist);
        postState(obj.toString());
        refreshPresets();
    }

    private JsonArray toTenthsArray(List<Double> seconds, int size, String what) throws ApiException {
        if (seconds.size() != 1 && seconds.size() != size) {
            throw new ApiException("Give one " + what + " or one for each of the " + size + " presets");
        }
        JsonArray array = new JsonArray();
        for (int count = 0; count < size; count++) {
            double value = seconds.get(seconds.size() == 1 ? 0 : count);
            array.add(Math.round(Math.max(0, value) * 10));
        }
        return array;
    }

    protected void refreshPresets() {
        try {
            getPresets();
        } catch (ApiException | JsonSyntaxException e) {
            logger.debug("Could not refresh the presets:{}", e.getMessage());
        }
    }

    @Override
    public void savePreset(int position, String presetName) throws ApiException {
        if (position < 1) {
            logger.warn("Preset position {} is not supported in this firmware version", position);
            return;
        }

        String name = presetName;
        if (name.isEmpty()) {
            name = "Preset " + position;
        }
        postState("{\"psave\":" + position + ",\"n\":" + gson.toJson(name) + ",\"ib\":true,\"sb\":true}");
    }

    @Override
    public void setSleepMode(String value) throws ApiException {
        postState("{\"nl\":{\"mode\":" + value + "}}");
    }

    @Override
    public void setLegacyWhite(String whiteChannel, PercentType brightness, int segmentIndex) throws ApiException {
        sendGetRequest(whiteChannel + brightness.toBigDecimal().multiply(BIG_DECIMAL_2_55) + "&SS=" + segmentIndex);
    }
}
