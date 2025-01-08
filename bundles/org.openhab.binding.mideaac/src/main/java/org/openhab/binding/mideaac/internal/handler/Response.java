/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;
import org.openhab.binding.mideaac.internal.handler.Timer.TimerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Response} performs the byte data stream decoding
 * The original reference is
 * https://github.com/georgezhao2010/midea_ac_lan/blob/06fc4b582a012bbbfd6bd5942c92034270eca0eb/custom_components/midea_ac_lan/midea/devices/ac/message.py#L418
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - Add Java Docs, minor fixes
 */
@NonNullByDefault
public class Response {
    byte[] data;

    // set empty to match the return from an empty byte avoid null
    float empty = (float) -19.0;
    private Logger logger = LoggerFactory.getLogger(Response.class);

    private final int version;
    String responseType;
    byte bodyType;

    private int getVersion() {
        return version;
    }

    /**
     * Response class Parameters
     * 
     * @param data byte array from device
     * @param version version of the device
     * @param responseType response type
     * @param bodyType Body type
     */
    public Response(byte[] data, int version, String responseType, byte bodyType) {
        this.data = data;
        this.version = version;
        this.bodyType = bodyType;
        this.responseType = responseType;

        if (logger.isDebugEnabled()) {
            logger.debug("Power State: {}", getPowerState());
            logger.debug("Target Temperature: {}", getTargetTemperature());
            logger.debug("Operational Mode: {}", getOperationalMode());
            logger.debug("Fan Speed: {}", getFanSpeed());
            logger.debug("On Timer: {}", getOnTimer());
            logger.debug("Off Timer: {}", getOffTimer());
            logger.debug("Swing Mode: {}", getSwingMode());
            logger.debug("Sleep Function: {}", getSleepFunction());
            logger.debug("Turbo Mode: {}", getTurboMode());
            logger.debug("Eco Mode: {}", getEcoMode());
            logger.debug("Indoor Temperature: {}", getIndoorTemperature());
            logger.debug("Outdoor Temperature: {}", getOutdoorTemperature());
            logger.debug("LED Display: {}", getDisplayOn());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Prompt Tone: {}", getPromptTone());
            logger.trace("Appliance Error: {}", getApplianceError());
            logger.trace("Auxiliary Heat: {}", getAuxHeat());
            logger.trace("Fahrenheit: {}", getFahrenheit());
            logger.trace("Humidity: {}", getHumidity());
            logger.trace("Alternate Target Temperature {}", getAlternateTargetTemperature());
        }

        /**
         * Trace Log Response and Body Type for V3. V2 set at "" and 0x00
         * This was for future development since only 0xC0 is currently used
         */
        if (version == 3) {
            logger.trace("Response and Body Type: {}, {}", responseType, bodyType);
            if ("notify2".equals(responseType) && bodyType == -95) { // 0xA0 = -95
                logger.trace("Response Handler: XA0Message");
            } else if ("notify1".equals(responseType) && bodyType == -91) { // 0xA1 = -91
                logger.trace("Response Handler: XA1Message");
            } else if (("notify2".equals(responseType) || "set".equals(responseType) || "query".equals(responseType))
                    && (bodyType == 0xB0 || bodyType == 0xB1 || bodyType == 0xB5)) {
                logger.trace("Response Handler: XBXMessage");
            } else if (("set".equals(responseType) || "query".equals(responseType)) && bodyType == -64) { // 0xC0 = -64
                logger.trace("Response Handler: XCOMessage");
            } else if ("query".equals(responseType) && bodyType == 0xC1) {
                logger.trace("Response Handler: XC1Message");
            } else {
                logger.trace("Response Handler: _general_");
            }
        }
    }

    /**
     * Device On or Off
     * 
     * @return power state true or false
     */
    public boolean getPowerState() {
        return (data[0x01] & 0x1) > 0;
    }

    /**
     * Read only
     * 
     * @return prompt tone true or false
     */
    public boolean getPromptTone() {
        return (data[0x01] & 0x40) > 0;
    }

    /**
     * Read only
     * 
     * @return appliance error true or false
     */
    public boolean getApplianceError() {
        return (data[0x01] & 0x80) > 0;
    }

