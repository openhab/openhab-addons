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
package org.openhab.binding.homematic.internal.communicator.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.BinRpcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a BIN-RPC message from the socket and handles the method call.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BinRpcResponseHandler implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(BinRpcResponseHandler.class);

    private Socket socket;
    private RpcResponseHandler<byte[]> rpcResponseHandler;
    private HomematicConfig config;
    private long created;

    public BinRpcResponseHandler(Socket socket, RpcResponseHandler<byte[]> rpcResponseHandler, HomematicConfig config) {
        this.socket = socket;
        this.rpcResponseHandler = rpcResponseHandler;
        this.config = config;
        this.created = System.currentTimeMillis();
    }

    /**
     * Reads the event from the Homematic gateway and handles the method call.
     */
    @Override
    public void run() {
        try {
            boolean isMaxAliveReached;
            do {
                BinRpcMessage message = new BinRpcMessage(socket.getInputStream(), true, config.getEncoding());
                logger.trace("Event BinRpcMessage: {}", message);
                byte[] returnValue = rpcResponseHandler.handleMethodCall(message.getMethodName(),
                        message.getResponseData());
                if (returnValue != null) {
                    socket.getOutputStream().write(returnValue);
                }
                isMaxAliveReached = System.currentTimeMillis() - created > (config.getSocketMaxAlive() * 1000);
            } while (!isMaxAliveReached);

        } catch (EOFException eof) {
            // ignore
        } catch (Exception e) {
            logger.warn("{}", e.getMessage(), e);
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}
