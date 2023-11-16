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
package org.openhab.binding.haywardomnilogic.internal.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.HaywardException;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ColorLogic Handler
 *
 * @author Matt Myers - Initial contribution
 */
@NonNullByDefault
public class HaywardColorLogicHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardColorLogicHandler.class);

    public HaywardColorLogicHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void getTelemetry(String xmlResponse) throws HaywardException {
        List<String> systemIDs = new ArrayList<>();
        List<String> data = new ArrayList<>();

        Bridge bridge = getBridge();
        if (bridge != null) {
            HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
            if (bridgehandler != null) {
                systemIDs = bridgehandler.evaluateXPath("//ColorLogic-Light/@systemId", xmlResponse);
                String thingSystemID = getThing().getUID().getId();
                for (int i = 0; i < systemIDs.size(); i++) {
                    if (systemIDs.get(i).equals(thingSystemID)) {
                        // Light State
                        data = bridgehandler.evaluateXPath("//ColorLogic-Light/@lightState", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_LIGHTSTATE, data.get(i));

                        if ("0".equals(data.get(i))) {
                            updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_ENABLE, "0");
                        } else {
                            updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_ENABLE, "1");
                        }

                        // Current Show
                        data = bridgehandler.evaluateXPath("//ColorLogic-Light/@currentShow", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW, data.get(0));
                    }
                }
                this.updateStatus(ThingStatus.ONLINE);
            } else {
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof RefreshType)) {
            return;
        }

        String systemID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
        String poolID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_BOWID);

        Bridge bridge = getBridge();
        if (bridge != null) {
            HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
            if (bridgehandler != null) {
                String cmdString = this.cmdToString(command);
                String cmdURL = null;
                try {
                    switch (channelUID.getId()) {
                        case HaywardBindingConstants.CHANNEL_COLORLOGIC_ENABLE:
                            if (command == OnOffType.ON) {
                                cmdString = "1";
                            } else {
                                cmdString = "0";
                            }
                            cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                                    + "<Name>SetUIEquipmentCmd</Name><Parameters>"
                                    + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                    + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                    + bridgehandler.account.mspSystemID + "</Parameter>"
                                    + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                                    + "<Parameter name=\"EquipmentID\" dataType=\"int\">" + systemID + "</Parameter>"
                                    + "<Parameter name=\"IsOn\" dataType=\"int\">" + cmdString + "</Parameter>"
                                    + HaywardBindingConstants.COMMAND_SCHEDULE + "</Parameters></Request>";
                            break;
                        case HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW:
                            cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                                    + "<Name>SetStandAloneLightShow</Name><Parameters>"
                                    + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                    + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                    + bridgehandler.account.mspSystemID + "</Parameter>"
                                    + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                                    + "<Parameter name=\"LightID\" dataType=\"int\">" + systemID + "</Parameter>"
                                    + "<Parameter name=\"Show\" dataType=\"int\">" + cmdString + "</Parameter>"
                                    + "<Parameter name=\"Speed\" dataType=\"byte\">4</Parameter>"
                                    + "<Parameter name=\"Brightness\" dataType=\"byte\">4</Parameter>"
                                    + "<Parameter name=\"Reserved\" dataType=\"byte\">0</Parameter>"
                                    + HaywardBindingConstants.COMMAND_SCHEDULE + "</Parameters></Request>";
                            break;
                        default:
                            logger.warn("haywardCommand Unsupported type {}", channelUID);
                            return;
                    }

                    // *****Send Command to Hayward server
                    String xmlResponse = bridgehandler.httpXmlResponse(cmdURL);
                    String status = bridgehandler.evaluateXPath("//Parameter[@name='Status']/text()", xmlResponse)
                            .get(0);

                    if (!("0".equals(status))) {
                        logger.debug("haywardCommand XML response: {}", xmlResponse);
                        return;
                    }
                } catch (HaywardException e) {
                    logger.debug("Unable to send command to Hayward's server {}:{}:{}",
                            bridgehandler.config.endpointUrl, bridgehandler.config.username, e.getMessage());
                } catch (InterruptedException e) {
                    return;
                }
                this.updateStatus(ThingStatus.ONLINE);
            } else {
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        }
    }
}
