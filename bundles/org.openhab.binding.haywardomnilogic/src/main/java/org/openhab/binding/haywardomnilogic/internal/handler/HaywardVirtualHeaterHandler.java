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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingHandler;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingProperties;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Virtual Heater Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardVirtualHeaterHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardVirtualHeaterHandler.class);

    HaywardThingProperties prop = getConfig().as(HaywardThingProperties.class);

    public HaywardVirtualHeaterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void getTelemetry(String xmlResponse) throws Exception {
        List<String> data = new ArrayList<>();

        @SuppressWarnings("null")
        HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) getBridge().getHandler();

        if (bridgehandler != null) {
            // Current Setpoint
            data = bridgehandler.evaluateXPath("//VirtualHeater/@Current-Set-Point", xmlResponse);
            updateData(HaywardBindingConstants.CHANNEL_VIRTUALHEATER_CURRENTSETPOINT, data.get(0));

            // Enable
            data = bridgehandler.evaluateXPath("//VirtualHeater/@enable", xmlResponse);

            if (data.get(0).equals("yes")) {
                updateData(HaywardBindingConstants.CHANNEL_VIRTUALHEATER_ENABLE, "1");
            } else if (data.get(0).equals("no")) {
                updateData(HaywardBindingConstants.CHANNEL_VIRTUALHEATER_ENABLE, "0");
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
            String cmdURL = null;

            if (command == OnOffType.ON) {
                cmdString = "True";
            } else if (command == OnOffType.OFF) {
                cmdString = "False";
            }

            try {
                switch (channelUID.getId()) {
                    case HaywardBindingConstants.CHANNEL_VIRTUALHEATER_ENABLE:
                        cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS + "<Name>SetHeaterEnable</Name><Parameters>"
                                + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                + bridgehandler.account.mspSystemID + "</Parameter>"
                                + "<Parameter name=\"PoolID\" dataType=\"int\">" + prop.poolID + "</Parameter>"
                                + "<Parameter name=\"HeaterID\" dataType=\"int\">" + prop.systemID + "</Parameter>"
                                + "<Parameter name=\"Enabled\" dataType=\"bool\">" + cmdString + "</Parameter>"
                                + "</Parameters></Request>";
                        break;

                    case HaywardBindingConstants.CHANNEL_VIRTUALHEATER_CURRENTSETPOINT:
                        cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS + "<Name>SetUIHeaterCmd</Name><Parameters>"
                                + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                + bridgehandler.account.mspSystemID + "</Parameter>"
                                + "<Parameter name=\"PoolID\" dataType=\"int\">" + prop.poolID + "</Parameter>"
                                + "<Parameter name=\"HeaterID\" dataType=\"int\">" + prop.systemID + "</Parameter>"
                                + "<Parameter name=\"Temp\" dataType=\"int\">" + cmdString + "</Parameter>"
                                + "</Parameters></Request>";
                        break;
                    default:
                        logger.error("haywardCommand Unsupported type {}", channelUID);
                        return;
                }

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
