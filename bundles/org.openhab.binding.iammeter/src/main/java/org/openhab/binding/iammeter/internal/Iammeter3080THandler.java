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
package org.openhab.binding.iammeter.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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
public class Iammeter3080THandler extends IammeterBaseHandler {

    public Iammeter3080THandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    protected void resolveData(String response) {
        JsonElement iammeterDataElement = JsonParser.parseString(response);
        JsonObject iammeterData = iammeterDataElement.getAsJsonObject();
        String keyWord = "Datas";
        if (iammeterData.has("Datas") && iammeterData.has("SN")) {
            String[] groups = { "powerPhaseA", "powerPhaseB", "powerPhaseC" };
            for (int row = 0; row < groups.length; row++) {
                String gpName = groups[row];
                List<Channel> chnList = getThing().getChannelsOfGroup(gpName);
                for (IammeterWEM3080Channel channelConfig : IammeterWEM3080Channel.values()) {
                    Channel chnl = chnList.get(channelConfig.ordinal());
                    if (chnl != null) {
                        State state = getQuantityState(iammeterData.get(keyWord).getAsJsonArray().get(row)
                                .getAsJsonArray().get(channelConfig.ordinal()).toString(), channelConfig.getUnit());
                        updateState(chnl.getUID(), state);
                    }
                }
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }
}
