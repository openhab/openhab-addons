/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal;

import org.openhab.binding.mysensors.MySensorsBindingConstants;

/**
 * @author Tim Oberföll
 *
 *         Parser for the MySensors network.
 */
public class MySensorsMessageParser {

    /**
     * @param line Input is a String containing the message received from the MySensors network
     * @return Returns the content of the message as a MySensorsMessage
     */
    public static MySensorsMessage parse(String line) {
        String[] splitMessage = line.split(";");
        if (splitMessage.length > 4) {

            MySensorsMessage mysensorsmessage = new MySensorsMessage();

            int nodeId = Integer.parseInt(splitMessage[0]);

            mysensorsmessage.setNodeId(nodeId);
            mysensorsmessage.setChildId(Integer.parseInt(splitMessage[1]));
            mysensorsmessage.setMsgType(Integer.parseInt(splitMessage[2]));
            mysensorsmessage.setAck(Integer.parseInt(splitMessage[3]));
            mysensorsmessage.setSubType(Integer.parseInt(splitMessage[4]));
            if (splitMessage.length == 6) {
                String msg = splitMessage[5].replaceAll("\\r|\\n", "").trim();
                mysensorsmessage.setMsg(msg);
            } else {
                mysensorsmessage.setMsg("");
            }

            return mysensorsmessage;
        } else {
            return null;
        }

    }

    public static boolean isIVersionMessage(MySensorsMessage msg) {
        return (msg != null && msg.nodeId == 0 && msg.childId == 0
                && msg.msgType == MySensorsBindingConstants.MYSENSORS_MSG_TYPE_INTERNAL && msg.ack == 0
                && msg.subType == MySensorsBindingConstants.MYSENSORS_SUBTYPE_I_VERSION && msg.msg != null);
    }

    public static String generateAPIString(MySensorsMessage msg) {
        String APIString = "";
        APIString += msg.getNodeId() + ";";
        APIString += msg.getChildId() + ";";
        APIString += msg.getMsgType() + ";";
        APIString += msg.getAck() + ";";
        APIString += msg.getSubType() + ";";
        APIString += msg.getMsg() + "\n";

        return APIString;
    }
}
