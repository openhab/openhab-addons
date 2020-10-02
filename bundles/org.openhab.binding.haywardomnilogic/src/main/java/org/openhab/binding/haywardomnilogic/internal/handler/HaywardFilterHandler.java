/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.haywardomnilogic.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingHandler;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Filter Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardFilterHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardFilterHandler.class);

    HaywardThingProperties prop = getConfig().as(HaywardThingProperties.class);

    public HaywardFilterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void setFilterProperty(String systemID, String channelID, String data) {
        Channel chan = getThing().getChannel(channelID);
        if (chan != null) {
            Map<String, String> properties = editProperties();
            updateProperties(properties);
            logger.trace("Updated Hayward Filter {} {} to: {}", systemID, channelID, data);
        }
    }

    public void getFilterProperty(String systemID, String channelID, String data) {
        Channel chan = getThing().getChannel(channelID);
        if (chan != null) {
            updateState(chan.getUID(), new DecimalType(data));
            logger.trace("Updated Hayward Filter {} {} to: {}", systemID, channelID, data);
        }
    }

    public void getTelemetry(String xmlResponse) throws Exception {
        List<String> data = new ArrayList<>();
        List<String> systemIDs = new ArrayList<>();

        @SuppressWarnings("null")
        HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) getBridge().getHandler();
        if (bridgehandler != null) {
            systemIDs = bridgehandler.evaluateXPath("//Filter/@systemId", xmlResponse);
            String thingSystemID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
            for (int i = 0; i < systemIDs.size(); i++) {
                if (systemIDs.get(i).equals(thingSystemID)) {
                    // Operating Mode
                    data = bridgehandler.evaluateXPath("//Chlorinator/@operatingMode", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_OPERATINGMODE, data.get(0));

                    // Valve Position
                    data = bridgehandler.evaluateXPath("//Filter/@valvePosition", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_FILTER_VALVEPOSITION, data.get(0));

                    // Speed
                    data = bridgehandler.evaluateXPath("//Filter/@filterSpeed", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_FILTER_SPEED, data.get(0));

                    if (data.get(0).equals("0")) {
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_ENABLE, "0");
                    } else {
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_ENABLE, "1");
                    }

                    // State
                    data = bridgehandler.evaluateXPath("//Filter/@filterState", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_FILTER_STATE, data.get(0));

                    // lastSpeed
                    data = bridgehandler.evaluateXPath("//Filter/@lastSpeed", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_FILTER_LASTSPEED, data.get(0));
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof RefreshType)) {
            return;
        }

        prop.systemID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
        prop.poolID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_BOWID);

        @SuppressWarnings("null")
        HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) getBridge().getHandler();
        if (bridgehandler != null) {
            String cmdString = this.cmdToString(command);
            try {
                switch (channelUID.getId()) {
                    case HaywardBindingConstants.CHANNEL_FILTER_ENABLE:
                        if (command == OnOffType.ON) {
                            cmdString = "100";
                        } else {
                            cmdString = "0";
                        }
                        break;
                    case HaywardBindingConstants.CHANNEL_FILTER_SPEED:
                        break;
                    default:
                        logger.error("haywardCommand Unsupported type {}", channelUID);
                        return;
                }

                String cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                        + "<Name>SetUIEquipmentCmd</Name><Parameters>"
                        + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.config.token + "</Parameter>"
                        + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + bridgehandler.config.mspSystemID
                        + "</Parameter>" + "<Parameter name=\"PoolID\" dataType=\"int\">" + prop.poolID + "</Parameter>"
                        + "<Parameter name=\"EquipmentID\" dataType=\"int\">" + prop.systemID + "</Parameter>"
                        + "<Parameter name=\"IsOn\" dataType=\"int\">" + cmdString + "</Parameter>"
                        + HaywardBindingConstants.COMMAND_SCHEDULE + "</Parameters></Request>";

                // *****Send Command to Hayward server
                String xmlResponse = bridgehandler.httpXmlResponse(cmdURL);
                String status = bridgehandler.evaluateXPath("//Parameter[@name='Status']/text()", xmlResponse).get(0);

                if (!(status.equals("0"))) {
                    logger.error("haywardCommand XML response: {}", xmlResponse);
                    return;
                }
            } catch (Exception e) {
                logger.debug("Unable to send command to Hayward's server {}:{}", bridgehandler.config.hostname,
                        bridgehandler.config.username);
            }
        }
    }
}
