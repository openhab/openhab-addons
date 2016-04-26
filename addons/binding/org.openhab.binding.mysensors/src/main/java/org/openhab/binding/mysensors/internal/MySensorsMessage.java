/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim Oberföll
 *
 *         Used to store the content of a MySensors message
 */
public class MySensorsMessage {
    public static String GATEWAY_STARTUP_NOTIFICATION = "Gateway startup complete.";

    private Logger logger = LoggerFactory.getLogger(MySensorsMessage.class);

    public int nodeId = 0;
    public int childId = 0;
    public int msgType = 0;
    public int ack = 0;
    public int subType = 0;
    public String msg = "";
    public String oldMsg = "";
    public int retries = 0;
    public long nextSend = 0;

    public MySensorsMessage() {

    }

    public MySensorsMessage(int nodeId, int childId, int msgType, int ack, int subType, String msg) {
        this.nodeId = nodeId;
        this.childId = childId;
        this.msgType = msgType;
        this.ack = ack;
        this.subType = subType;
        this.msg = msg;
    }

    public void printDebug() {
        logger.debug("nodeId: " + this.nodeId);
        logger.debug("childId: " + this.childId);
        logger.debug("msgType: " + this.msgType);
        logger.debug("ack: " + this.ack);
        logger.debug("subType: " + this.subType);
        logger.debug("msg: " + this.msg);
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

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public String getMsg() {
        return msg;
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

    public String getOldMsg() {
        return oldMsg;
    }

    public void setOldMsg(String oldMsg) {
        this.oldMsg = oldMsg;
    }

}
