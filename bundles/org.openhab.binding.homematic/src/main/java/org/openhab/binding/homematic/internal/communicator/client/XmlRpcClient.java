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
package org.openhab.binding.homematic.internal.communicator.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcRequest;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcResponse;
import org.openhab.binding.homematic.internal.communicator.parser.RpcResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client implementation for sending messages via XML-RPC to the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class XmlRpcClient extends RpcClient<String> {
    private final Logger logger = LoggerFactory.getLogger(XmlRpcClient.class);
    private HttpClient httpClient;

    public XmlRpcClient(HomematicConfig config, HttpClient httpClient) throws IOException {
        super(config);
        this.httpClient = httpClient;
    }

    @Override
    public void dispose() {
    }

    @Override
    public RpcRequest<String> createRpcRequest(String methodName) {
        return new XmlRpcRequest(methodName);
    }

    /**
     * Returns the XML-RPC url.
     */
    @Override
    protected String getRpcCallbackUrl() {
        return "http://" + config.getCallbackHost() + ":" + config.getXmlCallbackPort();
    }

    @Override
    protected synchronized Object[] sendMessage(int port, RpcRequest<String> request) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Client XmlRpcRequest (port {}):\n{}", port, request);
        }
        return sendMessage(port, request, 0);
    }

    /**
     * Sends the message, retries if there was an error.
     */
    private synchronized Object[] sendMessage(int port, RpcRequest<String> request, int rpcRetryCounter)
            throws IOException {
        try {
            BytesContentProvider content = new BytesContentProvider(
                    request.createMessage().getBytes(config.getEncoding()));
            String url = String.format("http://%s:%s", config.getGatewayAddress(), port);
            if (port == config.getGroupPort()) {
                url += "/groups";
            }
            InputStreamResponseListener respListener = new InputStreamResponseListener();
            httpClient.POST(url).content(content)
                    .header(HttpHeader.CONTENT_TYPE, "text/xml;charset=" + config.getEncoding()).send(respListener);
            Response resp = respListener.get(config.getTimeout(), TimeUnit.SECONDS);
            ByteArrayOutputStream respData = new ByteArrayOutputStream(RESP_BUFFER_SIZE);
            int httpStatus = resp.getStatus();
            if (httpStatus == HttpStatus.OK_200) {
                byte[] recvBuffer = new byte[RESP_BUFFER_SIZE];
                InputStream input = respListener.getInputStream();
                while (true) {
                    int read = input.read(recvBuffer);
                    if (read == -1) {
                        break;
                    }
                    respData.write(recvBuffer, 0, read);
                }
            } else {
                logger.warn("XmlRpcRequest failure, status code: {} / request was: {}", httpStatus, request);
                resp.abort(new Exception());
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Client XmlRpcResponse: (port {}):\n{}", port, respData.toString(config.getEncoding()));
            }
            Object[] data = new XmlRpcResponse(new ByteArrayInputStream(respData.toByteArray()), config.getEncoding())
                    .getResponseData();
            return new RpcResponseParser(request).parse(data);
        } catch (UnknownRpcFailureException | UnknownParameterSetException ex) {
            throw ex;
        } catch (Exception ex) {
            if ("init".equals(request.getMethodName()) || rpcRetryCounter >= MAX_RPC_RETRY) {
                throw new IOException(ex.getMessage(), ex);
            } else {
                rpcRetryCounter++;
                logger.debug("XmlRpcMessage failure, sending message again {}/{}", rpcRetryCounter, MAX_RPC_RETRY);
                return sendMessage(port, request, rpcRetryCounter);
            }
        }
    }
}
