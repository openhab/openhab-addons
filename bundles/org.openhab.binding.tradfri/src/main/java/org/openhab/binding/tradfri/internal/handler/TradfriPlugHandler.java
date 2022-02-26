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
package org.openhab.binding.tradfri.internal.handler;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.CHANNEL_POWER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tradfri.internal.TradfriCoapClient;
import org.openhab.binding.tradfri.internal.model.TradfriPlugData;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriPlugHandler} is responsible for handling commands for individual plugs.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class TradfriPlugHandler extends TradfriThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TradfriPlugHandler.class);

    public TradfriPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onUpdate(JsonElement data) {
        if (active && !(data.isJsonNull())) {
            TradfriPlugData state = new TradfriPlugData(data);
            updateStatus(state.getReachabilityStatus() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

            updateState(CHANNEL_POWER, state.getOnOffState() ? OnOffType.ON : OnOffType.OFF);
            updateDeviceProperties(state);
        }
    }

    private void setState(OnOffType onOff) {
        TradfriPlugData data = new TradfriPlugData();
        data.setOnOffState(onOff == OnOffType.ON);
        set(data.getJsonString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (active) {
            if (command instanceof RefreshType) {
                TradfriCoapClient coapClient = this.coapClient;
                if (coapClient != null) {
                    logger.debug("Refreshing channel {}", channelUID);
                    coapClient.asyncGet(this);
                } else {
                    logger.debug("coapClient is null!");
                }
                return;
            }

            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    if (command instanceof OnOffType) {
                        setState(((OnOffType) command));
                    } else {
                        logger.debug("Cannot handle command '{}' for channel '{}'", command, CHANNEL_POWER);
                    }
                    break;
                default:
                    logger.error("Unknown channel UID {}", channelUID);
            }
        }
    }
}
