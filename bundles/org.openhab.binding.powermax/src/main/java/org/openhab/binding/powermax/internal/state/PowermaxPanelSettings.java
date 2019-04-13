/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openhab.binding.powermax.internal.message.PowermaxSendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to store all the settings of the alarm system
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxPanelSettings {

    private final Logger logger = LoggerFactory.getLogger(PowermaxPanelSettings.class);

    /** Number of PGM and X10 devices managed by the system */
    private static final int NB_PGM_X10_DEVICES = 16;

    /** Raw buffers for settings */
    private Byte[][] rawSettings;

    private PowermaxPanelType panelType;
    private String[] phoneNumbers;
    private int bellTime;
    private boolean silentPanic;
    private boolean quickArm;
    private boolean bypassEnabled;
    private boolean partitionsEnabled;
    private String[] pinCodes;
    private String panelEprom;
    private String panelSoftware;
    private String panelSerial;
    private PowermaxZoneSettings[] zoneSettings;
    private PowermaxX10Settings[] x10Settings;
    private boolean[] keypad1wEnrolled;
    private boolean[] keypad2wEnrolled;
    private boolean[] sirensEnrolled;

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
    public String getPanelEprom() {
        return panelEprom;
    }

    /**
     * @return the panel software version
     */
    public String getPanelSoftware() {
        return panelSoftware;
    }

    /**
     * @return the panel serial ID
     */
    public String getPanelSerial() {
        return panelSerial;
    }

    /**
     * @return the number of zones
     */
    public int getNbZones() {
        return zoneSettings.length;
    }

    /**
     * Get the settings relative to a zone
     *
     * @param zone the zone index (from 1 to NumberOfZones)
     *
     * @return the settings of the zone
     */
    public PowermaxZoneSettings getZoneSettings(int zone) {
        return ((zone < 1) || (zone > zoneSettings.length)) ? null : zoneSettings[zone - 1];
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
    public PowermaxX10Settings getX10Settings(int idx) {
        return ((idx < 1) || (idx >= x10Settings.length)) ? null : x10Settings[idx];
    }

    /**
     * @param idx the keypad index (first is 1)
     *
     * @return true if the 1 way keypad is enrolled; false if not
     */
    public boolean isKeypad1wEnrolled(int idx) {
        return ((keypad1wEnrolled == null) || (idx < 1) || (idx >= keypad1wEnrolled.length)) ? false
                : keypad1wEnrolled[idx - 1];
    }

    /**
     * @param idx the keypad index (first is 1)
     *
     * @return true if the 2 way keypad is enrolled; false if not
     */
    public boolean isKeypad2wEnrolled(int idx) {
        return ((keypad2wEnrolled == null) || (idx < 1) || (idx >= keypad2wEnrolled.length)) ? false
                : keypad2wEnrolled[idx - 1];
    }

    /**
     * @param idx the siren index (first is 1)
     *
     * @return true if the siren is enrolled; false if not
     */
    public boolean isSirenEnrolled(int idx) {
        return ((sirensEnrolled == null) || (idx < 1) || (idx >= sirensEnrolled.length)) ? false
                : sirensEnrolled[idx - 1];
    }

    /**
     * @return the PIN code of the first user of null if unknown (standard mode)
     */
    public String getFirstPinCode() {
        return (pinCodes == null) ? null : pinCodes[0];
    }

    public void updateRawSettings(byte[] data) {
        if ((data == null) || (data.length < 3)) {
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

    private byte[] readSettings(PowermaxSendType msgType, int start, int end) {
        byte[] message = msgType.getMessage();
        int page = message[2] & 0x000000FF;
        int index = message[1] & 0x000000FF;
        return readSettings(page, index + start, index + end);
    }

    private byte[] readSettings(int page, int start, int end) {
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

    private String readSettingsAsString(PowermaxSendType msgType, int start, int end) {
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
                            try {
                                result += new String(data, i, 1, "US-ASCII");
                            } catch (UnsupportedEncodingException e) {
                                logger.debug("Unhandled character code {}", data[i]);
                            }
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
     * @param timeSet the time in milliseconds used to set time and date; null if no sync time requested
     *
     * @return true if no problem encountered to get all the settings; false if not
     */
    @SuppressWarnings("null")
    public boolean process(boolean PowerlinkMode, PowermaxPanelType defaultPanelType, Long timeSet) {
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
        pinCodes = new String[userCnt];
        panelEprom = null;
        panelSoftware = null;
        panelSerial = null;
        zoneSettings = new PowermaxZoneSettings[zoneCnt];
        x10Settings = new PowermaxX10Settings[NB_PGM_X10_DEVICES];
        keypad1wEnrolled = new boolean[keypad1wCnt];
        keypad2wEnrolled = new boolean[keypad2wCnt];
        sirensEnrolled = new boolean[sirenCnt];

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
                if (timeSet != null) {
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
                    pinCodes[i] = String.format("%02X%02X", data[i * 2] & 0x000000FF, data[i * 2 + 1] & 0x000000FF);
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

            // Check if partitions are enabled
            byte[] partitions = readSettings(PowermaxSendType.DL_PARTITIONS, 0, 0x10 + zoneCnt);
            if (partitions != null) {
                partitionsEnabled = (partitions[0] & 0x000000FF) == 1;
            } else {
                logger.debug("Cannot get partitions information");
                result = false;
            }
            if (!partitionsEnabled) {
                partitionCnt = 1;
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
                    byte sensorTypeCode;
                    String sensorTypeStr;
                    if (panelType.isPowerMaster()) {
                        zoneEnrolled = !Arrays.equals(Arrays.copyOfRange(dataMr, i * 10 + 4, i * 10 + 9), zero5);
                        zoneInfo = data[i];
                        sensorTypeCode = dataMr[i * 10 + 5];
                        try {
                            PowermasterSensorType sensorType = PowermasterSensorType.fromCode(sensorTypeCode);
                            sensorTypeStr = sensorType.getLabel();
                        } catch (IllegalArgumentException e) {
                            sensorTypeStr = null;
                        }
                    } else {
                        zoneEnrolled = !Arrays.equals(Arrays.copyOfRange(data, i * 4, i * 4 + 3), zero3);
                        zoneInfo = data[i * 4 + 3];
                        sensorTypeCode = data[i * 4 + 2];
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
                        keypad2wEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 10 + 4, i * 10 + 9), zero5);
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
                        sirensEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 10 + 4, i * 10 + 9), zero5);
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
                        keypad1wEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 4, i * 4 + 2), zero2);
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
                        keypad2wEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 4, i * 4 + 3), zero3);
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
                        sirensEnrolled[i] = !Arrays.equals(Arrays.copyOfRange(data, i * 4, i * 4 + 3), zero3);
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

    /**
     * Log information about the current settings
     */
    public void log() {
        logger.info("{}", getInfo());
    }

    /**
     * Log help information relative to items and sitemap entries to be created
     */
    public void helpItems() {
        int zoneCnt = panelType.getWireless() + panelType.getWired();

        String items = "Help for defining items:\n" + "\nGroup GPowermax \"Alarm\""
                + "\nString Powermax_partition_status \"Partition status [%s]\" (GPowermax) {powermax=\"partition_status\"}"
                + "\nSwitch Powermax_partition_ready \"Partition ready\" (GPowermax) {powermax=\"partition_ready\", autoupdate=\"false\"}"
                + "\nSwitch Powermax_partition_bypass \"Partition bypass\" (GPowermax) {powermax=\"partition_bypass\", autoupdate=\"false\"}"
                + "\nSwitch Powermax_partition_alarm \"Partition alarm\" (GPowermax) {powermax=\"partition_alarm\", autoupdate=\"false\"}"
                + "\nSwitch Powermax_panel_trouble \"Panel trouble\" (GPowermax) {powermax=\"panel_trouble\", autoupdate=\"false\"}"
                + "\nSwitch Powermax_panel_alert_in_mem \"Panel alert in memory\" (GPowermax) {powermax=\"panel_alert_in_memory\", autoupdate=\"false\"}"
                + "\nSwitch Powermax_partition_armed \"Partition armed\" (GPowermax) {powermax=\"partition_armed\", autoupdate=\"false\"}"
                + "\nString Powermax_partition_arm_mode \"Partition arm mode [%s]\" (GPowermax) {powermax=\"partition_arm_mode\", autoupdate=\"false\"}";

        String sitemap = "Help for defining sitemap:\n" + "\nText label=\"Security\" icon=\"lock\" {"
                + "\nSwitch item=Powermax_partition_armed mappings=[OFF=\"Disarmed\", ON=\"Armed\"]"
                + "\nSwitch item=Powermax_partition_arm_mode mappings=[Disarmed=\"Disarmed\", Stay=\"Armed home\", Armed=\"Armed away\"] valuecolor=[==\"Armed\"=\"green\",==\"Stay\"=\"orange\"]"
                + "\nSwitch item=Powermax_command mappings=[get_event_log=\"Event log\", download_setup=\"Get setup\", log_setup=\"Log setup\", help_items=\"Help items\"]";

        for (int i = 1; i <= zoneCnt; i++) {
            if (zoneSettings[i - 1] != null) {
                items += String.format(
                        "\nSwitch Powermax_zone%d_status \"Zone %d status\" (GPowermax) {powermax=\"zone_status:%d\", autoupdate=\"false\"}"
                                + "\nContact Powermax_zone%d_status2 \"Zone %d status [%%s]\" (GPowermax) {powermax=\"zone_status:%d\"}"
                                + "\nDateTime Powermax_zone%d_last_trip \"Zone %d last trip [%%1$tH:%%1$tM]\" (GPowermax) {powermax=\"zone_last_trip:%d\"}"
                                + "\nSwitch Powermax_zone%d_bypassed \"Zone %d bypassed\" (GPowermax) {powermax=\"zone_bypassed:%d\", autoupdate=\"false\"}"
                                + "\nSwitch Powermax_zone%d_armed \"Zone %d armed\" (GPowermax) {powermax=\"zone_armed:%d\", autoupdate=\"false\"}"
                                + "\nSwitch Powermax_zone%d_low_battery \"Zone %d low battery\" (GPowermax) {powermax=\"zone_low_battery:%d\", autoupdate=\"false\"}",
                        i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i);
            }
        }

        items += "\nString Powermax_command \"Command\" (GPowermax) {powermax=\"command\", autoupdate=\"false\"}"
                + "\nString Powermax_event_log_1 \"Event log 1 [%s]\" (GPowermax) {powermax=\"event_log:1\"}"
                + "\nString Powermax_event_log_2 \"Event log 2 [%s]\" (GPowermax) {powermax=\"event_log:2\"}"
                + "\nString Powermax_event_log_3 \"Event log 3 [%s]\" (GPowermax) {powermax=\"event_log:3\"}"
                + "\nString Powermax_event_log_4 \"Event log 4 [%s]\" (GPowermax) {powermax=\"event_log:4\"}"
                + "\nString Powermax_event_log_5 \"Event log 5 [%s]\" (GPowermax) {powermax=\"event_log:5\"}"
                + "\nString Powermax_panel_mode \"Panel mode [%s]\" (GPowermax) {powermax=\"panel_mode\"}"
                + "\nString Powermax_panel_type \"Panel type [%s]\" (GPowermax) {powermax=\"panel_type\"}"
                + "\nString Powermax_panel_eeprom \"EPROM [%s]\" (GPowermax) {powermax=\"panel_eprom\"}"
                + "\nString Powermax_panel_software \"Software version [%s]\" (GPowermax) {powermax=\"panel_software\"}"
                + "\nString Powermax_panel_serial \"Serial [%s]\" (GPowermax) {powermax=\"panel_serial\"}";

        if (x10Settings[0] != null && x10Settings[0].isEnabled()) {
            items += "\nSwitch Powermax_PGM_status \"PGM status\" (GPowermax) {powermax=\"PGM_status\", autoupdate=\"false\"}";
        }

        for (int i = 1; i < NB_PGM_X10_DEVICES; i++) {
            if (x10Settings[i] != null && x10Settings[i].isEnabled()) {
                items += String.format(
                        "\nSwitch Powermax_X10_%d_status \"X10 %d status\" (GPowermax) {powermax=\"X10_status:%d\", autoupdate=\"false\"}"
                                + "\nString Powermax_X10_%d_status2 \"X10 %d status [%%s]\" (GPowermax) {powermax=\"X10_status:%d\", autoupdate=\"false\"}",
                        i, i, i, i, i, i);
                sitemap += String.format(
                        "\nSwitch item=Powermax_X10_%d_status2 mappings=[OFF=\"Off\", ON=\"On\", DIM=\"Dim\", BRIGHT=\"Bright\"]",
                        i);
            }
        }

        sitemap += "\nGroup item=GPowermax label=\"Alarm\"" + "\n}";

        logger.info("Powermax alarm binding:\n{}\n\n{}\n", items, sitemap);
    }
}
