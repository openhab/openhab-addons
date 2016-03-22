/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.internal.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tim Oberföll - Initial contribution
 */
public class MySensorsHandler extends BaseThingHandler implements MySensorsUpdateListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsHandler.class);

    private MySensorsSensorConfiguration configuration = null;

    private int nodeId = 0;
    private int childId = 0;
    private boolean requestAck = false;

    private String oldMsgContent = "";

    public MySensorsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        configuration = getConfigAs(MySensorsSensorConfiguration.class);
        nodeId = Integer.parseInt(configuration.nodeId);
        childId = Integer.parseInt(configuration.childId);
        requestAck = configuration.requestAck;

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        getBridgeHandler().getBridgeConnection().removeUpdateListener(this);
        updateStatus(ThingStatus.OFFLINE);
        super.dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String msg = "";
        int subType = 0;
        int int_requestack = 0;
        if (requestAck) {
            int_requestack = 1;
        }

        if (channelUID.getId().equals(CHANNEL_STATUS)) {

            subType = MYSENSORS_SUBTYPE_V_STATUS;

            if (command instanceof OnOffType) {
                if ((OnOffType) command == OnOffType.ON) {
                    msg = "1";
                } else if ((OnOffType) command == OnOffType.OFF) {
                    msg = "0";
                }
            }

        } else if (channelUID.getId().equals(CHANNEL_DIMMER)) {
            if (command instanceof PercentType) {
                msg = ((PercentType) command).toString();
                subType = MYSENSORS_SUBTYPE_V_PERCENTAGE;
            } else {
                if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.ON) {
                        msg = "1";
                    } else if ((OnOffType) command == OnOffType.OFF) {
                        msg = "0";
                    }
                }
                subType = MYSENSORS_SUBTYPE_V_STATUS;
            }
        } else if (channelUID.getId().equals(CHANNEL_COVER)) {
            if (command instanceof PercentType) {
                msg = ((PercentType) command).toString();
                subType = MYSENSORS_SUBTYPE_V_PERCENTAGE;
            } else {
                if (command instanceof UpDownType) {
                    if ((UpDownType) command == UpDownType.UP) {
                        subType = MYSENSORS_SUBTYPE_V_UP;
                    } else if ((UpDownType) command == UpDownType.DOWN) {
                        subType = MYSENSORS_SUBTYPE_V_DOWN;
                    }
                } else if (command instanceof StopMoveType) {
                    if ((StopMoveType) command == StopMoveType.STOP) {
                        subType = MYSENSORS_SUBTYPE_V_STOP;
                    }
                }

                msg = "1";

            }
        } else if (channelUID.getId().equals(CHANNEL_HVAC_SETPOINT_HEAT)) {
            subType = MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_HEAT;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_HVAC_SETPOINT_COOL)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_COOL;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_HVAC_FLOW_STATE)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_HVAC_FLOW_STATE;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_HVAC_FLOW_MODE)) {
            subType = MYSENSORS_SUBTYPE_V_HVAC_FLOW_MODE;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_HVAC_SPEED)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_HVAC_SPEED;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR1)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_VAR1;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR2)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_VAR2;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR3)) {
            subType = MYSENSORS_SUBTYPE_V_VAR3;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR4)) {
            subType = MYSENSORS_SUBTYPE_V_VAR4;
            msg = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR5)) {
            subType = MYSENSORS_SUBTYPE_V_VAR5;
            msg = command.toString();
        } else {
            msg = "";
        }

        MySensorsMessage newMsg = new MySensorsMessage(nodeId, childId, MYSENSORS_MSG_TYPE_SET, int_requestack, subType,
                msg);
        newMsg.setOldMsg(oldMsgContent);
        oldMsgContent = msg;

        getBridgeHandler().getBridgeConnection().addMySensorsOutboundMessage(newMsg);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#handleUpdate(org.eclipse.smarthome.core.thing.
     * ChannelUID, org.eclipse.smarthome.core.types.State)
     */
    @Override
    public void handleUpdate(ChannelUID channelUID, org.eclipse.smarthome.core.types.State newState) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.mysensors.handler.MySensorsUpdateListener#statusUpdateReceived(org.openhab.binding.mysensors.
     * handler.MySensorsStatusUpdateEvent)
     */
    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        MySensorsMessage msg = event.getData();

        // or is this an update message?
        if (nodeId == msg.getNodeId()) { // is this message for me?
            if (msg.getMsgType() == MYSENSORS_MSG_TYPE_SET) {
                if (childId == msg.getChildId()) { // which child should be updated?
                    if (CHANNEL_MAP.containsKey(msg.getSubType())) {
                        String channel = CHANNEL_MAP.get(msg.getSubType());
                        if (channel.equals(CHANNEL_BARO)) {
                            updateState(channel, new StringType(msg.getMsg()));
                        } else if (channel.equals(CHANNEL_STATUS)) {
                            if (msg.getMsg().equals("1")) {
                                updateState(channel, OnOffType.ON);
                            } else {
                                updateState(channel, OnOffType.OFF);
                            }
                        } else if (channel.equals(CHANNEL_ARMED) || channel.equals(CHANNEL_TRIPPED)) {
                            if (msg.getMsg().equals("1")) {
                                updateState(channel, OpenClosedType.OPEN);
                            } else {
                                updateState(channel, OpenClosedType.CLOSED);
                            }
                        } else if (channel.equals(CHANNEL_DIMMER)) {
                            updateState(channel, new PercentType(msg.getMsg()));
                        } else if (channel.equals(CHANNEL_COVER)) {
                            if (msg.getMsg().equals(MYSENSORS_SUBTYPE_V_UP)) {
                                updateState(channel, UpDownType.UP);
                            } else if (msg.getMsg().equals(MYSENSORS_SUBTYPE_V_DOWN)) {
                                updateState(channel, UpDownType.DOWN);
                            }
                        } else if (channel.equals(CHANNEL_HVAC_FLOW_STATE)) {
                            updateState(channel, new StringType(msg.getMsg()));
                        } else if (channel.equals(CHANNEL_HVAC_FLOW_MODE)) {
                            updateState(channel, new StringType(msg.getMsg()));
                        } else if (channel.equals(CHANNEL_HVAC_SPEED)) {
                            updateState(channel, new StringType(msg.getMsg()));
                        } else {
                            updateState(channel, new DecimalType(msg.getMsg()));
                        }
                        oldMsgContent = msg.getMsg();
                    }
                }
            } else if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) { // INTERNAL MESSAGE?
                if (CHANNEL_MAP_INTERNAL.containsKey(msg.getSubType())) {
                    String channel = CHANNEL_MAP_INTERNAL.get(msg.getSubType());
                    if (channel.equals(CHANNEL_VERSION)) {
                        updateState(channel, new StringType(msg.getMsg()));
                    } else if (channel.equals(CHANNEL_BATTERY)) {
                        updateState(channel, new DecimalType(msg.getMsg()));
                    }
                }
            }
        }

    }

    /**
     * Returns the BridgeHandler of the bridge/gateway to the MySensors network
     *
     * @return BridgeHandler of the bridge/gateway to the MySensors network
     */
    public synchronized MySensorsBridgeHandler getBridgeHandler() {
        MySensorsBridgeHandler myBridgeHandler = null;

        Bridge bridge = getBridge();
        myBridgeHandler = (MySensorsBridgeHandler) bridge.getHandler();

        return myBridgeHandler;
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        MySensorsBridgeHandler bridgeHandler = (MySensorsBridgeHandler) thingHandler;
        bridgeHandler.getBridgeConnection().addUpdateListener(this);
    }

    @Override
    public void disconnectEvent() {
        // TODO Auto-generated method stub

    }
}
