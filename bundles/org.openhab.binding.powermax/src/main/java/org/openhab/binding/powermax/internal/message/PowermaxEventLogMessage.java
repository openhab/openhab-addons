/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxState;

/**
 * A class for EVENTLOG message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxEventLogMessage extends PowermaxBaseMessage {

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxEventLogMessage(byte[] message) {
        super(message);
    }

    @Override
    protected @Nullable PowermaxState handleMessageInternal(@Nullable PowermaxCommManager commManager) {
        if (commManager == null) {
            return null;
        }

        PowermaxPanelSettings panelSettings = commManager.getPanelSettings();
        PowermaxState updatedState = commManager.createNewState();

        byte[] message = getRawData();
        int eventNum = message[3] & 0x000000FF;

        debug("Event number", eventNum);

        if (eventNum == 1) {
            int eventCnt = message[2] & 0x000000FF;
            updatedState.setEventLogSize(eventCnt - 1);

            debug("Event count", eventCnt);
        } else {
            int second = message[4] & 0x000000FF;
            int minute = message[5] & 0x000000FF;
            int hour = message[6] & 0x000000FF;
            int day = message[7] & 0x000000FF;
            int month = message[8] & 0x000000FF;
            int year = (message[9] & 0x000000FF) + 2000;
            String timestamp = String.format("%02d/%02d/%04d %02d:%02d:%02d", day, month, year, hour, minute, second);
            byte eventZone = message[10];
            byte logEvent = message[11];
            String logEventStr = PowermaxMessageConstants.getSystemEvent(logEvent & 0x000000FF).toString();
            String logUserStr = panelSettings.getZoneOrUserName(eventZone & 0x000000FF);

            String eventStr;
            if (panelSettings.getPanelType().getPartitions() > 1) {
                String part;
                if ((second & 0x01) == 0x01) {
                    part = "Part. 1";
                } else if ((second & 0x02) == 0x02) {
                    part = "Part. 2";
                } else if ((second & 0x04) == 0x04) {
                    part = "Part. 3";
                } else {
                    part = "Panel";
                }
                eventStr = String.format("%s / %s: %s (%s)", timestamp, part, logEventStr, logUserStr);
            } else {
                eventStr = String.format("%s: %s (%s)", timestamp, logEventStr, logUserStr);
            }
            updatedState.setEventLogSize(eventNum - 1);
            updatedState.setEventLog(eventNum - 1, eventStr);

            debug("Event " + eventNum + " date/time", timestamp);
            debug("Event " + eventNum + " zone code", eventZone, logUserStr);
            debug("Event " + eventNum + " event code", logEvent, logEventStr);
        }

        return updatedState;
    }
}
