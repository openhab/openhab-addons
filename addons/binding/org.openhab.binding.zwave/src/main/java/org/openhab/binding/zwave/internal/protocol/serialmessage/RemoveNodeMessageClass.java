/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import java.io.ByteArrayOutputStream;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class RemoveNodeMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RemoveNodeMessageClass.class);

    private final int REMOVE_NODE_ANY = 1;
    private final int REMOVE_NODE_CONTROLLER = 2;
    private final int REMOVE_NODE_SLAVE = 3;
    private final int REMOVE_NODE_STOP = 5;

    private final int REMOVE_NODE_STATUS_LEARN_READY = 1;
    private final int REMOVE_NODE_STATUS_NODE_FOUND = 2;
    private final int REMOVE_NODE_STATUS_REMOVING_SLAVE = 3;
    private final int REMOVE_NODE_STATUS_REMOVING_CONTROLLER = 4;
    private final int REMOVE_NODE_STATUS_DONE = 6;
    private final int REMOVE_NODE_STATUS_FAILED = 7;

    public SerialMessage doRequestStart() {
        logger.debug("Setting controller into EXCLUSION mode.");

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessage.SerialMessageClass.RemoveNodeFromNetwork,
                SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.RemoveNodeFromNetwork,
                SerialMessage.SerialMessagePriority.High);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(REMOVE_NODE_ANY);
        outputData.write(0x01); // TODO: This should use the callbackId
        newMessage.setMessagePayload(outputData.toByteArray());

        return newMessage;
    }

    public SerialMessage doRequestStop() {
        logger.debug("Ending EXCLUSION mode.");

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessage.SerialMessageClass.RemoveNodeFromNetwork,
                SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.RemoveNodeFromNetwork,
                SerialMessage.SerialMessagePriority.High);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(REMOVE_NODE_STOP);
        outputData.write(254); // TODO: This should use the callbackId
        newMessage.setMessagePayload(outputData.toByteArray());

        return newMessage;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        switch (incomingMessage.getMessagePayloadByte(1)) {
            case REMOVE_NODE_STATUS_LEARN_READY:
                logger.debug("Remove Node: Learn ready.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.ExcludeStart));
                break;
            case REMOVE_NODE_STATUS_NODE_FOUND:
                logger.debug("Remove Node: Node found for removal.");
                break;
            case REMOVE_NODE_STATUS_REMOVING_SLAVE:
                logger.debug("NODE {}: Removing slave.", incomingMessage.getMessagePayloadByte(2));
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.ExcludeSlaveFound,
                        incomingMessage.getMessagePayloadByte(2)));
                break;
            case REMOVE_NODE_STATUS_REMOVING_CONTROLLER:
                logger.debug("NODE {}: Removing controller.", incomingMessage.getMessagePayloadByte(2));
                zController.notifyEventListeners(new ZWaveInclusionEvent(
                        ZWaveInclusionEvent.Type.ExcludeControllerFound, incomingMessage.getMessagePayloadByte(2)));
                break;
            case REMOVE_NODE_STATUS_DONE:
                if (incomingMessage.getMessagePayloadByte(2) != 0) {
                    logger.debug("NODE {}: Removed from network.", incomingMessage.getMessagePayloadByte(2));
                    zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.ExcludeDone,
                            incomingMessage.getMessagePayloadByte(2)));
                }
                logger.debug("Remove Node: Done.");
                break;
            case REMOVE_NODE_STATUS_FAILED:
                logger.debug("Remove Node: Failed.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.ExcludeFail));
                break;
            default:
                logger.debug("Remove Node: Unknown request ({}).", incomingMessage.getMessagePayloadByte(1));
                break;
        }
        checkTransactionComplete(lastSentMessage, incomingMessage);

        return transactionComplete;
    }
}
