/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.powermax.internal.message;

import org.openhab.binding.powermax.internal.state.PowermaxState;

/**
 * A class for PANEL message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxPanelMessage extends PowermaxBaseMessage {

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
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        super.handleMessage(commManager);

        if (commManager == null) {
            return null;
        }

        PowermaxState updatedState = commManager.createNewState();

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

            String alarmStatus;
            try {
                PowermaxAlarmType alarmType = PowermaxAlarmType.fromCode(eventType);
                alarmStatus = alarmType.getLabel();
            } catch (IllegalArgumentException e) {
                alarmStatus = "None";
            }
            updatedState.setAlarmType(alarmStatus);

            String troubleStatus;
            try {
                PowermaxTroubleType troubleType = PowermaxTroubleType.fromCode(eventType);
                troubleStatus = troubleType.getLabel();
            } catch (IllegalArgumentException e) {
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
