/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.converter.MySensorsTypeConverter;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.event.MySensorsNodeUpdateEventType;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGateway;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageAck;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageType;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChildConfig;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNodeConfig;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsThingHandler} is responsible for handling commands, which are
 * sent to one of the channels and messages received via the MySensors network.
 *
 * @author Tim Oberföll
 */
public class MySensorsThingHandler extends BaseThingHandler implements MySensorsGatewayEventListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MySensorsSensorConfiguration configuration;

    private DateTimeType lastUpdate;

    private MySensorsGateway myGateway;

    public MySensorsThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(MySensorsSensorConfiguration.class);

        logger.debug("Configuration: {}", configuration.toString());

        myGateway = getBridgeHandler().getMySensorsGateway();
        addIntoGateway(getThing(), configuration);

        registerListeners();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        myGateway.removeEventListener(this);
        myGateway.removeNode(configuration.nodeId);
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("MySensors Bridge Status updated to {} for device: {}", bridgeStatusInfo.getStatus(),
                getThing().getUID().toString());
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                || bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {

            if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
                registerListeners();
            } else {
                myGateway.removeEventListener(this);
            }

            // the node has the same status of the bridge
            updateStatus(bridgeStatusInfo.getStatus());
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Configuation update for thing {}-{}: {}", configuration.nodeId, configuration.childId, configurationParameters);
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        logger.trace("New state {} received for channel uid {}", newState, channelUID);
        super.handleUpdate(channelUID, newState);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command {} received for channel uid {}", command, channelUID);
        
        // We don't handle refresh commands yet
        if (command == RefreshType.REFRESH) {
            return;
        }

        int intRequestAck = configuration.requestAck ? 1 : 0;

        // just forward the message in case it is received via this channel. This is special!
        if (channelUID.getId().equals(CHANNEL_MYSENSORS_MESSAGE)) {
            if (command instanceof StringType) {
                StringType stringTypeMessage = (StringType) command;
                try {
                    MySensorsMessage msg = MySensorsMessage.parse(stringTypeMessage.toString());
                    myGateway.sendMessage(msg);
                } catch (ParseException e) {
                    logger.error("Invalid message to send", e);
                }
            }
        } else {
            MySensorsTypeConverter adapter = loadAdapterForChannelType(channelUID.getId());

            if (adapter != null) {
                logger.trace("Adapter {} found for type {}", adapter.getClass().getSimpleName(), channelUID.getId());

                MySensorsMessageSubType type = adapter.typeFromChannelCommand(channelUID.getId(), command);

                if (type != null) {
                    logger.trace("Type for channel: {}, command: {} of thing {} is: {}", thing.getUID(), command,
                            thing.getUID(), type);

                    MySensorsVariable var = myGateway.getVariable(configuration.nodeId, configuration.childId, type);

                    if (var != null) {

                        MySensorsMessageSubType subType = var.getType();

                        // Create the real message to send
                        MySensorsMessage newMsg = new MySensorsMessage(configuration.nodeId, configuration.childId,
                                MySensorsMessageType.SET, MySensorsMessageAck.getById(intRequestAck), configuration.revertState, configuration.smartSleep);

                        newMsg.setSubType(subType);
                        newMsg.setMsg(adapter.fromCommand(command));

                        myGateway.sendMessage(newMsg);

                    } else {
                        logger.warn("Variable not found, cannot handle command for thing {} of type {}", thing.getUID(),
                                channelUID.getId());
                    }
                } else {
                    logger.error("Could not get type of variable for channel: {}, command: {} of thing {}",
                            thing.getUID(), command, thing.getUID());
                }

            } else {
                logger.error("Type adapter not found for {}", channelUID.getId());
            }

        }
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Exception {
        handleIncomingMessageEvent(message);

    }

    @Override
    public void sensorUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var,
            MySensorsNodeUpdateEventType eventType) {
        switch (eventType) {
            case UPDATE:
            case REVERT:
                if ((node.getNodeId() == configuration.nodeId) && (child.getChildId() == configuration.childId)) {
                    handleChildUpdateEvent(var);
                    updateLastUpdate(node, eventType == MySensorsNodeUpdateEventType.REVERT);
                }
                break;
            case BATTERY:
                if (node.getNodeId() == configuration.nodeId) {
                    handleBatteryUpdateEvent(node);
                    updateLastUpdate(node, eventType == MySensorsNodeUpdateEventType.REVERT);
                }
                break;
        }

    }

    @Override
    public void connectionStatusUpdate(MySensorsAbstractConnection connection, boolean connected) throws Exception {
        if (!connected) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void nodeReachStatusChanged(MySensorsNode node, boolean reach) {
        if (node.getNodeId() == configuration.nodeId) {
            if (!reach) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        }

    }

    /**
     * For every thing there is a lastUpdate channel in which the date/time is stored
     * a message was received from this thing.
     */
    private void updateLastUpdate(MySensorsNode node, boolean isRevert) {
        // Don't always fire last update channel, do it only after a minute by
        if (lastUpdate == null || (System.currentTimeMillis() > (lastUpdate.getCalendar().getTimeInMillis() + 60000))
                || configuration.revertState) {
            DateTimeType dt = new DateTimeType(
                    new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(node.getLastUpdate()));
            lastUpdate = dt;
            updateState(CHANNEL_LAST_UPDATE, dt);
            if (!isRevert) {
                logger.debug("Setting last update for node/child {}/{} to {}", configuration.nodeId, configuration.childId, dt.toString());
            } else {
                logger.warn("Setting last update for node/child {}/{} BACK (due to revert) to {}", configuration.nodeId, configuration.childId,
                        dt.toString());
            }
        }
    }

    /**
     * Returns the BridgeHandler of the bridge/gateway to the MySensors network
     *
     * @return BridgeHandler of the bridge/gateway to the MySensors network
     */
    private synchronized MySensorsBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        MySensorsBridgeHandler myBridgeHandler = (MySensorsBridgeHandler) bridge.getHandler();

        return myBridgeHandler;
    }

    /**
     * Update the state of a child based on a MySensorsVariable var.
     * 
     * In case a V_PERCENTAGE is received for a Cover the channel CHANNEL_COVER needs to be updated
     * 
     * @param var variable that needs to be updated
     */
    private void handleChildUpdateEvent(MySensorsVariable var) {
        String channelName = getChannelNameFromVar(var);
        State newState = loadAdapterForChannelType(channelName).stateFromChannel(var);
        logger.debug("Updating channel: {}({}) value to: {}", channelName, var.getType(), newState);
        if(myGateway.getNode(configuration.nodeId).getChild(configuration.childId).getPresentationCode() == MySensorsMessageSubType.S_COVER)
            updateState(CHANNEL_COVER, newState);
        updateState(channelName, newState);

    }

    private void handleBatteryUpdateEvent(MySensorsNode node) {
        logger.debug("Updating channel: {} value to: {}", CHANNEL_BATTERY, node.getBatteryPercent());
        updateState(CHANNEL_BATTERY, new DecimalType(node.getBatteryPercent()));
    }

    private String getChannelNameFromVar(MySensorsVariable var) {
        // Cover thing has a specific behavior
        if (getThing().getThingTypeUID() == MySensorsBindingConstants.THING_TYPE_COVER) {
            return MySensorsBindingConstants.CHANNEL_COVER;
        } else {
            return CHANNEL_MAP.get(var.getType());
        }
    }

    private MySensorsTypeConverter loadAdapterForChannelType(String channelName) {
        return TYPE_MAP.get(channelName);
    }

    /**
     * If a new message is received via the MySensors bridge it is handed over to the ThingHandler
     * and processed in this method. After parsing the message the corresponding channel is updated.
     *
     * @param msg The message that was received by the MySensors gateway.
     */
    private void handleIncomingMessageEvent(MySensorsMessage msg) {
        // Am I the all knowing node that receives all messages?
        if (configuration.nodeId == MYSENSORS_NODE_ID_ALL_KNOWING && configuration.childId == MYSENSORS_CHILD_ID_ALL_KNOWING) {
            updateState(CHANNEL_MYSENSORS_MESSAGE,
                    new StringType(MySensorsMessage.generateAPIString(msg).replaceAll("(\\r|\\n)", "")));

        }
    }

    private void registerListeners() {
        if (!myGateway.isEventListenerRegisterd(this)) {
            logger.debug("Event listener for node {}-{} not registered yet, registering...", configuration.nodeId, configuration.childId);
            myGateway.addEventListener(this);
        }
    }

    private void addIntoGateway(Thing thing, MySensorsSensorConfiguration configuration) {
        MySensorsNode node = generateNodeFromThing(thing, configuration);
        if (node != null) {
            myGateway.addNode(node, true);
        } else {
            logger.error("Failed to build sensor for thing: {}", thing.getUID());
        }
    }

    private MySensorsNode generateNodeFromThing(Thing t, MySensorsSensorConfiguration configuration) {
        MySensorsNode node = null;

        try {
            Integer nodeId = t.getConfiguration().as(MySensorsSensorConfiguration.class).nodeId;
            Integer childId = t.getConfiguration().as(MySensorsSensorConfiguration.class).childId;
            MySensorsMessageSubType presentation = INVERSE_THING_UID_MAP.get(t.getThingTypeUID());

            if (presentation != null) {
                logger.trace("Building sensors from thing: {}, node: {}, child: {}, presentation: {}", t.getUID(),
                        nodeId, childId, presentation);

                MySensorsChild child = MySensorsChild.fromPresentation(presentation, childId);
                if (child != null) {
                	child.setChildConfig(generateChildConfig(configuration));
                    node = new MySensorsNode(nodeId);
                    node.setNodeConfig(generateNodeConfig(configuration));
                    node.addChild(child);
                }
            } else {
                logger.error("Error on building sensors from thing: {}, node: {}, child: {}, presentation: {}",
                        t.getUID(), nodeId, childId, presentation);
            }

        } catch (Exception e) {
            logger.error("Failing on create node/child for thing {}", thing.getUID(), e);
        }

        return node;

    }

    private MySensorsChildConfig generateChildConfig(MySensorsSensorConfiguration configuration) {
        MySensorsChildConfig ret = new MySensorsChildConfig();
        ret.setRequestAck(configuration.requestAck);
        ret.setRevertState(configuration.revertState);
        ret.setExpectUpdateTimeout(configuration.childUpdateTimeout);

        logger.trace("ChildConfig for {}/{}: {}", configuration.nodeId, configuration.childId, ret.toString());

        return ret;
    }

    private MySensorsNodeConfig generateNodeConfig(MySensorsSensorConfiguration configuration) {
        MySensorsNodeConfig ret = new MySensorsNodeConfig();
        ret.setRequestHeartbeatResponse(configuration.requestHeartbeatResponse);
        ret.setExpectUpdateTimeout(configuration.nodeUpdateTimeout);

        logger.trace("NodeConfig for {}/{}: {}", configuration.nodeId, configuration.childId, ret.toString());

        return ret;
    }
}
