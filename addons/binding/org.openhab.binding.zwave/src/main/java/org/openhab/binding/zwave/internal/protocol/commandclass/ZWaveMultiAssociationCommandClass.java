/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveAssociationEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Multi Instance Association command class.
 * This allows reading and writing of node association parameters with instance values
 *
 * @author Chris Jackson
 */
@XStreamAlias("multiAssociationCommandClass")
public class ZWaveMultiAssociationCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiAssociationCommandClass.class);

    private static final int MULTI_INSTANCE_MARKER = 0x00;

    private static final int MULTI_ASSOCIATIONCMD_SET = 1;
    private static final int MULTI_ASSOCIATIONCMD_GET = 2;
    private static final int MULTI_ASSOCIATIONCMD_REPORT = 3;
    private static final int MULTI_ASSOCIATIONCMD_REMOVE = 4;
    private static final int MULTI_ASSOCIATIONCMD_GROUPINGSGET = 5;
    private static final int MULTI_ASSOCIATIONCMD_GROUPINGSREPORT = 6;

    // Stores the list of association groups
    // private Map<Integer, ZWaveAssociationGroup> configAssociations = new HashMap<Integer, ZWaveAssociationGroup>();

    @XStreamOmitField
    private int updateAssociationsNode = 0;

    @XStreamOmitField
    private ZWaveAssociationGroup pendingAssociation = null;

    @XStreamOmitField
    private boolean initialiseDone = false;

    // This will be set when we query a node for the number of groups it supports
    private int maxGroups = 0;

    /**
     * Creates a new instance of the ZWaveMultiAssociationCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveMultiAssociationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.MULTI_INSTANCE_ASSOCIATION;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received MULTI_INSTANCE_ASSOCIATION command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case MULTI_ASSOCIATIONCMD_SET:
                processAssociationReport(serialMessage, offset);
                break;
            case MULTI_ASSOCIATIONCMD_REPORT:
                processAssociationReport(serialMessage, offset);
                break;
            case MULTI_ASSOCIATIONCMD_GROUPINGSREPORT:
                processGroupingsReport(serialMessage, offset);
                return;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command 0x%02X for command class %s (0x%02X).",
                        getNode().getNodeId(), command, getCommandClass().getLabel(), getCommandClass().getKey()));
        }
    }

    /**
     * Processes a CONFIGURATIONCMD_REPORT / CONFIGURATIONCMD_SET message.
     *
     * @param serialMessage
     *            the incoming message to process.
     * @param offset
     *            the offset position from which to start message processing.
     * @throws ZWaveSerialMessageException
     */
    protected void processAssociationReport(SerialMessage serialMessage, int offset)
            throws ZWaveSerialMessageException {
        // Extract the group index
        int group = serialMessage.getMessagePayloadByte(offset + 1);
        // The max associations supported (0 if the requested group is not supported)
        int maxAssociations = serialMessage.getMessagePayloadByte(offset + 2);
        // Number of outstanding requests (if the group is large, it may come in multiple frames)
        int following = serialMessage.getMessagePayloadByte(offset + 3);

        if (maxAssociations == 0) {
            // Unsupported association group. Nothing to do!
            if (updateAssociationsNode == group) {
                logger.debug("NODE {}: All association groups acquired.", getNode().getNodeId());

                updateAssociationsNode = 0;

                // This is used for network management, so send a network event
                getController().notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssociationUpdate,
                        getNode().getNodeId(), ZWaveNetworkEvent.State.Success));
            }
            return;
        }

        logger.debug("NODE {}: association group {} has max associations {}", getNode().getNodeId(), group,
                maxAssociations);

        // Are we waiting to synchronise the start of a new group?
        if (pendingAssociation == null) {
            pendingAssociation = new ZWaveAssociationGroup(group);
        }

        if (serialMessage.getMessagePayload().length > (offset + 4)) {
            logger.debug("NODE {}: Association group {} includes the following nodes:", getNode().getNodeId(), group);
            int dataLength = serialMessage.getMessagePayload().length - (offset + 4);
            int dataPointer = 0;

            // Process the root associations
            for (; dataPointer < dataLength; dataPointer++) {
                int node = serialMessage.getMessagePayloadByte(offset + 4 + dataPointer);
                if (node == MULTI_INSTANCE_MARKER) {
                    break;
                }
                logger.debug("NODE {}: Associated with Node {} in group {}", getNode().getNodeId(), node, group);

                // Add the node to the group
                pendingAssociation.addAssociation(node);
            }

            // Process the multi instance associations
            if (dataPointer < dataLength) {
                logger.trace("NODE {}: Includes multi_instance associations", getNode().getNodeId());

                // Step over the marker
                dataPointer++;
                for (; dataPointer < dataLength; dataPointer += 2) {
                    int node = serialMessage.getMessagePayloadByte(offset + 4 + dataPointer);
                    int endpoint = serialMessage.getMessagePayloadByte(offset + 5 + dataPointer);
                    if (node == MULTI_INSTANCE_MARKER) {
                        break;
                    }
                    logger.debug("NODE {}: Associated with Node {} endpoint {} in group", getNode().getNodeId(), node,
                            endpoint, group);

                    // Add the node to the group
                    pendingAssociation.addAssociation(node, endpoint);
                }
            }
        }

        // If this is the end of the group, update the list then let the listeners know
        if (following == 0) {
            // Clear the current information for this group
            ZWaveAssociationGroup associationGroup = getNode().getAssociationGroup(group);
            if (associationGroup != null) {
                // Update the group in the list
                getNode().getAssociationGroup(pendingAssociation.getIndex())
                        .setAssociations(pendingAssociation.getAssociations());
            }

            // Send an event to the users
            ZWaveAssociationEvent zEvent = new ZWaveAssociationEvent(getNode().getNodeId(), pendingAssociation);
            pendingAssociation = null;
            getController().notifyEventListeners(zEvent);
        }

        // Is this the end of the list
        if (following == 0 && group == updateAssociationsNode) {
            // This is the end of this group and the current 'get all groups' node
            // so we need to request the next group
            if (updateAssociationsNode < maxGroups) {
                updateAssociationsNode++;
                SerialMessage outputMessage = getAssociationMessage(updateAssociationsNode);
                if (outputMessage != null) {
                    getController().sendData(outputMessage);
                }
            } else {
                logger.debug("NODE {}: All association groups acquired.", getNode().getNodeId());
                // We have reached our maxNodes, notify listeners we are done.

                updateAssociationsNode = 0;
                // This is used for network management, so send a network event
                getController().notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssociationUpdate,
                        getNode().getNodeId(), ZWaveNetworkEvent.State.Success));
            }
        }
    }

    /**
     * Processes a ASSOCIATIONCMD_GROUPINGSREPORT message.
     *
     * @param serialMessage
     *            the incoming message to process.
     * @param offset
     *            the offset position from which to start message processing.
     * @throws ZWaveSerialMessageException
     */
    protected void processGroupingsReport(SerialMessage serialMessage, int offset) throws ZWaveSerialMessageException {
        maxGroups = serialMessage.getMessagePayloadByte(offset + 1);
        logger.debug("NODE {}: processGroupingsReport number of groups {}", getNode(), maxGroups);

        // Add an association for each group if it doesn't exist
        for (int groupId = 1; groupId <= maxGroups; groupId++) {
            if (getNode().getAssociationGroup(groupId) == null) {
                ZWaveAssociationGroup group = new ZWaveAssociationGroup(groupId);
                getNode().setAssociationGroup(group);
            }
        }

        // Start the process to query these nodes
        // updateAssociationsNode = 1;
        // SerialMessage sm = getAssociationMessage(updateAssociationsNode);
        // if (sm != null) {
        // getController().sendData(sm);
        // }

        initialiseDone = true;
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_SET command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public SerialMessage setAssociationMessage(int group, int node, int endpoint) {
        logger.debug(
                "NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_SET node {}, endpoint {}, group {}",
                getNode().getNodeId(), node, endpoint, group);
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(this.getNode().getNodeId());
        if (endpoint == 0) {
            outputData.write(4);
        } else {
            outputData.write(6);
        }
        outputData.write(getCommandClass().getKey());
        outputData.write(MULTI_ASSOCIATIONCMD_SET);
        outputData.write(group);
        if (endpoint == 0) {
            outputData.write(node);
        } else {
            outputData.write(0);
            outputData.write(node);
            outputData.write(endpoint);
        }
        result.setMessagePayload(outputData.toByteArray());

        return result;
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_REMOVE command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public SerialMessage removeAssociationMessage(int group, int node, int endpoint) {
        logger.debug(
                "NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_REMOVE node {}, endpoint {}, group {}",
                getNode().getNodeId(), node, endpoint, group);
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(this.getNode().getNodeId());
        if (endpoint == 0) {
            outputData.write(4);
        } else {
            outputData.write(6);
        }
        outputData.write(getCommandClass().getKey());
        outputData.write(MULTI_ASSOCIATIONCMD_REMOVE);
        outputData.write(group);
        if (endpoint == 0) {
            outputData.write(node);
        } else {
            outputData.write(0);
            outputData.write(node);
            outputData.write(endpoint);
        }
        result.setMessagePayload(outputData.toByteArray());

        return result;
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_GET command
     *
     * @param group
     *            the association group to read
     * @return the serial message
     */
    public SerialMessage getAssociationMessage(int group) {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_GET group {}",
                getNode().getNodeId(), group);
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(this.getNode().getNodeId());
        outputData.write(3);
        outputData.write(getCommandClass().getKey());
        outputData.write(MULTI_ASSOCIATIONCMD_GET);
        outputData.write(group);

        result.setMessagePayload(outputData.toByteArray());

        return result;
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_GROUPINGSGET command
     *
     * @return the serial message
     */
    public SerialMessage getGroupingsMessage() {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_GROUPINGSGET",
                getNode().getNodeId());
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(this.getNode().getNodeId());
        outputData.write(2);
        outputData.write(getCommandClass().getKey());
        outputData.write(MULTI_ASSOCIATIONCMD_GROUPINGSGET);

        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    /**
     * Request all association groups.
     * This method requests the number of groups from a node, when that
     * replay is processed we request association group 1 and set flags so that
     * when the response is received the command class automatically
     * requests the next group. This continues until we reach the maximum
     * number of group the device reports to us or until the device returns
     * a group with no members.
     */
    public void getAllAssociations() {
        SerialMessage serialMessage = getGroupingsMessage();
        if (serialMessage != null) {
            getController().sendData(serialMessage);
        }
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(getGroupingsMessage());
        }

        return result;
    }
}
