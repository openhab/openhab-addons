/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

import java.util.HashMap;

import org.openhab.binding.powermax.internal.state.PowermaxState;

/**
 * A class for PANEL message handling
 *
 * @author Laurent Garnier
 * @since 1.9.0
 */
public class PowermaxPanelMessage extends PowermaxBaseMessage {

    private static final HashMap<Integer, String> ALARM_TYPES;
    static {
        ALARM_TYPES = new HashMap<Integer, String>();
        ALARM_TYPES.put(0x01, "Intruder");
        ALARM_TYPES.put(0x02, "Intruder");
        ALARM_TYPES.put(0x03, "Intruder");
        ALARM_TYPES.put(0x04, "Intruder");
        ALARM_TYPES.put(0x05, "Intruder");
        ALARM_TYPES.put(0x06, "Tamper");
        ALARM_TYPES.put(0x07, "Tamper");
        ALARM_TYPES.put(0x08, "Tamper");
        ALARM_TYPES.put(0x09, "Tamper");
        ALARM_TYPES.put(0x0B, "Panic");
        ALARM_TYPES.put(0x0C, "Panic");
        ALARM_TYPES.put(0x20, "Fire");
        ALARM_TYPES.put(0x23, "Emergency");
        ALARM_TYPES.put(0x49, "Gas");
        ALARM_TYPES.put(0x4D, "Flood");
    }

    private static final HashMap<Integer, String> TROUBLE_TYPES;
    static {
        TROUBLE_TYPES = new HashMap<Integer, String>();
        TROUBLE_TYPES.put(0x0A, "Communication");
        TROUBLE_TYPES.put(0x0F, "General");
        TROUBLE_TYPES.put(0x29, "Battery");
        TROUBLE_TYPES.put(0x2B, "Power");
        TROUBLE_TYPES.put(0x2D, "Battery");
        TROUBLE_TYPES.put(0x2F, "Jamming");
        TROUBLE_TYPES.put(0x31, "Communication");
        TROUBLE_TYPES.put(0x33, "Telephone");
        TROUBLE_TYPES.put(0x36, "Power");
        TROUBLE_TYPES.put(0x38, "Battery");
        TROUBLE_TYPES.put(0x3B, "Battery");
        TROUBLE_TYPES.put(0x3C, "Battery");
        TROUBLE_TYPES.put(0x40, "Battery");
        TROUBLE_TYPES.put(0x43, "Battery");
    }

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxPanelMessage(byte[] message) {
        super(message);
    }

    @Override
    public PowermaxState handleMessage() {
        super.handleMessage();

        PowermaxState updatedState = new PowermaxState();

        byte[] message = getRawData();
        int msgCnt = message[2] & 0x000000FF;

        for (int i = 1; i <= msgCnt; i++) {
            byte eventZone = message[2 + 2 * i];
            byte logEvent = message[3 + 2 * i];
            int eventType = logEvent & 0x0000007F;
            String logEventStr = (eventType < PowermaxEventLogMessage.LOG_EVENT_TABLE.length)
                    ? PowermaxEventLogMessage.LOG_EVENT_TABLE[eventType]
                    : "UNKNOWN";
            String logUserStr = ((eventZone & 0x000000FF) < PowermaxEventLogMessage.LOG_USER_TABLE.length)
                    ? PowermaxEventLogMessage.LOG_USER_TABLE[eventZone & 0x000000FF]
                    : "UNKNOWN";
            updatedState.setPanelStatus(logEventStr + " (" + logUserStr + ")");

            String alarmStatus = ALARM_TYPES.get(eventType);
            if (alarmStatus == null) {
                alarmStatus = "None";
            }
            updatedState.setAlarmType(alarmStatus);

            String troubleStatus = TROUBLE_TYPES.get(eventType);
            if (troubleStatus == null) {
                troubleStatus = "None";
            }
            updatedState.setTroubleType(troubleStatus);

            if (eventType == 0x60) {
                // System reset
                updatedState.setDownloadSetupRequired(true);
            }
        }

        return updatedState;
    }

    @Override
    public String toString() {
        String str = super.toString();

        byte[] message = getRawData();
        int msgCnt = message[2] & 0x000000FF;

        str += "\n - event count = " + msgCnt;
        for (int i = 1; i <= msgCnt; i++) {
            byte eventZone = message[2 + 2 * i];
            byte logEvent = message[3 + 2 * i];

            str += "\n - event " + i + " zone code = " + String.format("%08X", eventZone);
            str += "\n - event " + i + " event code = " + String.format("%08X", logEvent);
        }

        return str;
    }

}
