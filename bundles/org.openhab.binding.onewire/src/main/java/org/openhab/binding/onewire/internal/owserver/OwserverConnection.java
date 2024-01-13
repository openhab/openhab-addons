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
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwserverConnection} defines the protocol for connections to owservers.
 *
 * Data is requested by using one of the read / write methods. In case of errors, an {@link OwException}
 * is thrown. All other exceptions are caught and handled.
 *
 * The data request methods follow a general pattern:
 * * build the appropriate {@link OwserverPacket} for the request
 * * call {@link #request(OwserverPacket)} to ask for the data, which then
 * * uses {@link #write(OwserverPacket)} to get the request to the server and
 * * uses {@link #read(boolean)} to get the result
 *
 * Hereby, the resulting packet is examined on an appropriate return code (!= -1) and whether the
 * expected payload is attached. If not, an {@link OwException} is thrown.
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
    private boolean tryingConnectionRecovery = false;

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
        logger.debug("Trying to (re)start OW server connection - previous state: {}",
                owserverConnectionState.toString());
        connectionErrorCounter = 0;
        tryingConnectionRecovery = true;
        boolean success = false;
        do {
            success = open();
            if (success && owserverConnectionState != OwserverConnectionState.FAILED) {
                tryingConnectionRecovery = false;
            }
        } while (!success && (owserverConnectionState != OwserverConnectionState.FAILED || tryingConnectionRecovery));
    }

    /**
     * stop the owserver connection and report new {@link OwserverConnectionState} to {@link #thingHandlerCallback}.
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
            return Arrays.stream(returnPacket.getPayloadString().split(",")).map(this::stringToSensorId)
                    .filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            throw new OwException("invalid of empty packet when requesting directory");
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
     * Errors are caught and interpreted as sensor not present.
     *
     * @param path full owfs path to sensor
     * @return OnOffType, ON=present, OFF=not present
     */
    public State checkPresence(String path) {
        State returnValue = OnOffType.OFF;
        try {
            OwserverPacket requestPacket;
            requestPacket = new OwserverPacket(OwserverMessageType.PRESENT, path, OwserverControlFlag.UNCACHED);

            OwserverPacket returnPacket = request(requestPacket);
            if (returnPacket.getReturnCode() == 0) {
                returnValue = OnOffType.ON;
            }

        } catch (OwException ignored) {
        }
        logger.trace("presence {} : {}", path, returnValue);
        return returnValue;
    }

    /**
     * read a decimal type
     *
     * @param path full owfs path to sensor
     * @return DecimalType if successful
     * @throws OwException in case an error occurs
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
            throw new OwException("invalid or empty packet when requesting decimal type");
        }

        return returnState;
    }

    /**
     * read a decimal type array
     *
     * @param path full owfs path to sensor
     * @return a List of DecimalType values if successful
     * @throws OwException in case an error occurs
     */
    public List<State> readDecimalTypeArray(String path) throws OwException {
        List<State> returnList = new ArrayList<>();
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path);
        OwserverPacket returnPacket = request(requestPacket);
        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            Arrays.stream(returnPacket.getPayloadString().split(","))
                    .forEach(v -> returnList.add(DecimalType.valueOf(v.trim())));
        } else {
            throw new OwException("invalid or empty packet when requesting decimal type array");
        }

        return returnList;
    }

    /**
     * read a string
     *
     * @param path full owfs path to sensor
     * @return requested String
     * @throws OwException in case an error occurs
     */
    public String readString(String path) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path);
        OwserverPacket returnPacket = request(requestPacket);

        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            return returnPacket.getPayloadString().trim();
        } else {
            throw new OwException("invalid or empty packet when requesting string type");
        }
    }

    /**
     * read all sensor pages
     *
     * @param path full owfs path to sensor
     * @return page buffer
     * @throws OwException in case an error occurs
     */
    public OwPageBuffer readPages(String path) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path + "/pages/page.ALL");
        OwserverPacket returnPacket = request(requestPacket);
        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            return returnPacket.getPayload();
        } else {
            throw new OwException("invalid or empty packet when requesting pages");
        }
    }

    /**
     * write a DecimalType
     *
     * @param path full owfs path to the sensor
     * @param value the value to write
     * @throws OwException in case an error occurs
     */
    public void writeDecimalType(String path, DecimalType value) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.WRITE, path);
        requestPacket.appendPayload(String.valueOf(value));

        // request method throws an OwException in case of issues...
        OwserverPacket returnPacket = request(requestPacket);

        logger.trace("wrote: {}, got: {} ", requestPacket, returnPacket);
    }

    /**
     * process a request to the owserver
     *
     * @param requestPacket the request to be send
     * @return the raw owserver answer
     * @throws OwException in case an error occurs
     */
    private OwserverPacket request(OwserverPacket requestPacket) throws OwException {
        OwserverPacket returnPacket = new OwserverPacket(OwserverPacketType.RETURN);

        // answer to value write is always empty
        boolean payloadExpected = requestPacket.getMessageType() != OwserverMessageType.WRITE;

        try {
            // write request - error may be thrown
            write(requestPacket);

            // try to read data as long as we don't get any feedback and no error is thrown...
            do {
                if (requestPacket.getMessageType() == OwserverMessageType.PRESENT
                        || requestPacket.getMessageType() == OwserverMessageType.NOP) {
                    returnPacket = read(true);
                } else {
                    returnPacket = read(false);
                }
            } while (returnPacket.isPingPacket() || returnPacket.hasPayload() != payloadExpected);

        } catch (OwException e) {
            logger.debug("failed requesting {}->{} [{}]", requestPacket, returnPacket, e.getMessage());
            throw e;
        }

        if (!returnPacket.hasControlFlag(OwserverControlFlag.PERSISTENCE)) {
            logger.trace("closing connection because persistence was denied");
            close();
        }

        // Success! Reset error counter.
        connectionErrorCounter = 0;
        return returnPacket;
    }

    /**
     * open/reopen the connection to the owserver
     *
     * In case of issues, the connection is closed using {@link #closeOnError()} and false is returned.
     * If the {@link #owserverConnectionState} is in STOPPED or FAILED, the method directly returns false.
     *
     * @return true if open
     */
    private boolean open() {
        try {
            if (owserverConnectionState == OwserverConnectionState.CLOSED || tryingConnectionRecovery) {
                // open socket & set timeout to 3000ms
                final Socket owserverSocket = new Socket(owserverAddress, owserverPort);
                owserverSocket.setSoTimeout(3000);
                this.owserverSocket = owserverSocket;

                owserverInputStream = new DataInputStream(owserverSocket.getInputStream());
                owserverOutputStream = new DataOutputStream(owserverSocket.getOutputStream());

                owserverConnectionState = OwserverConnectionState.OPENED;
                thingHandlerCallback.reportConnectionState(owserverConnectionState);

                logger.debug("OW connection state: opened to {}:{}", owserverAddress, owserverPort);
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
     * close connection and report connection state to callback
     */
    private void close() {
        this.close(true);
    }

    /**
     * close the connection to the owserver instance.
     *
     * @param reportConnectionState true, if connection state shall be reported to callback
     */
    private void close(boolean reportConnectionState) {
        final Socket owserverSocket = this.owserverSocket;
        if (owserverSocket != null) {
            try {
                owserverSocket.close();
                owserverConnectionState = OwserverConnectionState.CLOSED;
                logger.debug("closed connection");
            } catch (IOException e) {
                owserverConnectionState = OwserverConnectionState.FAILED;
                logger.warn("could not close connection: {}", e.getMessage());
            }
        }

        this.owserverSocket = null;
        this.owserverInputStream = null;
        this.owserverOutputStream = null;

        if (reportConnectionState) {
            thingHandlerCallback.reportConnectionState(owserverConnectionState);
        }
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
     * close the connection to the owserver instance after an error occured.
     * if {@link #CONNECTION_MAX_RETRY} is exceeded, {@link #owserverConnectionState} is set to FAILED
     * and state is reported to callback.
     */
    private void closeOnError() {
        connectionErrorCounter++;
        close(false);
        if (connectionErrorCounter > CONNECTION_MAX_RETRY) {
            logger.debug("OW connection state: set to failed as max retries exceeded.");
            owserverConnectionState = OwserverConnectionState.FAILED;
            tryingConnectionRecovery = false;
            thingHandlerCallback.reportConnectionState(owserverConnectionState);
        } else if (!tryingConnectionRecovery) {
            // as close did not report connections state and we are not trying to recover ...
            thingHandlerCallback.reportConnectionState(owserverConnectionState);
        }
    }

    /**
     * write to the owserver
     *
     * In case of issues, the connection is closed using {@link #closeOnError()} and an
     * {@link OwException} is thrown.
     *
     * @param requestPacket data to write
     * @throws OwException in case an error occurs
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
                    throw new OwException("I/O Error: output stream not available on write");
                }
            } else {
                // was not opened
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
     * In case of errors (which may also be due to an erroneous path), the connection is checked and potentially closed
     * using {@link #checkConnection()}.
     *
     * @param noTimeoutException retry in case of read time outs instead of exiting with an {@link OwException}.
     * @return the read packet
     * @throws OwException in case an error occurs
     */
    private OwserverPacket read(boolean noTimeoutException) throws OwException {
        OwserverPacket returnPacket = new OwserverPacket(OwserverPacketType.RETURN);
        final DataInputStream owserverInputStream = this.owserverInputStream;
        if (owserverInputStream != null) {
            DataInputStream inputStream = owserverInputStream;
            try {
                returnPacket = new OwserverPacket(inputStream, OwserverPacketType.RETURN);
            } catch (EOFException e) {
                // Read suddenly ended ....
                logger.warn("EOFException: exception while reading packet - {}", e.getMessage());
                checkConnection();
                throw new OwException("EOFException: exception while reading packet - " + e.getMessage());
            } catch (OwException e) {
                // Some other issue
                checkConnection();
                throw e;
            } catch (IOException e) {
                // Read time out
                if ("Read timed out".equals(e.getMessage()) && noTimeoutException) {
                    logger.trace("timeout - setting error code to -1");
                    // will lead to re-try reading in request method!!!
                    returnPacket.setPayload("timeout");
                    returnPacket.setReturnCode(-1);
                } else {
                    // Other I/O issue
                    checkConnection();
                    throw new OwException("I/O error: exception while reading packet - " + e.getMessage());
                }
            }
            logger.trace("read: {}", returnPacket);
        } else {
            logger.debug("input stream not available on read");
            closeOnError();
            throw new OwException("I/O Error: input stream not available on read");
        }

        return returnPacket;
    }
}
