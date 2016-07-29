/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller.
 * <p>
 * This class is the base class for the serial message class. It handles the request from the application, and the
 * processing of the responses from the controller.
 * <p>
 * When a message is sent to the controller, the controller responds with a RESPONSE.
 * <p>
 * When the controller has further data, it responds with a REQUEST.
 * <p>
 * These calls map to the handleResponse and handleRequest methods which must be overridden by the individual classes.
 *
 * @author Chris Jackson
 */
public abstract class ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveCommandProcessor.class);

    private static HashMap<SerialMessage.SerialMessageClass, Class<? extends ZWaveCommandProcessor>> messageMap = null;
    protected boolean transactionComplete = false;
    /**
     * Map of Long (received time) and {@link SerialMessage}. Use TreeMap so it's sorted from oldest to newest
     */
    private final Map<Long, SerialMessage> incomingMessageTable = new TreeMap<Long, SerialMessage>();

    public ZWaveCommandProcessor() {
    }

    /**
     * Checks if the processor marked the transaction as complete
     *
     * @return true is the transaction was completed.
     */
    public boolean isTransactionComplete() {
        return transactionComplete;
    }

    /**
     * Perform a check to see if this is the expected reply and we can complete the transaction
     *
     * @param lastSentMessage The original message we sent to the controller
     * @param incomingMessage The response from the controller
     */
    protected void checkTransactionComplete(SerialMessage lastSentMessage, SerialMessage latestIncomingMessage) {
        // Put the message in our table so it will be processed now or later
        incomingMessageTable.put(System.currentTimeMillis(), latestIncomingMessage);

        // First, check if we're waiting for an ACK from the controller
        // This is used for multi-stage transactions to ensure we get all parts of the
        // transaction before completing.
        if (lastSentMessage == null || lastSentMessage.isAckPending()) {
            logger.debug("Checking transaction complete: Message has Ack Pending: {}", lastSentMessage);
            // Return until we get the ack, then come back and compare. This is necessary since, per ZWaveSendThread,
            // we sometimes get the response before the ack. See ZWaveSendThreadcomment starting with "A transaction
            // consists of (up to) 4 parts"
            return;
        }

        logger.debug("Checking transaction complete: Sent {}", lastSentMessage.toString());
        final Iterator<Map.Entry<Long, SerialMessage>> iter = incomingMessageTable.entrySet().iterator();
        final long expired = System.currentTimeMillis() - 10000; // Discard responses from 10 seconds ago or longer
        while (iter.hasNext()) {
            final Map.Entry<Long, SerialMessage> entry = iter.next();
            // Check if it's expired
            if (entry.getKey() < expired) {
                iter.remove();
                continue;
            }
            final SerialMessage incomingMessage = entry.getValue();
            logger.debug("Checking transaction complete: Recv {}", incomingMessage.toString());
            final boolean ignoreTransmissionCompleteMismatch = false; // TODO: change
            if (incomingMessage.getMessageClass() == lastSentMessage.getExpectedReply()
                    && !incomingMessage.isTransactionCanceled()) {
                logger.debug(
                        "Checking transaction complete: class={}, callback id={}, expected={}, cancelled={}        transaction complete!",
                        incomingMessage.getMessageClass(), lastSentMessage.getCallbackId(),
                        lastSentMessage.getExpectedReply(), incomingMessage.isTransactionCanceled());
                transactionComplete = true;
                return;
            } else if (ignoreTransmissionCompleteMismatch) {
                logger.debug(
                        "Checking transaction complete: class={}, callback id={}, expected={}, cancelled={}      MISMATCH IGNORED",
                        incomingMessage.getMessageClass(), lastSentMessage.getCallbackId(),
                        lastSentMessage.getExpectedReply(), incomingMessage.isTransactionCanceled());
                transactionComplete = true; // TODO: this was to test if this was preventing successful security
                                            // pairing, fix properly and remove
                return;
            } else {
                logger.debug(
                        "Checking transaction complete: class={}, callback id={}, expected={}, cancelled={}      MISMATCH",
                        incomingMessage.getMessageClass(), lastSentMessage.getCallbackId(),
                        lastSentMessage.getExpectedReply(), incomingMessage.isTransactionCanceled());
            }
        }
    }

    /**
     * Method for handling the response from the controller
     *
     * @param zController the ZWave controller
     * @param lastSentMessage The original message we sent to the controller
     * @param incomingMessage The response from the controller
     * @return
     * @throws ZWaveSerialMessageException
     */
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.warn("TODO: {} unsupported RESPONSE.", incomingMessage.getMessageClass().getLabel());
        return false;
    }

    /**
     * Method for handling the request from the controller
     *
     * @param zController the ZWave controller
     * @param lastSentMessage The original message we sent to the controller
     * @param incomingMessage The response from the controller
     * @return
     * @throws ZWaveSerialMessageException
     */
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.warn("TODO: {} unsupported REQUEST.", incomingMessage.getMessageClass().getLabel());
        return false;
    }

    /**
     * Returns the message processor for the specified message class
     *
     * @param serialMessage The message class required to be processed
     * @return The message processor
     */
    public static ZWaveCommandProcessor getMessageDispatcher(SerialMessage.SerialMessageClass serialMessage) {
        if (messageMap == null) {
            messageMap = new HashMap<SerialMessage.SerialMessageClass, Class<? extends ZWaveCommandProcessor>>();
            messageMap.put(SerialMessage.SerialMessageClass.AddNodeToNetwork, AddNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.ApplicationCommandHandler,
                    ApplicationCommandMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.ApplicationUpdate, ApplicationUpdateMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.AssignReturnRoute, AssignReturnRouteMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.AssignSucReturnRoute,
                    AssignSucReturnRouteMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.DeleteReturnRoute, DeleteReturnRouteMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.DeleteSUCReturnRoute,
                    DeleteSucReturnRouteMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.EnableSuc, EnableSucMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.GetRoutingInfo, GetRoutingInfoMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.GetVersion, GetVersionMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.GetSucNodeId, GetSucNodeIdMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.GetControllerCapabilities,
                    GetControllerCapabilitiesMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.IdentifyNode, IdentifyNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.MemoryGetId, MemoryGetIdMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RemoveFailedNodeID, RemoveFailedNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.IsFailedNodeID, IsFailedNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RemoveNodeFromNetwork, RemoveNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.ReplaceFailedNode, ReplaceFailedNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RequestNetworkUpdate,
                    RequestNetworkUpdateMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RequestNodeInfo, RequestNodeInfoMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RequestNodeNeighborUpdate,
                    RequestNodeNeighborUpdateMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SendData, SendDataMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SerialApiGetCapabilities,
                    SerialApiGetCapabilitiesMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SerialApiGetInitData,
                    SerialApiGetInitDataMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SerialApiSetTimeouts,
                    SerialApiSetTimeoutsMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SerialApiSoftReset, SerialApiSoftResetMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SetSucNodeID, SetSucNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SetDefault, ControllerSetDefaultMessageClass.class);
        }

        Constructor<? extends ZWaveCommandProcessor> constructor;
        try {
            if (messageMap.get(serialMessage) == null) {
                logger.warn("SerialMessage class {} is not implemented!", serialMessage.getLabel());
                return null;
            }
            constructor = messageMap.get(serialMessage).getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            logger.error("Command processor error");
        }

        return null;
    }
}
