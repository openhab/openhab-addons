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
package org.openhab.binding.dirigera.internal.handler;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartPlugHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SmartPlugHandler extends PlugHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartPlugHandler.class);

    public SmartPlugHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        // handle general initialize like setting bridge
        super.initialize();
        // finally get attributes from model in order to get initial values
        JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_DEVICES);
        logger.trace("DIRIGERA SMART_PLUG values for initial update {}", values);
        handleUpdate(values);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // handling done in PlugHandler - this handler only provides additional read-only channels
        super.handleCommand(channelUID, command);

        // only handle RefreshType
        if (command instanceof RefreshType) {
            JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_DEVICES);
            handleUpdate(values);
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        // handle reachable flag
        super.handleUpdate(update);
        // now device specific
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            logger.trace("DIRIGERA SMART_PLUG update delivered {} attributes", attributes.length());
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    if (CHANNEL_POWER.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                QuantityType.valueOf(attributes.getDouble(key), Units.WATT));
                    } else if (CHANNEL_CURRENT.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                QuantityType.valueOf(attributes.getDouble(key), Units.AMPERE));
                    } else if (CHANNEL_POTENTIAL.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                QuantityType.valueOf(attributes.getDouble(key), Units.VOLT));
                    } else {
                        logger.trace("DIRIGERA SMART_PLUG no channel for {} available", key);
                    }
                } else {
                    logger.trace("DIRIGERA SMART_PLUG no targetChannel for {}", key);
                }
            }
        }
    }
}
