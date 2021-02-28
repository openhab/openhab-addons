/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.benqprojector.internal.configuration.BenqProjectorConfiguration;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorConnector;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorSerialConnector;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorTcpConnector;
import org.openhab.binding.benqprojector.internal.enums.Switch;
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
    private static final int DEFAULT_TIMEOUT_MS = 5 * 1000;

    private static final String UNSUPPORTED_ITM = "Unsupported item";
    private static final String BLOCK_ITM = "Block item";
    private static final String ILLEGAL_FMT = "Illegal format";

    private final Logger logger = LoggerFactory.getLogger(BenqProjectorDevice.class);

    private BenqProjectorConnector connection;
    private boolean connected = false;

    public BenqProjectorDevice(SerialPortManager serialPortManager, BenqProjectorConfiguration config) {
        connection = new BenqProjectorSerialConnector(serialPortManager, config.serialPort);
    }

    public BenqProjectorDevice(BenqProjectorConfiguration config) {
        connection = new BenqProjectorTcpConnector(config.host, config.port);
    }

    private synchronized String sendQuery(String query, int timeout)
            throws BenqProjectorCommandException, BenqProjectorException {
        logger.debug("Query: '{}'", query);
        String response = connection.sendMessage(query, timeout);

        if (response.length() == 0) {
            throw new BenqProjectorException("No response received");
        }

        if (response.contains(UNSUPPORTED_ITM)) {
            throw new BenqProjectorCommandException("Unsupported Command response received for command: " + query);
        }

        if (response.contains(BLOCK_ITM)) {
            throw new BenqProjectorCommandException("Block Item received for command: " + query);
        }

        if (response.contains(ILLEGAL_FMT)) {
            throw new BenqProjectorCommandException("Illegal Format response received for command: " + query);
        }

        logger.debug("Response: '{}'", response);

        // example: sour=?*SOUR=HDMI2
        String[] responseParts = response.split("=");
        if (responseParts.length != 3) {
            throw new BenqProjectorCommandException("Invalid respose for command: " + query);
        }

        return responseParts[2].toLowerCase();
    }

    protected void sendCommand(String command, int timeout)
            throws BenqProjectorCommandException, BenqProjectorException {
        sendQuery(command, timeout);
    }

    protected void sendCommand(String command) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(command, DEFAULT_TIMEOUT_MS);
    }

    /*
     * protected int queryInt(String query) throws BenqProjectorCommandException, BenqProjectorException {
     * String response = sendQuery(query, DEFAULT_TIMEOUT);
     * return Integer.parseInt(response);
     * }
     */

    protected String queryString(String query) throws BenqProjectorCommandException, BenqProjectorException {
        return sendQuery(query, DEFAULT_TIMEOUT_MS);
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
        sendCommand(String.format("blank=%s", (value == Switch.ON ? "on" : "off")), DEFAULT_TIMEOUT_MS);
    }

    /*
     * Freeze
     */
    public Switch getFreeze() throws BenqProjectorCommandException, BenqProjectorException {
        return (queryString("freeze=?").contains("on") ? Switch.ON : Switch.OFF);
    }

    public void setFreeze(Switch value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("freeze=%s", (value == Switch.ON ? "on" : "off")), DEFAULT_TIMEOUT_MS);
    }

    /*
     * Direct Command
     */
    public void sendDirectCommand(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(value);
    }
}
