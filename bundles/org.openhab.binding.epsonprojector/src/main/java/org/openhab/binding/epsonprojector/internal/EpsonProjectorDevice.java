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
package org.openhab.binding.epsonprojector.internal;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.epsonprojector.internal.configuration.EpsonProjectorConfiguration;
import org.openhab.binding.epsonprojector.internal.connector.EpsonProjectorConnector;
import org.openhab.binding.epsonprojector.internal.connector.EpsonProjectorSerialConnector;
import org.openhab.binding.epsonprojector.internal.connector.EpsonProjectorTcpConnector;
import org.openhab.binding.epsonprojector.internal.enums.AspectRatio;
import org.openhab.binding.epsonprojector.internal.enums.Background;
import org.openhab.binding.epsonprojector.internal.enums.ColorMode;
import org.openhab.binding.epsonprojector.internal.enums.ErrorMessage;
import org.openhab.binding.epsonprojector.internal.enums.Gamma;
import org.openhab.binding.epsonprojector.internal.enums.Luminance;
import org.openhab.binding.epsonprojector.internal.enums.PowerStatus;
import org.openhab.binding.epsonprojector.internal.enums.Switch;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide high level interface to Epson projector.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public class EpsonProjectorDevice {
    private static final int[] MAP64 = new int[] { 0, 3, 7, 11, 15, 19, 23, 27, 31, 35, 39, 43, 47, 51, 55, 59, 63, 66,
            70, 74, 78, 82, 86, 90, 94, 98, 102, 106, 110, 114, 118, 122, 126, 129, 133, 137, 141, 145, 149, 153, 157,
            161, 165, 169, 173, 177, 181, 185, 189, 192, 196, 200, 204, 208, 212, 216, 220, 224, 228, 232, 236, 240,
            244, 248, 252 };

    private static final int[] MAP60 = new int[] { 0, 4, 8, 12, 16, 20, 25, 29, 33, 37, 41, 46, 50, 54, 58, 62, 67, 71,
            75, 79, 83, 88, 92, 96, 100, 104, 109, 113, 117, 121, 125, 130, 134, 138, 142, 146, 151, 155, 159, 163, 167,
            172, 176, 180, 184, 188, 193, 197, 201, 205, 209, 214, 218, 222, 226, 230, 235, 239, 243, 247, 251 };

    private static final int[] MAP49 = new int[] { 0, 5, 10, 15, 20, 25, 30, 35, 40, 46, 51, 56, 61, 66, 71, 76, 81, 87,
            92, 97, 102, 107, 112, 117, 122, 128, 133, 138, 143, 148, 153, 158, 163, 168, 174, 179, 184, 189, 194, 199,
            204, 209, 215, 220, 225, 230, 235, 240, 245, 250 };

    private static final int[] MAP48 = new int[] { 0, 5, 10, 15, 20, 26, 31, 36, 41, 47, 52, 57, 62, 67, 73, 78, 83, 88,
            94, 99, 104, 109, 114, 120, 125, 130, 135, 141, 146, 151, 156, 161, 167, 172, 177, 182, 188, 193, 198, 203,
            208, 214, 219, 224, 229, 235, 240, 245, 250 };

    private static final int[] MAP40 = new int[] { 0, 6, 12, 18, 24, 31, 37, 43, 49, 56, 62, 68, 74, 81, 87, 93, 99,
            106, 112, 118, 124, 131, 137, 143, 149, 156, 162, 168, 174, 181, 187, 193, 199, 206, 212, 218, 224, 231,
            237, 243, 249 };

    private static final int[] MAP20 = new int[] { 0, 12, 24, 36, 48, 60, 73, 85, 97, 109, 121, 134, 146, 158, 170, 182,
            195, 207, 219, 231, 243 };

    private static final int[] MAP18 = new int[] { 0, 13, 26, 40, 53, 67, 80, 94, 107, 121, 134, 148, 161, 175, 188,
            202, 215, 229, 242 };

    private static final int[] MAP_COLOR_TEMP = new int[] { 0, 25, 51, 76, 102, 128, 153, 179, 204, 230 };
    private static final int[] MAP_FLESH_COLOR = new int[] { 0, 36, 73, 109, 146, 182, 219 };

    private static final int DEFAULT_TIMEOUT = 5 * 1000;
    private static final int POWER_ON_TIMEOUT = 100 * 1000;
    private static final int POWER_OFF_TIMEOUT = 130 * 1000;
    private static final int LAMP_REFRESH_WAIT_MINUTES = 5;

    private static final String ON = "ON";
    private static final String ERR = "ERR";
    private static final String IMEVENT = "IMEVENT";

    private final Logger logger = LoggerFactory.getLogger(EpsonProjectorDevice.class);

    private @Nullable ScheduledExecutorService scheduler = null;
    private @Nullable ScheduledFuture<?> timeoutJob;

    private EpsonProjectorConnector connection;
    private ExpiringCache<Integer> cachedLampHours = new ExpiringCache<>(Duration.ofMinutes(LAMP_REFRESH_WAIT_MINUTES),
            this::queryLamp);
    private boolean connected = false;
    private boolean ready = true;

    public EpsonProjectorDevice(SerialPortManager serialPortManager, EpsonProjectorConfiguration config) {
        connection = new EpsonProjectorSerialConnector(serialPortManager, config.serialPort);
    }

    public EpsonProjectorDevice(EpsonProjectorConfiguration config) {
        connection = new EpsonProjectorTcpConnector(config.host, config.port);
    }

    public boolean isReady() {
        return ready;
    }

    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    private synchronized @Nullable String sendQuery(String query, int timeout)
            throws EpsonProjectorCommandException, EpsonProjectorException {
        logger.debug("Query: '{}'", query);
        String response = connection.sendMessage(query, timeout);

        if (response.length() == 0) {
            throw new EpsonProjectorException("No response received");
        }

        response = response.replace("\r:", "");
        logger.debug("Response: '{}'", response);

        if (ERR.equals(response) || response.startsWith(IMEVENT)) {
            throw new EpsonProjectorCommandException("Error response received for command: " + query);
        }

        if ("PWR OFF".equals(query) && ":".equals(response)) {
            // When PWR OFF command is sent, next command can be sent 10 seconds after the colon is received
            logger.debug("Refusing further commands for 10 seconds to power OFF completion");
            ready = false;
            ScheduledExecutorService scheduler = this.scheduler;
            if (scheduler != null) {
                timeoutJob = scheduler.schedule(() -> {
                    ready = true;
                }, 10, TimeUnit.SECONDS);
            }
        }

        return response;
    }

    private String splitResponse(@Nullable String response)
            throws EpsonProjectorCommandException, EpsonProjectorException {
        if (response != null && !"".equals(response)) {
            String[] pieces = response.split("=");

            if (pieces.length < 2) {
                throw new EpsonProjectorCommandException("Invalid response from projector: " + response);
            }

            return pieces[1].trim();
        } else {
            throw new EpsonProjectorException("No response received");
        }
    }

    protected void sendCommand(String command, int timeout)
            throws EpsonProjectorCommandException, EpsonProjectorException {
        sendQuery(command, timeout);
    }

    protected void sendCommand(String command) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(command, DEFAULT_TIMEOUT);
    }

    protected int queryInt(String query, int timeout, int radix)
            throws EpsonProjectorCommandException, EpsonProjectorException {
        String response = sendQuery(query, timeout);

        String str = splitResponse(response);

        // if the response has two number groups, get the first one (Aspect Ratio does this)
        if (str.contains(" ")) {
            String[] subStr = str.split(" ");
            str = subStr[0];
        }

        try {
            return Integer.parseInt(str, radix);
        } catch (NumberFormatException nfe) {
            throw new EpsonProjectorCommandException(
                    "Unable to parse response '" + str + "' as Integer for command: " + query);
        }
    }

    protected int queryInt(String query, int timeout) throws EpsonProjectorCommandException, EpsonProjectorException {
        return queryInt(query, timeout, 10);
    }

    protected int queryInt(String query) throws EpsonProjectorCommandException, EpsonProjectorException {
        return queryInt(query, DEFAULT_TIMEOUT, 10);
    }

    protected int queryHexInt(String query, int timeout)
            throws EpsonProjectorCommandException, EpsonProjectorException {
        return queryInt(query, timeout, 16);
    }

    protected int queryHexInt(String query) throws EpsonProjectorCommandException, EpsonProjectorException {
        return queryInt(query, DEFAULT_TIMEOUT, 16);
    }

    protected String queryString(String query) throws EpsonProjectorCommandException, EpsonProjectorException {
        String response = sendQuery(query, DEFAULT_TIMEOUT);
        return splitResponse(response);
    }

    public void connect() throws EpsonProjectorException {
        connection.connect();
        connected = true;
    }

    public void disconnect() throws EpsonProjectorException {
        connection.disconnect();
        connected = false;
        ready = true;
        ScheduledFuture<?> timeoutJob = this.timeoutJob;
        if (timeoutJob != null) {
            timeoutJob.cancel(true);
            this.timeoutJob = null;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    /*
     * Power
     */
    public PowerStatus getPowerStatus() throws EpsonProjectorCommandException, EpsonProjectorException {
        int val = queryInt("PWR?");
        return PowerStatus.forValue(val);
    }

    public void setPower(Switch value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("PWR %s", value.name()), value == Switch.ON ? POWER_ON_TIMEOUT : POWER_OFF_TIMEOUT);
    }

    /*
     * Key code
     */
    public void sendKeyCode(String value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("KEY %s", value));
    }

    /*
     * Vertical Keystone
     */
    public int getVerticalKeystone() throws EpsonProjectorCommandException, EpsonProjectorException {
        int vkey = queryInt("VKEYSTONE?");
        for (int i = 0; i < MAP60.length; i++) {
            if (vkey == MAP60[i]) {
                return i - 30;
            }
        }
        return 0;
    }

    public void setVerticalKeystone(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        value = value + 30;
        if (value >= 0 && value <= 60) {
            sendCommand(String.format("VKEYSTONE %d", MAP60[value]));
        }
    }

    /*
     * Horizontal Keystone
     */
    public int getHorizontalKeystone() throws EpsonProjectorCommandException, EpsonProjectorException {
        int hkey = queryInt("HKEYSTONE?");
        for (int i = 0; i < MAP60.length; i++) {
            if (hkey == MAP60[i]) {
                return i - 30;
            }
        }
        return 0;
    }

    public void setHorizontalKeystone(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        value = value + 30;
        if (value >= 0 && value <= 60) {
            sendCommand(String.format("HKEYSTONE %d", MAP60[value]));
        }
    }

    /*
     * Auto Keystone
     */

    public Switch getAutoKeystone() throws EpsonProjectorCommandException, EpsonProjectorException {
        String val = queryString("AUTOKEYSTONE?");
        return val.equals(ON) ? Switch.ON : Switch.OFF;
    }

    public void setAutoKeystone(Switch value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("AUTOKEYSTONE %s", value.name()), DEFAULT_TIMEOUT);
    }

    /*
     * Freeze
     */
    public Switch getFreeze() throws EpsonProjectorCommandException, EpsonProjectorException {
        String val = queryString("FREEZE?");
        return val.equals(ON) ? Switch.ON : Switch.OFF;
    }

    public void setFreeze(Switch value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("FREEZE %s", value.name()), DEFAULT_TIMEOUT);
    }

    /*
     * Aspect Ratio
     */
    public AspectRatio getAspectRatio() throws EpsonProjectorCommandException, EpsonProjectorException {
        int val = queryHexInt("ASPECT?");
        return AspectRatio.forValue(val);
    }

    public void setAspectRatio(AspectRatio value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("ASPECT %02X", value.toInt()));
    }

    /*
     * Luminance
     */
    public Luminance getLuminance() throws EpsonProjectorCommandException, EpsonProjectorException {
        int val = queryHexInt("LUMINANCE?");
        return Luminance.forValue(val);
    }

    public void setLuminance(Luminance value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("LUMINANCE %02X", value.toInt()));
    }

    /*
     * Source
     */
    public String getSource() throws EpsonProjectorCommandException, EpsonProjectorException {
        return queryString("SOURCE?");
    }

    public void setSource(String value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("SOURCE %s", value));
    }

    /*
     * Brightness
     */
    public int getBrightness() throws EpsonProjectorCommandException, EpsonProjectorException {
        int brt = queryInt("BRIGHT?");
        for (int i = 0; i < MAP48.length; i++) {
            if (brt == MAP48[i]) {
                return i - 24;
            }
        }
        return 0;
    }

    public void setBrightness(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        value = value + 24;
        if (value >= 0 && value <= 48) {
            sendCommand(String.format("BRIGHT %d", MAP48[value]));
        }
    }

    /*
     * Contrast
     */
    public int getContrast() throws EpsonProjectorCommandException, EpsonProjectorException {
        int con = queryInt("CONTRAST?");
        for (int i = 0; i < MAP48.length; i++) {
            if (con == MAP48[i]) {
                return i - 24;
            }
        }
        return 0;
    }

    public void setContrast(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        value = value + 24;
        if (value >= 0 && value <= 48) {
            sendCommand(String.format("CONTRAST %d", MAP48[value]));
        }
    }

    /*
     * Density
     */
    public int getDensity() throws EpsonProjectorCommandException, EpsonProjectorException {
        int den = queryInt("DENSITY?");
        for (int i = 0; i < MAP64.length; i++) {
            if (den == MAP64[i]) {
                return i - 32;
            }
        }
        return 0;
    }

    public void setDensity(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        value = value + 32;
        if (value >= 0 && value <= 64) {
            sendCommand(String.format("DENSITY %d", MAP64[value]));
        }
    }

    /*
     * Tint
     */
    public int getTint() throws EpsonProjectorCommandException, EpsonProjectorException {
        int tint = queryInt("TINT?");
        for (int i = 0; i < MAP64.length; i++) {
            if (tint == MAP64[i]) {
                return i - 32;
            }
        }
        return 0;
    }

    public void setTint(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        value = value + 32;
        if (value >= 0 && value <= 64) {
            sendCommand(String.format("TINT %d", MAP64[value]));
        }
    }

    /*
     * Color Temperature
     */
    public int getColorTemperature() throws EpsonProjectorCommandException, EpsonProjectorException {
        int ctemp = queryInt("CTEMP?");
        for (int i = 0; i < MAP_COLOR_TEMP.length; i++) {
            if (ctemp == MAP_COLOR_TEMP[i]) {
                return i;
            }
        }
        return 0;
    }

    public void setColorTemperature(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        if (value >= 0 && value <= 9) {
            sendCommand(String.format("CTEMP %d", MAP_COLOR_TEMP[value]));
        }
    }

    /*
     * Flesh Color
     */
    public int getFleshColor() throws EpsonProjectorCommandException, EpsonProjectorException {
        int fclr = queryInt("FCOLOR?");
        for (int i = 0; i < MAP_FLESH_COLOR.length; i++) {
            if (fclr == MAP_FLESH_COLOR[i]) {
                return i;
            }
        }
        return 0;
    }

    public void setFleshColor(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        if (value >= 0 && value <= 6) {
            sendCommand(String.format("FCOLOR %d", MAP_FLESH_COLOR[value]));
        }
    }

    /*
     * Color Mode
     */
    public ColorMode getColorMode() throws EpsonProjectorCommandException, EpsonProjectorException {
        int val = queryHexInt("CMODE?");
        return ColorMode.forValue(val);
    }

    public void setColorMode(ColorMode value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("CMODE %02X", value.toInt()));
    }

    /*
     * Horizontal Position
     */
    public int getHorizontalPosition() throws EpsonProjectorCommandException, EpsonProjectorException {
        int hpos = queryInt("HPOS?");
        for (int i = 0; i < MAP49.length; i++) {
            if (hpos == MAP49[i]) {
                return i - 23;
            }
        }
        return 0;
    }

    public void setHorizontalPosition(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        value = value + 23;
        if (value >= 0 && value <= 49) {
            sendCommand(String.format("HPOS %d", MAP49[value]));
        }
    }

    /*
     * Vertical Position
     */
    public int getVerticalPosition() throws EpsonProjectorCommandException, EpsonProjectorException {
        int vpos = queryInt("VPOS?");
        for (int i = 0; i < MAP18.length; i++) {
            if (vpos == MAP18[i]) {
                return i - 8;
            }
        }
        return 0;
    }

    public void setVerticalPosition(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        value = value + 8;
        if (value >= 0 && value <= 18) {
            sendCommand(String.format("VPOS %d", MAP18[value]));
        }
    }

    /*
     * Gamma
     */
    public Gamma getGamma() throws EpsonProjectorCommandException, EpsonProjectorException {
        int val = queryHexInt("GAMMA?");
        return Gamma.forValue(val);
    }

    public void setGamma(Gamma value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("GAMMA %02X", value.toInt()));
    }

    /*
     * Volume
     */
    public int getVolume(int maxVolume) throws EpsonProjectorCommandException, EpsonProjectorException {
        int vol = this.queryInt("VOL?");
        switch (maxVolume) {
            case 20:
                return this.getMappingValue(MAP20, vol);
            case 40:
                return this.getMappingValue(MAP40, vol);
        }
        return 0;
    }

    private int getMappingValue(int[] map, int value) {
        for (int i = 0; i < map.length; i++) {
            if (value == map[i]) {
                return i;
            }
        }
        return 0;
    }

    public void setVolume(int value, int maxVolume) throws EpsonProjectorCommandException, EpsonProjectorException {
        if (value >= 0 && value <= maxVolume) {
            switch (maxVolume) {
                case 20:
                    this.sendCommand(String.format("VOL %d", MAP20[value]));
                    return;
                case 40:
                    this.sendCommand(String.format("VOL %d", MAP40[value]));
                    return;
            }
        }
    }

    /*
     * AV Mute
     */
    public Switch getMute() throws EpsonProjectorCommandException, EpsonProjectorException {
        String val = queryString("MUTE?");
        return val.equals(ON) ? Switch.ON : Switch.OFF;
    }

    public void setMute(Switch value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("MUTE %s", value.name()));
    }

    /*
     * Horizontal Reverse
     */
    public Switch getHorizontalReverse() throws EpsonProjectorCommandException, EpsonProjectorException {
        String val = queryString("HREVERSE?");
        return val.equals(ON) ? Switch.ON : Switch.OFF;
    }

    public void setHorizontalReverse(Switch value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("HREVERSE %s", value.name()));
    }

    /*
     * Vertical Reverse
     */
    public Switch getVerticalReverse() throws EpsonProjectorCommandException, EpsonProjectorException {
        String val = queryString("VREVERSE?");
        return val.equals(ON) ? Switch.ON : Switch.OFF;
    }

    public void setVerticalReverse(Switch value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("VREVERSE %s", value.name()));
    }

    /*
     * Background Select for AV Mute
     */
    public Background getBackground() throws EpsonProjectorCommandException, EpsonProjectorException {
        int val = queryHexInt("MSEL?");
        return Background.forValue(val);
    }

    public void setBackground(Background value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("MSEL %02X", value.toInt()));
    }

    /*
     * Lamp Time (hours) - get from cache
     */
    public int getLampTime() throws EpsonProjectorCommandException, EpsonProjectorException {
        Integer lampHours = cachedLampHours.getValue();

        if (lampHours != null) {
            return lampHours.intValue();
        } else {
            throw new EpsonProjectorCommandException("cachedLampHours returned null");
        }
    }

    /*
     * Get Lamp Time
     */
    private @Nullable Integer queryLamp() {
        try {
            return Integer.valueOf(queryInt("LAMP?"));
        } catch (EpsonProjectorCommandException | EpsonProjectorException e) {
            logger.debug("Error executing command LAMP?", e);
            return null;
        }
    }

    /*
     * Error Code
     */
    public int getError() throws EpsonProjectorCommandException, EpsonProjectorException {
        return queryHexInt("ERR?");
    }

    /*
     * Error Code Description
     */
    public String getErrorString() throws EpsonProjectorCommandException, EpsonProjectorException {
        int err = queryHexInt("ERR?");
        return ErrorMessage.forCode(err);
    }
}
