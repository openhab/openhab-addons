/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * digitalSTROM JSON Parser class. Externalizes code from
 * {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl}
 *
 * @author Rouven Schürch - Initial contribution
 *
 */
@NonNullByDefault
public final class DSJsonParser {

    private DSJsonParser() {
    }

    public static List<OutputChannelEnum> getOutputChannels(JsonObject jsonObject) {
        List<OutputChannelEnum> outputChannels = new ArrayList<>();
        JsonElement jsonOutputChannels = jsonObject.get(JSONApiResponseKeysEnum.OUTPUT_CHANNELS.getKey());
        if (jsonOutputChannels != null && jsonOutputChannels.isJsonArray()) {
            JsonArray array = jsonObject.get(JSONApiResponseKeysEnum.OUTPUT_CHANNELS.getKey()).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) != null) {
                    int channelId = array.get(i).getAsJsonObject().get("channelID").getAsInt();
                    outputChannels.add(OutputChannelEnum.getChannel(channelId));
                }
            }
        } else if (jsonOutputChannels != null && jsonOutputChannels.isJsonObject()) {
            for (Entry<String, JsonElement> entry : jsonObject.get(JSONApiResponseKeysEnum.OUTPUT_CHANNELS.getKey())
                    .getAsJsonObject().entrySet()) {
                int channelId = entry.getValue().getAsJsonObject().get("channelID").getAsInt();
                outputChannels.add(OutputChannelEnum.getChannel(channelId));
            }
        }

        return outputChannels;
    }
}
