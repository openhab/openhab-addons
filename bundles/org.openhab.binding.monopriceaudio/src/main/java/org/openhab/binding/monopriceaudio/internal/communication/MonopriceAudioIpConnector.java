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
package org.openhab.binding.monopriceaudio.internal.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with the MonopriceAudio device through a serial over IP connection
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the MonopriceAudio binding
 */
@NonNullByDefault
public class MonopriceAudioIpConnector extends MonopriceAudioConnector {

    private final Logger logger = LoggerFactory.getLogger(MonopriceAudioIpConnector.class);

    private final @Nullable String address;
    private final int port;
    private final String uid;

    private @Nullable Socket clientSocket;

    /**
     * Constructor
     *
     * @param address the IP address of the serial over IP device
     * @param port the TCP port to be used
     * @param uid the thing uid string
     * @param amp the AmplifierModel being used
     */
    public MonopriceAudioIpConnector(@Nullable String address, int port, String uid, AmplifierModel amp) {
        this.address = address;
        this.port = port;
        this.uid = uid;
        super.amp = amp;
    }

    /**
     * Default constructor for temporary connector object that gets replaced during initialize()
     */
    public MonopriceAudioIpConnector() {
        this.address = "";
        this.port = -1;
        this.uid = "";
    }

    @Override
    public synchronized void open() throws MonopriceAudioException {
        logger.debug("Opening IP connection on IP {} port {}", this.address, this.port);
        try {
            Socket clientSocket = new Socket(this.address, this.port);
            clientSocket.setSoTimeout(100);

            dataOut = new DataOutputStream(clientSocket.getOutputStream());
            dataIn = new DataInputStream(clientSocket.getInputStream());

            Thread thread = new MonopriceAudioReaderThread(this, this.uid, this.address + "." + this.port);

            setReaderThread(thread);
            thread.start();

            this.clientSocket = clientSocket;

            setConnected(true);

            logger.debug("IP connection opened");
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            setConnected(false);
            throw new MonopriceAudioException("Opening IP connection failed: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void close() {
        logger.debug("Closing IP connection");
        super.cleanup();
        Socket clientSocket = this.clientSocket;
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
            }
            this.clientSocket = null;
        }
        setConnected(false);
        logger.debug("IP connection closed");
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer.
     * In case of socket timeout, the returned value is 0.
     *
     * @param dataBuffer the buffer into which the data is read.
     *
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     *
     * @throws MonopriceAudioException - If the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     */
    @Override
    protected int readInput(byte[] dataBuffer) throws MonopriceAudioException {
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new MonopriceAudioException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (SocketTimeoutException e) {
            return 0;
        } catch (IOException e) {
            throw new MonopriceAudioException("readInput failed: " + e.getMessage(), e);
        }
    }
}
