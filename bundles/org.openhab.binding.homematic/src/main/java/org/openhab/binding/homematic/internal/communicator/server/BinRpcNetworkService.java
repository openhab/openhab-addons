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
package org.openhab.binding.homematic.internal.communicator.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.BinRpcMessage;
import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;
import org.openhab.core.common.ThreadPoolManager;

/**
 * Waits for a message from the Homematic gateway and starts the RpcCallbackHandler to handle the message.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BinRpcNetworkService implements Runnable {
    private static final byte BIN_EMPTY_STRING[] = { 'B', 'i', 'n', 1, 0, 0, 0, 8, 0, 0, 0, 3, 0, 0, 0, 0 };
    private static final byte BIN_EMPTY_ARRAY[] = { 'B', 'i', 'n', 1, 0, 0, 0, 8, 0, 0, 1, 0, 0, 0, 0, 0 };
    private static final byte BIN_EMPTY_EVENT_LIST[] = { 'B', 'i', 'n', 1, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0,
            3, 0, 0, 0, 5, 'e', 'v', 'e', 'n', 't' };

    private static final String RPC_POOL_NAME = "homematicRpc";
    private ServerSocket serverSocket;
    private boolean accept = true;
    private HomematicConfig config;
    private RpcResponseHandler<byte[]> rpcResponseHandler;

    /**
     * Creates the socket for listening to events from the Homematic gateway.
     */
    public BinRpcNetworkService(RpcEventListener listener, HomematicConfig config) throws IOException {
        this.config = config;

        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(config.getBinCallbackPort()));

        this.rpcResponseHandler = new RpcResponseHandler<byte[]>(listener) {

            @Override
            protected byte[] getEmptyStringResult() {
                return BIN_EMPTY_STRING;
            }

            @Override
            protected byte[] getEmptyEventListResult() {
                return BIN_EMPTY_EVENT_LIST;
            }

            @Override
            protected byte[] getEmptyArrayResult() {
                return BIN_EMPTY_ARRAY;
            }

            @Override
            protected RpcRequest<byte[]> createRpcRequest() {
                return new BinRpcMessage(null, BinRpcMessage.TYPE.RESPONSE, config.getEncoding());
            }
        };
    }

    /**
     * Listening for events and starts the callbackHandler if an event received.
     */
    @Override
    public void run() {
        while (accept) {
            try {
                Socket cs = serverSocket.accept();
                BinRpcResponseHandler rpcHandler = new BinRpcResponseHandler(cs, rpcResponseHandler, config);
                ThreadPoolManager.getPool(RPC_POOL_NAME).execute(rpcHandler);
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    /**
     * Stops the listening.
     */
    public void shutdown() {
        accept = false;
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            // ignore
        }
    }
}
