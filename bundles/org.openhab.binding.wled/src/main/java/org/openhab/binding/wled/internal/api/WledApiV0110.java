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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wled.internal.WLedHandler;
import org.openhab.binding.wled.internal.WledState.PresetState;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WledApiV0130} is the json Api methods for firmware version 0.11.0 and newer
 * as newer firmwares come out with breaking changes, extend this class into a newer firmware version class.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WledApiV0110 extends WledApiV084 {

    public WledApiV0110(WLedHandler handler, HttpClient httpClient) {
        super(handler, httpClient);
    }

    @Override
    public void initialize() throws ApiException {
        super.initialize();
        getPresets();
    }

    protected void getPresets() throws JsonSyntaxException, ApiException {
        List<StateOption> presetsOptions = new ArrayList<>();
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
                if (preset.bri == 0) {
                    playlistsOptions.add(new StateOption(Integer.toString(counter), preset.n));
                } else {
                    presetsOptions.add(new StateOption(Integer.toString(counter), preset.n));
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
    public void savePreset(int position, String presetName) throws ApiException {
        if (position < 1) {
            logger.warn("Preset position {} is not supported in this firmware version", position);
            return;
        }

        String name = presetName;
        if (name.isEmpty()) {
            name = "Preset " + position;
        }
        postState("{\"psave\":" + position + ",\"n\":\"" + name + "\",\"ib\":true,\"sb\":true}");
    }
}
