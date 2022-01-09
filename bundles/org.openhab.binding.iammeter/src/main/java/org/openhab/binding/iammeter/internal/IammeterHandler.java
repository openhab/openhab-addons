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
package org.openhab.binding.iammeter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link IammeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Yang Bo - Initial contribution
 */

@NonNullByDefault
public class IammeterHandler extends IammeterBaseHandler {

    public IammeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void resolveData(String response) {
        JsonElement iammeterDataElement = JsonParser.parseString(response);
        JsonObject iammeterData = iammeterDataElement.getAsJsonObject();
        String keyWord = "Data";
        if (iammeterData.has("data") || (iammeterData.has("Data") && iammeterData.has("SN"))) {
            if (iammeterData.has("data")) {
                keyWord = "data";
            }
            for (IammeterWEM3080Channel channelConfig : IammeterWEM3080Channel.values()) {
                Channel channel = getThing().getChannel(channelConfig.getId());
                if (channel != null) {
                    State state = getQuantityState(
                            iammeterData.get(keyWord).getAsJsonArray().get(channelConfig.ordinal()).toString(),
                            channelConfig.getUnit());
                    updateState(channel.getUID(), state);
                }
            }
        }
    }
}
