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

import org.openhab.binding.powermax.internal.state.PowermaxArmMode;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.openhab.binding.powermax.internal.state.PowermaxZoneSettings;

/**
 * A class for STATUS message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxStatusMessage extends PowermaxBaseMessage {

    private static final String[] EVENT_TYPE_TABLE = new String[] { "None", "Tamper Alarm", "Tamper Restore", "Open",
            "Closed", "Violated (Motion)", "Panic Alarm", "RF Jamming", "Tamper Open", "Communication Failure",
            "Line Failure", "Fuse", "Not Active", "Low Battery", "AC Failure", "Fire Alarm", "Emergency",
            "Siren Tamper", "Siren Tamper Restore", "Siren Low Battery", "Siren AC Fail" };

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxStatusMessage(byte[] message) {
        super(message);
    }

    @Override
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        super.handleMessage(commManager);

        if (commManager == null) {
            return null;
        }

        PowermaxPanelSettings panelSettings = commManager.getPanelSettings();
        PowermaxState updatedState = commManager.createNewState();

        byte[] message = getRawData();
        byte eventType = message[3];

        if (eventType == 0x02) {
            int zoneStatus = (message[4] & 0x000000FF) | ((message[5] << 8) & 0x0000FF00)
                    | ((message[6] << 16) & 0x00FF0000) | ((message[7] << 24) & 0xFF000000);
            int batteryStatus = (message[8] & 0x000000FF) | ((message[9] << 8) & 0x0000FF00)
                    | ((message[10] << 16) & 0x00FF0000) | ((message[11] << 24) & 0xFF000000);

            for (int i = 1; i <= panelSettings.getNbZones(); i++) {
                updatedState.setSensorTripped(i, ((zoneStatus >> (i - 1)) & 0x1) > 0);
                updatedState.setSensorLowBattery(i, ((batteryStatus >> (i - 1)) & 0x1) > 0);
            }
        } else if (eventType == 0x04) {
            byte sysStatus = message[4];
            byte sysFlags = message[5];
            byte eventZone = message[6];
            byte zoneEType = message[7];
            int x10Status = (message[10] & 0x000000FF) | ((message[11] << 8) & 0x0000FF00);

            String zoneETypeStr = ((zoneEType & 0x000000FF) < EVENT_TYPE_TABLE.length)
                    ? EVENT_TYPE_TABLE[zoneEType & 0x000000FF]
                    : "UNKNOWN";

            if (zoneEType == 0x03) {
                updatedState.setSensorTripped(eventZone, Boolean.TRUE);
                updatedState.setSensorLastTripped(eventZone, System.currentTimeMillis());
            } else if (zoneEType == 0x04) {
                updatedState.setSensorTripped(eventZone, Boolean.FALSE);
            } else if (zoneEType == 0x05) {
                PowermaxZoneSettings zone = panelSettings.getZoneSettings(eventZone);
                if ((zone != null) && zone.getSensorType().equalsIgnoreCase("unknown")) {
                    zone.setSensorType("Motion");
                }
                updatedState.setSensorTripped(eventZone, Boolean.TRUE);
                updatedState.setSensorLastTripped(eventZone, System.currentTimeMillis());
            }

            // PGM & X10 devices
            for (int i = 0; i < panelSettings.getNbPGMX10Devices(); i++) {
                updatedState.setPGMX10DeviceStatus(i, ((x10Status >> i) & 0x1) > 0);
            }

            String sysStatusStr = "";
            if ((sysFlags & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Ready, ";
                updatedState.setReady(true);
            } else {
                sysStatusStr = sysStatusStr + "Not ready, ";
                updatedState.setReady(false);
            }
            if (((sysFlags >> 1) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Alert in memory, ";
                updatedState.setAlertInMemory(true);
            } else {
                updatedState.setAlertInMemory(false);
            }
            if (((sysFlags >> 2) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Trouble, ";
                updatedState.setTrouble(true);
            } else {
                updatedState.setTrouble(false);
            }
            if (((sysFlags >> 3) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Bypass on, ";
                updatedState.setBypass(true);
            } else {
                updatedState.setBypass(false);
                for (int i = 1; i <= panelSettings.getNbZones(); i++) {
                    updatedState.setSensorBypassed(i, false);
                }
            }
            if (((sysFlags >> 4) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Last 10 seconds, ";
            }
            if (((sysFlags >> 5) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + zoneETypeStr;
                if (eventZone == 0xFF) {
                    sysStatusStr = sysStatusStr + " from Panel, ";
                } else if (eventZone > 0) {
                    sysStatusStr = sysStatusStr + String.format(" in Zone %d, ", eventZone);
                } else {
                    sysStatusStr = sysStatusStr + ", ";
                }
            }
            if (((sysFlags >> 6) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Status changed, ";
            }
            if (((sysFlags >> 7) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Alarm event, ";
                updatedState.setAlarmActive(true);
            } else {
                updatedState.setAlarmActive(false);
            }
            sysStatusStr = sysStatusStr.substring(0, sysStatusStr.length() - 2);
            String statusStr;
            try {
                PowermaxArmMode armMode = PowermaxArmMode.fromCode(sysStatus & 0x000000FF);
                statusStr = armMode.getName();
            } catch (IllegalArgumentException e) {
                statusStr = "UNKNOWN";
            }
            updatedState.setArmMode(statusStr);
            updatedState.setStatusStr(statusStr + ", " + sysStatusStr);

            for (int i = 1; i <= panelSettings.getNbZones(); i++) {
                PowermaxZoneSettings zone = panelSettings.getZoneSettings(i);
                if (zone != null) {
                    // mode: armed or not: 4=armed home; 5=armed away
                    int mode = sysStatus & 0x0000000F;
                    // Zone is shown as armed if
                    // the sensor type always triggers an alarm
                    // or the system is armed away (mode = 5)
                    // or the system is armed home (mode = 4) and the zone is not interior(-follow)
                    boolean armed = (!zone.getType().equalsIgnoreCase("Non-Alarm") && (zone.isAlwaysInAlarm()
                            || (mode == 0x5) || ((mode == 0x4) && !zone.getType().equalsIgnoreCase("Interior-Follow")
                                    && !zone.getType().equalsIgnoreCase("Interior"))));
                    updatedState.setSensorArmed(i, armed);
                }
            }
        } else if (eventType == 0x06) {
            int zoneBypass = (message[8] & 0x000000FF) | ((message[9] << 8) & 0x0000FF00)
                    | ((message[10] << 16) & 0x00FF0000) | ((message[11] << 24) & 0xFF000000);

            for (int i = 1; i <= panelSettings.getNbZones(); i++) {
                updatedState.setSensorBypassed(i, ((zoneBypass >> (i - 1)) & 0x1) > 0);
            }
        }

        return updatedState;
    }

    @Override
    public String toString() {
        String str = super.toString();

        byte[] message = getRawData();
        byte eventType = message[3];

        str += "\n - event type = " + String.format("%02X", eventType);
        if (eventType == 0x02) {
            int zoneStatus = (message[4] & 0x000000FF) | ((message[5] << 8) & 0x0000FF00)
                    | ((message[6] << 16) & 0x00FF0000) | ((message[7] << 24) & 0xFF000000);
            int batteryStatus = (message[8] & 0x000000FF) | ((message[9] << 8) & 0x0000FF00)
                    | ((message[10] << 16) & 0x00FF0000) | ((message[11] << 24) & 0xFF000000);

            str += "\n - zone status = " + String.format("%08X", zoneStatus);
            str += "\n - battery status = " + String.format("%08X", batteryStatus);
        } else if (eventType == 0x04) {
            byte sysStatus = message[4];
            byte sysFlags = message[5];
            byte eventZone = message[6];
            byte zoneEType = message[7];
            int x10Status = (message[10] & 0x000000FF) | ((message[11] << 8) & 0x0000FF00);

            String zoneETypeStr = ((zoneEType & 0x000000FF) < EVENT_TYPE_TABLE.length)
                    ? EVENT_TYPE_TABLE[zoneEType & 0x000000FF]
                    : "UNKNOWN";

            str += "\n - system status = " + String.format("%02X", sysStatus);
            str += "\n - system flags = " + String.format("%02X", sysFlags);
            str += "\n - event zone = " + eventZone;
            str += String.format("\n - zone event type = %02X (%s)", zoneEType, zoneETypeStr);
            str += "\n - X10 status = " + String.format("%04X", x10Status);
        } else if (eventType == 0x06) {
            int zoneBypass = (message[8] & 0x000000FF) | ((message[9] << 8) & 0x0000FF00)
                    | ((message[10] << 16) & 0x00FF0000) | ((message[11] << 24) & 0xFF000000);

            str += "\n - zone bypass = " + String.format("%08X", zoneBypass);
        }

        return str;
    }
}
