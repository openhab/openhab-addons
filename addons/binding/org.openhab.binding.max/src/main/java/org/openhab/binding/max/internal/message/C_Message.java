/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import static org.openhab.binding.max.MaxBinding.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * The {@link C_Message} contains configuration about a MAX! device.
 *
 * @author Andreas Heil (info@aheil.de) - Initial version
 * @author Marcel Verpaalen - Detailed parsing, OH2 Update
 * @since 1.4.0
 */
public final class C_Message extends Message {

    private final Logger logger = LoggerFactory.getLogger(C_Message.class);

    private String rfAddress = null;
    private int length = 0;
    private DeviceType deviceType = null;
    private int roomId = -1;
    private String serialNumber = null;
    private BigDecimal tempComfort = null;
    private BigDecimal tempEco = null;
    private BigDecimal tempSetpointMax = null;
    private BigDecimal tempSetpointMin = null;
    private BigDecimal tempOffset = null;
    private BigDecimal tempOpenWindow = null;
    private BigDecimal durationOpenWindow = null;
    private BigDecimal decalcification = null;
    private BigDecimal valveMaximum = null;
    private BigDecimal valveOffset = null;
    private BigDecimal boostDuration = null;
    private BigDecimal boostValve = null;
    private String programData = null;

    private HashMap<String, Object> properties = new HashMap<>();

    public C_Message(String raw) {
        super(raw);
        String[] tokens = this.getPayload().split(Message.DELIMETER);

        rfAddress = tokens[0];

        byte[] bytes = Base64.decodeBase64(tokens[1].getBytes());

        int[] data = new int[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            data[i] = bytes[i] & 0xFF;
        }

        length = data[0];
        if (length != data.length - 1) {
            logger.debug("C_Message malformed: wrong data length. Expected bytes {}, actual bytes {}", length,
                    data.length - 1);
        }

        String rfAddress2 = Utils.toHex(data[1], data[2], data[3]);
        if (!rfAddress.toUpperCase().equals(rfAddress2.toUpperCase())) {
            logger.debug("C_Message malformed: wrong RF address. Expected address {}, actual address {}",
                    rfAddress.toUpperCase(), rfAddress2.toUpperCase());
        }

        deviceType = DeviceType.create(data[4]);
        roomId = data[5] & 0xFF;

        serialNumber = getSerialNumber(bytes);
        if (deviceType == DeviceType.HeatingThermostatPlus || deviceType == DeviceType.HeatingThermostat
                || deviceType == DeviceType.WallMountedThermostat) {
            parseHeatingThermostatData(bytes);
        }

        if (deviceType == DeviceType.Cube) {
            parseCubeData(bytes);
        }
        if (deviceType == DeviceType.EcoSwitch || deviceType == DeviceType.ShutterContact) {
            logger.trace("Device {} type {} Data: '{}'", rfAddress, deviceType.toString(), parseData(bytes));
        }
    }

