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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.HaywardException;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Virtual Heater Handler
 *
 * @author Matt Myers - Initial contribution
 */
@NonNullByDefault
public class HaywardVirtualHeaterHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardVirtualHeaterHandler.class);

    public HaywardVirtualHeaterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            setStateDescriptions();
            updateStatus(ThingStatus.ONLINE);
        } catch (HaywardException e) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void setStateDescriptions() throws HaywardException {
        Bridge bridge = getBridge();
        if (bridge != null) {
            HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
            if (bridgehandler != null) {
                // Set heater min and max speeds
                Channel ch = thing.getChannel(HaywardBindingConstants.CHANNEL_VIRTUALHEATER_CURRENTSETPOINT);
                if (ch != null) {
                    StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                            .withMinimum(new BigDecimal(getThing().getProperties()
                                    .get(HaywardBindingConstants.PROPERTY_VIRTUALHEATER_MINSETTABLEWATERTEMP)))
                            .withMaximum(new BigDecimal(getThing().getProperties()
                                    .get(HaywardBindingConstants.PROPERTY_VIRTUALHEATER_MAXSETTABLEWATERTEMP)))
                            .build();
                    bridgehandler.updateChannelStateDescriptionFragment(ch, stateDescriptionFragment);
                }
            }
        }
    }

    @Override
    public void getTelemetry(String xmlResponse) throws HaywardException {
        List<String> systemIDs = new ArrayList<>();
        List<String> data = new ArrayList<>();

        Bridge bridge = getBridge();
        if (bridge != null) {
            HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
            if (bridgehandler != null) {
                systemIDs = bridgehandler.evaluateXPath("//VirtualHeater/@systemId", xmlResponse);
                String thingSystemID = getThing().getUID().getId();
                for (int i = 0; i < systemIDs.size(); i++) {
                    if (systemIDs.get(i).equals(thingSystemID)) {
                        data = bridgehandler.evaluateXPath("//VirtualHeater/@Current-Set-Point", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_VIRTUALHEATER_CURRENTSETPOINT, data.get(i));

                        data = bridgehandler.evaluateXPath("//VirtualHeater/@enable", xmlResponse);
                        if (data.get(i).equals("yes")) {
                            updateData(HaywardBindingConstants.CHANNEL_VIRTUALHEATER_ENABLE, "1");
                        } else if (data.get(i).equals("no")) {
                            updateData(HaywardBindingConstants.CHANNEL_VIRTUALHEATER_ENABLE, "0");
                        }
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
        String heaterMinSetTemp = getThing().getProperties()
                .get(HaywardBindingConstants.PROPERTY_VIRTUALHEATER_MINSETTABLEWATERTEMP);
        String heaterMaxSetTemp = getThing().getProperties()
                .get(HaywardBindingConstants.PROPERTY_VIRTUALHEATER_MAXSETTABLEWATERTEMP);

        Bridge bridge = getBridge();
        if (bridge != null) {
            HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
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
                            cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                                    + "<Name>SetHeaterEnable</Name><Parameters>"
                                    + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                    + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                    + bridgehandler.account.mspSystemID + "</Parameter>"
                                    + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                                    + "<Parameter name=\"HeaterID\" dataType=\"int\">" + systemID + "</Parameter>"
                                    + "<Parameter name=\"Enabled\" dataType=\"bool\">" + cmdString + "</Parameter>"
                                    + "</Parameters></Request>";
                            break;

                        case HaywardBindingConstants.CHANNEL_VIRTUALHEATER_CURRENTSETPOINT:
                            if (heaterMinSetTemp != null && heaterMaxSetTemp != null) {
                                if (Integer.parseInt(cmdString) < Integer.parseInt(heaterMinSetTemp)) {
                                    cmdString = heaterMinSetTemp;
                                } else if (Integer.parseInt(cmdString) > Integer.parseInt(heaterMaxSetTemp)) {
                                    cmdString = heaterMaxSetTemp;
                                }
                            }

                            cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                                    + "<Name>SetUIHeaterCmd</Name><Parameters>"
                                    + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                    + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                    + bridgehandler.account.mspSystemID + "</Parameter>"
                                    + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                                    + "<Parameter name=\"HeaterID\" dataType=\"int\">" + systemID + "</Parameter>"
                                    + "<Parameter name=\"Temp\" dataType=\"int\">" + cmdString + "</Parameter>"
                                    + "</Parameters></Request>";
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
