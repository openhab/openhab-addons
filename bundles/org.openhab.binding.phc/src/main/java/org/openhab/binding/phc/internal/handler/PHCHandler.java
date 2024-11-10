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
package org.openhab.binding.phc.internal.handler;

import static org.openhab.binding.phc.internal.PHCBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PHCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Hohaus - Initial contribution
 *
 */
public class PHCHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PHCHandler.class);

    private String moduleAddress; // like DIP switches
    private byte module;
    private final short[] times = new short[4];
    private final Map<String, State> channelState = new HashMap<>();
    private PHCBridgeHandler bridgeHandler;

    public PHCHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        moduleAddress = (String) getConfig().get(ADDRESS);

        if (getPHCBridgeHandler() == null) {
            return;
        }

        module = Byte.parseByte(new StringBuilder(moduleAddress).reverse().toString(), 2);

        if (getThing().getThingTypeUID().equals(THING_TYPE_AM) || getThing().getThingTypeUID().equals(THING_TYPE_JRM)) {
            module |= 0x40;
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_DIM)) {
            module |= 0xA0;
        }
        getPHCBridgeHandler().addModule(module);

        if (getThing().getThingTypeUID().equals(THING_TYPE_JRM)) {
            times[0] = (short) (((BigDecimal) getConfig().get(UP_DOWN_TIME_1)).shortValue() * 10);
            times[1] = (short) (((BigDecimal) getConfig().get(UP_DOWN_TIME_2)).shortValue() * 10);
            times[2] = (short) (((BigDecimal) getConfig().get(UP_DOWN_TIME_3)).shortValue() * 10);
            times[3] = (short) (((BigDecimal) getConfig().get(UP_DOWN_TIME_4)).shortValue() * 10);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_DIM)) {
            times[0] = (((BigDecimal) getConfig().get(DIM_TIME_1)).shortValue());
            times[1] = (((BigDecimal) getConfig().get(DIM_TIME_2)).shortValue());
        }

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public void handleIncoming(String channelId, OnOffType state) {
        if (logger.isDebugEnabled()) {
            logger.debug("EM command: {}, last: {}, in: {}", channelId, channelState.get(channelId), state);
        }

        if (!channelState.containsKey(channelId) || !channelState.get(channelId).equals(state)) {
            postCommand(channelId, state);
            channelState.put(channelId, state);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final String groupId = channelUID.getGroupId();
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            if ((CHANNELS_JRM.equals(groupId) && (command instanceof UpDownType || command instanceof StopMoveType))
                    || (CHANNELS_DIM.equals(groupId)
                            && (command instanceof OnOffType || command instanceof PercentType))) {
                getPHCBridgeHandler().send(groupId, module & 0x1F, channelUID.getIdWithoutGroup(), command,
                        times[Integer.parseInt(channelUID.getIdWithoutGroup())]);
            } else if ((CHANNELS_AM.equals(groupId) || CHANNELS_EM_LED.equals(groupId))
                    && command instanceof OnOffType) {
                getPHCBridgeHandler().send(groupId, module & 0x1F, channelUID.getIdWithoutGroup(), command, (short) 0);
            }

            logger.debug("send command: {}, {}", channelUID, command);
        } else {
            logger.info("The Thing {} is offline.", getThing().getUID());
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (isInitialized()) { // prevents change of address
            validateConfigurationParameters(configurationParameters);

            Configuration configuration = editConfiguration();
            for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
                if (!configurationParmeter.getKey().equals(ADDRESS)) {
                    configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
                } else {
                    configuration.put(configurationParmeter.getKey(), moduleAddress);
                }
            }

            // persist new configuration and reinitialize handler
            dispose();
            updateConfiguration(configuration);
            initialize();
        } else {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    private PHCBridgeHandler getPHCBridgeHandler() {
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "The Thing requires to select a Bridge");
                return null;
            }

            ThingHandler handler = bridge.getHandler();
            if (handler instanceof PHCBridgeHandler phcBridgeHandler) {
                bridgeHandler = phcBridgeHandler;
            } else {
                logger.debug("No available bridge handler for {}.", bridge.getUID());

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR,
                        "No available bridge handler.");

                return null;
            }
        }

        return bridgeHandler;
    }
}
