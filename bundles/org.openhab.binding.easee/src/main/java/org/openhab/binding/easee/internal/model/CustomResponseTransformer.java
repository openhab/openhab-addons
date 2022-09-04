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
package org.openhab.binding.easee.internal.model;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.handler.ChannelProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

import com.google.gson.JsonObject;

/**
 * transforms the http response into the openhab datamodel (instances of State).
 * This class is used to handle special cases which cannot be mapped by the generic transformer.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
class CustomResponseTransformer {
    private final ChannelProvider channelProvider;

    CustomResponseTransformer(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    /**
     * allows additional updates of special/composite channels.
     *
     * @param triggerChannel the channel which triggers the additional update
     * @param value updated value of the triggering channel
     * @param rawData raw json data provided by the API
     */
    Map<Channel, State> transform(Channel triggerChannel, String value, JsonObject rawData) {
        Map<Channel, State> result = new HashMap<>(20);

        switch (triggerChannel.getUID().getId()) {
            case CHANNEL_GROUP_CHARGER_STATE + "#" + CHANNEL_CHARGER_OP_MODE:
                updateChargerStartStop(result, value, rawData);
                break;
            case CHANNEL_GROUP_CHARGER_STATE + "#" + CHANNEL_CHARGER_DYNAMIC_CURRENT:
                updateChargerPauseResume(result, value);
                break;
            case CHANNEL_GROUP_CIRCUIT_DYNAMIC_CURRENT + "#" + CHANNEL_CIRCUIT_DYNAMIC_CURRENT_PHASE1:
                updateCompositePhaseChannel(result, rawData, CHANNEL_GROUP_CIRCUIT_DYNAMIC_CURRENT,
                        CHANNEL_CIRCUIT_DYNAMIC_CURRENTS, CHANNEL_CIRCUIT_DYNAMIC_CURRENT_PHASE1,
                        CHANNEL_CIRCUIT_DYNAMIC_CURRENT_PHASE2, CHANNEL_CIRCUIT_DYNAMIC_CURRENT_PHASE3);
                break;
            case CHANNEL_GROUP_CIRCUIT_SETTINGS + "#" + CHANNEL_CIRCUIT_MAX_CURRENT_PHASE1:
                updateCompositePhaseChannel(result, rawData, CHANNEL_GROUP_CIRCUIT_SETTINGS,
                        CHANNEL_CIRCUIT_MAX_CURRENTS, CHANNEL_CIRCUIT_MAX_CURRENT_PHASE1,
                        CHANNEL_CIRCUIT_MAX_CURRENT_PHASE2, CHANNEL_CIRCUIT_MAX_CURRENT_PHASE3);
                break;
            case CHANNEL_GROUP_CIRCUIT_SETTINGS + "#" + CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENT_PHASE1:
                updateCompositePhaseChannel(result, rawData, CHANNEL_GROUP_CIRCUIT_SETTINGS,
                        CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENTS, CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENT_PHASE1,
                        CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENT_PHASE2, CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENT_PHASE3);
                break;
        }

        return result;
    }

    private void updateChargerStartStop(Map<Channel, State> result, String value, JsonObject rawData) {
        Channel channel = channelProvider.getChannel(CHANNEL_GROUP_CHARGER_COMMANDS, CHANNEL_CHARGER_START_STOP);
        if (channel != null) {
            int val = Integer.valueOf(value);
            // state >= 3 will mean charging, ready to charge or charging finished
            boolean charging = val >= CHARGER_OP_STATE_CHARGING;

            String rfnc = Utils.getAsString(rawData, CHANNEL_CHARGER_REASON_FOR_NO_CURRENT);
            int reasonForNoCurrent = Integer.valueOf(rfnc == null ? "-1" : rfnc);
            boolean paused = (val == CHARGER_OP_STATE_WAITING
                    && reasonForNoCurrent == CHARGER_REASON_FOR_NO_CURRENT_PAUSED);

            result.put(channel, OnOffType.from(charging || paused));
        }
    }

    private void updateChargerPauseResume(Map<Channel, State> result, String value) {
        Channel channel = channelProvider.getChannel(CHANNEL_GROUP_CHARGER_COMMANDS, CHANNEL_CHARGER_PAUSE_RESUME);
        if (channel != null) {
            int val = Integer.valueOf(value);
            // value == 0 will mean paused
            boolean paused = val == CHARGER_DYNAMIC_CURRENT_PAUSE;

            result.put(channel, OnOffType.from(paused));
        }
    }

    private void updateCompositePhaseChannel(Map<Channel, State> result, JsonObject rawData, final String group,
            final String targetChannel, final String channelPhase1, final String channelPhase2,
            final String channelPhase3) {
        Channel channel = channelProvider.getChannel(group, targetChannel);
        String phase1 = Utils.getAsString(rawData, channelPhase1);
        String phase2 = Utils.getAsString(rawData, channelPhase2);
        String phase3 = Utils.getAsString(rawData, channelPhase3);
        if (channel != null && phase1 != null && phase2 != null && phase3 != null) {
            phase1 = phase1.replaceAll("\\.0", "");
            phase2 = phase2.replaceAll("\\.0", "");
            phase3 = phase3.replaceAll("\\.0", "");
            result.put(channel, new StringType(phase1 + ";" + phase2 + ";" + phase3));
        }
    }
}
