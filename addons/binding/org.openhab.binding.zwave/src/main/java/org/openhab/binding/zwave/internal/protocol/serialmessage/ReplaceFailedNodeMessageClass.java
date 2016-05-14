/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class ReplaceFailedNodeMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ReplaceFailedNodeMessageClass.class);

    private final int FAILED_NODE_REMOVE_STARTED = 0x00;
    private final int FAILED_NODE_NOT_PRIMARY_CONTROLLER = 0x02;
    private final int FAILED_NODE_NO_CALLBACK_FUNCTION = 0x04;
    private final int FAILED_NODE_NOT_FOUND = 0x08;
    private final int FAILED_NODE_REMOVE_PROCESS_BUSY = 0x10;
    private final int FAILED_NODE_REMOVE_FAIL = 0x20;

    private final int FAILED_NODE_OK = 0x00;
    private final int FAILED_NODE_REMOVED = 0x01;
    private final int FAILED_NODE_NOT_REMOVED = 0x02;

    public SerialMessage doRequest(int nodeId) {
        logger.debug("NODE {}: Marking node as having failed.", nodeId);

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.ReplaceFailedNode, SerialMessageType.Request,
                SerialMessageClass.ReplaceFailedNode, SerialMessagePriority.High);
        byte[] newPayload = { (byte) nodeId, (byte) 0x01 };
        newMessage.setMessagePayload(newPayload);
        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got ReplaceFailedNode response.");
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        switch (incomingMessage.getMessagePayloadByte(0)) {
            case FAILED_NODE_REMOVE_STARTED:
                // The replacing process started and now the new node must emit its node information frame to start the
                // assign process.
                logger.debug("NODE {}: Replace failed node successfully placed on stack.", nodeId);
                break;
            case FAILED_NODE_NOT_PRIMARY_CONTROLLER:
                // The replacing process was aborted because the controller is not a primary/inclusion/SIS controller.
                logger.error("NODE {}: Replace failed node failed as not Primary Controller for node!", nodeId);
                transactionComplete = true;
                break;
            case FAILED_NODE_NO_CALLBACK_FUNCTION:
                // The replacing process was aborted because no call back function is used.
                logger.error("NODE {}: Replace failed node failed as no callback function!", nodeId);
                transactionComplete = true;
                break;
            case FAILED_NODE_NOT_FOUND:
                // The replacing process aborted because the node was found, thereby not a failing node.
                logger.error("NODE {}: Replace failed node failed as node if functioning!", nodeId);
                transactionComplete = true;
                break;
            case FAILED_NODE_REMOVE_PROCESS_BUSY:
                // The replacing process is busy.
                logger.error("NODE {}: Replace failed node failed as Controller Busy!", nodeId);
                transactionComplete = true;
                break;
            case FAILED_NODE_REMOVE_FAIL:
                // The replacing process could not be started because of transmitter busy.
                logger.error("NODE {}: Replace failed node failed!", nodeId);
                transactionComplete = true;
                break;
            default:
                logger.error("NODE {}: Replace failed node not placed on stack due to error 0x{}.", nodeId,
                        Integer.toHexString(incomingMessage.getMessagePayloadByte(0)));
                transactionComplete = true;
                break;
        }

        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        logger.debug("NODE {}: Got ReplaceFailedNode request.", nodeId);
        switch (incomingMessage.getMessagePayloadByte(1)) {// TODO: Should this be (&& 0x0f)?
            case FAILED_NODE_OK:
                // The node is working properly (removed from the failed nodes list). Replace process is stopped.
                logger.error("NODE {}: Unable to remove failed node as it is not a failed node!", nodeId);
                transactionComplete = true;
                break;
            case FAILED_NODE_REMOVED:
                logger.debug("NODE {}: Successfully removed node from controller database!", nodeId);
                transactionComplete = true;
                break;
            case FAILED_NODE_NOT_REMOVED:
                logger.error("NODE {}: Unable to remove failed node!", nodeId);
                transactionComplete = true;
                break;
            default:
                logger.error("NODE {}: Replace failed node returned with response 0x{}.", nodeId,
                        Integer.toHexString(incomingMessage.getMessagePayloadByte(1)));
                transactionComplete = true;
                break;
        }

        return true;
    }
}
