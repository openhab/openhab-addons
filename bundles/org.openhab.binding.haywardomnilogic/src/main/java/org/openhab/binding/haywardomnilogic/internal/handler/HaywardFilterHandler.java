/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
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
    private Map<String, State> channelStates = new HashMap<>();

    public HaywardFilterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            setStateDescriptions();
            updateStatus(ThingStatus.ONLINE);
        } catch (HaywardException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to set FilterHandler StateDescriptions");
        }
    }

    @Override
    public void setStateDescriptions() throws HaywardException {
        List<StateOption> options = new ArrayList<>();
        String option;

        Bridge bridge = getBridge();
        if (bridge != null) {
            HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
            if (bridgehandler != null) {
                // Set Filter Speed % min and max speeds
                Channel ch = thing.getChannel(HaywardBindingConstants.CHANNEL_FILTER_SPEEDPERCENT);
                if (ch != null) {
                    StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                            .withMinimum(new BigDecimal(
                                    getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_MINSPEED)))
                            .withMaximum(new BigDecimal(
                                    getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_MAXSPEED)))
                            .build();
                    bridgehandler.updateChannelStateDescriptionFragment(ch, stateDescriptionFragment);
                }

                // Set Filter Speed RPM min and max speeds
                ch = thing.getChannel(HaywardBindingConstants.CHANNEL_FILTER_SPEEDRPM);
                if (ch != null) {
                    StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                            .withMinimum(new BigDecimal(
                                    getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_MINRPM)))
                            .withMaximum(new BigDecimal(
                                    getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_MAXRPM)))
                            .build();
                    bridgehandler.updateChannelStateDescriptionFragment(ch, stateDescriptionFragment);
                }

                // Set Filter Speed States
                ch = thing.getChannel(HaywardBindingConstants.CHANNEL_FILTER_SPEEDSELECT);
                if (ch != null) {
                    options.add(new StateOption("0", "Off"));
                    option = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_LOWSPEED);
                    if (option != null) {
                        options.add(new StateOption(option, "Low"));
                    }
                    option = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_MEDSPEED);
                    if (option != null) {
                        options.add(new StateOption(option, "Medium"));
                    }
                    option = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_HIGHSPEED);
                    if (option != null) {
                        options.add(new StateOption(option, "High"));
                    }
                    option = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_CUSTOMSPEED);
                    if (option != null) {
                        options.add(new StateOption(option, "Custom"));
                    }

                    StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                            .withOptions(options).build();
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
                systemIDs = bridgehandler.evaluateXPath("//Filter/@systemId", xmlResponse);
                String thingSystemID = getThing().getUID().getId();
                for (int i = 0; i < systemIDs.size(); i++) {
                    if (systemIDs.get(i).equals(thingSystemID)) {
                        // Valve Position
                        data = bridgehandler.evaluateXPath("//Filter/@valvePosition", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_VALVEPOSITION, data.get(i));

                        // Speed percent
                        data = bridgehandler.evaluateXPath("//Filter/@filterSpeed", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_SPEEDPERCENT, data.get(i));

                        // Speed rpm
                        String filterMaxRpm = getThing().getProperties()
                                .get(HaywardBindingConstants.PROPERTY_FILTER_MAXRPM);
                        if (filterMaxRpm != null) {
                            Integer rpmSpeed = (Integer.parseInt(data.get(i))) * (Integer.parseInt(filterMaxRpm)) / 100;
                            updateData(HaywardBindingConstants.CHANNEL_FILTER_SPEEDRPM, rpmSpeed.toString());
                        }

                        if (data.get(i).equals("0")) {
                            updateData(HaywardBindingConstants.CHANNEL_FILTER_ENABLE, "0");
                        } else {
                            updateData(HaywardBindingConstants.CHANNEL_FILTER_ENABLE, "1");
                        }

                        // Speed Select
                        data = bridgehandler.evaluateXPath("//Filter/@filterSpeed", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_SPEEDSELECT, data.get(i));

                        // State
                        data = bridgehandler.evaluateXPath("//Filter/@filterState", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_STATE, data.get(i));

                        // lastSpeed
                        data = bridgehandler.evaluateXPath("//Filter/@lastSpeed", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_FILTER_LASTSPEED, data.get(i));
                        channelStates.putAll(updateData(HaywardBindingConstants.CHANNEL_FILTER_LASTSPEED, data.get(i)));
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
        String filterMinSpeed = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_MINSPEED);
        String filterMaxSpeed = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_MAXSPEED);
        String filterMaxRpm = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_FILTER_MAXRPM);

        Bridge bridge = getBridge();
        if (bridge != null) {
            HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
            if (bridgehandler != null) {
                String cmdString = this.cmdToString(command);
                try {
                    switch (channelUID.getId()) {
                        case HaywardBindingConstants.CHANNEL_FILTER_ENABLE:
                            if (command == OnOffType.ON) {
                                cmdString = channelStates.get(HaywardBindingConstants.CHANNEL_FILTER_LASTSPEED)
                                        .format("%d");
                            } else {
                                cmdString = "0";
                            }
                            break;
                        case HaywardBindingConstants.CHANNEL_FILTER_SPEEDPERCENT:
                            if (filterMinSpeed != null && filterMaxSpeed != null) {
                                if (Integer.parseInt(cmdString) > 0
                                        && Integer.parseInt(cmdString) < Integer.parseInt(filterMinSpeed)) {
                                    cmdString = filterMinSpeed;
                                } else if (Integer.parseInt(cmdString) > Integer.parseInt(filterMaxSpeed)) {
                                    cmdString = filterMaxSpeed;
                                }
                            }
                            break;
                        case HaywardBindingConstants.CHANNEL_FILTER_SPEEDRPM:
                            // Convert cmdString from RPM to Percent
                            if (filterMaxRpm != null && filterMaxSpeed != null && filterMinSpeed != null) {
                                cmdString = Integer
                                        .toString((Integer.parseInt(cmdString) * 100 / Integer.parseInt(filterMaxRpm)));
                                if (Integer.parseInt(cmdString) > 0
                                        && Integer.parseInt(cmdString) < Integer.parseInt(filterMinSpeed)) {
                                    cmdString = filterMinSpeed;
                                } else if (Integer.parseInt(cmdString) > Integer.parseInt(filterMaxSpeed)) {
                                    cmdString = filterMaxSpeed;
                                }
                            }
                            break;
                        case HaywardBindingConstants.CHANNEL_FILTER_SPEEDSELECT:
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
