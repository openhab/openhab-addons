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
package org.openhab.binding.powermax.internal.state;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.message.PowermaxMessageConstants;
import org.openhab.binding.powermax.internal.message.PowermaxSendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to store all the settings of the alarm system
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxPanelSettings {

    /** Number of PGM and X10 devices managed by the system */
    private static final int NB_PGM_X10_DEVICES = 16;

    private final Logger logger = LoggerFactory.getLogger(PowermaxPanelSettings.class);

    /** Raw buffers for settings */
    private Byte[][] rawSettings;

    private PowermaxPanelType panelType;
    private String[] phoneNumbers;
    private int bellTime;
    private boolean silentPanic;
    private boolean quickArm;
    private boolean bypassEnabled;
    private boolean partitionsEnabled;
    private String @Nullable [] pinCodes;
    private @Nullable String panelEprom;
    private @Nullable String panelSoftware;
    private @Nullable String panelSerial;
    private PowermaxZoneSettings[] zoneSettings;
    private PowermaxX10Settings[] x10Settings;
    private boolean @Nullable [] keypad1wEnrolled;
    private boolean @Nullable [] keypad2wEnrolled;
    private boolean @Nullable [] sirensEnrolled;

    /**
     * Constructor
     *
     * @param defaultPanelType the default panel type to consider
     */
    public PowermaxPanelSettings(PowermaxPanelType defaultPanelType) {
        rawSettings = new Byte[0x100][];
        panelType = defaultPanelType;
        phoneNumbers = new String[4];
        bellTime = 4;
        int zoneCnt = panelType.getWireless() + panelType.getWired();
        zoneSettings = new PowermaxZoneSettings[zoneCnt];
        x10Settings = new PowermaxX10Settings[NB_PGM_X10_DEVICES];
    }

    /**
     * @return the panel type
     */
    public PowermaxPanelType getPanelType() {
        return panelType;
    }

    /**
     * @return the length of time the bell or siren sounds (in minutes)
     */
    public int getBellTime() {
        return bellTime;
    }

    /**
     * @return true if panic alarms are silent; false if audible
     */
    public boolean isSilentPanic() {
        return silentPanic;
    }

    /**
     * @return true if bypassing zones is enabled; false if not
     */
    public boolean isBypassEnabled() {
        return bypassEnabled;
    }

    /**
     * @return true if partitions usage is enabled; false if not
     */
    public boolean isPartitionsEnabled() {
        return partitionsEnabled;
    }

    /**
     * @return the panel EEPROM version
     */
    public @Nullable String getPanelEprom() {
        return panelEprom;
    }

    /**
     * @return the panel software version
     */
    public @Nullable String getPanelSoftware() {
        return panelSoftware;
    }

    /**
     * @return the panel serial ID
     */
    public @Nullable String getPanelSerial() {
        return panelSerial;
    }

    /**
     * @return the number of zones
     */
    public int getNbZones() {
        return zoneSettings.length;
    }

    /**
     * @return an integer stream for iterating over the range of zone numbers
     */
    public IntStream getZoneRange() {
        return IntStream.rangeClosed(1, getNbZones());
    }

    /**
     * Get the settings relative to a zone
     *
     * @param zone the zone index (from 1 to NumberOfZones)
     *
     * @return the settings of the zone
     */
    public @Nullable PowermaxZoneSettings getZoneSettings(int zone) {
        return ((zone < 1) || (zone > zoneSettings.length)) ? null : zoneSettings[zone - 1];
    }

    /**
     * Get a zone's display name
     *
     * @param zone the zone index (from 1 to NumberOfZones)
     *
     * @return the name of the zone
     */
    public @Nullable String getZoneName(int zone) {
        PowermaxZoneSettings zoneSettings = getZoneSettings(zone);
        return (zoneSettings == null) ? null : zoneSettings.getName();
    }

    /**
     * Get a friendly display name for a zone, user, or device
     * (any possible source for an event)
     *
     * @param zoneOrUser the zone, user, or device code
     *
     * @return the display name
     */
    public String getZoneOrUserName(int zoneOrUser) {
        String zoneName = getZoneName(zoneOrUser);

        if (zoneOrUser >= 1 && zoneOrUser <= zoneSettings.length && zoneName != null) {
            return String.format("%s[%d]", zoneName, zoneOrUser);
        } else {
            return PowermaxMessageConstants.getZoneOrUser(zoneOrUser);
        }
    }

    /**
     * @return the number of PGM and X10 devices managed by the system
     */
    public int getNbPGMX10Devices() {
        return NB_PGM_X10_DEVICES;
    }

    /**
     * Get the settings relative to the PGM
     *
     * @return the settings of the PGM
     */
    public PowermaxX10Settings getPGMSettings() {
        return x10Settings[0];
    }

    /**
     * Get the settings relative to a X10 device
     *
     * @param idx the index (from 1 to 15)
     *
     * @return the settings of the X10 device
     */
    public @Nullable PowermaxX10Settings getX10Settings(int idx) {
        return ((idx < 1) || (idx >= x10Settings.length)) ? null : x10Settings[idx];
    }

    /**
     * @param idx the keypad index (first is 1)
     *
     * @return true if the 1 way keypad is enrolled; false if not
     */
    public boolean isKeypad1wEnrolled(int idx) {
        boolean @Nullable [] localKeypad1wEnrolled = keypad1wEnrolled;
        return ((localKeypad1wEnrolled == null) || (idx < 1) || (idx >= localKeypad1wEnrolled.length)) ? false
                : localKeypad1wEnrolled[idx - 1];
    }

    /**
     * @param idx the keypad index (first is 1)
     *
     * @return true if the 2 way keypad is enrolled; false if not
     */
    public boolean isKeypad2wEnrolled(int idx) {
        boolean @Nullable [] localKeypad2wEnrolled = keypad2wEnrolled;
        return ((localKeypad2wEnrolled == null) || (idx < 1) || (idx >= localKeypad2wEnrolled.length)) ? false
                : localKeypad2wEnrolled[idx - 1];
    }

    /**
     * @param idx the siren index (first is 1)
     *
     * @return true if the siren is enrolled; false if not
     */
    public boolean isSirenEnrolled(int idx) {
        boolean @Nullable [] localSirensEnrolled = sirensEnrolled;
        return ((localSirensEnrolled == null) || (idx < 1) || (idx >= localSirensEnrolled.length)) ? false
                : localSirensEnrolled[idx - 1];
    }

    /**
     * @return the PIN code of the first user of an empty string if unknown (standard mode)
     */
    public String getFirstPinCode() {
        String @Nullable [] localPinCodes = pinCodes;
        return (localPinCodes == null || localPinCodes.length == 0) ? "" : localPinCodes[0];
    }

    public void updateRawSettings(byte[] data) {
        if (data.length < 3) {
            return;
        }
        int start = 0;
        int end = data.length - 3;
        int index = data[0] & 0x000000FF;
        int page = data[1] & 0x000000FF;
        int pageMin = page + (index + start) / 0x100;
        int indexPageMin = (index + start) % 0x100;
        int pageMax = page + (index + end) / 0x100;
        int indexPageMax = (index + end) % 0x100;
        index = 2;
        for (int i = pageMin; i <= pageMax; i++) {
            start = 0;
            end = 0xFF;
            if (i == pageMin) {
                start = indexPageMin;
            }
            if (i == pageMax) {
                end = indexPageMax;
            }
            if (rawSettings[i] == null) {
                rawSettings[i] = new Byte[0x100];
            }
            for (int j = start; j <= end; j++) {
                rawSettings[i][j] = data[index++];
            }
        }
    }

    private byte @Nullable [] readSettings(PowermaxSendType msgType, int start, int end) {
        byte[] message = msgType.getMessage();
        int page = message[2] & 0x000000FF;
        int index = message[1] & 0x000000FF;
        return readSettings(page, index + start, index + end);
    }

    private byte @Nullable [] readSettings(int page, int start, int end) {
        int pageMin = page + start / 0x100;
        int indexPageMin = start % 0x100;
        int pageMax = page + end / 0x100;
        int indexPageMax = end % 0x100;
        int index = 0;
        boolean missingData = false;
        for (int i = pageMin; i <= pageMax; i++) {
            int start2 = 0;
            int end2 = 0xFF;
            if (i == pageMin) {
                start2 = indexPageMin;
            }
            if (i == pageMax) {
                end2 = indexPageMax;
            }
            index += end2 - start2 + 1;
            for (int j = start2; j <= end2; j++) {
                if ((rawSettings[i] == null) || (rawSettings[i][j] == null)) {
                    missingData = true;
                    break;
                }
            }
            if (missingData) {
                break;
            }
        }
        if (missingData) {
            logger.debug("readSettings({}, {}, {}): missing data", page, start, end);
            return null;
        }
        byte[] result = new byte[index];
        index = 0;
        for (int i = pageMin; i <= pageMax; i++) {
            int start2 = 0;
            int end2 = 0xFF;
            if (i == pageMin) {
                start2 = indexPageMin;
            }
            if (i == pageMax) {
                end2 = indexPageMax;
            }
            for (int j = start2; j <= end2; j++) {
                result[index++] = rawSettings[i][j];
            }
        }
        return result;
    }

    private @Nullable String readSettingsAsString(PowermaxSendType msgType, int start, int end) {
        byte[] message = msgType.getMessage();
        int page = message[2] & 0x000000FF;
        int index = message[1] & 0x000000FF;
        String result = null;
        byte[] data = readSettings(page, index + start, index + end);
        if ((data != null) && ((data[0] & 0x000000FF) != 0x000000FF)) {
            result = "";
            for (int i = 0; i < data.length; i++) {
                boolean endStr = false;
                switch (data[i] & 0x000000FF) {
                    case 0:
                        endStr = true;
                        break;
                    case 1:
                        result += "é";
                        break;
                    case 3:
                        result += "è";
                        break;
                    case 5:
                        result += "à";
                        break;
                    default:
                        if ((data[i] & 0x000000FF) >= 0x20) {
                            result += new String(data, i, 1, StandardCharsets.US_ASCII);
                        } else {
                            logger.debug("Unhandled character code {}", data[i]);
                        }
                        break;
                }
                if (endStr) {
                    break;
                }
            }
            result = result.trim();
        }
        return result;
    }

    /**
     * Process and store all the panel settings from the raw buffers
     *
     * @param PowerlinkMode true if in Powerlink mode or false if in standard mode
     * @param defaultPanelType the default panel type to consider if not found in the raw buffers
     * @param timeSet the time in milliseconds used to set time and date; 0 if no sync time requested
     *
     * @return true if no problem encountered to get all the settings; false if not
     */
    public boolean process(boolean PowerlinkMode, PowermaxPanelType defaultPanelType, long timeSet) {
        logger.debug("Process settings Powerlink {}", PowerlinkMode);

        boolean result = true;
        boolean result2;
        byte[] data;

        // Identify panel type
        panelType = defaultPanelType;
        if (PowerlinkMode) {
            data = readSettings(PowermaxSendType.DL_SERIAL, 7, 7);
            if (data != null) {
                try {
                    panelType = PowermaxPanelType.fromCode(data[0]);
                } catch (IllegalArgumentException e) {
                    logger.debug("Powermax alarm binding: unknwon panel type for code {}", data[0] & 0x000000FF);
                    panelType = defaultPanelType;
                }
            } else {
                logger.debug("Cannot get panel type");
                result = false;
            }
        }

        int zoneCnt = panelType.getWireless() + panelType.getWired();
        int customCnt = panelType.getCustomZones();
        int userCnt = panelType.getUserCodes();
        int partitionCnt = panelType.getPartitions();
        int sirenCnt = panelType.getSirens();
        int keypad1wCnt = panelType.getKeypads1w();
        int keypad2wCnt = panelType.getKeypads2w();

        phoneNumbers = new String[4];
        bellTime = 4;
        silentPanic = false;
        quickArm = false;
        bypassEnabled = false;
        partitionsEnabled = false;
        String[] localPinCodes = new String[userCnt];
        panelEprom = null;
        panelSoftware = null;
        panelSerial = null;
        zoneSettings = new PowermaxZoneSettings[zoneCnt];
        x10Settings = new PowermaxX10Settings[NB_PGM_X10_DEVICES];
        boolean[] localKeypad1wEnrolled = new boolean[keypad1wCnt];
        boolean[] localKeypad2wEnrolled = new boolean[keypad2wCnt];
        boolean[] localSirensEnrolled = new boolean[sirenCnt];

        if (PowerlinkMode) {
            // Check time and date
            data = readSettings(PowermaxSendType.DL_TIME, 0, 5);
            if (data != null) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, data[0] & 0x000000FF);
                cal.set(Calendar.MINUTE, data[1] & 0x000000FF);
                cal.set(Calendar.HOUR_OF_DAY, data[2] & 0x000000FF);
                cal.set(Calendar.DAY_OF_MONTH, data[3] & 0x000000FF);
                cal.set(Calendar.MONTH, (data[4] & 0x000000FF) - 1);
                cal.set(Calendar.YEAR, (data[5] & 0x000000FF) + 2000);
                long timeRead = cal.getTimeInMillis();
                logger.debug("Powermax alarm binding: time {}",
                        String.format("%02d/%02d/%04d %02d:%02d:%02d", cal.get(Calendar.DAY_OF_MONTH),
                                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));

                // Check if time sync was OK
                if (timeSet > 0) {
                    long delta = (timeRead - timeSet) / 1000;
                    if (delta <= 5) {
                        logger.debug("Powermax alarm binding: time sync OK (delta {} s)", delta);
                    } else {
                        logger.info("Powermax alarm binding: time sync failed ! (delta {} s)", delta);
                    }
                }
            } else {
                logger.debug("Cannot get time and date settings");
                result = false;
            }

            // Process zone names
            result2 = true;
            for (int i = 0; i < (26 + customCnt); i++) {
                String str = readSettingsAsString(PowermaxSendType.DL_ZONESTR, i * 16, (i + 1) * 16 - 1);
                if (str != null) {
                    try {
                        PowermaxZoneName zoneName = PowermaxZoneName.fromId(i);
                        zoneName.setName(str);
                    } catch (IllegalArgumentException e) {
                        logger.debug("Zone id out of bounds {}", i);
                    }
                } else {
                    result2 = false;
                }
            }
            if (!result2) {
                logger.debug("Cannot get all zone names");
                result = false;
            }

            // Process communication settings
            result2 = true;
            for (int i = 0; i < phoneNumbers.length; i++) {
                data = readSettings(PowermaxSendType.DL_PHONENRS, 8 * i, 8 * i + 7);
                if (data != null) {
                    for (int j = 0; j < 8; j++) {
                        if ((data[j] & 0x000000FF) != 0x000000FF) {
                            if (j == 0) {
                                phoneNumbers[i] = "";
                            }
                            if (phoneNumbers[i] != null) {
                                phoneNumbers[i] += String.format("%02X", data[j] & 0x000000FF);
                            }
                        }
                    }
                } else {
                    result2 = false;
                }
            }
            if (!result2) {
                logger.debug("Cannot get all communication settings");
                result = false;
            }

            // Process alarm settings
            data = readSettings(PowermaxSendType.DL_COMMDEF, 0, 0x1B);
            if (data != null) {
                bellTime = data[3] & 0x000000FF;
                silentPanic = (data[0x19] & 0x00000010) == 0x00000010;
                quickArm = (data[0x1A] & 0x00000008) == 0x00000008;
                bypassEnabled = (data[0x1B] & 0x000000C0) != 0;
            } else {
                logger.debug("Cannot get alarm settings");
                result = false;
            }

            // Process user PIN codes
            data = readSettings(
                    panelType.isPowerMaster() ? PowermaxSendType.DL_MR_PINCODES : PowermaxSendType.DL_PINCODES, 0,
                    2 * userCnt - 1);
            if (data != null) {
                for (int i = 0; i < userCnt; i++) {
                    localPinCodes[i] = String.format("%02X%02X", data[i * 2] & 0x000000FF,
                            data[i * 2 + 1] & 0x000000FF);
                }
            } else {
                logger.debug("Cannot get PIN codes");
                result = false;
            }

            // Process EEPROM version
            panelEprom = readSettingsAsString(PowermaxSendType.DL_PANELFW, 0, 15);
            if (panelEprom == null) {
                logger.debug("Cannot get EEPROM version");
                result = false;
            }

            // Process software version
            panelSoftware = readSettingsAsString(PowermaxSendType.DL_PANELFW, 16, 31);
            if (panelSoftware == null) {
                logger.debug("Cannot get software version");
                result = false;
            }

            // Process serial ID
            panelSerial = "";
            data = readSettings(PowermaxSendType.DL_SERIAL, 0, 5);
            if (data != null) {
                for (int i = 0; i <= 5; i++) {
                    if ((data[i] & 0x000000FF) != 0x000000FF) {
                        panelSerial += String.format("%02X", data[i] & 0x000000FF);
                    } else {
                        panelSerial += ".";
                    }
                }
            } else {
                logger.debug("Cannot get serial ID");
                result = false;
            }

            // Check if partitions are enabled (only on panels that support partitions)
            byte[] partitions = null;
            if (partitionCnt > 1) {
                partitions = readSettings(PowermaxSendType.DL_PARTITIONS, 0, 0x10 + zoneCnt);
                if (partitions != null) {
                    partitionsEnabled = (partitions[0] & 0x000000FF) == 1;
                } else {
                    logger.debug("Cannot get partitions information");
                    result = false;
                }
                if (!partitionsEnabled) {
                    partitionCnt = 1;
                }
            }

            // Process zone settings
            data = readSettings(PowermaxSendType.DL_ZONES, 0, zoneCnt * 4 - 1);
            byte[] zoneNr = null;
            byte[] dataMr = null;
            if (panelType.isPowerMaster()) {
                zoneNr = readSettings(PowermaxSendType.DL_MR_ZONENAMES, 0, zoneCnt - 1);
                dataMr = readSettings(PowermaxSendType.DL_MR_ZONES, 0, zoneCnt * 10 - 2);
            } else {
                zoneNr = readSettings(PowermaxSendType.DL_ZONENAMES, 0, zoneCnt - 1);
            }
            if ((data != null) && (zoneNr != null)) {
                byte[] zero3 = new byte[] { 0, 0, 0 };
                byte[] zero5 = new byte[] { 0, 0, 0, 0, 0 };

                for (int i = 0; i < zoneCnt; i++) {
                    String zoneName;
                    try {
                        PowermaxZoneName zone = PowermaxZoneName.fromId(zoneNr[i] & 0x0000001F);
                        zoneName = zone.getName();
                    } catch (IllegalArgumentException e) {
                        logger.debug("Zone id out of bounds {}", zoneNr[i] & 0x0000001F);
                        zoneName = null;
                    }

                    boolean zoneEnrolled;
                    byte zoneInfo;
                    String sensorTypeStr;
                    if (panelType.isPowerMaster()) {
                        zoneInfo = data[i];
                        if (dataMr != null) {
                            zoneEnrolled = !Arrays.equals(Arrays.copyOfRange(dataMr, i * 10 + 4, i * 10 + 9), zero5);
                            byte sensorTypeCode = dataMr[i * 10 + 5];
                            try {
                                PowermasterSensorType sensorType = PowermasterSensorType.fromCode(sensorTypeCode);
                                sensorTypeStr = sensorType.getLabel();
                            } catch (IllegalArgumentException e) {
                                sensorTypeStr = null;
                            }
                        } else {
                            zoneEnrolled = false;
                            sensorTypeStr = null;
                        }
                    } else {
                        zoneEnrolled = !Arrays.equals(Arrays.copyOfRange(data, i * 4, i * 4 + 3), zero3);
                        zoneInfo = data[i * 4 + 3];
                        byte sensorTypeCode = data[i * 4 + 2];
                        try {
                            PowermaxSensorType sensorType = PowermaxSensorType
                                    .fromCode((byte) (sensorTypeCode & 0x0000000F));
                            sensorTypeStr = sensorType.getLabel();
                        } catch (IllegalArgumentException e) {
                            sensorTypeStr = null;
                        }
                    }
                    if (zoneEnrolled) {
                        byte zoneType = (byte) (zoneInfo & 0x0000000F);
                        byte zoneChime = (byte) ((zoneInfo >> 4) & 0x00000003);

                        boolean[] part = new boolean[partitionCnt];
                        if (partitionCnt > 1) {
                            for (int j = 0; j < partitionCnt; j++) {
                                part[j] = (partitions != null) ? ((partitions[0x11 + i] & (1 << j)) != 0) : true;
                            }
                        } else {
                            part[0] = true;
                        }

                        zoneSettings[i] = new PowermaxZoneSettings(zoneName, zoneType, zoneChime, sensorTypeStr, part);
                    }
                }
            } else {
                logger.debug("Cannot get zone settings");
                result = false;
            }

            data = readSettings(PowermaxSendType.DL_PGMX10, 0, 148);
            zoneNr = readSettings(PowermaxSendType.DL_X10NAMES, 0, NB_PGM_X10_DEVICES - 2);
            if ((data != null) && (zoneNr != null)) {
                for (int i = 0; i < NB_PGM_X10_DEVICES; i++) {
                    boolean enabled = false;
                    String zoneName = null;
                    for (int j = 0; j <= 8; j++) {
                        if (data[5 + i + j * 0x10] != 0) {
                            enabled = true;
                            break;
                        }
                    }
                    if (i > 0) {
                        try {
                            PowermaxZoneName zone = PowermaxZoneName.fromId(zoneNr[i - 1] & 0x0000001F);
                            zoneName = zone.getName();
                        } catch (IllegalArgumentException e) {
                            logger.debug("Zone id out of bounds {}", zoneNr[i - 1] & 0x0000001F);
                            zoneName = null;
                        }
                    }
                    x10Settings[i] = new PowermaxX10Settings(zoneName, enabled);
                }
            } else {
                logger.debug("Cannot get PGM / X10 settings");
                result = false;
            }

            if (panelType.isPowerMaster()) {
                // Process 2 way keypad settings
                data = readSettings(PowermaxSendType.DL_MR_KEYPADS, 0, keypad2wCnt * 10 - 1);
                if (data != null) {
                    byte[] zero5 = new byte[] { 0, 0, 0, 0, 0 };

                    for (int i = 0; i < keypad2wCnt; i++) {
                        localKeypad2wEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 10 + 4, i * 10 + 9),
                                zero5);
                    }
                } else {
                    logger.debug("Cannot get 2 way keypad settings");
                    result = false;
                }
                // Process siren settings
                data = readSettings(PowermaxSendType.DL_MR_SIRENS, 0, sirenCnt * 10 - 1);
                if (data != null) {
                    byte[] zero5 = new byte[] { 0, 0, 0, 0, 0 };

                    for (int i = 0; i < sirenCnt; i++) {
                        localSirensEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 10 + 4, i * 10 + 9),
                                zero5);
                    }
                } else {
                    logger.debug("Cannot get siren settings");
                    result = false;
                }
            } else {
                // Process 1 way keypad settings
                data = readSettings(PowermaxSendType.DL_1WKEYPAD, 0, keypad1wCnt * 4 - 1);
                if (data != null) {
                    byte[] zero2 = new byte[] { 0, 0 };

                    for (int i = 0; i < keypad1wCnt; i++) {
                        localKeypad1wEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 4, i * 4 + 2), zero2);
                    }
                } else {
                    logger.debug("Cannot get 1 way keypad settings");
                    result = false;
                }
                // Process 2 way keypad settings
                data = readSettings(PowermaxSendType.DL_2WKEYPAD, 0, keypad2wCnt * 4 - 1);
                if (data != null) {
                    byte[] zero3 = new byte[] { 0, 0, 0 };

                    for (int i = 0; i < keypad2wCnt; i++) {
                        localKeypad2wEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 4, i * 4 + 3), zero3);
                    }
                } else {
                    logger.debug("Cannot get 2 way keypad settings");
                    result = false;
                }
                // Process siren settings
                data = readSettings(PowermaxSendType.DL_SIRENS, 0, sirenCnt * 4 - 1);
                if (data != null) {
                    byte[] zero3 = new byte[] { 0, 0, 0 };

                    for (int i = 0; i < sirenCnt; i++) {
                        localSirensEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 4, i * 4 + 3), zero3);
                    }
                } else {
                    logger.debug("Cannot get siren settings");
                    result = false;
                }
            }
        } else {
            if (!partitionsEnabled) {
                partitionCnt = 1;
            }
            boolean[] part = new boolean[partitionCnt];
            for (int j = 0; j < partitionCnt; j++) {
                part[j] = true;
            }
            for (int i = 0; i < zoneCnt; i++) {
                zoneSettings[i] = new PowermaxZoneSettings(null, (byte) 0xFF, (byte) 0xFF, null, part);
            }
            for (int i = 0; i < NB_PGM_X10_DEVICES; i++) {
                x10Settings[i] = new PowermaxX10Settings(null, true);
            }
        }

        pinCodes = localPinCodes;
        keypad1wEnrolled = localKeypad1wEnrolled;
        keypad2wEnrolled = localKeypad2wEnrolled;
        sirensEnrolled = localSirensEnrolled;

        return result;
    }

    /**
     * Update the name of a zone
     *
     * @param zoneIdx the zone index (first zone is index 1)
     * @param zoneNameIdx the index in the table of zone names
     */
    public void updateZoneName(int zoneIdx, byte zoneNameIdx) {
        PowermaxZoneSettings zone = getZoneSettings(zoneIdx);
        if (zone != null) {
            String name;
            try {
                PowermaxZoneName zoneName = PowermaxZoneName.fromId(zoneNameIdx & 0x0000001F);
                name = zoneName.getName();
            } catch (IllegalArgumentException e) {
                logger.debug("Zone id out of bounds {}", zoneNameIdx & 0x0000001F);
                name = null;
            }
            zone.setName(name);
        }
    }

    /**
     * Update the type of a zone
     *
     * @param zoneIdx the zone index (first zone is index 1)
     * @param zoneInfo the zone info as an internal code
     */
    public void updateZoneInfo(int zoneIdx, int zoneInfo) {
        PowermaxZoneSettings zone = getZoneSettings(zoneIdx);
        if (zone != null) {
            zone.setType((byte) (zoneInfo & 0x0000000F));
        }
    }

    public String getInfo() {
        String str = "\nPanel is of type " + panelType.getLabel();

        int zoneCnt = panelType.getWireless() + panelType.getWired();
        int partitionCnt = panelType.getPartitions();
        int sirenCnt = panelType.getSirens();
        int keypad1wCnt = panelType.getKeypads1w();
        int keypad2wCnt = panelType.getKeypads2w();
        // int customCnt = panelType.getCustomZones();

        if (!partitionsEnabled) {
            partitionCnt = 1;
        }

        // for (int i = 0; i < (26 + customCnt); i++) {
        // String name;
        // try {
        // PowermaxZoneName zoneName = PowermaxZoneName.fromId(i);
        // name = zoneName.getName();
        // } catch (IllegalArgumentException e) {
        // logger.debug("Zone id out of bounds {}", i);
        // name = null;
        // }
        // str += String.format("\nZone name %d; %s", i + 1, name);
        // }
        for (int i = 0; i < phoneNumbers.length; i++) {
            if (phoneNumbers[i] != null) {
                str += String.format("\nPhone number %d: %s", i + 1, phoneNumbers[i]);
            }
        }
        str += String.format("\nBell time: %d minutes", bellTime);
        str += String.format("\nSilent panic: %s", silentPanic ? "enabled" : "disabled");
        str += String.format("\nQuick arm: %s", quickArm ? "enabled" : "disabled");
        str += String.format("\nZone bypass: %s", bypassEnabled ? "enabled" : "disabled");
        str += String.format("\nEPROM: %s", (panelEprom != null) ? panelEprom : "Undefined");
        str += String.format("\nSW: %s", (panelSoftware != null) ? panelSoftware : "Undefined");
        str += String.format("\nSerial: %s", (panelSerial != null) ? panelSerial : "Undefined");
        str += String.format("\nUse partitions: %s", partitionsEnabled ? "enabled" : "disabled");
        str += String.format("\nNumber of partitions: %d", partitionCnt);
        for (int i = 0; i < zoneCnt; i++) {
            if (zoneSettings[i] != null) {
                String partStr = "";
                for (int j = 1; j <= partitionCnt; j++) {
                    if (zoneSettings[i].isInPartition(j)) {
                        partStr += j + " ";
                    }
                }
                str += String.format("\nZone %d %s: %s (chime = %s; sensor type = %s; partitions = %s)", i + 1,
                        zoneSettings[i].getName(), zoneSettings[i].getType(), zoneSettings[i].getChime(),
                        zoneSettings[i].getSensorType(), partStr);
            }
        }
        for (int i = 0; i < NB_PGM_X10_DEVICES; i++) {
            if (x10Settings[i] != null && x10Settings[i].isEnabled()) {
                str += String.format("\n%s: %s enabled", (i == 0) ? "PGM" : ("X10 " + i),
                        (x10Settings[i].getName() != null) ? x10Settings[i].getName() : "");
            }
        }
        for (int i = 1; i <= sirenCnt; i++) {
            if (isSirenEnrolled(i)) {
                str += String.format("\nSiren %d enrolled", i);
            }
        }
        for (int i = 1; i <= keypad1wCnt; i++) {
            if (isKeypad1wEnrolled(i)) {
                str += String.format("\nKeypad 1w %d enrolled", i);
            }
        }
        for (int i = 1; i <= keypad2wCnt; i++) {
            if (isKeypad2wEnrolled(i)) {
                str += String.format("\nKeypad 2w %d enrolled", i);
            }
        }
        return "Powermax alarm binding:" + str;
    }
}
