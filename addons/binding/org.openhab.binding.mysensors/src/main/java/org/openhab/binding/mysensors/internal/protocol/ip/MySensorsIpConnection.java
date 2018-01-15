/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.ip;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;

/**
 * Implements the TCP/IP connection to the ethernet gateway of the MySensors network.
 *
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsIpConnection extends MySensorsAbstractConnection {

    private Socket sock = null;

    public MySensorsIpConnection(MySensorsGatewayConfig myConf, MySensorsEventRegister myEventRegister) {
        super(myConf, myEventRegister);
    }

    /**
     * Tries to accomplish a TCP/IP connection via socket to ethernet gateway.
     */
    @Override
    public boolean establishConnection() {
        logger.debug("Connecting to IP bridge [{}:{}]", myGatewayConfig.getIpAddress(), myGatewayConfig.getTcpPort());

        boolean ret = false;

        if (StringUtils.isNotEmpty(myGatewayConfig.getIpAddress())) {
            try {
                sock = new Socket(myGatewayConfig.getIpAddress(), myGatewayConfig.getTcpPort());
                mysConReader = new MySensorsReader(sock.getInputStream());
                mysConWriter = new MySensorsWriter(sock.getOutputStream());

                ret = startReaderWriterThread(mysConReader, mysConWriter);
            } catch (UnknownHostException e) {
                logger.error("Error while trying to connect to: {}:{}", myGatewayConfig.getIpAddress(),
                        myGatewayConfig.getTcpPort(), e);
            } catch (IOException e) {
                logger.error("Error while trying to connect InputStreamReader", e);
            }
        } else {
            logger.error("IP must be not null/empty");
        }

        return ret;
    }

    /**
     * Ensures a clean disconnect from the TCP/IP connection to the gateway.
     */
    @Override
    public void stopConnection() {
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

    @Override
    public String toString() {
        return "MySensorsIpConnection [ipAddress=" + myGatewayConfig.getIpAddress() + ", tcpPort="
                + myGatewayConfig.getTcpPort() + "]";
    }

}
