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
package org.openhab.binding.haywardomnilogic.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Chlorinator Handler
 *
 * @author Matt Myers - Initial contribution
 */
@NonNullByDefault
public class HaywardChlorinatorHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardChlorinatorHandler.class);
    private Map<String, State> channelStates = new HashMap<>();

    public HaywardChlorinatorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void getTelemetry(String xmlResponse) throws HaywardException {
        List<String> systemIDs = new ArrayList<>();
        List<String> data = new ArrayList<>();

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HaywardBridgeHandler bridgehandler) {
            systemIDs = bridgehandler.evaluateXPath("//Chlorinator/@systemId", xmlResponse);
            String thingSystemID = getThing().getUID().getId();
            for (int i = 0; i < systemIDs.size(); i++) {
                if (systemIDs.get(i).equals(thingSystemID)) {
                    // Enable
                    data = bridgehandler.evaluateXPath("//Chlorinator/@enable", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE, data.get(i));
                    channelStates.putAll(updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE, data.get(i)));

                    // Operating Mode
                    data = bridgehandler.evaluateXPath("//Chlorinator/@operatingMode", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_OPERATINGMODE, data.get(i));

                    // Timed Percent
                    data = bridgehandler.evaluateXPath("//Chlorinator/@Timed-Percent", xmlResponse);
                    channelStates
                            .putAll(updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_TIMEDPERCENT, data.get(i)));

                    // scMode
                    data = bridgehandler.evaluateXPath("//Chlorinator/@scMode", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_SCMODE, data.get(i));

                    // Error Bit Array
                    data = bridgehandler.evaluateXPath("//Chlorinator/@chlrError", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_ERROR, String
                            .format("%16s", Integer.toBinaryString(Integer.parseInt(data.get(i)))).replace(" ", "0"));

                    // Alert Bit Array
                    data = bridgehandler.evaluateXPath("//Chlorinator/@chlrAlert", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_ALERT, String
                            .format("%16s", Integer.toBinaryString(Integer.parseInt(data.get(i)))).replace(" ", "0"));

                    // Average Salt Level
                    data = bridgehandler.evaluateXPath("//Chlorinator/@avgSaltLevel", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_AVGSALTLEVEL, data.get(i));

                    // Instant Salt Level
                    data = bridgehandler.evaluateXPath("//Chlorinator/@instantSaltLevel", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_INSTANTSALTLEVEL, data.get(i));

                    // Status Bit Array
                    data = bridgehandler.evaluateXPath("//Chlorinator/@status", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_CHLORINATOR_STATUS, String
                            .format("%16s", Integer.toBinaryString(Integer.parseInt(data.get(i)))).replace(" ", "0"));
                }
            }
            this.updateStatus(ThingStatus.ONLINE);
        } else {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof RefreshType)) {
            return;
        }

        String chlorCfgState = null;
        String chlorTimedPercent = "0";

        String systemID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
        String poolID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_BOWID);

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HaywardBridgeHandler bridgehandler) {
            String cmdString = this.cmdToString(command);
            try {
                switch (channelUID.getId()) {
                    case HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE:
                        chlorTimedPercent = channelStates.get(HaywardBindingConstants.CHANNEL_CHLORINATOR_TIMEDPERCENT)
                                .format("%d");
                        if ("1".equals(cmdString)) {
                            chlorCfgState = "3";
                        } else {
                            chlorCfgState = "2";
                        }
                        break;
                    case HaywardBindingConstants.CHANNEL_CHLORINATOR_TIMEDPERCENT:
                        if (channelStates.get(HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE) == OnOffType.ON) {
                            chlorCfgState = "3";
                        } else {
                            chlorCfgState = "2";
                        }
                        chlorTimedPercent = cmdString;
                        break;
                    default:
                        logger.warn("haywardCommand Unsupported type {}", channelUID);
                        return;
                }

                String cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS + "<Name>SetCHLORParams</Name><Parameters>"
                        + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                        + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                        + bridgehandler.account.mspSystemID + "</Parameter>"
                        + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                        + "<Parameter name=\"ChlorID\" dataType=\"int\" alias=\"EquipmentID\">" + systemID
                        + "</Parameter>" + "<Parameter name=\"CfgState\" dataType=\"byte\" alias=\"Data1\">"
                        + chlorCfgState + "</Parameter>"
                        + "<Parameter name=\"OpMode\" dataType=\"byte\" alias=\"Data2\">1</Parameter>"
                        + "<Parameter name=\"BOWType\" dataType=\"byte\" alias=\"Data3\">1</Parameter>"
                        + "<Parameter name=\"CellType\" dataType=\"byte\" alias=\"Data4\">4</Parameter>"
                        + "<Parameter name=\"TimedPercent\" dataType=\"byte\" alias=\"Data5\">" + chlorTimedPercent
                        + "</Parameter>"
                        + "<Parameter name=\"SCTimeout\" dataType=\"byte\" unit=\"hour\" alias=\"Data6\">24</Parameter>"
                        + "<Parameter name=\"ORPTimout\" dataType=\"byte\" unit=\"hour\" alias=\"Data7\">24</Parameter>"
                        + "</Parameters></Request>";

                // *****Send Command to Hayward server
                String xmlResponse = bridgehandler.httpXmlResponse(cmdURL);
                String status = bridgehandler.evaluateXPath("//Parameter[@name='Status']/text()", xmlResponse).get(0);

                if (!("0".equals(status))) {
                    logger.debug("haywardCommand XML response: {}", xmlResponse);
                    return;
                }
            } catch (HaywardException e) {
                logger.debug("Unable to send command to Hayward's server {}:{}:{}", bridgehandler.config.endpointUrl,
                        bridgehandler.config.username, e.getMessage());
            } catch (InterruptedException e) {
                return;
            }
            this.updateStatus(ThingStatus.ONLINE);
        } else {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }
}
