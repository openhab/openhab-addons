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
package org.openhab.binding.epsonprojector.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.epsonprojector.internal.connector.EpsonProjectorConnector;
import org.openhab.binding.epsonprojector.internal.connector.EpsonProjectorDefaultConnector;
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
import org.openhab.core.common.ThreadPoolManager;
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
    private static final String THING_HANDLER_THREADPOOL_NAME = "thingHandler";

    protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(THING_HANDLER_THREADPOOL_NAME);

    private static final int[] mapColorTemperature = new int[] { 0, 25, 51, 76, 102, 128, 153, 179, 204, 230 };
    private static final int[] mapFleshTemperature = new int[] { 0, 36, 73, 109, 146, 182, 219 };

    private static final int[] map64 = new int[] { 0, 3, 7, 11, 15, 19, 23, 27, 31, 35, 39, 43, 47, 51, 55, 59, 63, 66,
            70, 74, 78, 82, 86, 90, 94, 98, 102, 106, 110, 114, 118, 122, 126, 129, 133, 137, 141, 145, 149, 153, 157,
            161, 165, 169, 173, 177, 181, 185, 189, 192, 196, 200, 204, 208, 212, 216, 220, 224, 228, 232, 236, 240,
            244, 248, 252 };

    private static final int[] map60 = new int[] { 0, 4, 8, 12, 16, 20, 25, 29, 33, 37, 41, 46, 50, 54, 58, 62, 67, 71,
            75, 79, 83, 88, 92, 96, 100, 104, 109, 113, 117, 121, 125, 130, 134, 138, 142, 146, 151, 155, 159, 163, 167,
            172, 176, 180, 184, 188, 193, 197, 201, 205, 209, 214, 218, 222, 226, 230, 235, 239, 243, 247, 251 };

    private static final int[] map48 = new int[] { 0, 5, 10, 15, 20, 26, 31, 36, 41, 47, 52, 57, 62, 67, 73, 78, 83, 88,
            94, 99, 104, 109, 114, 120, 125, 130, 135, 141, 146, 151, 156, 161, 167, 172, 177, 182, 188, 193, 198, 203,
            208, 214, 219, 224, 229, 235, 240, 245, 250 };

    private static final int[] map20 = new int[] { 0, 12, 24, 36, 48, 60, 73, 85, 97, 109, 121, 134, 146, 158, 170, 182,
            195, 207, 219, 231, 243 };

    private static final int DEFAULT_TIMEOUT = 5 * 1000;
    private static final int POWER_ON_TIMEOUT = 100 * 1000;
    private static final int POWER_OFF_TIMEOUT = 130 * 1000;

    private static final String ERR = "ERR";
    private static final String ERR_INT = "255";

    private Logger logger = LoggerFactory.getLogger(EpsonProjectorDevice.class);

    private EpsonProjectorConnector connection;
    private long lastLampQry = 0;
    private int cachedLampHours = 0;
    private boolean connected = false;
    private boolean ready = false;

    public EpsonProjectorDevice(SerialPortManager serialPortManager, String serialPort) {
        connection = new EpsonProjectorSerialConnector(serialPortManager, serialPort);
        ready = true;
    }

    public EpsonProjectorDevice(String ip, int port) {
        connection = new EpsonProjectorTcpConnector(ip, port);
        ready = true;
    }

    public EpsonProjectorDevice() {
        connection = new EpsonProjectorDefaultConnector();
        ready = false;
    }

    private synchronized @Nullable String sendQuery(String query, int timeout) throws EpsonProjectorException {
        if (!ready) {
            logger.debug("Refusing command {} while not ready", query);
        }

        logger.debug("Query: '{}'", query);
        String response = connection.sendMessage(query, timeout);

        if (response.length() == 0) {
            throw new EpsonProjectorException("No response received");
        }

        response = response.replace("\r:", "");
        logger.debug("Response: '{}'", response);

        if (ERR.equals(response)) {
            logger.debug("Error response received for command: {}", query);
        }

        if ("PWR OFF".equals(query) && ":".equals(response)) {
            // When PWR OFF command is sent, next command can be send after 10 seconds after the colon is received
            logger.debug("Refusing further commands for 10 seconds to power OFF completion");
            ready = false;
            scheduler.scheduleWithFixedDelay(() -> {
                ready = true;
            }, 0, 10000, TimeUnit.MILLISECONDS);
        }

        return response;
    }

    private String splitResponse(String response) throws EpsonProjectorException {
        if (response != null && !"".equals(response)) {
            String[] pieces = response.split("=");

            if (pieces.length < 2) {
                throw new EpsonProjectorException("Invalid response from projector: " + response);
            }

            String str = pieces[1].trim();

            return str;
        } else {
            throw new EpsonProjectorException("No response received");
        }
    }

    protected void sendCommand(String command, int timeout) throws EpsonProjectorException {
        sendQuery(command, timeout);
    }

    protected void sendCommand(String command) throws EpsonProjectorException {
        sendCommand(command, DEFAULT_TIMEOUT);
    }

    protected int queryInt(String query, int timeout, int radix) throws EpsonProjectorException {
        String response = sendQuery(query, timeout);

        if (ERR.equals(response)) {
            return Integer.parseInt(ERR_INT, radix);
        }

        String str = splitResponse(response);

        // if the response has two number groups, get the first one (Aspect Ratio does this)
        if (str.contains(" ")) {
            String[] subStr = str.split(" ");
            str = subStr[0];
        }

        return Integer.parseInt(str, radix);
    }

    protected int queryInt(String query, int timeout) throws EpsonProjectorException {
        return queryInt(query, timeout, 10);
    }

    protected int queryInt(String query) throws EpsonProjectorException {
        return queryInt(query, DEFAULT_TIMEOUT, 10);
    }

    protected int queryHexInt(String query, int timeout) throws EpsonProjectorException {
        return queryInt(query, timeout, 16);
    }

    protected int queryHexInt(String query) throws EpsonProjectorException {
        return queryInt(query, DEFAULT_TIMEOUT, 16);
    }

    protected String queryString(String query) throws EpsonProjectorException {
        String response = sendQuery(query, DEFAULT_TIMEOUT);

        if (ERR.equals(response)) {
            return ERR;
        } else {
            return splitResponse(response);
        }
    }

    public void connect() throws EpsonProjectorException {
        connection.connect();
        connected = true;
    }

    public void disconnect() throws EpsonProjectorException {
        connection.disconnect();
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    /*
     * Power
     */
    public PowerStatus getPowerStatus() throws EpsonProjectorException {
        int val = queryInt("PWR?");
        PowerStatus retval = PowerStatus.forValue(val);
        return retval;
    }

    public void setPower(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("PWR %s", value.name()), value == Switch.ON ? POWER_ON_TIMEOUT : POWER_OFF_TIMEOUT);
    }

    /*
     * Key code
     */
    public void sendKeyCode(int value) throws EpsonProjectorException {
        sendCommand(String.format("KEY %02X", value));
    }

    /*
     * Vertical Keystone
     */
    public int getVerticalKeystone() throws EpsonProjectorException {
        int vkey = queryInt("VKEYSTONE?");
        for (int i = 0; i < map60.length; i++) {
            if (vkey == map60[i]) {
                return i - 30;
            }
        }
        return 0;
    }

    public void setVerticalKeystone(int value) throws EpsonProjectorException {
        value = value + 30;
        if (value >= 0 && value <= 60) {
            sendCommand(String.format("VKEYSTONE %d", map60[value]));
        }
    }

    /*
     * Horizontal Keystone
     */
    public int getHorizontalKeystone() throws EpsonProjectorException {
        int hkey = queryInt("HKEYSTONE?");
        for (int i = 0; i < map60.length; i++) {
            if (hkey == map60[i]) {
                return i - 30;
            }
        }
        return 0;
    }

    public void setHorizontalKeystone(int value) throws EpsonProjectorException {
        value = value + 30;
        if (value >= 0 && value <= 60) {
            sendCommand(String.format("HKEYSTONE %d", map60[value]));
        }
    }

    /*
     * Auto Keystone
     */

    public Switch getAutoKeystone() throws EpsonProjectorException {
        int val = queryInt("AUTOKEYSTONE?");
        return val == 0 ? Switch.OFF : Switch.ON;
    }

    public void setAutoKeystone(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("AUTOKEYSTONE %s", value.name()), DEFAULT_TIMEOUT);
    }

    /*
     * Freeze
     */
    public Switch getFreeze() throws EpsonProjectorException {
        String val = queryString("FREEZE?");
        return val.equals("OFF") ? Switch.OFF : Switch.ON;
    }

    public void setFreeze(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("FREEZE %s", value.name()), DEFAULT_TIMEOUT);
    }

    /*
     * Aspect Ratio
     */
    public AspectRatio getAspectRatio() throws EpsonProjectorException {
        int val = queryHexInt("ASPECT?");
        AspectRatio retval = AspectRatio.forValue(val);
        return retval;
    }

    public void setAspectRatio(AspectRatio value) throws EpsonProjectorException {
        sendCommand(String.format("ASPECT %02X", value.toInt()));
    }

    /*
     * Luminance
     */
    public Luminance getLuminance() throws EpsonProjectorException {
        int val = queryHexInt("LUMINANCE?");
        Luminance retval = Luminance.forValue(val);
        return retval;
    }

    public void setLuminance(Luminance value) throws EpsonProjectorException {
        sendCommand(String.format("LUMINANCE %02X", value.toInt()));
    }

    /*
     * Source
     */
    public String getSource() throws EpsonProjectorException {
        return queryString("SOURCE?");
    }

    public void setSource(String value) throws EpsonProjectorException {
        sendCommand(String.format("SOURCE %s", value));
    }

    /*
     * Brightness
     */
    public int getBrightness() throws EpsonProjectorException {
        int brt = queryInt("BRIGHT?");
        for (int i = 0; i < map48.length; i++) {
            if (brt == map48[i]) {
                return i - 24;
            }
        }
        return 0;
    }

    public void setBrightness(int value) throws EpsonProjectorException {
        value = value + 24;
        if (value >= 0 && value <= 48) {
            sendCommand(String.format("BRIGHT %d", map48[value]));
        }
    }

    /*
     * Contrast
     */
    public int getContrast() throws EpsonProjectorException {
        int con = queryInt("CONTRAST?");
        for (int i = 0; i < map48.length; i++) {
            if (con == map48[i]) {
                return i - 24;
            }
        }
        return 0;
    }

    public void setContrast(int value) throws EpsonProjectorException {
        value = value + 24;
        if (value >= 0 && value <= 48) {
            sendCommand(String.format("CONTRAST %d", map48[value]));
        }
    }

    /*
     * Density
     */
    public int getDensity() throws EpsonProjectorException {
        int den = queryInt("DENSITY?");
        for (int i = 0; i < map64.length; i++) {
            if (den == map64[i]) {
                return i - 32;
            }
        }
        return 0;
    }

    public void setDensity(int value) throws EpsonProjectorException {
        value = value + 32;
        if (value >= 0 && value <= 64) {
            sendCommand(String.format("DENSITY %d", map64[value]));
        }
    }

    /*
     * Tint
     */
    public int getTint() throws EpsonProjectorException {
        int tint = queryInt("TINT?");
        for (int i = 0; i < map64.length; i++) {
            if (tint == map64[i]) {
                return i - 32;
            }
        }
        return 0;
    }

    public void setTint(int value) throws EpsonProjectorException {
        value = value + 32;
        if (value >= 0 && value <= 64) {
            sendCommand(String.format("TINT %d", map64[value]));
        }
    }

    /*
     * Color Temperature
     */
    public int getColorTemperature() throws EpsonProjectorException {
        return queryInt("CTEMP?"); // TODO
    }

    public void setColorTemperature(int value) throws EpsonProjectorException {
        sendCommand(String.format("CTEMP %d", value)); // TODO
    }

    /*
     * Flesh Color
     */
    public int getFleshColor() throws EpsonProjectorException {
        return queryHexInt("FCOLOR?"); // TODO
    }

    public void setFleshColor(int value) throws EpsonProjectorException {
        sendCommand(String.format("FCOLOR %02X", value)); // TODO
    }

    /*
     * Color Mode
     */
    public ColorMode getColorMode() throws EpsonProjectorException {
        int val = queryHexInt("CMODE?");
        ColorMode retval = ColorMode.forValue(val);
        return retval;
    }

    public void setColorMode(ColorMode value) throws EpsonProjectorException {
        sendCommand(String.format("CMODE %02X", value.toInt()));
    }

    /*
     * Horizontal Position
     */
    public int getHorizontalPosition() throws EpsonProjectorException {
        return queryInt("HPOS?"); // TODO
    }

    public void setHorizontalPosition(int value) throws EpsonProjectorException {
        sendCommand(String.format("HPOS %d", value)); // TODO
    }

    /*
     * Vertical Position
     */
    public int getVerticalPosition() throws EpsonProjectorException {
        return queryInt("VPOS?"); // TODO
    }

    public void setVerticalPosition(int value) throws EpsonProjectorException {
        sendCommand(String.format("VPOS %d", value)); // TODO
    }

    /*
     * Gamma
     */
    public Gamma getGamma() throws EpsonProjectorException {
        int val = queryHexInt("GAMMA?");
        Gamma retval = Gamma.forValue(val);
        return retval;
    }

    public void setGamma(Gamma value) throws EpsonProjectorException {
        sendCommand(String.format("GAMMA %02X", value.toInt()));
    }

    /*
     * Volume
     */
    public int getVolume() throws EpsonProjectorException {
        int vol = queryInt("VOL?");
        for (int i = 0; i < map20.length; i++) {
            if (vol == map20[i]) {
                return i;
            }
        }
        return 0;
    }

    public void setVolume(int value) throws EpsonProjectorException {
        if (value >= 0 && value <= 20) {
            sendCommand(String.format("VOL %d", map20[value]));
        }
    }

    /*
     * AV Mute
     */
    public Switch getMute() throws EpsonProjectorException {
        String val = queryString("MUTE?");
        return val.equals("OFF") ? Switch.OFF : Switch.ON;
    }

    public void setMute(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("MUTE %s", value.name()));
    }

    /*
     * Horizontal Reverse
     */
    public Switch getHorizontalReverse() throws EpsonProjectorException {
        String val = queryString("HREVERSE?");
        return val.equals("OFF") ? Switch.OFF : Switch.ON;
    }

    public void setHorizontalReverse(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("HREVERSE %s", value.name()));
    }

    /*
     * Vertical Reverse
     */
    public Switch getVerticalReverse() throws EpsonProjectorException {
        String val = queryString("VREVERSE?");
        return val.equals("OFF") ? Switch.OFF : Switch.ON;
    }

    public void setVerticalReverse(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("VREVERSE %s", value.name()));
    }

    /*
     * Background Select for AV Mute
     */
    public Background getBackground() throws EpsonProjectorException {
        int val = queryHexInt("MSEL?");
        Background retval = Background.forValue(val);
        return retval;
    }

    public void setBackground(Background value) throws EpsonProjectorException {
        sendCommand(String.format("MSEL %02X", value.toInt()));
    }

    /*
     * Lamp Time (hours)
     */
    public int getLampTime() throws EpsonProjectorException {
        long current = System.currentTimeMillis();

        // only do lamp time query once per ~5 minute interval
        if ((current - lastLampQry) > 297000) {
            cachedLampHours = queryInt("LAMP?");
            lastLampQry = System.currentTimeMillis();
        }
        return cachedLampHours;
    }

    /*
     * Error Code
     */
    public int getError() throws EpsonProjectorException {
        return queryHexInt("ERR?");
    }

    /*
     * Error Code Description
     */
    public String getErrorString() throws EpsonProjectorException {
        int err = queryInt("ERR?");
        return ErrorMessage.forCode(err);
    }
}
