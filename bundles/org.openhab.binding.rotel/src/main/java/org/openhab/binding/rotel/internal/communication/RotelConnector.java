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
package org.openhab.binding.rotel.internal.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.protocol.RotelAbstractProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for communicating with the Rotel device
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public abstract class RotelConnector {

    private final Logger logger = LoggerFactory.getLogger(RotelConnector.class);

    private final boolean simu;
    protected final Thread readerThread;

    /** The output stream */
    protected @Nullable OutputStream dataOut;

    /** The input stream */
    protected @Nullable InputStream dataIn;

    /** true if the connection is established, false if not */
    private boolean connected;

    /**
     * Constructor
     *
     * @param protocolHandler the protocol handler
     * @param simu whether the communication is simulated or real
     * @param readerThreadName the name of thread to be created
     */
    public RotelConnector(RotelAbstractProtocolHandler protocolHandler, boolean simu, String readerThreadName) {
        this.simu = simu;
        this.readerThread = new RotelReaderThread(this, protocolHandler, readerThreadName);
    }

    /**
     * Get whether the connection is established or not
     *
     * @return true if the connection is established
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Set whether the connection is established or not
     *
     * @param connected true if the connection is established
     */
    protected void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Open the connection with the Rotel device
     *
     * @throws RotelException - In case of any problem
     */
    public abstract void open() throws RotelException;

    /**
     * Close the connection with the Rotel device
     */
    public abstract void close();

    /**
     * Stop the thread that handles the feedback messages and close the opened input and output streams
     */
    protected void cleanup() {
        readerThread.interrupt();
        try {
            readerThread.join();
        } catch (InterruptedException e) {
        }
        OutputStream dataOut = this.dataOut;
        if (dataOut != null) {
            try {
                dataOut.close();
            } catch (IOException e) {
            }
            this.dataOut = null;
        }
        InputStream dataIn = this.dataIn;
        if (dataIn != null) {
            try {
                dataIn.close();
            } catch (IOException e) {
            }
            this.dataIn = null;
        }
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer.
     *
     * @param dataBuffer the buffer into which the data is read.
     *
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     *
     * @throws RotelException - If the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     * @throws InterruptedIOException - if the thread was interrupted during the reading of the input stream
     */
    protected int readInput(byte[] dataBuffer) throws RotelException, InterruptedIOException {
        if (simu) {
            throw new RotelException("readInput failed: should not be called in simu mode");
        }
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new RotelException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (IOException e) {
            logger.debug("readInput failed: {}", e.getMessage());
            throw new RotelException("readInput failed", e);
        }
    }

    /**
     * Request the Rotel device to execute a command
     *
     * @param cmd the command to execute
     * @param dataBuffer the data buffer containing the encoded command
     *
     * @throws RotelException - In case of any problem
     */
    public void writeOutput(RotelCommand cmd, byte[] dataBuffer) throws RotelException {
        if (simu) {
            return;
        }
        OutputStream dataOut = this.dataOut;
        if (dataOut == null) {
            throw new RotelException("Send command \"" + cmd.getLabel() + "\" failed: output stream is null");
        }
        try {
            dataOut.write(dataBuffer);
            dataOut.flush();
        } catch (IOException e) {
            logger.debug("Send command \"{}\" failed: {}", cmd, e.getMessage());
            throw new RotelException("Send command \"" + cmd.getLabel() + "\" failed", e);
        }
        logger.debug("Send command \"{}\" succeeded", cmd);
    }
}
