/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.message;

import java.text.ParseException;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;

/**
 * Used to store the content of a MySensors message.
 *
 * @author Tim Oberföll
 *
 */
public class MySensorsMessage {

    // I version message for startup check
    public static final MySensorsMessage I_VERSION_MESSAGE = new MySensorsMessage(
            MySensorsNode.MYSENSORS_NODE_ID_RESERVED_GATEWAY_0, MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_0,
            MySensorsMessageType.INTERNAL, MySensorsMessageAck.FALSE, false, MySensorsMessageSubType.I_VERSION, "");

    private int nodeId;
    private int childId;
    private MySensorsMessageType msgType; // type of message: request, internal, presentation ...
    private MySensorsMessageAck ack;
    private boolean revert;
    private MySensorsMessageSubType subType;
    private String msg;
    private int retries;
    private long nextSend;
    private boolean smartSleep;
    private MySensorsMessageDirection direction = MySensorsMessageDirection.OUTGOING;

    public MySensorsMessage() {

    }

    public MySensorsMessage(int nodeId, int childId, MySensorsMessageType msgType, MySensorsMessageAck ack,
            boolean revert) {
        setNodeId(nodeId);
        setChildId(childId);
        setMsgType(msgType);
        setAck(ack);
        setRevert(revert);
    }

    public MySensorsMessage(int nodeId, int childId, MySensorsMessageType msgType, MySensorsMessageAck ack,
            boolean revert, boolean smartSleep) {
        setNodeId(nodeId);
        setChildId(childId);
        setMsgType(msgType);
        setAck(ack);
        setRevert(revert);
        setSmartSleep(smartSleep);
    }

    public MySensorsMessage(int nodeId, int childId, MySensorsMessageType msgType, MySensorsMessageAck ack,
            boolean revert, MySensorsMessageSubType subType, String msg) {
        setNodeId(nodeId);
        setChildId(childId);
        setMsgType(msgType);
        setAck(ack);
        setRevert(revert);
        setSubType(subType);
        setMsg(msg);
    }

    public MySensorsMessage(int nodeId, int childId, MySensorsMessageType msgType, MySensorsMessageAck ack,
            boolean revert, MySensorsMessageSubType subType, String msg, boolean smartSleep) {
        setNodeId(nodeId);
        setChildId(childId);
        setMsgType(msgType);
        setAck(ack);
        setRevert(revert);
        setSubType(subType);
        setMsg(msg);
        setSmartSleep(smartSleep);
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public MySensorsMessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MySensorsMessageType msgType) {
        this.msgType = msgType;
    }

    public boolean getRevert() {
        return revert;
    }

    public void setRevert(boolean revert) {
        this.revert = revert;
    }

    public MySensorsMessageAck getAck() {
        return ack;
    }

    public void setAck(MySensorsMessageAck ack) {
        this.ack = ack;
    }

    public void setAck(boolean ack) {
        setAck(ack ? MySensorsMessageAck.TRUE : MySensorsMessageAck.FALSE);
    }

    public boolean isAck() {
        if (ack == MySensorsMessageAck.TRUE) {
            return true;
        } else {
            return false;
        }
    }

    public MySensorsMessageSubType getSubType() {
        return subType;
    }

    public void setSubType(MySensorsMessageSubType subType) {
        this.subType = subType;
    }

    public String getMsg() {
        return msg;
    }

    public void setSmartSleep(boolean smartSleep) {
        this.smartSleep = smartSleep;
    }

