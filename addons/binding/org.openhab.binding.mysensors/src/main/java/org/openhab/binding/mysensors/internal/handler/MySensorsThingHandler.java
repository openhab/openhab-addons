/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
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
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageParser;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsThingHandler} is responsible for handling commands, which are
 * sent to one of the channels and messages received via the MySensors network.
 *
 * @author Tim Oberföll
 */
public class MySensorsThingHandler extends BaseThingHandler implements MySensorsUpdateListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsThingHandler.class);

    private MySensorsSensorConfiguration configuration = null;

    private int nodeId = 0;
    private int childId = 0;
    private boolean requestAck = false;
    private boolean revertState = true;
    private boolean smartSleep = false;

    private DateTimeType lastUpdate = null;

    private Map<Integer, String> oldMsgContent = new HashMap<Integer, String>();

    public MySensorsThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(MySensorsSensorConfiguration.class);
        nodeId = Integer.parseInt(configuration.nodeId);
        childId = Integer.parseInt(configuration.childId);
        requestAck = configuration.requestAck;
        revertState = configuration.revertState;
        smartSleep = configuration.smartSleep;
        logger.debug("Configuration: nodeId {}, chiledId: {}, requestAck: {}, revertState: {}, smartSleep: {}"
        		, nodeId, childId, requestAck, revertState, smartSleep);
        if (!getBridgeHandler().getBridgeConnection().isEventListenerRegisterd(this)) {
            logger.debug("Event listener for node {}-{} not registered yet, registering...", nodeId, childId);
            getBridgeHandler().getBridgeConnection().addEventListener(this);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("MySensors Bridge Status updated to {} for device: {}", bridgeStatusInfo.getStatus().toString(),
                getThing().getUID().toString());
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)
                || bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            if (!getBridgeHandler().getBridgeConnection().isEventListenerRegisterd(this)) {
                logger.debug("Event listener for node {}-{} not registered yet, registering...", nodeId, childId);
                getBridgeHandler().getBridgeConnection().addEventListener(this);
            }

            // the node has the same status of the bridge
            updateStatus(bridgeStatusInfo.getStatus());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        /*
         * We don't handle refresh commands yet
         *
         */
        if (command == RefreshType.REFRESH) {
            return;
        }

        String msgPayload = "";
        int subType = 0;
        int int_requestack = 0;
        if (requestAck) {
            int_requestack = 1;
        }

        // just forward the message in case it is received via this channel. This is special!
        if (channelUID.getId().equals(CHANNEL_MYSENSORS_MESSAGE)) {
            if (command instanceof StringType) {
                StringType stringTypeMessage = (StringType) command;
                MySensorsMessage msg = MySensorsMessageParser.parse(stringTypeMessage.toString());
                getBridgeHandler().getBridgeConnection().addMySensorsOutboundMessage(msg);
                return;
            }
        } else if (channelUID.getId().equals(CHANNEL_STATUS)) {

            subType = MYSENSORS_SUBTYPE_V_STATUS;

            if (command instanceof OnOffType) {
                if ((OnOffType) command == OnOffType.ON) {
                    msgPayload = "1";
                } else if ((OnOffType) command == OnOffType.OFF) {
                    msgPayload = "0";
                }
            }

        } else if (channelUID.getId().equals(CHANNEL_DIMMER)) {
            if (command instanceof PercentType) {
                msgPayload = ((PercentType) command).toString();
                subType = MYSENSORS_SUBTYPE_V_PERCENTAGE;
            } else {
                if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.ON) {
                        msgPayload = "1";
                    } else if ((OnOffType) command == OnOffType.OFF) {
                        msgPayload = "0";
                    }
                }
                subType = MYSENSORS_SUBTYPE_V_STATUS;
            }
        } else if (channelUID.getId().equals(CHANNEL_COVER)) {
            if (command instanceof PercentType) {
                msgPayload = ((PercentType) command).toString();
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

                msgPayload = "1";

            }
        } else if (channelUID.getId().equals(CHANNEL_HVAC_SETPOINT_HEAT)) {
            subType = MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_HEAT;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_HVAC_SETPOINT_COOL)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_COOL;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_HVAC_FLOW_STATE)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_HVAC_FLOW_STATE;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_HVAC_FLOW_MODE)) {
            subType = MYSENSORS_SUBTYPE_V_HVAC_FLOW_MODE;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_HVAC_SPEED)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_HVAC_SPEED;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR1)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_VAR1;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR2)) {// Unverified
            subType = MYSENSORS_SUBTYPE_V_VAR2;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR3)) {
            subType = MYSENSORS_SUBTYPE_V_VAR3;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR4)) {
            subType = MYSENSORS_SUBTYPE_V_VAR4;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VAR5)) {
            subType = MYSENSORS_SUBTYPE_V_VAR5;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_FLOW)) {
            subType = MYSENSORS_SUBTYPE_V_FLOW;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_VOLUME)) {
            subType = MYSENSORS_SUBTYPE_V_VOLUME;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_TEXT)) {
            subType = MYSENSORS_SUBTYPE_V_TEXT;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_IR_SEND)) {
            subType = MYSENSORS_SUBTYPE_V_IR_SEND;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_IR_RECEIVE)) {
            subType = MYSENSORS_SUBTYPE_V_IR_RECEIVE;
            msgPayload = command.toString();
        } else if (channelUID.getId().equals(CHANNEL_ORP)) {
        	subType = MYSENSORS_SUBTYPE_V_ORP;
        	msgPayload = command.toString();
        } else {
            msgPayload = "";
        }

        MySensorsMessage newMsg = new MySensorsMessage(nodeId, childId, MYSENSORS_MSG_TYPE_SET, int_requestack,
                revertState, subType, msgPayload);

        String oldPayload = oldMsgContent.get(subType);
        if (oldPayload == null) {
            oldPayload = "";
        }
        newMsg.setOldMsg(oldPayload);
        oldMsgContent.put(subType, msgPayload);

        if(smartSleep) {
        	getBridgeHandler().getBridgeConnection().addMySensorsOutboundSmartSleepMessage(newMsg);
        } else {
        	getBridgeHandler().getBridgeConnection().addMySensorsOutboundMessage(newMsg);
        }
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        switch (event.getEventType()) {
            case INCOMING_MESSAGE:
                handleIncomingMessageEvent((MySensorsMessage) event.getData());
                break;
            case NODE_STATUS_UPDATE:
                // TODO Network Sanity Checker could put node to 'unreachable' causing, here, to set this thing to
                // OFFLINE
                if (!((MySensorsNode) event.getData()).isReachable()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Returns the BridgeHandler of the bridge/gateway to the MySensors network
     *
     * @return BridgeHandler of the bridge/gateway to the MySensors network
     */
    private synchronized MySensorsBridgeHandler getBridgeHandler() {
        MySensorsBridgeHandler myBridgeHandler = null;

        Bridge bridge = getBridge();
        myBridgeHandler = (MySensorsBridgeHandler) bridge.getHandler();

        return myBridgeHandler;
    }

    /**
     * If a new message is received via the MySensors bridge it is handed over to the ThingHandler
     * and processed in this method. After parsing the message the corresponding channel is updated.
     *
     * @param msg The message that was received by the MySensors gateway.
     */
    private void handleIncomingMessageEvent(MySensorsMessage msg) {
        // Am I the all knowing node that receives all messages?
        if (nodeId == MYSENSORS_NODE_ID_ALL_KNOWING && childId == MYSENSORS_CHILD_ID_ALL_KNOWING) {
            updateState(CHANNEL_MYSENSORS_MESSAGE,
                    new StringType(MySensorsMessageParser.generateAPIString(msg).replaceAll("(\\r|\\n)", "")));

        } else if (nodeId == msg.getNodeId()) { // is this message for me?

            updateLastUpdate();

            if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) { // INTERNAL MESSAGE?
                if (CHANNEL_MAP_INTERNAL.containsKey(msg.getSubType())) {
                    String channel = CHANNEL_MAP_INTERNAL.get(msg.getSubType());
                    if (channel.equals(CHANNEL_VERSION)) {
                        updateState(channel, new StringType(msg.getMsg()));
                    } else if (channel.equals(CHANNEL_BATTERY)) {
                        updateState(channel, new DecimalType(msg.getMsg()));
                    }
                }
            } else if (msg.getMsgType() == MYSENSORS_MSG_TYPE_SET) {

                if (childId == msg.getChildId()) { // which child should be updated?
                    if (CHANNEL_MAP.containsKey(msg.getSubType())) {
                        String channel = CHANNEL_MAP.get(msg.getSubType());

                        switch (channel) {
                            case CHANNEL_STATUS:
                                if (msg.getMsg().equals("1")) {
                                    updateState(channel, OnOffType.ON);
                                } else if (msg.getMsg().equals("0")) {
                                    updateState(channel, OnOffType.OFF);
                                }
                                break;

                            case CHANNEL_ARMED:
                            case CHANNEL_TRIPPED:
                            case CHANNEL_LOCK_STATUS:
                                if (msg.getMsg().equals("1")) {
                                    updateState(channel, OpenClosedType.OPEN);
                                } else {
                                    updateState(channel, OpenClosedType.CLOSED);
                                }
                                break;

                            case CHANNEL_DIMMER:
                                updateState(channel, new PercentType(msg.getMsg()));
                                break;

                            case CHANNEL_BARO:
                            case CHANNEL_HVAC_FLOW_STATE:
                            case CHANNEL_HVAC_FLOW_MODE:
                            case CHANNEL_HVAC_SPEED:
                            case CHANNEL_TEXT:
                            case CHANNEL_IR_SEND:
                            case CHANNEL_IR_RECEIVE:
                            case CHANNEL_GUST:
                            case CHANNEL_RAIN:
                            case CHANNEL_VAR1:
                            case CHANNEL_VAR2:
                            case CHANNEL_VAR3:
                            case CHANNEL_VAR4:
                            case CHANNEL_VAR5:
                            case CHANNEL_RGB:
                            case CHANNEL_RGBW:
                            case CHANNEL_CUSTOM:
                            case CHANNEL_POSITION:
                            case CHANNEL_IR_RECORD:
                                updateState(channel, new StringType(msg.getMsg()));
                                break;

                            case CHANNEL_COVER:
                                if (msg.getMsg().equals(MYSENSORS_SUBTYPE_V_UP)) {
                                    updateState(channel, UpDownType.UP);
                                } else if (msg.getMsg().equals(MYSENSORS_SUBTYPE_V_DOWN)) {
                                    updateState(channel, UpDownType.DOWN);
                                }
                                break;

                            case CHANNEL_TEMP:
                            case CHANNEL_HUM:
                            case CHANNEL_VOLT:
                            case CHANNEL_WATT:
                            case CHANNEL_KWH:
                            case CHANNEL_PRESSURE:
                            case CHANNEL_WIND:
                            case CHANNEL_UV:
                            case CHANNEL_RAINRATE:
                            case CHANNEL_WEIGHT:
                            case CHANNEL_IMPEDANCE:
                            case CHANNEL_CURRENT:
                            case CHANNEL_DISTANCE:
                            case CHANNEL_LIGHT_LEVEL:
                            case CHANNEL_FLOW:
                            case CHANNEL_VOLUME:
                            case CHANNEL_LEVEL:
                            case CHANNEL_CO2_LEVEL:
                            case CHANNEL_PH:
                                updateState(channel, new DecimalType(msg.getMsg()));
                                break;

                            default:
                                updateState(channel, new DecimalType(msg.getMsg()));

                        }

                        oldMsgContent.put(msg.getSubType(), msg.getMsg());

                    }
                }
            } else if (msg.getMsgType() == MYSENSORS_MSG_TYPE_REQ) {
                if (childId == msg.getChildId()) {
                    logger.debug("Request received!");
                    msg.setMsgType(MYSENSORS_MSG_TYPE_SET);
                    String oldVal = oldMsgContent.get(msg.getSubType());
                    if (oldVal == null) {
                        oldVal = "";
                    }
                    msg.setMsg(oldVal);
                    getBridgeHandler().getBridgeConnection().addMySensorsOutboundMessage(msg);
                }
            }

        }
    }

    /**
     * For every thing there is a lastUpdate channel in which the date/time is stored
     * a message was received from this thing.
     */
    private void updateLastUpdate() {
        // Don't always fire last update channel, do it only after a minute by
        if (lastUpdate == null || (System.currentTimeMillis() > (lastUpdate.getCalendar().getTimeInMillis() + 60000))) {
            DateTimeType dt = new DateTimeType();
            lastUpdate = dt;
            updateState(CHANNEL_LAST_UPDATE, dt);
            logger.debug("Setting last update for node {} to {}", nodeId, dt.toString());
        }
    }
}
