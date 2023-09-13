/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.state.PowermaxArmMode;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxSensorType;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.openhab.binding.powermax.internal.state.PowermaxZoneSettings;

/**
 * A class for STATUS message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxStatusMessage extends PowermaxBaseMessage {

    private static byte[] zoneBytes(byte zones1, byte zones9, byte zones17, byte zones25) {
        return new byte[] { zones25, zones17, zones9, zones1 };
    }

    private static boolean[] zoneBits(byte[] zoneBytes) {
        boolean[] zones = new boolean[33];
        BigInteger bigint = new BigInteger(1, zoneBytes);

        for (int i = 1; i <= 32; i++) {
            zones[i] = bigint.testBit(i - 1);
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
    protected @Nullable PowermaxState handleMessageInternal(@Nullable PowermaxCommManager commManager) {
        if (commManager == null) {
            return null;
        }

        PowermaxPanelSettings panelSettings = commManager.getPanelSettings();
        PowermaxState updatedState = commManager.createNewState();

        byte[] message = getRawData();
        byte eventType = message[3];
        String eventTypeStr = PowermaxMessageConstants.getZoneEventType(eventType & 0x000000FF);

        debug("Event type", eventType, eventTypeStr);

        // Each event type except 0x04 contains two sets of zone bitmasks.
        // Each set is four bytes (32 bits) where each bit indicates the state
        // of the corresponding zone (1 = set, 0 = unset).

        if (eventType == 0x01) {
            // These bits are set when a zone causes an alarm
            //
            // Set 1: Alarm caused by zone being open/tripped
            // Set 2: Alarm caused by a tamper
            //
            // Note: active alarms are cleared when the Memory flag is turned off
            // (the panel won't send a follow-up event with these bits set to zero)

            byte[] alarmStatusBytes = zoneBytes(message[4], message[5], message[6], message[7]);
            byte[] tamperStatusBytes = zoneBytes(message[8], message[9], message[10], message[11]);

            boolean[] alarmStatus = zoneBits(alarmStatusBytes);
            boolean[] tamperStatus = zoneBits(tamperStatusBytes);

            String alarmStatusStr = zoneList(alarmStatusBytes);
            String tamperStatusStr = zoneList(tamperStatusBytes);

            panelSettings.getZoneRange().forEach(i -> {
                updatedState.getZone(i).alarmed.setValue(alarmStatus[i]);
                updatedState.getZone(i).tamperAlarm.setValue(tamperStatus[i]);
            });

            debug("Alarm status", alarmStatusBytes, alarmStatusStr);
            debug("Tamper alarm status", tamperStatusBytes, tamperStatusStr);
        } else if (eventType == 0x02) {
            // Set 1: List of zones that are open/tripped
            // Set 2: List of zones that have a low-battery condition

            byte[] zoneStatusBytes = zoneBytes(message[4], message[5], message[6], message[7]);
            byte[] batteryStatusBytes = zoneBytes(message[8], message[9], message[10], message[11]);

            boolean[] zoneStatus = zoneBits(zoneStatusBytes);
            boolean[] batteryStatus = zoneBits(batteryStatusBytes);

            String zoneStatusStr = zoneList(zoneStatusBytes);
            String batteryStatusStr = zoneList(batteryStatusBytes);

            panelSettings.getZoneRange().forEach(i -> {
                updatedState.getZone(i).tripped.setValue(zoneStatus[i]);
                updatedState.getZone(i).lowBattery.setValue(batteryStatus[i]);
            });

            debug("Zone status", zoneStatusBytes, zoneStatusStr);
            debug("Battery status", batteryStatusBytes, batteryStatusStr);
        } else if (eventType == 0x03) {
            // Set 1: Inactivity / loss of supervision
            // Set 2: Zone has an active tamper condition

            byte[] inactiveStatusBytes = zoneBytes(message[4], message[5], message[6], message[7]);
            byte[] tamperStatusBytes = zoneBytes(message[8], message[9], message[10], message[11]);

            boolean[] inactiveStatus = zoneBits(inactiveStatusBytes);
            boolean[] tamperStatus = zoneBits(tamperStatusBytes);

            String inactiveStatusStr = zoneList(inactiveStatusBytes);
            String tamperStatusStr = zoneList(tamperStatusBytes);

            panelSettings.getZoneRange().forEach(i -> {
                updatedState.getZone(i).inactive.setValue(inactiveStatus[i]);
                updatedState.getZone(i).tampered.setValue(tamperStatus[i]);
            });

            debug("Inactive status", inactiveStatusBytes, inactiveStatusStr);
            debug("Tamper status", tamperStatusBytes, tamperStatusStr);
        } else if (eventType == 0x04) {
            // System & zone status message (not like the other event types)

            byte sysStatus = message[4];
            byte sysFlags = message[5];
            int eventZone = message[6] & 0x000000FF;
            int zoneEType = message[7] & 0x000000FF;
            int x10Status = (message[10] & 0x000000FF) | ((message[11] << 8) & 0x0000FF00);

            String eventZoneStr = panelSettings.getZoneOrUserName(eventZone);
            String zoneETypeStr = PowermaxMessageConstants.getZoneEvent(zoneEType);

            if (zoneEType != 0x00 && eventZone > 0 && eventZone <= panelSettings.getNbZones()) {
                updatedState.getZone(eventZone).lastMessage.setValue(zoneETypeStr);
                updatedState.getZone(eventZone).lastMessageTime.setValue(System.currentTimeMillis());
            }

            if (zoneEType == 0x03) {
                // Open
                updatedState.getZone(eventZone).tripped.setValue(true);
                updatedState.getZone(eventZone).lastTripped.setValue(System.currentTimeMillis());
            } else if (zoneEType == 0x04) {
                // Closed
                updatedState.getZone(eventZone).tripped.setValue(false);
            } else if (zoneEType == 0x05) {
                // Violated (Motion)
                PowermaxZoneSettings zone = panelSettings.getZoneSettings(eventZone);
                if ((zone != null) && "unknown".equalsIgnoreCase(zone.getSensorType())) {
                    zone.setSensorType(PowermaxSensorType.MOTION_SENSOR_1.getLabel());
                }
                updatedState.getZone(eventZone).tripped.setValue(true);
                updatedState.getZone(eventZone).lastTripped.setValue(System.currentTimeMillis());
            }

            // PGM & X10 devices
            for (int i = 0; i < panelSettings.getNbPGMX10Devices(); i++) {
                updatedState.setPGMX10DeviceStatus(i, ((x10Status >> i) & 0x1) > 0);
            }

            String sysStatusStr = "";
            if ((sysFlags & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Ready, ";
                updatedState.ready.setValue(true);
            } else {
                sysStatusStr = sysStatusStr + "Not ready, ";
                updatedState.ready.setValue(false);
            }
            if (((sysFlags >> 1) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Alert in memory, ";
                updatedState.alertInMemory.setValue(true);
            } else {
                updatedState.alertInMemory.setValue(false);

                // When the memory flag is cleared, also clear all zone alarms and tamper alarms
                panelSettings.getZoneRange().forEach(i -> {
                    updatedState.getZone(i).alarmed.setValue(false);
                    updatedState.getZone(i).tamperAlarm.setValue(false);
                });
            }
            if (((sysFlags >> 2) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Trouble, ";
                updatedState.trouble.setValue(true);
            } else {
                updatedState.trouble.setValue(false);
            }
            if (((sysFlags >> 3) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Bypass on, ";
                updatedState.bypass.setValue(true);
            } else {
                updatedState.bypass.setValue(false);
                panelSettings.getZoneRange().forEach(i -> {
                    updatedState.getZone(i).bypassed.setValue(false);
                });
            }
            if (((sysFlags >> 4) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Last 10 seconds, ";
            }
            if (((sysFlags >> 5) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + zoneETypeStr;
                if (eventZone == 0xFF) {
                    sysStatusStr = sysStatusStr + " from Panel, ";
                } else if (eventZone > 0) {
                    sysStatusStr = sysStatusStr + String.format(" in %s, ", eventZoneStr);
                } else {
                    sysStatusStr = sysStatusStr + ", ";
                }
            }
            if (((sysFlags >> 6) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Status changed, ";
            }
            if (((sysFlags >> 7) & 0x1) == 1) {
                sysStatusStr = sysStatusStr + "Alarm event, ";
                updatedState.alarmActive.setValue(true);
            } else {
                updatedState.alarmActive.setValue(false);
            }
            sysStatusStr = sysStatusStr.substring(0, sysStatusStr.length() - 2);
            String statusStr;
            try {
                PowermaxArmMode armMode = PowermaxArmMode.fromCode(sysStatus & 0x000000FF);
                statusStr = armMode.getName();
            } catch (IllegalArgumentException e) {
                statusStr = "UNKNOWN";
            }
            updatedState.armMode.setValue(statusStr);
            updatedState.statusStr.setValue(statusStr + ", " + sysStatusStr);

            debug("System status", sysStatus, statusStr);
            debug("System flags", sysFlags, sysStatusStr);
            debug("Event zone", eventZone, eventZoneStr);
            debug("Zone event type", zoneEType, zoneETypeStr);
            debug("X10 status", x10Status);

            panelSettings.getZoneRange().forEach(i -> {
                PowermaxZoneSettings zone = panelSettings.getZoneSettings(i);
                if (zone != null) {
                    // mode: armed or not
                    int mode = sysStatus & 0x0000000F;
                    // Zone is shown as armed if
                    // the sensor type always triggers an alarm
                    // or the system is armed away
                    // or the system is armed home and the zone is not interior(-follow)
                    boolean armed = (!"Non-Alarm".equalsIgnoreCase(zone.getType())
                            && (zone.isAlwaysInAlarm() || (mode == PowermaxArmMode.ARMED_AWAY.getCode())
                                    || ((mode == PowermaxArmMode.ARMED_HOME.getCode())
                                            && !"Interior-Follow".equalsIgnoreCase(zone.getType())
                                            && !"Interior".equalsIgnoreCase(zone.getType()))));
                    updatedState.getZone(i).armed.setValue(armed);
                }
            });
        } else if (eventType == 0x06) {
            // Set 1: List of zones that are enrolled (we don't currently use this)
            // Set 2: List of zones that are bypassed

            byte[] zoneBypassBytes = zoneBytes(message[8], message[9], message[10], message[11]);
            boolean[] zoneBypass = zoneBits(zoneBypassBytes);
            String zoneBypassStr = zoneList(zoneBypassBytes);

            panelSettings.getZoneRange().forEach(i -> {
                updatedState.getZone(i).bypassed.setValue(zoneBypass[i]);
            });

            debug("Zone bypass", zoneBypassBytes, zoneBypassStr);
        }

        // Note: in response to a STATUS request, the panel will also send
        // messages with eventType = 0x05, 0x07, 0x08, and 0x09 but these
        // haven't been decoded yet

        return updatedState;
    }
}
