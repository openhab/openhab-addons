/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.model;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.binding.myuplink.internal.handler.ChannelProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * transforms the http response into the openhab datamodel (instances of State)
 * this is a transformer for the smart home mode data received from the api.
 *
 * @author Anders Alfredsson - initial contribution
 */
@NonNullByDefault
public class SmartHomeModeResponseTransformer implements ResponseTransformer {
    private final Logger logger = LoggerFactory.getLogger(SmartHomeModeResponseTransformer.class);
    private final ChannelProvider channelProvider;

    public SmartHomeModeResponseTransformer(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transform(JsonObject jsonData, String group) {
        String mode = Utils.getAsString(jsonData, JSON_KEY_SMART_HOME_MODE);
        Channel channel = channelProvider.getChannel(group, CHANNEL_ID_SMART_HOME_MODE);

        if (channel == null) {
            logger.warn(
                    "Smart home mode channel not found. This is likely because of a bug. Please report to the developers.");
            return Map.of();
        } else {
            State newState;

            if (mode == null) {
                newState = UnDefType.UNDEF;
            } else {
                newState = new StringType(mode);
            }

            return Map.of(channel, newState);
        }
    }
}
