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
 * The Pump Handler
 *
 * @author Matt Myers - Initial contribution
 */
@NonNullByDefault
public class HaywardPumpHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardPumpHandler.class);
    private Map<String, State> channelStates = new HashMap<>();

    public HaywardPumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            setStateDescriptions();
            updateStatus(ThingStatus.ONLINE);
        } catch (HaywardException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to setPumpHandler StateDescriptions");
        }
    }

    @Override
    public void setStateDescriptions() throws HaywardException {
        List<StateOption> options = new ArrayList<>();
        String option;

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HaywardBridgeHandler bridgehandler) {
            // Set Pump % min and max speeds
            Channel ch = thing.getChannel(HaywardBindingConstants.CHANNEL_PUMP_SPEEDPERCENT);
            if (ch != null) {
                StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                        .withMinimum(new BigDecimal(
                                getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MINSPEED)))
                        .withMaximum(new BigDecimal(
                                getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MAXSPEED)))
                        .withPattern("%d %%").withStep(new BigDecimal(5)).withReadOnly(false).build();
                bridgehandler.updateChannelStateDescriptionFragment(ch, stateDescriptionFragment);
            }

            // Set Pump Speed RPM min and max speeds
            ch = thing.getChannel(HaywardBindingConstants.CHANNEL_PUMP_SPEEDRPM);
            if (ch != null) {
                StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                        .withMinimum(new BigDecimal(
                                getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MINRPM)))
                        .withMaximum(new BigDecimal(
                                getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MAXRPM)))
                        .withPattern("%d rpm").withStep(new BigDecimal(200)).withReadOnly(false).build();
                bridgehandler.updateChannelStateDescriptionFragment(ch, stateDescriptionFragment);
            }

            // Set Pump Speed States
            ch = thing.getChannel(HaywardBindingConstants.CHANNEL_PUMP_SPEEDSELECT);
            if (ch != null) {
                options.add(new StateOption("0", "Off"));
                option = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_LOWSPEED);
                if (option != null) {
                    options.add(new StateOption(option, "Low"));
                }
                option = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MEDSPEED);
                if (option != null) {
                    options.add(new StateOption(option, "Medium"));
                }
                option = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_HIGHSPEED);
                if (option != null) {
                    options.add(new StateOption(option, "High"));
                }
                option = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_CUSTOMSPEED);
                if (option != null) {
                    options.add(new StateOption(option, "Custom"));
                }

                StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                        .withOptions(options).build();
                bridgehandler.updateChannelStateDescriptionFragment(ch, stateDescriptionFragment);
            }
        }
    }

    @Override
    public void getTelemetry(String xmlResponse) throws HaywardException {
        List<String> systemIDs = new ArrayList<>();
        List<String> data = new ArrayList<>();

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HaywardBridgeHandler bridgehandler) {
            systemIDs = bridgehandler.evaluateXPath("//Pump/@systemId", xmlResponse);
            String thingSystemID = getThing().getUID().getId();
            for (int i = 0; i < systemIDs.size(); i++) {
                if (systemIDs.get(i).equals(thingSystemID)) {
                    // Speed percent
                    data = bridgehandler.evaluateXPath("//Pump/@pumpSpeed", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_PUMP_SPEEDPERCENT, data.get(i));

                    // Speed rpm
                    String pumpMaxRpm = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MAXRPM);
                    if (pumpMaxRpm != null) {
                        Integer rpmSpeed = (Integer.parseInt(data.get(i))) * (Integer.parseInt(pumpMaxRpm)) / 100;
                        updateData(HaywardBindingConstants.CHANNEL_PUMP_SPEEDRPM, rpmSpeed.toString());
                    }

                    if ("0".equals(data.get(i))) {
                        updateData(HaywardBindingConstants.CHANNEL_PUMP_ENABLE, "0");
                    } else {
                        updateData(HaywardBindingConstants.CHANNEL_PUMP_ENABLE, "1");
                    }

                    // Speed Select
                    data = bridgehandler.evaluateXPath("//Pump/@pumpSpeed", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_PUMP_SPEEDSELECT, data.get(i));

                    // State
                    data = bridgehandler.evaluateXPath("//Pump/@pumpState", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_PUMP_STATE, data.get(i));

                    // lastSpeed
                    data = bridgehandler.evaluateXPath("//Pump/@lastSpeed", xmlResponse);
                    updateData(HaywardBindingConstants.CHANNEL_PUMP_LASTSPEED, data.get(i));
                    channelStates.putAll(updateData(HaywardBindingConstants.CHANNEL_PUMP_LASTSPEED, data.get(i)));
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

        String systemID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
        String poolID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_BOWID);
        String pumpMinSpeed = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MINSPEED);
        String pumpMaxSpeed = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MAXSPEED);
        String pumpMaxRpm = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_PUMP_MAXRPM);

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HaywardBridgeHandler bridgehandler) {
            String cmdString = this.cmdToString(command);
            try {
                switch (channelUID.getId()) {
                    case HaywardBindingConstants.CHANNEL_PUMP_ENABLE:
                        if (command == OnOffType.ON) {
                            cmdString = channelStates.get(HaywardBindingConstants.CHANNEL_PUMP_LASTSPEED).format("%d");
                        } else {
                            cmdString = "0";
                        }
                        break;
                    case HaywardBindingConstants.CHANNEL_PUMP_SPEEDPERCENT:
                        if (pumpMinSpeed != null && pumpMaxSpeed != null) {
                            if (Integer.parseInt(cmdString) > 0
                                    && Integer.parseInt(cmdString) < Integer.parseInt(pumpMinSpeed)) {
                                cmdString = pumpMinSpeed;
                            } else if (Integer.parseInt(cmdString) > Integer.parseInt(pumpMaxSpeed)) {
                                cmdString = pumpMaxSpeed;
                            }
                        }
                        break;
                    case HaywardBindingConstants.CHANNEL_PUMP_SPEEDRPM:
                        // Convert cmdString from RPM to Percent
                        if (pumpMaxRpm != null && pumpMaxSpeed != null && pumpMinSpeed != null) {
                            cmdString = Integer
                                    .toString((Integer.parseInt(cmdString) * 100 / Integer.parseInt(pumpMaxRpm)));
                            if (Integer.parseInt(cmdString) > 0
                                    && Integer.parseInt(cmdString) < Integer.parseInt(pumpMinSpeed)) {
                                cmdString = pumpMinSpeed;
                            } else if (Integer.parseInt(cmdString) > Integer.parseInt(pumpMaxSpeed)) {
                                cmdString = pumpMaxSpeed;
                            }
                        }
                        break;
                    case HaywardBindingConstants.CHANNEL_PUMP_SPEEDSELECT:
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
