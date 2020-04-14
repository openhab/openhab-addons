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
import org.openhab.binding.epsonprojector.internal.connector.EpsonProjectorSerialConnector;
import org.openhab.binding.epsonprojector.internal.connector.EpsonProjectorTcpConnector;
import org.openhab.binding.epsonprojector.internal.enums.AspectRatio;
import org.openhab.binding.epsonprojector.internal.enums.Background;
import org.openhab.binding.epsonprojector.internal.enums.Color;
import org.openhab.binding.epsonprojector.internal.enums.ColorMode;
import org.openhab.binding.epsonprojector.internal.enums.CommunicationSpeed;
import org.openhab.binding.epsonprojector.internal.enums.ErrorMessage;
import org.openhab.binding.epsonprojector.internal.enums.Gamma;
import org.openhab.binding.epsonprojector.internal.enums.GammaStep;
import org.openhab.binding.epsonprojector.internal.enums.Luminance;
import org.openhab.binding.epsonprojector.internal.enums.PowerStatus;
import org.openhab.binding.epsonprojector.internal.enums.Sharpness;
import org.openhab.binding.epsonprojector.internal.enums.Source;
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
 */
@NonNullByDefault
public class EpsonProjectorDevice {
    private static final String THING_HANDLER_THREADPOOL_NAME = "thingHandler";

    protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(THING_HANDLER_THREADPOOL_NAME);

    private static final int DEFAULT_TIMEOUT = 5 * 1000;
    private static final int POWER_ON_TIMEOUT = 100 * 1000;
    private static final int POWER_OFF_TIMEOUT = 130 * 1000;

    private Logger logger = LoggerFactory.getLogger(EpsonProjectorDevice.class);

    private EpsonProjectorConnector connection;
    private boolean connected;
    private boolean ready;

    public EpsonProjectorDevice(@Nullable SerialPortManager serialPortManager, String serialPort) {
        connection = new EpsonProjectorSerialConnector(serialPortManager, serialPort);
        ready = true;
    }