    public boolean isSmartSleep() {
        return smartSleep;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public long getNextSend() {
        return nextSend;
    }

    public void setNextSend(long nextSend) {
        this.nextSend = nextSend;
    }

    public MySensorsMessageDirection getDirection() {
        return direction;
    }

    public void setDirection(MySensorsMessageDirection direction) {
        this.direction = direction;
    }

    /**
     * Checks if the received message is a I_CONFIG (internal MySensors) message.
     *
     * @return true, if the received message is a I_CONFIG message.
     */
    public boolean isIConfigMessage() {
        return (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_0
                || childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255)
                && (msgType == MySensorsMessageType.INTERNAL) && (ack == MySensorsMessageAck.FALSE)
                && (subType == MySensorsMessageSubType.I_CONFIG);
    }

    /**
     * Checks if the received message is a I_VERSION (internal MySensors) message.
     *
     * @return true, if the received message is a I_VERSION message.
     */
    public boolean isIVersionMessage() {
        return (nodeId == MySensorsNode.MYSENSORS_NODE_ID_RESERVED_GATEWAY_0)
                && (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_0
                        || childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255)
                && (msgType == MySensorsMessageType.INTERNAL) && (ack == MySensorsMessageAck.FALSE)
                && (subType == MySensorsMessageSubType.I_VERSION);
    }

    /**
     * Checks if the received message is a I_HEARTBEAT_RESPONSE (internal MySensors) message.
     *
     * @return true, if the received message is a I_HEARTBEAT_RESPONSE message.
     */
    public boolean isIHearbeatResponse() {
        return (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255) && (msgType == MySensorsMessageType.INTERNAL)
                && (subType == MySensorsMessageSubType.I_HEARTBEAT_RESPONSE);
    }

    /**
     * Checks if the received message is a I_TIME (internal MySensors) message.
     *
     * @return true, if the received message is a I_TIME message.
     */
    public boolean isITimeMessage() {
        return (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_0
                || childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255)
                && (msgType == MySensorsMessageType.INTERNAL) && (ack == MySensorsMessageAck.FALSE)
                && (subType == MySensorsMessageSubType.I_TIME);
    }

    /**
     * Checks if the received message is a I_ID_REQUEST (internal MySensors) message.
     *
     * @return true, if the received message is a I_ID_REQUEST message.
     */
    public boolean isIdRequestMessage() {
        return (nodeId == MySensorsNode.MYSENSORS_NODE_ID_RESERVED_255)
                && (msgType == MySensorsMessageType.INTERNAL) && (ack == MySensorsMessageAck.FALSE)
                && (subType == MySensorsMessageSubType.I_ID_REQUEST);
    }

    /**
     * Checks if the received message is a presentation message.
     *
     * @return true, if the received message is a presentation message.
     */
    public boolean isPresentationMessage() {
        return msgType == MySensorsMessageType.PRESENTATION;
    }

    public boolean isSetReqMessage() {
        return msgType == MySensorsMessageType.REQ || msgType == MySensorsMessageType.SET;
    }

    public boolean isReqMessage() {
        return msgType == MySensorsMessageType.REQ;
    }

    public boolean isSetMessage() {
        return msgType == MySensorsMessageType.SET;
    }

    public boolean isInternalMessage() {
        return msgType == MySensorsMessageType.INTERNAL;
    }

    /**
     * Checks if the received message is a heartbeat(response) received from a node
     *
     * @return true, if it is a heartbeat
     */
    public boolean isHeartbeatResponseMessage() {
        return (subType == MySensorsMessageSubType.I_HEARTBEAT_RESPONSE
                && childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255);
    }

    /**
     * Is this a debug message?
     *
     * @return true if this message is I_DEBUG
     */
    public boolean isDebugMessage() {
        return (nodeId == MySensorsNode.MYSENSORS_NODE_ID_RESERVED_GATEWAY_0)
                && (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255)
                && (msgType == MySensorsMessageType.INTERNAL) && (subType == MySensorsMessageSubType.I_DEBUG);
    }

    /**
     * @param line Input is a String containing the message received from the MySensors network
     * @return Returns the content of the message as a MySensorsMessage
     *
     * @throws ParseException
     */
    public static MySensorsMessage parse(String line) throws ParseException {
        try {
            String[] splitMessage = line.split(";");
            if (splitMessage.length > 4) {

                MySensorsMessage mysensorsmessage = new MySensorsMessage();

                int nodeId = Integer.parseInt(splitMessage[MySensorsMessagePart.NODE.getId()]);

                mysensorsmessage.setNodeId(nodeId);
                mysensorsmessage.setChildId(Integer.parseInt(splitMessage[MySensorsMessagePart.CHILD.getId()]));

                int msgTypeId = Integer.parseInt(splitMessage[MySensorsMessagePart.TYPE.getId()]);
                mysensorsmessage.setMsgType(MySensorsMessageType.getById(msgTypeId));

                int ackId = Integer.parseInt(splitMessage[MySensorsMessagePart.ACK.getId()]);
                mysensorsmessage.setAck(MySensorsMessageAck.getById(ackId));

                int subTypeId = Integer.parseInt(splitMessage[MySensorsMessagePart.SUBTYPE.getId()]);
                if (mysensorsmessage.getMsgType() == MySensorsMessageType.INTERNAL) {
                    mysensorsmessage.setSubType(MySensorsMessageSubType.getInternalById(subTypeId));
                } else if (mysensorsmessage.getMsgType() == MySensorsMessageType.PRESENTATION) {
                    mysensorsmessage.setSubType(MySensorsMessageSubType.getPresentationById(subTypeId));
                } else {
                    mysensorsmessage.setSubType(MySensorsMessageSubType.getSetReqById(subTypeId));
                }

                if (splitMessage.length == 6) {
                    String msg = splitMessage[5].replaceAll("\\r|\\n", "").trim();
                    mysensorsmessage.setMsg(msg);
                } else {
                    mysensorsmessage.setMsg("");
                }
                return mysensorsmessage;
            } else {
                throw new ParseException("Message length is not > 4", 0);
            }

        } catch (Exception e) {
            throw new ParseException(e.getClass() + " : " + e.getMessage(), 0);
        }
    }

    /**
     * Converts a MySensorsMessage object to a String.
     *
     * @param msg the MySensorsMessage that should be converted.
     * @return the MySensorsMessage as a String.
     */
    public static String generateAPIString(MySensorsMessage msg) {
        String apiString = "";
        apiString += msg.getNodeId() + ";";
        apiString += msg.getChildId() + ";";
        apiString += msg.getMsgType().getId() + ";";
        apiString += msg.getAck().getId() + ";";
        apiString += msg.getSubType().getId() + ";";
        apiString += msg.getMsg() + "\n";

        return apiString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ack.getId();
        result = prime * result + childId;
        result = prime * result + ((msg == null) ? 0 : msg.hashCode());
        result = prime * result + msgType.getId();
        result = prime * result + (int) (nextSend ^ (nextSend >>> 32));
        result = prime * result + nodeId;
        result = prime * result + retries;
        result = prime * result + (revert ? 1231 : 1237);
        result = prime * result + subType.getId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MySensorsMessage other = (MySensorsMessage) obj;
        if (ack != other.ack) {
            return false;
        }
        if (childId != other.childId) {
            return false;
        }
        if (msg == null) {
            if (other.msg != null) {
                return false;
            }
        } else if (!msg.equals(other.msg)) {
            return false;
        }
        if (msgType != other.msgType) {
            return false;
        }
        if (nextSend != other.nextSend) {
            return false;
        }
        if (nodeId != other.nodeId) {
            return false;
        }
        if (retries != other.retries) {
            return false;
        }
        if (revert != other.revert) {
            return false;
        }
        if (subType != other.subType) {
            return false;
        }
        return true;
    }

    /**
     * Generate a custom hash by message parts passed as vararg
     * Usage example: customHashCode(MYSENSORS_MSG_PAYLOAD_PART, MYSENSORS_MSG_SUBTYPE_PART);
     *
     * @param messagePart one or mode valid message part, use MYSENSORS_MSG_*_PART definition
     *
     * @return the hash code
     */
    public int customHashCode(MySensorsMessagePart... messageParts) {

        final int prime = 101;

        int result = 1;
        for (int i = 0; i < messageParts.length; i++) {
            if (messageParts[i] == MySensorsMessagePart.PAYLOAD) {
                result = prime * result + ((msg == null) ? 0 : msg.hashCode());
            } else if (messageParts[i] == MySensorsMessagePart.SUBTYPE) {
                result = prime * result + subType.getId();
            } else if (messageParts[i] == MySensorsMessagePart.ACK) {
                result = prime * result + ack.getId();
            } else if (messageParts[i] == MySensorsMessagePart.TYPE) {
                result = prime * result + msgType.getId();
            } else if (messageParts[i] == MySensorsMessagePart.CHILD) {
                result = prime * result + childId;
            } else if (messageParts[i] == MySensorsMessagePart.NODE) {
                result = prime * result + nodeId;
            } else {
                throw new IllegalArgumentException("Messsage part must be in [0,5] interval");
            }

        }

        return result;

    }

    @Override
    public String toString() {
        return "MySensorsMessage [" + generateAPIString(this) + "]";
    }

}
