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
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class AddNodeMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AddNodeMessageClass.class);

    private final int ADD_NODE_ANY = 1;
    private final int ADD_NODE_CONTROLLER = 2;
    private final int ADD_NODE_SLAVE = 3;
    private final int ADD_NODE_EXISTING = 4;
    private final int ADD_NODE_STOP = 5;
    private final int ADD_NODE_STOP_FAILED = 6;

    private final int ADD_NODE_STATUS_LEARN_READY = 1;
    private final int ADD_NODE_STATUS_NODE_FOUND = 2;
    private final int ADD_NODE_STATUS_ADDING_SLAVE = 3;
    private final int ADD_NODE_STATUS_ADDING_CONTROLLER = 4;
    private final int ADD_NODE_STATUS_PROTOCOL_DONE = 5;
    private final int ADD_NODE_STATUS_DONE = 6;
    private final int ADD_NODE_STATUS_FAILED = 7;

    private final int OPTION_HIGH_POWER = 0x80;
    private final int OPTION_NETWORK_WIDE = 0x40;

    public SerialMessage doRequestStart(boolean highPower, boolean networkWide) {
        logger.debug("Setting controller into INCLUSION mode, highPower:{} networkWide:{}.", highPower, networkWide);

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessage.SerialMessageClass.AddNodeToNetwork,
                SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.AddNodeToNetwork,
                SerialMessage.SerialMessagePriority.High);
        byte command = ADD_NODE_ANY;
        if (highPower == true) {
            command |= OPTION_HIGH_POWER;
        }
        if (networkWide == true) {
            command |= OPTION_NETWORK_WIDE;
        }

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(command);
        outputData.write(0x01); // TODO: This should use the callbackId
        newMessage.setMessagePayload(outputData.toByteArray());

        return newMessage;
    }

    public SerialMessage doRequestStop() {
        logger.debug("Ending INCLUSION mode.");

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessage.SerialMessageClass.AddNodeToNetwork,
                SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.AddNodeToNetwork,
                SerialMessage.SerialMessagePriority.High);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(ADD_NODE_STOP);
        outputData.write(0x01); // TODO: This should use the callbackId
        newMessage.setMessagePayload(outputData.toByteArray());

        return newMessage;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) {
        try {
            switch (incomingMessage.getMessagePayloadByte(1)) {
                case ADD_NODE_STATUS_LEARN_READY:
                    logger.debug("Add Node: Learn ready.");
                    zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.IncludeStart));
                    break;
                case ADD_NODE_STATUS_NODE_FOUND:
                    logger.debug("Add Node: New node found.");
                    break;
                case ADD_NODE_STATUS_ADDING_SLAVE:
                    logger.debug("NODE {}: Adding slave.", incomingMessage.getMessagePayloadByte(2));

                    int length = incomingMessage.getMessagePayloadByte(3);

                    Basic basic = Basic.getBasic(incomingMessage.getMessagePayloadByte(4));
                    Generic generic = Generic.getGeneric(incomingMessage.getMessagePayloadByte(5));
                    Specific specific = Specific.getSpecific(generic, incomingMessage.getMessagePayloadByte(6));

                    List<CommandClass> commandClasses = new ArrayList<CommandClass>();

                    for (int i = 7; i < length + 4; i++) {
                        int data = incomingMessage.getMessagePayloadByte(i);

                        CommandClass commandClass = CommandClass.getCommandClass(data);
                        if (commandClass == null) {
                            continue;
                        }

                        // Check if this is the control marker
                        if (commandClass == CommandClass.MARK) {
                            // TODO: Implement control command classes
                            break;
                        }

                        commandClasses.add(commandClass);
                    }

                    ZWaveInclusionEvent event = new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.IncludeSlaveFound,
                            incomingMessage.getMessagePayloadByte(2), basic, generic, specific, commandClasses);
                    zController.notifyEventListeners(event);
                    break;
                case ADD_NODE_STATUS_ADDING_CONTROLLER:
                    logger.debug("NODE {}: Adding controller.", incomingMessage.getMessagePayloadByte(2));
                    zController.notifyEventListeners(new ZWaveInclusionEvent(
                            ZWaveInclusionEvent.Type.IncludeControllerFound, incomingMessage.getMessagePayloadByte(2)));
                    break;
                case ADD_NODE_STATUS_PROTOCOL_DONE:
                    logger.debug("Add Node: Protocol done.");
                    break;
                case ADD_NODE_STATUS_DONE:
                    logger.debug("Add Node: Done.");
                    zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.IncludeDone,
                            incomingMessage.getMessagePayloadByte(2)));
                    break;
                case ADD_NODE_STATUS_FAILED:
                    logger.debug("Add Node: Failed.");
                    zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.IncludeFail));
                    break;
                default:
                    logger.debug("Unknown request ({}).", incomingMessage.getMessagePayloadByte(1));
                    break;
            }
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        checkTransactionComplete(lastSentMessage, incomingMessage);

        return transactionComplete;
    }
}
