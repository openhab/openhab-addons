/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.message;

/**
 * Parser for the MySensors messages.
 *
 * @author Tim Oberföll
 *
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

    /**
     * Converts a MySensorsMessage object to a String.
     *
     * @param msg the MySensorsMessage that should be converted.
     * @return the MySensorsMessage as a String.
     */
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
