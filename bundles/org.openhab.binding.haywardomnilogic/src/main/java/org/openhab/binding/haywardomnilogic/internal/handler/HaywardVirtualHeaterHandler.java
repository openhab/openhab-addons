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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingHandler;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingProperties;
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
            int cmdValue = 0;
            String cmdBool = null;
            String cmdString = null;
            String cmdURL = null;

            if (command == OnOffType.OFF) {
                cmdValue = 0;
                cmdBool = "false";
            } else if (command == OnOffType.ON) {
                cmdValue = 1;
                cmdBool = "True";
            } else if (command instanceof DecimalType) {
                cmdValue = ((DecimalType) command).intValue();
            } else if (command instanceof StringType) {
                cmdString = ((StringType) command).toString();
            } else if (command instanceof QuantityType) {
                cmdValue = ((QuantityType<?>) command).intValue();
            } else {
                logger.error("command type {} is not supported", command);
                return;
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
                                + "<Parameter name=\"Enabled\" dataType=\"bool\">" + cmdBool + "</Parameter>"
                                + "</Parameters></Request>";
                        break;

                    case HaywardBindingConstants.CHANNEL_VIRTUALHEATER_CURRENTSETPOINT:
                        cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS + "<Name>SetUIHeaterCmd</Name><Parameters>"
                                + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                + bridgehandler.account.mspSystemID + "</Parameter>"
                                + "<Parameter name=\"PoolID\" dataType=\"int\">" + prop.poolID + "</Parameter>"
                                + "<Parameter name=\"HeaterID\" dataType=\"int\">" + prop.systemID + "</Parameter>"
                                + "<Parameter name=\"Temp\" dataType=\"int\">" + cmdValue + "</Parameter>"
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
