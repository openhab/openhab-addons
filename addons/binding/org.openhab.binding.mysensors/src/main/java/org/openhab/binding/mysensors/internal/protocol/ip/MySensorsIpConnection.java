/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.ip;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.openhab.binding.mysensors.internal.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the TCP/IP connection to the ethernet gateway of the MySensors network.
 *
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsIpConnection extends MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsIpConnection.class);

    private String ipAddress = "";
    private int tcpPort = 0;
    public int sendDelay = 0;

    private Socket sock = null;

    public MySensorsIpConnection(MySensorsBridgeHandler bridgeHandler, String ipAddress, int tcpPort, int sendDelay) {
        super(bridgeHandler);
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.sendDelay = sendDelay;
    }

    /**
     * Tries to accomplish a TCP/IP connection via socket to ethernet gateway.
     */
    @Override
    public boolean _connect() {
        logger.debug("Connecting to IP bridge [{}:{}]", ipAddress, tcpPort);

        boolean ret = false;

        if (ipAddress == null || ipAddress.isEmpty()) {
            logger.error("IP must be not null/empty");
        } else {
            try {
                sock = new Socket(ipAddress, tcpPort);
                mysConReader = new MySensorIpReader(sock.getInputStream(), this);
                mysConWriter = new MySensorsIpWriter(sock, this, sendDelay);

                ret = startReaderWriterThread(mysConReader, mysConWriter);
            } catch (UnknownHostException e) {
                logger.error("Error while trying to connect to: {}:{}", ipAddress, tcpPort);
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("Error while trying to connect InputStreamReader");
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Ensures a clean disconnect from the TCP/IP connection to the gateway.
     */
    @Override
    public void _disconnect() {
        logger.debug("Disconnecting from IP bridge ...");

        if (mysConWriter != null) {
            mysConWriter.stopWriting();
            mysConWriter = null;
        }

        if (mysConReader != null) {
            mysConReader.stopReader();
            mysConReader = null;
        }

        // Shut down socket
        try {
            if (sock != null && sock.isConnected()) {
                sock.close();
                sock = null;
            }
        } catch (IOException e) {
            logger.error("cannot disconnect from socket, message: {}", e.getMessage());
        }

    }
}
