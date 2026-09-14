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
package org.openhab.binding.benqprojector.internal;

import static org.openhab.binding.benqprojector.internal.BenqProjectorBindingConstants.*;

import java.time.Duration;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.benqprojector.internal.configuration.BenqProjectorConfiguration;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorConnector;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorSerialConnector;
import org.openhab.binding.benqprojector.internal.connector.BenqProjectorTcpConnector;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide high level interface to BenQ projector.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class BenqProjectorDevice {
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
        final String response = connection.sendMessage(query);

        if (response.isBlank()) {
            throw new BenqProjectorException("No response received");
        }

        if (response.contains(UNSUPPORTED_ITM)) {
            throw new BenqProjectorCommandException(UNSUPPORTED_ITM + " response received for command: " + query);
        }

        logger.debug("Response: '{}'", response);

        // example: SOUR=HDMI2
        final String[] responseParts = response.split("=");
        if (responseParts.length != 2) {
            throw new BenqProjectorCommandException("Invalid response for command: " + query);
        }

        return responseParts[1].toLowerCase(Locale.ENGLISH);
    }

    protected void sendCommand(String command) throws BenqProjectorCommandException, BenqProjectorException {
        sendQuery(command);
    }

    protected int queryInt(String query) throws BenqProjectorCommandException, BenqProjectorException {
        final String response = sendQuery(query);
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
    public OnOffType getPower() throws BenqProjectorCommandException, BenqProjectorException {
        return OnOffType.from(queryString("pow=?").contains("on"));
    }

    public void setPower(OnOffType value) throws BenqProjectorCommandException, BenqProjectorException {
        if (value == OnOffType.ON) {
            sendCommand("pow=on");
        } else {
            // some projectors need the off command twice to switch off
            sendCommand("pow=off");
            sendCommand("pow=off");
        }
    }

    /*
     * Source
     */
    public String getSource() throws BenqProjectorCommandException, BenqProjectorException {
        return queryString("sour=?");
    }

    public void setSource(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("sour=%s", value));
    }

    /*
     * Picture Mode
     */
    public String getPictureMode() throws BenqProjectorCommandException, BenqProjectorException {
        return queryString("appmod=?");
    }

    public void setPictureMode(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("appmod=%s", value));
    }

    /*
     * Aspect Ratio
     */
    public String getAspectRatio() throws BenqProjectorCommandException, BenqProjectorException {
        return queryString("asp=?");
    }

    public void setAspectRatio(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("asp=%s", value));
    }

    /*
     * Blank Screen
     */
    public OnOffType getBlank() throws BenqProjectorCommandException, BenqProjectorException {
        return OnOffType.from(queryString("blank=?").contains("on"));
    }

    public void setBlank(OnOffType value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("blank=%s", (value == OnOffType.ON ? "on" : "off")));
    }

    /*
     * Freeze
     */
    public OnOffType getFreeze() throws BenqProjectorCommandException, BenqProjectorException {
        return OnOffType.from(queryString("freeze=?").contains("on"));
    }

    public void setFreeze(OnOffType value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(String.format("freeze=%s", (value == OnOffType.ON ? "on" : "off")));
    }

    /*
     * Direct Command
     */
    public void sendDirectCommand(String value) throws BenqProjectorCommandException, BenqProjectorException {
        sendCommand(value);
    }

    /*
     * Lamp Time (hours) - Get from cache or throw BenqProjectorCommandException if null.
     */
    public int getLampTime() throws BenqProjectorCommandException {
        final Integer lampHours = cachedLampHours.getValue();

        if (lampHours != null) {
            return lampHours.intValue();
        }
        throw new BenqProjectorCommandException("cachedLampHours returned null");
    }

    /*
     * Get Lamp Time for the ExpiringCache. If command fails, swallow any exceptions and return null.
     */
    private @Nullable Integer queryLamp() {
        try {
            return queryInt("ltim=?");
        } catch (BenqProjectorCommandException | BenqProjectorException e) {
            logger.debug("Error executing command ltim=?", e);
            return null;
        }
    }
}
