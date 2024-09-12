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
package org.openhab.binding.sonyprojector.internal.communication.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonyprojector.internal.SonyProjectorModel;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with Sony Projectors through a serial over IP connection
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SonyProjectorSerialOverIpConnector extends SonyProjectorSerialConnector {

    private final Logger logger = LoggerFactory.getLogger(SonyProjectorSerialOverIpConnector.class);

    private String address;
    private int port;

    private @Nullable Socket clientSocket;

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param address the IP address of the projector
     * @param port the TCP port to be used
     * @param model the projector model in use
     */
    public SonyProjectorSerialOverIpConnector(SerialPortManager serialPortManager, String address, Integer port,
            SonyProjectorModel model) {
        super(serialPortManager, "dummyPort", model);

        this.address = address;
        this.port = port;
    }

    @Override
    public synchronized void open() throws ConnectionException {
        if (!connected) {
            logger.debug("Opening serial over IP connection on IP {} port {}", this.address, this.port);
            try {
                Socket clientSocket = new Socket(this.address, this.port);
                clientSocket.setSoTimeout(100);

                dataOut = new DataOutputStream(clientSocket.getOutputStream());
                dataIn = new DataInputStream(clientSocket.getInputStream());

                this.clientSocket = clientSocket;

                connected = true;

                logger.debug("Serial over IP connection opened");
            } catch (IOException | SecurityException | IllegalArgumentException e) {
                throw new ConnectionException("@text/exception.opening-serial-over-ip-connection-failed", e);
            }
        }
    }

    @Override
    public synchronized void close() {
        if (connected) {
            logger.debug("Closing serial over IP connection");
            super.close();
            Socket clientSocket = this.clientSocket;
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                }
                this.clientSocket = null;
            }
            connected = false;
        }
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer.
     * In case of socket timeout, the returned value is 0.
     *
     * @param dataBuffer the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     * @throws CommunicationException if the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     */
    @Override
    protected int readInput(byte[] dataBuffer) throws CommunicationException {
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new CommunicationException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (SocketTimeoutException e) {
            return 0;
        } catch (IOException e) {
            logger.debug("readInput failed: {}", e.getMessage());
            throw new CommunicationException("readInput failed", e);
        }
    }
}