    private String getSerialNumber(byte[] bytes) {
        byte[] sn = new byte[10];

        for (int i = 0; i < 10; i++) {
            sn[i] = bytes[i + 8];
        }

        try {
            return new String(sn, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.debug("Cannot encode serial number from C message due to encoding issues.");
        }

        return "";
    }

    private String parseData(byte[] bytes) {
        if (bytes.length <= 18) {
            return "";
        }
        try {
            int DataStart = 18;
            byte[] sn = new byte[bytes.length - DataStart];

            for (int i = 0; i < sn.length; i++) {
                sn[i] = bytes[i + DataStart];
            }
            logger.trace("DataBytes: {}", Utils.getHex(sn));
            try {
                return new String(sn, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.debug("Cannot encode device string from C message due to encoding issues.");
            }

        } catch (Exception e) {
            logger.debug("Exception occurred during parsing: {}", e.getMessage(), e);
        }

        return "";
    }

    private void parseCubeData(byte[] bytes) {
        if (bytes.length != 238) {
            logger.debug("Unexpected lenght for Cube message {}, expected: 238", bytes.length);
        }
        try {
            properties.put("Portal Enabled", Integer.toString(bytes[0x18] & 0xFF));
            properties.put("Portal URL", new String(Arrays.copyOfRange(bytes, 0x55, 0xD5), "UTF-8").trim());
            properties.put("TimeZone (Winter)", new String(Arrays.copyOfRange(bytes, 0xD6, 0xDA), "UTF-8").trim());
            properties.put("TimeZone (Daylight)",
                    new String(Arrays.copyOfRange(bytes, 0x00E2, 0x00E6), "UTF-8").trim());

            properties.put("Unknown1", Utils.getHex(Arrays.copyOfRange(bytes, 0x13, 0x33))); // Pushbutton Up config
                                                                                             // 0=auto, 1=eco, 2=comfort
            properties.put("Unknown2", Utils.getHex(Arrays.copyOfRange(bytes, 0x34, 0x54))); // Pushbutton down config
                                                                                             // 0=auto, 1=eco, 2=comfort
            properties.put("Winter Time", parseTimeInfo(Arrays.copyOfRange(bytes, 0xDB, 0xE2), "Winter").toString()); // Date
                                                                                                                      // of
                                                                                                                      // wintertime
            properties.put("Summer Time", parseTimeInfo(Arrays.copyOfRange(bytes, 0xE7, 0xEF), "Summer").toString()); // Date
                                                                                                                      // of
                                                                                                                      // summertime

        } catch (UnsupportedEncodingException e) {
            logger.debug("Cannot encode device string from C message due to encoding issues.");
        } catch (Exception e) {
            logger.debug("Exception occurred during parsing: {}", e.getMessage(), e);
        }
    }

    private Date parseTimeInfo(byte[] bytes, String suffix) {
        int month = bytes[0] & 0xFF;
        int weekDay = bytes[1] & 0xFF;
        int hour = bytes[2] & 0xFF;
        int utcOffset = new BigInteger(Arrays.copyOfRange(bytes, 0x03, 0x07)).intValue();
        suffix = " (" + suffix + ")";
        properties.put("Utc Offset" + suffix, utcOffset);
        Calendar pCal = Calendar.getInstance();
        pCal.set(Calendar.getInstance().get(Calendar.YEAR), month - 1, 15, hour, 0, 0);
        pCal.set(Calendar.DAY_OF_WEEK, weekDay + 1);
        pCal.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
        return pCal.getTime();
    }

    private void parseHeatingThermostatData(byte[] bytes) {
        try {

            int plusDataStart = 18;
            int programDataStart = 11;
            tempComfort = new BigDecimal((bytes[plusDataStart] & 0xFF) / 2D);
            tempEco = new BigDecimal((bytes[plusDataStart + 1] & 0xFF) / 2D);
            tempSetpointMax = new BigDecimal((bytes[plusDataStart + 2] & 0xFF) / 2D);
            tempSetpointMin = new BigDecimal((bytes[plusDataStart + 3] & 0xFF) / 2D);
            properties.put(PROPERTY_THERMO_COMFORT_TEMP, tempComfort.setScale(1, RoundingMode.HALF_DOWN));
            properties.put(PROPERTY_THERMO_ECO_TEMP, tempEco.setScale(1, RoundingMode.HALF_DOWN));
            properties.put(PROPERTY_THERMO_MAX_TEMP_SETPOINT, tempSetpointMax.setScale(1, RoundingMode.HALF_DOWN));
            properties.put(PROPERTY_THERMO_MIN_TEMP_SETPOINT, tempSetpointMin.setScale(1, RoundingMode.HALF_DOWN));
            if (bytes.length < 211) {
                // Device is a WallMountedThermostat
                programDataStart = 4;
                logger.trace("WallThermostat byte {}: {}", bytes.length - 3,
                        Float.toString(bytes[bytes.length - 3] & 0xFF));
                logger.trace("WallThermostat byte {}: {}", bytes.length - 2,
                        Float.toString(bytes[bytes.length - 2] & 0xFF));
                logger.trace("WallThermostat byte {}: {}", bytes.length - 1,
                        Float.toString(bytes[bytes.length - 1] & 0xFF));
            } else {
                // Device is a HeatingThermostat(+)
                tempOffset = new BigDecimal((bytes[plusDataStart + 4] & 0xFF) / 2D - 3.5);
                tempOpenWindow = new BigDecimal((bytes[plusDataStart + 5] & 0xFF) / 2D);
                durationOpenWindow = new BigDecimal((bytes[plusDataStart + 6] & 0xFF) * 5);
                boostDuration = new BigDecimal(bytes[plusDataStart + 7] & 0xFF >> 5);
                boostValve = new BigDecimal((bytes[plusDataStart + 7] & 0x1F) * 5);
                decalcification = new BigDecimal(bytes[plusDataStart + 8]);
                valveMaximum = new BigDecimal((bytes[plusDataStart + 9] & 0xFF) * 100 / 255);
                valveOffset = new BigDecimal((bytes[plusDataStart + 10] & 0xFF) * 100 / 255);
                properties.put(PROPERTY_THERMO_OFFSET_TEMP, tempOffset.setScale(1, RoundingMode.HALF_DOWN));
                properties.put(PROPERTY_THERMO_WINDOW_OPEN_TEMP, tempOpenWindow.setScale(1, RoundingMode.HALF_DOWN));
                properties.put(PROPERTY_THERMO_WINDOW_OPEN_DURATION,
                        durationOpenWindow.setScale(0, RoundingMode.HALF_DOWN));
                properties.put(PROPERTY_THERMO_BOOST_DURATION, boostDuration.setScale(0, RoundingMode.HALF_DOWN));
                properties.put(PROPERTY_THERMO_BOOST_VALVEPOS, boostValve.setScale(0, RoundingMode.HALF_DOWN));
                properties.put(PROPERTY_THERMO_DECALCIFICATION, decalcification.setScale(0, RoundingMode.HALF_DOWN));
                properties.put(PROPERTY_THERMO_VALVE_MAX, valveMaximum.setScale(0, RoundingMode.HALF_DOWN));
                properties.put(PROPERTY_THERMO_VALVE_OFFSET, valveOffset.setScale(0, RoundingMode.HALF_DOWN));
            }
            programData = "";
            int ln = 13 * 6; // first day = Sat
            String startTime = "00:00h";
            for (int char_idx = plusDataStart + programDataStart; char_idx < (plusDataStart + programDataStart
                    + 26 * 7); char_idx++) {
                if (ln % 13 == 0) {
                    programData += "\r\n Day " + Integer.toString((ln / 13) % 7) + ": ";
                    startTime = "00:00h";
                }
                int progTime = (bytes[char_idx + 1] & 0xFF) * 5 + (bytes[char_idx] & 0x01) * 1280;
                int progMinutes = progTime % 60;
                int progHours = (progTime - progMinutes) / 60;
                String endTime = Integer.toString(progHours) + ":" + String.format("%02d", progMinutes) + "h";
                programData += startTime + "-" + endTime + " " + Double.toString(bytes[char_idx] / 4) + "C  ";
                startTime = endTime;
                char_idx++;
                ln++;
            }

        } catch (Exception e) {
            logger.debug("Exception occurred during heater data: {}", e.getMessage(), e);
        }
        return;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public MessageType getType() {
        return MessageType.C;
    }

    public String getRFAddress() {
        return rfAddress;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public int getRoomID() {
        return roomId;
    }

    @Override
    public void debug(Logger logger) {
        logger.debug("=== C_Message === ");
        logger.trace("\tRAW:                    {}", this.getPayload());
        logger.debug("DeviceType:               {}", deviceType.toString());
        logger.debug("SerialNumber:             {}", serialNumber);
        logger.debug("RFAddress:                {}", rfAddress);
        logger.debug("RoomID:                   {}", roomId);
        for (String key : properties.keySet()) {
            if (!key.startsWith("Unknown")) {
                String propertyName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), ' ');
                logger.debug("{}:{}{}", propertyName, Strings.repeat(" ", 25 - propertyName.length()),
                        properties.get(key));
            } else {
                logger.debug("{}:{}{}", key, Strings.repeat(" ", 25 - key.length()), properties.get(key));
            }
        }
        if (programData != null) {
            logger.trace("ProgramData:          {}", programData);
        }
    }
}
