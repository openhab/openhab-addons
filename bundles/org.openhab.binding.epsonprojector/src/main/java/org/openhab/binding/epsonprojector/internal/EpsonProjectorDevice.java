/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.StateOption;
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

    private static final String ERR = "ERR";
    private static final String IMEVENT = "IMEVENT";

    private final Logger logger = LoggerFactory.getLogger(EpsonProjectorDevice.class);

    private @Nullable ScheduledExecutorService scheduler = null;
    private @Nullable ScheduledFuture<?> timeoutJob;

    private EpsonProjectorConfiguration config;
    private EpsonProjectorConnector connection;
    private ExpiringCache<Integer> cachedLampHours = new ExpiringCache<>(Duration.ofMinutes(LAMP_REFRESH_WAIT_MINUTES),
            this::queryLamp);
    private boolean connected = false;
    private volatile boolean ready = true;

    public EpsonProjectorDevice(SerialPortManager serialPortManager, EpsonProjectorConfiguration config) {
        this.config = config;
        connection = new EpsonProjectorSerialConnector(serialPortManager, config.serialPort);
    }

    public EpsonProjectorDevice(EpsonProjectorConfiguration config) {
        this.config = config;
        connection = new EpsonProjectorTcpConnector(config.host, config.port, config.password);
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
            scheduleDelay(10);
        }

        return response;
    }

    private void scheduleDelay(int delay) {
        final ScheduledExecutorService scheduler = this.scheduler;
        if (scheduler != null) {
            ready = false;
            timeoutJob = scheduler.schedule(() -> {
                ready = true;
            }, delay, TimeUnit.SECONDS);
        }
    }

    private String splitResponse(@Nullable String response)
            throws EpsonProjectorCommandException, EpsonProjectorException {
        if (response != null && !"".equals(response)) {
            final String[] parts = response.split("=");

            if (parts.length < 2) {
                throw new EpsonProjectorCommandException("Invalid response from projector: " + response);
            }

            return parts[1].trim();
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
        String response = splitResponse(sendQuery(query, timeout));

        // if the response has two number groups, get the first one (Aspect Ratio does this)
        if (response.contains(" ")) {
            String[] subStr = response.split(" ");
            response = subStr[0];
        }

        try {
            return Integer.parseInt(response, radix);
        } catch (NumberFormatException nfe) {
            throw new EpsonProjectorCommandException(
                    "Unable to parse response '" + response + "' as Integer for command: " + query);
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
        return splitResponse(sendQuery(query, DEFAULT_TIMEOUT));
    }

    public void connect() throws EpsonProjectorException {
        connection.connect();
        connected = true;
    }

    public void disconnect() throws EpsonProjectorException {
        connection.disconnect();
        connected = false;
        ready = true;
        final ScheduledFuture<?> timeoutJob = this.timeoutJob;
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
        return PowerStatus.forValue(queryInt("PWR?"));
    }

    public void setPower(OnOffType value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("PWR %s", value.name()),
                value == OnOffType.ON ? POWER_ON_TIMEOUT : POWER_OFF_TIMEOUT);
        if (value == OnOffType.ON) {
            logger.debug("Refusing further commands for 5 seconds during POWER ON");
            scheduleDelay(5);
        }
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
        final int vkey = queryInt("VKEYSTONE?");
        for (int i = 0; i < MAP60.length; i++) {
            if (vkey == MAP60[i]) {
                return i - 30;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Vertical Keystone for response: " + vkey);
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
        final int hkey = queryInt("HKEYSTONE?");
        for (int i = 0; i < MAP60.length; i++) {
            if (hkey == MAP60[i]) {
                return i - 30;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Horizontal Keystone for response: " + hkey);
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

    public OnOffType getAutoKeystone() throws EpsonProjectorCommandException, EpsonProjectorException {
        return OnOffType.from(queryString("AUTOKEYSTONE?"));
    }

    public void setAutoKeystone(OnOffType value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("AUTOKEYSTONE %s", value.name()), DEFAULT_TIMEOUT);
    }

    /*
     * Freeze
     */
    public OnOffType getFreeze() throws EpsonProjectorCommandException, EpsonProjectorException {
        return OnOffType.from(queryString("FREEZE?"));
    }

    public void setFreeze(OnOffType value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("FREEZE %s", value.name()), DEFAULT_TIMEOUT);
    }

    /*
     * Aspect Ratio
     */
    public AspectRatio getAspectRatio() throws EpsonProjectorCommandException, EpsonProjectorException {
        return AspectRatio.forValue(queryHexInt("ASPECT?"));
    }

    public void setAspectRatio(AspectRatio value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("ASPECT %02X", value.toInt()));
    }

    /*
     * Luminance
     */
    public Luminance getLuminance() throws EpsonProjectorCommandException, EpsonProjectorException {
        return Luminance.forValue(queryHexInt("LUMINANCE?"));
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
        final int brt = queryInt("BRIGHT?");
        for (int i = 0; i < MAP48.length; i++) {
            if (brt == MAP48[i]) {
                return i - 24;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Brightness for response: " + brt);
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
        final int con = queryInt("CONTRAST?");
        for (int i = 0; i < MAP48.length; i++) {
            if (con == MAP48[i]) {
                return i - 24;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Contrast for response: " + con);
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
        final int den = queryInt("DENSITY?");
        for (int i = 0; i < MAP64.length; i++) {
            if (den == MAP64[i]) {
                return i - 32;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Density for response: " + den);
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
        final int tint = queryInt("TINT?");
        for (int i = 0; i < MAP64.length; i++) {
            if (tint == MAP64[i]) {
                return i - 32;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Tint for response: " + tint);
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
        final int ctemp = queryInt("CTEMP?");
        for (int i = 0; i < MAP_COLOR_TEMP.length; i++) {
            if (ctemp == MAP_COLOR_TEMP[i]) {
                return i;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Color Temperature for response: " + ctemp);
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
        final int fclr = queryInt("FCOLOR?");
        for (int i = 0; i < MAP_FLESH_COLOR.length; i++) {
            if (fclr == MAP_FLESH_COLOR[i]) {
                return i;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Flesh Color for response: " + fclr);
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
        return ColorMode.forValue(queryHexInt("CMODE?"));
    }

    public void setColorMode(ColorMode value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("CMODE %02X", value.toInt()));
    }

    /*
     * Horizontal Position
     */
    public int getHorizontalPosition() throws EpsonProjectorCommandException, EpsonProjectorException {
        final int hpos = queryInt("HPOS?");
        for (int i = 0; i < MAP49.length; i++) {
            if (hpos == MAP49[i]) {
                return i - 23;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Horizontal Position for response: " + hpos);
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
        final int vpos = queryInt("VPOS?");
        for (int i = 0; i < MAP18.length; i++) {
            if (vpos == MAP18[i]) {
                return i - 8;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Vertical Position for response: " + vpos);
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
        return Gamma.forValue(queryHexInt("GAMMA?"));
    }

    public void setGamma(Gamma value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("GAMMA %02X", value.toInt()));
    }

    /*
     * Volume
     */
    public int getVolume() throws EpsonProjectorCommandException, EpsonProjectorException {
        final int vol = this.queryInt("VOL?");
        switch (config.maxVolume) {
            case 20:
                return this.getMappingValue(MAP20, vol);
            case 40:
                return this.getMappingValue(MAP40, vol);
            default:
                throw new EpsonProjectorCommandException("Invalid volume range: " + config.maxVolume);
        }
    }

    private int getMappingValue(int[] map, int value) throws EpsonProjectorCommandException {
        for (int i = 0; i < map.length; i++) {
            if (value == map[i]) {
                return i;
            }
        }
        throw new EpsonProjectorCommandException("Unable to map Volume for response: " + value);
    }

    public void setVolume(int value) throws EpsonProjectorCommandException, EpsonProjectorException {
        if (value >= 0 && value <= config.maxVolume) {
            switch (config.maxVolume) {
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
    public OnOffType getMute() throws EpsonProjectorCommandException, EpsonProjectorException {
        return OnOffType.from(queryString("MUTE?"));
    }

    public void setMute(OnOffType value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("MUTE %s", value.name()));
    }

    /*
     * Horizontal Reverse
     */
    public OnOffType getHorizontalReverse() throws EpsonProjectorCommandException, EpsonProjectorException {
        return OnOffType.from(queryString("HREVERSE?"));
    }

    public void setHorizontalReverse(OnOffType value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("HREVERSE %s", value.name()));
    }

    /*
     * Vertical Reverse
     */
    public OnOffType getVerticalReverse() throws EpsonProjectorCommandException, EpsonProjectorException {
        return OnOffType.from(queryString("VREVERSE?"));
    }

    public void setVerticalReverse(OnOffType value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("VREVERSE %s", value.name()));
    }

    /*
     * Background Select for AV Mute
     */
    public Background getBackground() throws EpsonProjectorCommandException, EpsonProjectorException {
        return Background.forValue(queryHexInt("MSEL?"));
    }

    public void setBackground(Background value) throws EpsonProjectorCommandException, EpsonProjectorException {
        sendCommand(String.format("MSEL %02X", value.toInt()));
    }

    /*
     * Lamp Time (hours) - Get from cache or throw EpsonProjectorCommandException if null.
     */
    public int getLampTime() throws EpsonProjectorCommandException {
        final Integer lampHours = cachedLampHours.getValue();

        if (lampHours != null) {
            return lampHours.intValue();
        }
        throw new EpsonProjectorCommandException("cachedLampHours returned null");
    }

    /*
     * Get Lamp Time for the ExpiringCache. If command fails, swallow any exceptions and return null.
     */
    private @Nullable Integer queryLamp() {
        try {
            return queryInt("LAMP?");
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
        return ErrorMessage.forCode(queryHexInt("ERR?"));
    }

    /*
     * Source List
     */
    public List<StateOption> getSourceList() throws EpsonProjectorException {
        final List<StateOption> sourceListOptions = new ArrayList<>();

        try {
            // example: 30 HDMI1 A0 HDMI2
            final String[] sources = queryString("SOURCELIST?").split(" ");

            if (sources.length % 2 != 0) {
                logger.debug("getSourceList(): {} has odd number of elements!", Arrays.toString(sources));
            } else if (sources[0].length() != 2) {
                logger.debug("getSourceList(): {} has invalid first entry", Arrays.toString(sources));
            } else {
                IntStream.range(0, sources.length / 2)
                        .forEach(i -> sourceListOptions.add(new StateOption(sources[i * 2], sources[i * 2 + 1])));
            }
        } catch (EpsonProjectorCommandException e) {
            logger.debug("getSourceList(): {}", e.getMessage());
        }
        return sourceListOptions;
    }

    /*
     * Projector Model
     */
    public String getModel() throws EpsonProjectorCommandException, EpsonProjectorException {
        return queryString("PJINFO?");
    }

    /*
     * Projector Serial Number
     */
    public String getSerialNumber() throws EpsonProjectorCommandException, EpsonProjectorException {
        return queryString("SNO?");
    }
}