    public EpsonProjectorDevice(String ip, int port) {
        connection = new EpsonProjectorTcpConnector(ip, port);
        ready = true;
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

        if ("ERR".equals(response)) {
            throw new EpsonProjectorException("Error response received");
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

    protected void sendCommand(String command, int timeout) throws EpsonProjectorException {
        sendQuery(command, timeout);
    }

    protected void sendCommand(String command) throws EpsonProjectorException {
        sendCommand(command, DEFAULT_TIMEOUT);
    }

    protected int queryInt(String query, int timeout, int radix) throws EpsonProjectorException {
        String response = sendQuery(query, timeout);

        if (response != null && !"".equals(response)) {
            String[] pieces = response.split("=");
            String str = pieces[1].trim();

            return Integer.parseInt(str, radix);
        } else {
            throw new EpsonProjectorException("No response received");
        }
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
        return queryInt("VKEYSTONE?");
    }

    public void setVerticalKeystone(int value) throws EpsonProjectorException {
        sendCommand(String.format("VKEYSTONE %d", value));
    }

    /*
     * Horizontal Keystone
     */
    public int getHorizontalKeystone() throws EpsonProjectorException {
        return queryInt("HKEYSTONE?");
    }

    public void setHorizontalKeystone(int value) throws EpsonProjectorException {
        sendCommand(String.format("HKEYSTONE %d", value));
    }

    /*
     * Auto Keystone
     */
    public int getAutoKeystone() throws EpsonProjectorException {
        return queryInt("AUTOKEYSTONE?");
    }

    public void setAutoKeystone(int value) throws EpsonProjectorException {
        sendCommand(String.format("HKEYSTONE %d", value));
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
    public Source getSource() throws EpsonProjectorException {
        int val = queryHexInt("SOURCE?");
        Source retval = Source.forValue(val);
        return retval;
    }

    public void setSource(Source value) throws EpsonProjectorException {
        sendCommand(String.format("SOURCE %02X", value.toInt()));
    }

    public int getDirectSource() throws EpsonProjectorException {
        return queryHexInt("SOURCE?");
    }

    public void setDirectSource(int value) throws EpsonProjectorException {
        sendCommand(String.format("SOURCE %02X", value));
    }

    /*
     * Brightness
     */
    public int getBrightness() throws EpsonProjectorException {
        return queryInt("BRIGHT?");
    }

    public void setBrightness(int value) throws EpsonProjectorException {
        sendCommand(String.format("BRIGHT %d", value));
    }

    /*
     * Contrast
     */
    public int getContrast() throws EpsonProjectorException {
        return queryInt("CONTRAST?");
    }

    public void setContrast(int value) throws EpsonProjectorException {
        sendCommand(String.format("CONTRAST %d", value));
    }

    /*
     * Density
     */
    public int getDensity() throws EpsonProjectorException {
        return queryInt("DENSITY?");
    }

    public void setDensity(int value) throws EpsonProjectorException {
        sendCommand(String.format("DENSITY %d", value));
    }

    /*
     * Tint
     */
    public int getTint() throws EpsonProjectorException {
        return queryInt("TINT?");
    }

    public void setTint(int value) throws EpsonProjectorException {
        sendCommand(String.format("TINT %d", value));
    }

    /*
     * Sharpness
     */
    public int getSharpness(Sharpness sharpness) throws EpsonProjectorException {
        return queryHexInt("SHARP? %02X", sharpness.toInt());
    }

    public void setSharpness(Sharpness sharpness, int value) throws EpsonProjectorException {
        sendCommand(String.format("SHARP %d %02X", value, sharpness.toInt()));
    }

    /*
     * Color Temperature
     */
    public int getColorTemperature() throws EpsonProjectorException {
        return queryInt("CTEMP?");
    }

    public void setColorTemperature(int value) throws EpsonProjectorException {
        sendCommand(String.format("CTEMP %d", value));
    }

    /*
     * Flesh Color
     */
    public int getFleshColor() throws EpsonProjectorException {
        return queryHexInt("FCOLOR?");
    }

    public void setFleshColor(int value) throws EpsonProjectorException {
        sendCommand(String.format("FCOLOR %02X", value));
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
        return queryInt("HPOS?");
    }

    public void setHorizontalPosition(int value) throws EpsonProjectorException {
        sendCommand(String.format("HPOS %d", value));
    }

    /*
     * Vertical Position
     */
    public int getVerticalPosition() throws EpsonProjectorException {
        return queryInt("VPOS?");
    }

    public void setVerticalPosition(int value) throws EpsonProjectorException {
        sendCommand(String.format("VPOS %d", value));
    }

    /*
     * Tracking
     */
    public int getTracking() throws EpsonProjectorException {
        return queryInt("TRACKIOK?");
    }

    public void setTracking(int value) throws EpsonProjectorException {
        sendCommand(String.format("TRACKIOK %d", value));
    }

    /*
     * Sync
     */
    public int getSync() throws EpsonProjectorException {
        return queryInt("SYNC?");
    }

    public void setSync(int value) throws EpsonProjectorException {
        sendCommand(String.format("SYNC %d", value));
    }

    /*
     * Offset Red
     */
    public int getOffsetRed() throws EpsonProjectorException {
        return queryInt("OFFSETR?");
    }

    public void setOffsetRed(int value) throws EpsonProjectorException {
        sendCommand(String.format("OFFSETR %d", value));
    }

    /*
     * Offset Green
     */
    public int getOffsetGreen() throws EpsonProjectorException {
        return queryInt("OFFSETG?");
    }

    public void setOffsetGreen(int value) throws EpsonProjectorException {
        sendCommand(String.format("OFFSETG %d", value));
    }

    /*
     * Offset Blue
     */
    public int getOffsetBlue() throws EpsonProjectorException {
        return queryInt("OFFSETB?");
    }

    public void setOffsetBlue(int value) throws EpsonProjectorException {
        sendCommand(String.format("OFFSETB %d", value));
    }

    /*
     * Gain Red
     */
    public int getGainRed() throws EpsonProjectorException {
        return queryInt("GAINR?");
    }

    public void setGainRed(int value) throws EpsonProjectorException {
        sendCommand(String.format("GAINR %d", value));
    }

    /*
     * Gain Green
     */
    public int getGainGreen() throws EpsonProjectorException {
        return queryInt("GAING?");
    }

    public void setGainGreen(int value) throws EpsonProjectorException {
        sendCommand(String.format("GAING %d", value));
    }

    /*
     * Gain Blue
     */
    public int getGainBlue() throws EpsonProjectorException {
        return queryInt("GAINB?");
    }

    public void setGainBlue(int value) throws EpsonProjectorException {
        sendCommand(String.format("GAINB %d", value));
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
     * Gamma Step
     */
    public int getGammaStep(GammaStep step) throws EpsonProjectorException {
        return queryHexInt(String.format("GAMMALV? %02X", step.toInt()));
    }

    public void setGammaStep(GammaStep step, int value) throws EpsonProjectorException {
        sendCommand(String.format("GAMMALV %02X %d", step.toInt(), value));
    }

    /*
     * Color
     */
    public Color getColor() throws EpsonProjectorException {
        int val = queryHexInt("CSEL?");
        Color retval = Color.forValue(val);
        return retval;
    }

    public void setColor(Color value) throws EpsonProjectorException {
        sendCommand(String.format("CSEL %02X", value.toInt()));
    }

    /*
     * Mute
     */
    public Switch getMute() throws EpsonProjectorException {
        int val = queryInt("MUTE?");
        return val == 0 ? Switch.OFF : Switch.ON;
    }

    public void setMute(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("MUTE %s", value.name()), DEFAULT_TIMEOUT);
    }

    /*
     * Horizontal Reverse
     */
    public Switch getHorizontalReverse() throws EpsonProjectorException {
        int val = queryInt("HREVERSE?");
        return val == 0 ? Switch.OFF : Switch.ON;
    }

    public void setHorizontalReverse(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("HREVERSE %s", value.name()));
    }

    /*
     * Vertical Reverse
     */
    public Switch getVerticalReverse() throws EpsonProjectorException {
        int val = queryInt("VREVERSE?");
        return val == 0 ? Switch.OFF : Switch.ON;
    }

    public void setVerticalReverse(Switch value) throws EpsonProjectorException {
        sendCommand(String.format("VREVERSE %s", value.name()));
    }

    /*
     * Background
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
     * Speed
     */
    public CommunicationSpeed getCommunicationSpeed() throws EpsonProjectorException {
        int val = queryInt("SPEED?");
        CommunicationSpeed retval = CommunicationSpeed.forValue(val);
        return retval;
    }

    public void setCommunicationSpeed(CommunicationSpeed value) throws EpsonProjectorException {
        sendCommand(String.format("SPEED %s", value.toInt()));
    }

    /*
     * Lamp Time
     */
    public int getLampTime() throws EpsonProjectorException {
        return queryInt("LAMP?");
    }

    /*
     * Error
     */
    public int getError() throws EpsonProjectorException {
        return queryHexInt("ERR?");
    }

    /*
     * Error
     */
    public String getErrorString() throws EpsonProjectorException {
        int err = queryInt("ERR?");
        return ErrorMessage.forCode(err);
    }

}
