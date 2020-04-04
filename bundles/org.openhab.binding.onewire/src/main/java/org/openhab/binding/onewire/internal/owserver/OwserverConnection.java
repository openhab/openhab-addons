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
package org.openhab.binding.onewire.internal.owserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwserverConnection} defines the protocol for connections to owservers
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class OwserverConnection {
    public static final int DEFAULT_PORT = 4304;
    public static final int KEEPALIVE_INTERVAL = 1000;

    private static final int CONNECTION_MAX_RETRY = 5;

    private final Logger logger = LoggerFactory.getLogger(OwserverConnection.class);

    private final OwserverBridgeHandler thingHandlerCallback;
    private String owserverAddress = "";
    private int owserverPort = DEFAULT_PORT;

    private @Nullable Socket owserverSocket = null;
    private @Nullable DataInputStream owserverInputStream = null;
    private @Nullable DataOutputStream owserverOutputStream = null;
    private OwserverConnectionState owserverConnectionState = OwserverConnectionState.STOPPED;

    // reset to 0 after successful request
    private int connectionErrorCounter = 0;

    public OwserverConnection(OwserverBridgeHandler owBaseBridgeHandler) {
        this.thingHandlerCallback = owBaseBridgeHandler;
    }

    /**
     * set the owserver host address
     *
     * @param address as String (IP or FQDN), defaults to localhost
     */
    public void setHost(String address) {
        this.owserverAddress = address;
        if (owserverConnectionState != OwserverConnectionState.STOPPED) {
            close();
        }
    }

    /**
     * set the owserver port
     *
     * @param port defaults to 4304
     */
    public void setPort(int port) {
        this.owserverPort = port;
        if (owserverConnectionState != OwserverConnectionState.STOPPED) {
            close();
        }
    }

    /**
     * start the owserver connection
     */
    public void start() {
        connectionErrorCounter = 0;
        owserverConnectionState = OwserverConnectionState.CLOSED;
        boolean success = false;
        do {
            success = open();
        } while (!success && owserverConnectionState != OwserverConnectionState.FAILED);
    }

    /**
     * stop the owserver connection
     */
    public void stop() {
        close();
        owserverConnectionState = OwserverConnectionState.STOPPED;
        thingHandlerCallback.reportConnectionState(owserverConnectionState);
    }

    /**
     * list all devices on this owserver
     *
     * @return a list of device ids
     */
    public @NonNullByDefault({}) List<SensorId> getDirectory(String basePath) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.DIRALL, basePath);
        OwserverPacket returnPacket = request(requestPacket);

        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            connectionErrorCounter = 0;
            return Arrays.stream(returnPacket.getPayloadString().split(",")).map(this::stringToSensorId)
                    .filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            throw new OwException("invalid of empty packet");
        }
    }

    private @Nullable SensorId stringToSensorId(String s) {
        try {
            return new SensorId(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * check sensor presence
     *
     * @param path full owfs path to sensor
     * @return OnOffType, ON=present, OFF=not present
     * @throws OwException
     */
    public State checkPresence(String path) throws OwException {
        State returnValue = OnOffType.OFF;
        try {
            OwserverPacket requestPacket;
            requestPacket = new OwserverPacket(OwserverMessageType.PRESENT, path, OwserverControlFlag.UNCACHED);

            OwserverPacket returnPacket = request(requestPacket);
            if (returnPacket.getReturnCode() == 0) {
                returnValue = OnOffType.ON;
            }

        } catch (OwException e) {
            returnValue = OnOffType.OFF;
        }
        logger.trace("presence {} : {}", path, returnValue);
        return returnValue;
    }

    /**
     * read a decimal type
     *
     * @param path full owfs path to sensor
     * @return DecimalType if successful
     * @throws OwException
     */
    public State readDecimalType(String path) throws OwException {
        State returnState = UnDefType.UNDEF;
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path);

        OwserverPacket returnPacket = request(requestPacket);
        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            try {
                returnState = DecimalType.valueOf(returnPacket.getPayloadString().trim());
            } catch (NumberFormatException e) {
                throw new OwException("could not parse '" + returnPacket.getPayloadString().trim() + "' to a number");
            }
        } else {
            throw new OwException("invalid or empty packet");
        }

        return returnState;
    }

    /**
     * read a decimal type array
     *
     * @param path full owfs path to sensor
     * @return a List of DecimalType values if successful
     * @throws OwException
     */
    public List<State> readDecimalTypeArray(String path) throws OwException {
        List<State> returnList = new ArrayList<>();
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path);
        OwserverPacket returnPacket = request(requestPacket);
        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            Arrays.stream(returnPacket.getPayloadString().split(","))
                    .forEach(v -> returnList.add(DecimalType.valueOf(v.trim())));
        } else {
            throw new OwException("invalid or empty packet");
        }

        return returnList;
    }

    /**
     * read a string
     *
     * @param path full owfs path to sensor
     * @return requested String
     * @throws OwException
     */
    public String readString(String path) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path);
        OwserverPacket returnPacket = request(requestPacket);

        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            return returnPacket.getPayloadString().trim();
        } else {
            throw new OwException("invalid or empty packet");
        }
    }

    /**
     * read all sensor pages
     *
     * @param path full owfs path to sensor
     * @return page buffer
     * @throws OwException
     */
    public OwPageBuffer readPages(String path) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path + "/pages/page.ALL");
        OwserverPacket returnPacket = request(requestPacket);
        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            return returnPacket.getPayload();
        } else {
            throw new OwException("invalid or empty packet");
        }
    }

    /**
     * write a DecimalType
     *
     * @param path full owfs path to the sensor
     * @param value the value to write
     * @throws OwException
     */
    public void writeDecimalType(String path, DecimalType value) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.WRITE, path);
        requestPacket.appendPayload(String.valueOf(value));

        OwserverPacket returnPacket = request(requestPacket);

        logger.trace("wrote: {}, got: {} ", requestPacket, returnPacket);
    }

    /**
     * process a request to the owserver
     *
     * @param requestPacket the request to be send
     * @return the raw owserver answer
     * @throws OwException
     */
    private OwserverPacket request(OwserverPacket requestPacket) throws OwException {
        OwserverPacket returnPacket = new OwserverPacket(OwserverPacketType.RETURN);
        // answer to value write is always empty
        boolean payloadExpected = requestPacket.getMessageType() != OwserverMessageType.WRITE;
        try {
            write(requestPacket);
            do {
                if (requestPacket.getMessageType() == OwserverMessageType.PRESENT
                        || requestPacket.getMessageType() == OwserverMessageType.NOP) {
                    returnPacket = read(true);
                } else {
                    returnPacket = read(false);
                }
            } while (returnPacket.isPingPacket() || !(returnPacket.hasPayload() == payloadExpected));
        } catch (OwException e) {
            logger.debug("failed requesting {}->{} [{}]", requestPacket, returnPacket, e.getMessage());
            throw e;
        }

        if (!returnPacket.hasControlFlag(OwserverControlFlag.PERSISTENCE)) {
            logger.trace("closing connection because persistence was denied");
            close();
        }

        connectionErrorCounter = 0;
        return returnPacket;
    }

    /**
     * open/reopen the connection to the owserver
     *
     * @return true if open
     */
    private boolean open() {
        try {
            if (owserverConnectionState == OwserverConnectionState.CLOSED) {
                // open socket & set timeout to 3000ms
                final Socket owserverSocket = new Socket(owserverAddress, owserverPort);
                owserverSocket.setSoTimeout(3000);
                this.owserverSocket = owserverSocket;

                owserverInputStream = new DataInputStream(owserverSocket.getInputStream());
                owserverOutputStream = new DataOutputStream(owserverSocket.getOutputStream());

                owserverConnectionState = OwserverConnectionState.OPENED;
                thingHandlerCallback.reportConnectionState(owserverConnectionState);

                logger.debug("opened OwServerConnection to {}:{}", owserverAddress, owserverPort);
                return true;
            } else if (owserverConnectionState == OwserverConnectionState.OPENED) {
                // socket already open, clear input buffer
                logger.trace("owServerConnection already open, skipping input buffer");
                final DataInputStream owserverInputStream = this.owserverInputStream;
                while (owserverInputStream != null) {
                    if (owserverInputStream.skip(owserverInputStream.available()) == 0) {
                        return true;
                    }
                }
                logger.debug("input stream not available on skipping");
                closeOnError();
                return false;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.debug("could not open owServerConnection to {}:{}: {}", owserverAddress, owserverPort,
                    e.getMessage());
            closeOnError();
            return false;
        }
    }

    /**
     * close the connection to the owserver instance
     */
    private void close() {
        final Socket owserverSocket = this.owserverSocket;
        if (owserverSocket != null) {
            try {
                owserverSocket.close();
            } catch (IOException e) {
                owserverConnectionState = OwserverConnectionState.FAILED;
                logger.warn("could not close connection: {}", e.getMessage());
            }
        }

        this.owserverSocket = null;
        this.owserverInputStream = null;
        this.owserverOutputStream = null;

        logger.debug("closed connection");
        owserverConnectionState = OwserverConnectionState.CLOSED;

        thingHandlerCallback.reportConnectionState(owserverConnectionState);
    }

    /**
     * check if the connection is dead and close it
     */
    private void checkConnection() {
        try {
            int pid = ((DecimalType) readDecimalType("/system/process/pid")).intValue();
            logger.debug("read pid {} -> connection still alive", pid);
            return;
        } catch (OwException e) {
            closeOnError();
        }
    }

    /**
     * close the connection to the owserver instance after an error occured
     */
    private void closeOnError() {
        connectionErrorCounter++;
        close();
        if (connectionErrorCounter > CONNECTION_MAX_RETRY) {
            owserverConnectionState = OwserverConnectionState.FAILED;
            thingHandlerCallback.reportConnectionState(owserverConnectionState);
        }
    }

    /**
     * write to the owserver
     *
     * @param requestPacket data to write
     * @throws OwException
     */
    private void write(OwserverPacket requestPacket) throws OwException {
        try {
            if (open()) {
                requestPacket.setControlFlags(OwserverControlFlag.PERSISTENCE);
                final DataOutputStream owserverOutputStream = this.owserverOutputStream;
                if (owserverOutputStream != null) {
                    owserverOutputStream.write(requestPacket.toBytes());
                    logger.trace("wrote: {}", requestPacket);
                } else {
                    logger.debug("output stream not available on write");
                    closeOnError();
                }
            } else {
                throw new OwException("I/O error: could not open connection to send request packet");
            }
        } catch (IOException e) {
            closeOnError();
            logger.debug("couldn't send {}, {}", requestPacket, e.getMessage());
            throw new OwException("I/O Error: exception while sending request packet - " + e.getMessage());
        }
    }

    /**
     * read from owserver
     *
     * @return the read packet
     * @throws OwException
     */
    private OwserverPacket read(boolean noTimeoutException) throws OwException {
        OwserverPacket returnPacket = new OwserverPacket(OwserverPacketType.RETURN);
        try {
            final DataInputStream owserverInputStream = this.owserverInputStream;
            if (owserverInputStream != null) {
                DataInputStream inputStream = owserverInputStream;
                returnPacket = new OwserverPacket(inputStream, OwserverPacketType.RETURN);
                logger.trace("read: {}", returnPacket);
            } else {
                logger.debug("input stream not available on read");
                closeOnError();
            }
        } catch (EOFException e) {
            // nothing to read
        } catch (OwException e) {
            checkConnection();
            throw e;
        } catch (IOException e) {
            if (e.getMessage().equals("Read timed out") && noTimeoutException) {
                logger.trace("timeout - setting error code to -1");
                returnPacket.setPayload("timeout");
                returnPacket.setReturnCode(-1);
            } else {
                checkConnection();
                throw new OwException("I/O error: exception while reading packet - " + e.getMessage());
            }
        }

        return returnPacket;
    }

}
