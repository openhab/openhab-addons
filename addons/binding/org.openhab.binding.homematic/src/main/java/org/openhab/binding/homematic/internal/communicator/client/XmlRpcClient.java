/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpHeader;
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

    public XmlRpcClient(HomematicConfig config) throws IOException {
        super(config);
        httpClient = new HttpClient();
        httpClient.setConnectTimeout(config.getTimeout() * 1000L);

        try {
            httpClient.start();
        } catch (Exception ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    @Override
    public void dispose() {
        if (httpClient != null) {
            httpClient.destroy();
        }
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
            ContentResponse response = httpClient.POST(url).content(content)
                    .timeout(config.getTimeout(), TimeUnit.SECONDS)
                    .header(HttpHeader.CONTENT_TYPE, "text/xml;charset=" + config.getEncoding()).send();

            String result = new String(response.getContent(), config.getEncoding());
            if (logger.isTraceEnabled()) {
                logger.trace("Client XmlRpcResponse (port {}):\n{}", port, result);
            }

            Object[] data = new XmlRpcResponse(new ByteArrayInputStream(result.getBytes(config.getEncoding())),
                    config.getEncoding()).getResponseData();
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
