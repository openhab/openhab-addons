/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.client;

import java.io.IOException;
import java.net.Socket;

import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.BinRpcMessage;
import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;
import org.openhab.binding.homematic.internal.communicator.parser.RpcResponseParser;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client implementation for sending messages via BIN-RPC to the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BinRpcClient extends RpcClient<byte[]> {
    private final Logger logger = LoggerFactory.getLogger(BinRpcClient.class);

    private SocketHandler socketHandler;

    public BinRpcClient(HomematicConfig config) {
        super(config);
        socketHandler = new SocketHandler(config);
    }

    @Override
    public void dispose() {
        super.dispose();
        socketHandler.flush();
    }

    @Override
    protected RpcRequest<byte[]> createRpcRequest(String methodName) {
        return new BinRpcMessage(methodName, config.getEncoding());
    }

    @Override
    protected String getRpcCallbackUrl() {
        return "binary://" + config.getCallbackHost() + ":" + config.getBinCallbackPort();
    }

    @Override
    public void init(HmInterface hmInterface) throws IOException {
        super.init(hmInterface);
        socketHandler.removeSocket(config.getRpcPort(hmInterface));
    }

    /**
     * Sends a BIN-RPC message and parses the response to see if there was an error.
     */
    @Override
    protected synchronized Object[] sendMessage(int port, RpcRequest<byte[]> request) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Client BinRpcRequest:\n{}", request);
        }
        return sendMessage(port, request, 0);
    }

    /**
     * Sends the message, retries if there was an error.
     */
    private Object[] sendMessage(int port, RpcRequest<byte[]> request, int rpcRetryCounter) throws IOException {
        BinRpcMessage resp = null;
        try {
            Socket socket = socketHandler.getSocket(port);
            socket.getOutputStream().write(request.createMessage());
            resp = new BinRpcMessage(socket.getInputStream(), false, config.getEncoding());
            return new RpcResponseParser(request).parse(resp.getResponseData());
        } catch (UnknownRpcFailureException | UnknownParameterSetException rpcEx) {
            // throw immediately, don't retry the message
            throw rpcEx;
        } catch (IOException ioEx) {
            if ("init".equals(request.getMethodName()) || rpcRetryCounter >= MAX_RPC_RETRY) {
                throw ioEx;
            } else {
                rpcRetryCounter++;
                logger.debug("BinRpcMessage socket failure, sending message again {}/{}", rpcRetryCounter,
                        MAX_RPC_RETRY);
                socketHandler.removeSocket(port);
                return sendMessage(port, request, rpcRetryCounter);
            }
        } finally {
            if (logger.isTraceEnabled()) {
                logger.trace("Client BinRpcResponse:\n{}", resp == null ? "null" : resp.toString());
            }
        }
    }
}