    /**
     * Setpoint for Heat Pump
     * 
     * @return current setpoint in degrees C
     */
    public float getTargetTemperature() {
        return (data[0x02] & 0xf) + 16.0f + (((data[0x02] & 0x10) > 0) ? 0.5f : 0.0f);
    }

    /**
     * Cool, Heat, Fan Only, etc. See Command Base class
     * 
     * @return Cool, Heat, Fan Only, etc.
     */
    public OperationalMode getOperationalMode() {
        return OperationalMode.fromId((data[0x02] & 0xe0) >> 5);
    }

    /**
     * Low, Medium, High, Auto etc. See Command Base class
     * 
     * @return Low, Medium, High, Auto etc.
     */
    public FanSpeed getFanSpeed() {
        return FanSpeed.fromId(data[0x03] & 0x7f, getVersion());
    }

    /**
     * Creates String representation of the On timer to the channel
     * 
     * @return String of HH:MM
     */
    public Timer getOnTimer() {
        return new Timer((data[0x04] & 0x80) > 0, ((data[0x04] & (byte) 0x7c) >> 2),
                ((data[0x04] & 0x3) * 15 + 15 - (((data[0x06] & (byte) 0xf0) >> 4) & 0x0f)));
    }

    /**
     * This is used to carry the current On Timer (last response) through
     * subsequent Set commands, so it is not overwritten.
     * 
     * @return status plus String of HH:MM
     */
    public TimerData getOnTimerData() {
        int hours = 0;
        int minutes = 0;
        Timer timer = new Timer(true, hours, minutes);
        boolean status = (data[0x04] & 0x80) > 0;
        hours = ((data[0x04] & (byte) 0x7c) >> 2);
        minutes = ((data[0x04] & 0x3) * 15 + 15 - (((data[0x06] & (byte) 0xf0) >> 4) & 0x0f));
        return timer.new TimerData(status, hours, minutes);
    }

    /**
     * Creates String representation of the Off timer to the channel
     * 
     * @return String of HH:MM
     */
    public Timer getOffTimer() {
        return new Timer((data[0x05] & 0x80) > 0, ((data[0x05] & (byte) 0x7c) >> 2),
                ((data[0x05] & 0x3) * 15 + 15 - (data[0x06] & (byte) 0xf)));
    }

    /**
     * This is used to carry the Off timer (last response) through
     * subsequent Set commands, so it is not overwritten.
     * 
     * @return status plus String of HH:MM
     */
    public TimerData getOffTimerData() {
        int hours = 0;
        int minutes = 0;
        Timer timer = new Timer(true, hours, minutes);
        boolean status = (data[0x05] & 0x80) > 0;
        hours = ((data[0x05] & (byte) 0x7c) >> 2);
        minutes = (data[0x05] & 0x3) * 15 + 15 - (((data[0x06] & (byte) 0xf) & 0x0f));
        return timer.new TimerData(status, hours, minutes);
    }

    /**
     * Status of the vertical and/or horzontal louver
     * 
     * @return Vertical, Horizontal, Off, Both
     */
    public SwingMode getSwingMode() {
        return SwingMode.fromId(data[0x07] & 0x3f, getVersion());
    }

    /**
     * Read only - heat mode only
     * 
     * @return auxiliary heat active
     */
    public boolean getAuxHeat() {
        return (data[0x09] & (byte) 0x08) != 0;
    }

    /**
     * Ecomode status - Fan to Auto and temp to 24 C
     * 
     * @return Eco mode on (true) or (false)
     */
    public boolean getEcoMode() {
        return (data[0x09] & (byte) 0x10) != 0;
    }

    /**
     * Sleep function status. Setpoint Temp increases in first
     * two hours of sleep by 1 degree in Cool mode
     * 
     * @return Sleep mode on (true) or (false)
     */
    public boolean getSleepFunction() {
        return (data[0x0a] & (byte) 0x01) != 0;
    }

    /**
     * Turbo mode status for maximum cooling or heat
     * 
     * @return Turbo mode on (true) or (false)
     */
    public boolean getTurboMode() {
        return (data[0x0a] & (byte) 0x02) != 0;
    }

