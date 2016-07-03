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
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class DeleteReturnRouteMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DeleteReturnRouteMessageClass.class);

    public SerialMessage doRequest(int nodeId) {
        logger.debug("NODE {}: Deleting return routes", nodeId);

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.DeleteReturnRoute, SerialMessageType.Request,
                SerialMessageClass.DeleteReturnRoute, SerialMessagePriority.High);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(nodeId);
        outputData.write(0x01); // callback id
        newMessage.setMessagePayload(outputData.toByteArray());

        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        logger.debug("NODE {}: Got DeleteReturnRoute response.", nodeId);
        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            lastSentMessage.setAckRecieved();
            logger.debug("NODE {}: DeleteReturnRoute command in progress.", nodeId);
        } else {
            logger.error("NODE {}: DeleteReturnRoute command failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
        }

        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        logger.debug("NODE {}: Got DeleteReturnRoute request.", nodeId);
        if (incomingMessage.getMessagePayloadByte(1) != 0x00) {
            logger.error("NODE {}: Delete return routes failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
        } else {
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Success));
        }

        checkTransactionComplete(lastSentMessage, incomingMessage);

        return true;
    }
}
