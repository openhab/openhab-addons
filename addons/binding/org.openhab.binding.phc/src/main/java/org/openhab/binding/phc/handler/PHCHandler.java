/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.phc.handler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.phc.PHCBindingConstants;
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
    private final short[] upDownTimes = new short[4];
    private final Map<String, State> channelState = new HashMap<String, State>();
    private PHCBridgeHandler bridgeHandler;

    public PHCHandler(Thing thing) {
        super(thing);

    }

    @Override
    public void initialize() {
        moduleAddress = (String) getConfig().get(PHCBindingConstants.ADDRESS);

        if (getPHCBridgeHandler() == null) {
            return;
        }

        getPHCBridgeHandler()
                .addModule(Byte.parseByte(getThing().getThingTypeUID().equals(PHCBindingConstants.THING_TYPE_EM)
                        ? new StringBuilder(moduleAddress).reverse().toString()
                        : ("010" + new StringBuilder(moduleAddress).reverse().toString()), 2)); // 010x = 0x4x for
                                                                                                // AM and JRM

        if (getThing().getThingTypeUID().equals(PHCBindingConstants.THING_TYPE_JRM)) {
            upDownTimes[0] = (short) (((BigDecimal) getConfig().get(PHCBindingConstants.UP_DOWN_TIME_1)).shortValue()
                    * 10);
            upDownTimes[1] = (short) (((BigDecimal) getConfig().get(PHCBindingConstants.UP_DOWN_TIME_2)).shortValue()
                    * 10);
            upDownTimes[2] = (short) (((BigDecimal) getConfig().get(PHCBindingConstants.UP_DOWN_TIME_3)).shortValue()
                    * 10);
            upDownTimes[3] = (short) (((BigDecimal) getConfig().get(PHCBindingConstants.UP_DOWN_TIME_4)).shortValue()
                    * 10);
        }

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }

    }

    public void handleIncoming(String channelId, OnOffType state) {
        if (!channelState.containsKey(channelId) || !channelState.get(channelId).equals(state)) {
            postCommand(channelId, state);
            channelState.put(channelId, state);
        }
        logger.debug("EM command: {}, last: {}, in: {}", channelId, channelState.get(channelId), state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((PHCBindingConstants.CHANNELS_AM.equals(channelUID.getGroupId())) || PHCBindingConstants.CHANNELS_JRM
                .equals(channelUID.getGroupId())
                && (command instanceof OnOffType || command instanceof UpDownType || command instanceof StopMoveType)) {

            if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                getPHCBridgeHandler().send(channelUID.getGroupId(),
                        new StringBuilder(moduleAddress).reverse().toString(), channelUID.getIdWithoutGroup(), command,
                        PHCBindingConstants.CHANNELS_JRM.equals(channelUID.getGroupId())
                                ? upDownTimes[Integer.parseInt(channelUID.getIdWithoutGroup())]
                                : 0);
                logger.debug("send command: {}, {}", channelUID, command);

            } else {
                logger.info("The Thing {} is offline it requires to select a Bridge", getThing().getUID());
            }
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        if (PHCBindingConstants.CHANNELS_JRM_TIME.equals(channelUID.getGroupId())) {
            upDownTimes[Integer
                    .parseInt(channelUID.getIdWithoutGroup())] = (short) (((DecimalType) newState).floatValue() * 10);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (isInitialized()) { // prevents change of address

            validateConfigurationParameters(configurationParameters);

            Configuration configuration = editConfiguration();
            for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
                if (!configurationParmeter.getKey().equals(PHCBindingConstants.ADDRESS)) {
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
            if (handler instanceof PHCBridgeHandler) {
                bridgeHandler = (PHCBridgeHandler) handler;
            } else {
                logger.debug("No available bridge handler for {}.", bridge.getUID());
                return null;
            }
        }

        return bridgeHandler;
    }
}