    /**
     * If true display on indoor unit is degrees F, else C
     * 
     * @return Fahrenheit on (true) or Celsius
     */
    public boolean getFahrenheit() {
        return (data[0x0a] & (byte) 0x04) != 0;
    }

    /**
     * There is some variation in how this is handled by different
     * AC models. This covers at least 2 versions found.
     * 
     * @return Indoor temperature
     */
    public Float getIndoorTemperature() {
        double indoorTempInteger;
        double indoorTempDecimal;

        if (data[0] == (byte) 0xc0) {
            if (((Byte.toUnsignedInt(data[11]) - 50) / 2.0) < -19) {
                return (float) -19;
            }
            if (((Byte.toUnsignedInt(data[11]) - 50) / 2.0) > 50) {
                return (float) 50;
            } else {
                indoorTempInteger = (float) ((Byte.toUnsignedInt(data[11]) - 50f) / 2.0f);
            }

            indoorTempDecimal = (float) ((data[15] & 0x0F) * 0.1f);

            if (Byte.toUnsignedInt(data[11]) > 49) {
                return (float) (indoorTempInteger + indoorTempDecimal);
            } else {
                return (float) (indoorTempInteger - indoorTempDecimal);
            }
        }

        /**
         * Not observed or tested, but left in from original author
         * This was for future development since only 0xC0 is currently used
         */
        if (data[0] == (byte) 0xa0 || data[0] == (byte) 0xa1) {
            if (data[0] == (byte) 0xa0) {
                if ((data[1] >> 2) - 4 == 0) {
                    indoorTempInteger = -1;
                } else {
                    indoorTempInteger = (data[1] >> 2) + 12;
                }

                if (((data[1] >> 1) & 0x01) == 1) {
                    indoorTempDecimal = 0.5f;
                } else {
                    indoorTempDecimal = 0;
                }
            }
            if (data[0] == (byte) 0xa1) {
                if (((Byte.toUnsignedInt(data[13]) - 50) / 2.0f) < -19) {
                    return (float) -19;
                }
                if (((Byte.toUnsignedInt(data[13]) - 50) / 2.0f) > 50) {
                    return (float) 50;
                } else {
                    indoorTempInteger = (float) (Byte.toUnsignedInt(data[13]) - 50f) / 2.0f;
                }
                indoorTempDecimal = (data[18] & 0x0f) * 0.1f;

                if (Byte.toUnsignedInt(data[13]) > 49) {
                    return (float) (indoorTempInteger + indoorTempDecimal);
                } else {
                    return (float) (indoorTempInteger - indoorTempDecimal);
                }
            }
        }
        return empty;
    }

    /**
     * There is some variation in how this is handled by different
     * AC models. This covers at least 2 versions. Some models
     * do not report outside temp when the AC is off. Returns 0.0 in that case.
     * 
     * @return Outdoor temperature
     */
    public Float getOutdoorTemperature() {
        if (data[12] != (byte) 0xff) {
            double tempInteger = (float) (Byte.toUnsignedInt(data[12]) - 50f) / 2.0f;
            double tempDecimal = ((data[15] & 0xf0) >> 4) * 0.1f;
            if (Byte.toUnsignedInt(data[12]) > 49) {
                return (float) (tempInteger + tempDecimal);
            } else {
                return (float) (tempInteger - tempDecimal);
            }
        }
        return 0.0f;
    }

    /**
     * Returns the Alternative Target Temperature (not used)
     * 
     * @return Alternate target Temperature
     */
    public Float getAlternateTargetTemperature() {
        if ((data[13] & 0x1f) != 0) {
            return (data[13] & 0x1f) + 12.0f + (((data[0x02] & 0x10) > 0) ? 0.5f : 0.0f);
        } else {
            return 0.0f;
        }
    }

    /**
     * Returns status of Device LEDs
     * 
     * @return LEDs on (true) or (false)
     */
    public boolean getDisplayOn() {
        return (data[14] & (byte) 0x70) != (byte) 0x70;
    }

    /**
     * Not observed with units being tested
     * From reference Document
     * 
     * @return humidity
     */
    public int getHumidity() {
        return (data[19] & (byte) 0x7f);
    }
}
