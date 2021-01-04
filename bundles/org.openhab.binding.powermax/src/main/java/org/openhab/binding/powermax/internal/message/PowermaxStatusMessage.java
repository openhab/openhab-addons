/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

    private static byte[] zoneBytes(byte zones1, byte zones9, byte zones17, byte zones25) {
        return new byte[] { zones25, zones17, zones9, zones1 };
    }

    private static boolean[] zoneBits(byte[] zoneBytes) {
        boolean[] zones = new boolean[32];
        char[] binary = new BigInteger(zoneBytes).toString(2).toCharArray();
        int len = binary.length - 1;

        for (int i = len; i >= 0; i--) {
            zones[len - i + 1] = (binary[i] == '1');
        }

        return zones;
    }

    private static String zoneList(byte[] zoneBytes) {
        boolean[] zones = zoneBits(zoneBytes);
        List<String> names = new ArrayList<>();

        for (int i = 1; i < zones.length; i++) {
            if (zones[i]) {
                names.add(String.format("Zone %d", i));
            }
        }

        return String.join(", ", names);
    }

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
    protected PowermaxState handleMessageInternal(PowermaxCommManager commManager) {
        if (commManager == null) {
            return null;
        }

        PowermaxPanelSettings panelSettings = commManager.getPanelSettings();
        PowermaxState updatedState = commManager.createNewState();

        byte[] message = getRawData();
        byte eventType = message[3];
        String eventTypeStr = PowermaxMessageConstants.getMessageTypeString(eventType & 0x000000FF);

        debug("Event type", eventType, eventTypeStr);

        if (eventType == 0x02) {
            byte[] zoneStatusBytes = zoneBytes(message[4], message[5], message[6], message[7]);
            byte[] batteryStatusBytes = zoneBytes(message[8], message[9], message[10], message[11]);

            boolean[] zoneStatus = zoneBits(zoneStatusBytes);
            boolean[] batteryStatus = zoneBits(batteryStatusBytes);

            String zoneStatusStr = zoneList(zoneStatusBytes);
            String batteryStatusStr = zoneList(batteryStatusBytes);

            for (int i = 1; i <= panelSettings.getNbZones(); i++) {
                updatedState.setSensorTripped(i, zoneStatus[i]);
                updatedState.setSensorLowBattery(i, batteryStatus[i]);
            }

            debug("Zone status", zoneStatusBytes, zoneStatusStr);
            debug("Battery status", batteryStatusBytes, batteryStatusStr);
        } else if (eventType == 0x04) {
            byte sysStatus = message[4];
            byte sysFlags = message[5];
            byte eventZone = message[6];
            byte zoneEType = message[7];
            int x10Status = (message[10] & 0x000000FF) | ((message[11] << 8) & 0x0000FF00);

            String eventZoneStr = PowermaxMessageConstants.getZoneOrUserString(eventZone & 0x000000FF);
            String zoneETypeStr = PowermaxMessageConstants.getZoneEventString(zoneEType & 0x000000FF);

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

            debug("System status", sysStatus, statusStr);
            debug("System flags", sysFlags, sysStatusStr);
            debug("Event zone", eventZone, eventZoneStr);
            debug("Zone event type", zoneEType, zoneETypeStr);
            debug("X10 status", x10Status);

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
            byte[] zoneBypassBytes = zoneBytes(message[8], message[9], message[10], message[11]);
            boolean[] zoneBypass = zoneBits(zoneBypassBytes);
            String zoneBypassStr = zoneList(zoneBypassBytes);

            for (int i = 1; i <= panelSettings.getNbZones(); i++) {
                updatedState.setSensorBypassed(i, zoneBypass[i]);
            }

            debug("Zone bypass", zoneBypassBytes, zoneBypassStr);
        }

        return updatedState;
    }
}
