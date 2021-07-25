/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
 * The Filter Handler
 *
 * @author Matt Myers - Initial contribution
 */
@NonNullByDefault
public class HaywardFilterHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardFilterHandler.class);

    public HaywardFilterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void getTelemetry(String xmlResponse) throws HaywardException {
        List<String> systemIDs = new ArrayList<>();
        List<String> data = new ArrayList<>();

        Bridge bridge = getBridge();
        if (bridge != null) {
            HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
            if (bridgehandler != null) {
                systemIDs = bridgehandler.evaluateXPath("//Filter/@systemId", xmlResponse);
                String thingSystemID = getThing().getUID().getId();
                for (int i = 0; i < systemIDs.size(); i++) {
                    if (systemIDs.get(i).equals(thingSystemID)) {
                        // Operating Mode
                        data = bridgehandler.evaluateXPath("//Chlorinator/@operatingMode", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_OPERATINGMODE, data.get(i));

                        // Valve Position
                        data = bridgehandler.evaluateXPath("//Filter/@valvePosition", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_VALVEPOSITION, data.get(i));

                        // Speed
                        data = bridgehandler.evaluateXPath("//Filter/@filterSpeed", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_SPEED, data.get(i));

                        if (data.get(i).equals("0")) {
                            updateData(HaywardBindingConstants.CHANNEL_FILTER_ENABLE, "0");
                        } else {
                            updateData(HaywardBindingConstants.CHANNEL_FILTER_ENABLE, "1");
                        }

                        // State
                        data = bridgehandler.evaluateXPath("//Filter/@filterState", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_STATE, data.get(i));

                        // lastSpeed
                        data = bridgehandler.evaluateXPath("//Filter/@lastSpeed", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_LASTSPEED, data.get(i));
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
                            logger.warn("haywardCommand Unsupported type {}", channelUID);
                            return;
                    }

                    String cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                            + "<Name>SetUIEquipmentCmd</Name><Parameters>"
                            + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                            + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                            + bridgehandler.account.mspSystemID + "</Parameter>"
                            + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                            + "<Parameter name=\"EquipmentID\" dataType=\"int\">" + systemID + "</Parameter>"
                            + "<Parameter name=\"IsOn\" dataType=\"int\">" + cmdString + "</Parameter>"
                            + HaywardBindingConstants.COMMAND_SCHEDULE + "</Parameters></Request>";

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
