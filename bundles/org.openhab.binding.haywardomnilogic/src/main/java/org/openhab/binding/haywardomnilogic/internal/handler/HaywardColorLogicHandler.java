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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
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
    private Map<String, State> channelStates = new HashMap<>();

    public HaywardColorLogicHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            setStateDescriptions();
            if ("COLOR_LOGIC_UCL_V2"
                    .equals(getThing().getProperties().get(HaywardBindingConstants.PROPERTY_COLORLOGIC_TYPE))) {
                addV2Channels();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (HaywardException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to set ColorLogixHandler StateDescriptions");
        }
    }

    protected void addV2Channels() {
        if (thing.getChannel(HaywardBindingConstants.CHANNEL_COLORLOGIC_BRIGHTNESS) == null) {
            ThingBuilder thingBuilder = editThing();
            ChannelUID uid = new ChannelUID(thing.getUID(), HaywardBindingConstants.CHANNEL_COLORLOGIC_BRIGHTNESS);
            ChannelBuilder chnBuilder = ChannelBuilder.create(uid, "String");
            chnBuilder.withType(new ChannelTypeUID(HaywardBindingConstants.BINDING_ID,
                    HaywardBindingConstants.TYPE_COLORLOGIC_LIGHTBRIGHTNESS));
            chnBuilder.withLabel("Brightness");
            chnBuilder.withDescription("Brightness");
            Channel channel = chnBuilder.build();
            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());
        }

        if (thing.getChannel(HaywardBindingConstants.CHANNEL_COLORLOGIC_SPEED) == null) {
            ThingBuilder thingBuilder = editThing();
            ChannelUID uid = new ChannelUID(thing.getUID(), HaywardBindingConstants.CHANNEL_COLORLOGIC_SPEED);
            ChannelBuilder chnBuilder = ChannelBuilder.create(uid, "String");
            chnBuilder.withType(new ChannelTypeUID(HaywardBindingConstants.BINDING_ID,
                    HaywardBindingConstants.TYPE_COLORLOGIC_LIGHTSPEED));
            chnBuilder.withLabel("Speed");
            chnBuilder.withDescription("Speed");
            Channel channel = chnBuilder.build();
            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());
        }
    }

    @Override
    public void setStateDescriptions() throws HaywardException {
        List<StateOption> options = new ArrayList<>();
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HaywardBridgeHandler bridgehandler) {
            // Set Light Shows based on light type
            Channel ch = thing.getChannel(HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW);
            if (ch != null) {
                String lightType = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_COLORLOGIC_TYPE);
                if (lightType != null) {
                    if ("COLOR_LOGIC_2_5".equals(lightType) || "COLOR_LOGIC_4_0".equals(lightType)) {
                        options.add(new StateOption("0", "Voodoo Lounge"));
                        options.add(new StateOption("1", "Deep Blue Sea"));
                        options.add(new StateOption("2", "Afternoon Sky"));
                        options.add(new StateOption("3", "Emerald"));
                        options.add(new StateOption("4", "Sangria"));
                        options.add(new StateOption("5", "Cloud White"));
                        options.add(new StateOption("6", "Twilight"));
                        options.add(new StateOption("7", "Tranquility"));
                        options.add(new StateOption("8", "Gemstone"));
                        options.add(new StateOption("9", "USA"));
                        options.add(new StateOption("10", "Mardi Gras"));
                        options.add(new StateOption("11", "Cool Cabaret"));
                    } else if (lightType.contains("COLOR_LOGIC_UCL")) {
                        options.add(new StateOption("0", "Voodoo Lounge"));
                        options.add(new StateOption("1", "Deep Blue Sea"));
                        options.add(new StateOption("2", "Royal Blue"));
                        options.add(new StateOption("3", "Afternoon Sky"));
                        options.add(new StateOption("4", "Aqua Green"));
                        options.add(new StateOption("5", "Emerald"));
                        options.add(new StateOption("6", "Cloud White"));
                        options.add(new StateOption("7", "Warm Red"));
                        options.add(new StateOption("8", "Flamingo"));
                        options.add(new StateOption("9", "Vivid Violet"));
                        options.add(new StateOption("10", "Sangria"));
                        options.add(new StateOption("11", "Twilight"));
                        options.add(new StateOption("12", "Tranquility"));
                        options.add(new StateOption("13", "Gemstone"));
                        options.add(new StateOption("14", "USA"));
                        options.add(new StateOption("15", "Mardi Gras"));
                        options.add(new StateOption("16", "Cool Cabaret"));
                    }
                    if ("COLOR_LOGIC_UCL_V2".equals(lightType)) {
                        options.add(new StateOption("17", "Yellow"));
                        options.add(new StateOption("18", "Orange"));
                        options.add(new StateOption("19", "Gold"));
                        options.add(new StateOption("20", "Mint"));
                        options.add(new StateOption("21", "Teal"));
                        options.add(new StateOption("22", "Burnt Orange"));
                        options.add(new StateOption("23", "Pure White"));
                        options.add(new StateOption("24", "Crisp White"));
                        options.add(new StateOption("25", "Warm White"));
                        options.add(new StateOption("26", "Bright Yellow"));
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
                systemIDs = bridgehandler.evaluateXPath("//ColorLogic-Light/@systemId", xmlResponse);
                String thingSystemID = getThing().getUID().getId();
                for (int i = 0; i < systemIDs.size(); i++) {
                    if (systemIDs.get(i).equals(thingSystemID)) {
                        // Light State
                        data = bridgehandler.evaluateXPath("//ColorLogic-Light/@lightState", xmlResponse);
                        updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_STATE, data.get(i));

                        if ("0".equals(data.get(i))) {
                            updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_ENABLE, "0");
                        } else {
                            updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_ENABLE, "1");
                        }

                        // Current Show
                        data = bridgehandler.evaluateXPath("//ColorLogic-Light/@currentShow", xmlResponse);
                        channelStates.putAll(
                                updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW, data.get(0)));

                        // V2 Light Features
                        String lightType = getThing().getProperties()
                                .get(HaywardBindingConstants.PROPERTY_COLORLOGIC_TYPE);
                        if (lightType != null) {
                            if ("COLOR_LOGIC_UCL_V2".equals(lightType)) {
                                // Brightness
                                data = bridgehandler.evaluateXPath("//ColorLogic-Light/@brightness", xmlResponse);
                                channelStates.putAll(
                                        updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_BRIGHTNESS, data.get(0)));

                                // Speed
                                data = bridgehandler.evaluateXPath("//ColorLogic-Light/@speed", xmlResponse);
                                channelStates.putAll(
                                        updateData(HaywardBindingConstants.CHANNEL_COLORLOGIC_SPEED, data.get(0)));
                            }
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
        String show = channelStates.get(HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW).toString();
        String brightness = null;
        String speed = null;

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
                            String lightType = getThing().getProperties()
                                    .get(HaywardBindingConstants.PROPERTY_COLORLOGIC_TYPE);
                            if (lightType != null) {
                                if (!"COLOR_LOGIC_UCL_V2".equals(lightType)) {
                                    cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                                            + "<Name>SetStandAloneLightShow</Name><Parameters>"
                                            + "<Parameter name=\"Token\" dataType=\"String\">"
                                            + bridgehandler.account.token + "</Parameter>"
                                            + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                            + bridgehandler.account.mspSystemID + "</Parameter>"
                                            + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                                            + "<Parameter name=\"LightID\" dataType=\"int\">" + systemID
                                            + "</Parameter>" + "<Parameter name=\"Show\" dataType=\"int\">" + cmdString
                                            + "</Parameter>" + HaywardBindingConstants.COMMAND_SCHEDULE
                                            + "</Parameters></Request>";
                                } else {
                                    brightness = channelStates
                                            .get(HaywardBindingConstants.CHANNEL_COLORLOGIC_BRIGHTNESS).toString();
                                    speed = channelStates.get(HaywardBindingConstants.CHANNEL_COLORLOGIC_SPEED)
                                            .toString();
                                    cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                                            + "<Name>SetStandAloneLightShowV2</Name><Parameters>"
                                            + "<Parameter name=\"Token\" dataType=\"String\">"
                                            + bridgehandler.account.token + "</Parameter>"
                                            + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                            + bridgehandler.account.mspSystemID + "</Parameter>"
                                            + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                                            + "<Parameter name=\"LightID\" dataType=\"int\">" + systemID
                                            + "</Parameter>" + "<Parameter name=\"Show\" dataType=\"int\">" + cmdString
                                            + "</Parameter>" + "<Parameter name=\"Speed\" dataType=\"byte\">" + speed
                                            + "</Parameter>" + "<Parameter name=\"Brightness\" dataType=\"byte\">"
                                            + brightness + "</Parameter>" + HaywardBindingConstants.COMMAND_SCHEDULE
                                            + "</Parameters></Request>";

                                }
                            }
                            break;
                        case HaywardBindingConstants.CHANNEL_COLORLOGIC_BRIGHTNESS:
                            show = channelStates.get(HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW).toString();
                            speed = channelStates.get(HaywardBindingConstants.CHANNEL_COLORLOGIC_SPEED).toString();
                            if (Integer.parseInt(cmdString) > 4) {
                                cmdString = "4";
                            }
                            cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                                    + "<Name>SetStandAloneLightShowV2</Name><Parameters>"
                                    + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                    + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                    + bridgehandler.account.mspSystemID + "</Parameter>"
                                    + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                                    + "<Parameter name=\"LightID\" dataType=\"int\">" + systemID + "</Parameter>"
                                    + "<Parameter name=\"Show\" dataType=\"int\">" + show + "</Parameter>"
                                    + "<Parameter name=\"Speed\" dataType=\"byte\">" + speed + "</Parameter>"
                                    + "<Parameter name=\"Brightness\" dataType=\"byte\">" + cmdString + "</Parameter>"
                                    + HaywardBindingConstants.COMMAND_SCHEDULE + "</Parameters></Request>";
                            break;
                        case HaywardBindingConstants.CHANNEL_COLORLOGIC_SPEED:
                            brightness = channelStates.get(HaywardBindingConstants.CHANNEL_COLORLOGIC_BRIGHTNESS)
                                    .toString();
                            show = channelStates.get(HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW).toString();
                            if (Integer.parseInt(cmdString) > 8) {
                                cmdString = "8";
                            }
                            cmdURL = HaywardBindingConstants.COMMAND_PARAMETERS
                                    + "<Name>SetStandAloneLightShowV2</Name><Parameters>"
                                    + "<Parameter name=\"Token\" dataType=\"String\">" + bridgehandler.account.token
                                    + "</Parameter>" + "<Parameter name=\"MspSystemID\" dataType=\"int\">"
                                    + bridgehandler.account.mspSystemID + "</Parameter>"
                                    + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>"
                                    + "<Parameter name=\"LightID\" dataType=\"int\">" + systemID + "</Parameter>"
                                    + "<Parameter name=\"Show\" dataType=\"int\">" + show + "</Parameter>"
                                    + "<Parameter name=\"Speed\" dataType=\"byte\">" + cmdString + "</Parameter>"
                                    + "<Parameter name=\"Brightness\" dataType=\"byte\">" + brightness + "</Parameter>"
                                    + HaywardBindingConstants.COMMAND_SCHEDULE + "</Parameters></Request>";
                            break;
                        default:
                            logger.warn("haywardCommand Unsupported type {}", channelUID);
                            return;
                    }

                    // *****Send Command to Hayward server
                    if (cmdURL != null) {
                        String xmlResponse = bridgehandler.httpXmlResponse(cmdURL);
                        String status = bridgehandler.evaluateXPath("//Parameter[@name='Status']/text()", xmlResponse)
                                .get(0);

                        if (!("0".equals(status))) {
                            logger.debug("haywardCommand XML response: {}", xmlResponse);
                            return;
                        }
                    }
                } catch (HaywardException e) {
                    logger.debug("Unable to send command to Hayward's server {}:{}:{}",
                            bridgehandler.config.endpointUrl, bridgehandler.config.username, e.getMessage());
                } catch (InterruptedException e) {
                    return;
                }
                this.updateStatus(ThingStatus.ONLINE);
            } else

            {
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        }
    }
}
