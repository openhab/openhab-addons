/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.benqprojector.internal;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.benqprojector.internal.configuration.BenqProjectorConfiguration;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorConnector;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorSerialConnector;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorTcpConnector;
import org.openhab.binding.benqprojector.internal.enums.Switch;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide high level interface to BenQ projector.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class BenqProjectorDevice {
    private static final String UNSUPPORTED_ITM = "Unsupported item";
    private static final String BLOCK_ITM = "Block item";
    private static final String ILLEGAL_FMT = "Illegal format";

    private static final int LAMP_REFRESH_WAIT_MINUTES = 5;

    private ExpiringCache<Integer> cachedLampHours = new ExpiringCache<>(Duration.ofMinutes(LAMP_REFRESH_WAIT_MINUTES),
            this::queryLamp);

    private final Logger logger = LoggerFactory.getLogger(BenqProjectorDevice.class);

    private BenqProjectorConnector connection;
    private boolean connected = false;

    public BenqProjectorDevice(SerialPortManager serialPortManager, BenqProjectorConfiguration config) {
        connection = new BenqProjectorSerialConnector(serialPortManager, config.serialPort);
    }

    public BenqProjectorDevice(BenqProjectorConfiguration config) {
        connection = new BenqProjectorTcpConnector(config.host, config.port);
    }

    private synchronized String sendQuery(String query) throws BenqProjectorCommandException, BenqProjectorException {
        logger.debug("Query: '{}'", query);
        String response = connection.sendMessage(query);

        if (response.length() == 0) {
            throw new BenqProjectorException("No response received");
        }

        if (response.contains(UNSUPPORTED_ITM)) {
            return "UNSUPPORTED";
        }

        if (response.contains(BLOCK_ITM)) {
            throw new BenqProjectorCommandException("Block Item received for command: " + query);
        }

        if (response.contains(ILLEGAL_FMT)) {
            throw new BenqProjectorCommandException("Illegal Format response received for command: " + query);
        }

        logger.debug("Response: '{}'", response);

        // example: SOUR=HDMI2
        String[] responseParts = response.split("=");
        if (responseParts.length != 2) {
            throw new BenqProjectorCommandException("Invalid respose for command: " + query);
        }

        return responseParts[1].toLowerCase();
    }

    protected void sendCommand(String command) throws BenqProjectorCommandException, BenqProjectorException {
        sendQuery(command);
    }

    protected int queryInt(String query) throws BenqProjectorCommandException, BenqProjectorException {
        String response = sendQuery(query);
        try {
            return Integer.parseInt(response);
        } catch (NumberFormatException nfe) {
            throw new BenqProjectorCommandException(
                    "Unable to parse response '" + response + "' as Integer for command: " + query);
        }
    }

    protected String queryString(String query) throws BenqProjectorCommandException, BenqProjectorException {
        return sendQuery(query);
    }

    public void connect() throws BenqProjectorException {
        connection.connect();
        connected = true;
    }

    public void disconnect() throws BenqProjectorException {
        connection.disconnect();
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    /*
     * Power
     */
    public Switch getPowerStatus() throws BenqProjectorCommandException, BenqProjectorException {
        return (queryString("pow=?").contains("on") ? Switch.ON : Switch.OFF);
    }

    public void setPower(Switch value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(value == Switch.ON ? "pow=on" : "pow=off");
    }

    /*
     * Source
     */
    public @Nullable String getSource() throws BenqProjectorCommandException, BenqProjectorException {
        return queryString("sour=?");
    }

    public void setSource(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("sour=%s", value));
    }

    /*
     * Picture Mode
     */
    public @Nullable String getPictureMode() throws BenqProjectorCommandException, BenqProjectorException {
        return queryString("appmod=?");
    }

    public void setPictureMode(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("appmod=%s", value));
    }

    /*
     * Aspect Ratio
     */
    public @Nullable String getAspectRatio() throws BenqProjectorCommandException, BenqProjectorException {
        return queryString("asp=?");
    }

    public void setAspectRatio(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("asp=%s", value));
    }

    /*
     * Blank Screen
     */
    public Switch getBlank() throws BenqProjectorCommandException, BenqProjectorException {
        return (queryString("blank=?").contains("on") ? Switch.ON : Switch.OFF);
    }

    public void setBlank(Switch value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("blank=%s", (value == Switch.ON ? "on" : "off")));
    }

    /*
     * Freeze
     */
    public Switch getFreeze() throws BenqProjectorCommandException, BenqProjectorException {
        return (queryString("freeze=?").contains("on") ? Switch.ON : Switch.OFF);
    }

    public void setFreeze(Switch value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("freeze=%s", (value == Switch.ON ? "on" : "off")));
    }

    /*
     * Direct Command
     */
    public void sendDirectCommand(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(value);
    }

    /*
     * Lamp Time (hours) - get from cache
     */
    public int getLampTime() throws BenqProjectorCommandException, BenqProjectorException {
        Integer lampHours = cachedLampHours.getValue();

        if (lampHours != null) {
            return lampHours.intValue();
        } else {
            throw new BenqProjectorCommandException("cachedLampHours returned null");
        }
    }

    /*
     * Get Lamp Time
     */
    private @Nullable Integer queryLamp() {
        try {
            return Integer.valueOf(queryInt("ltim=?"));
        } catch (BenqProjectorCommandException | BenqProjectorException e) {
            logger.debug("Error executing command ltim=?", e);
            return null;
        }
    }
}
