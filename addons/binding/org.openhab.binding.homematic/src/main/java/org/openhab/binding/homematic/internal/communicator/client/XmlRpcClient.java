/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
public class XmlRpcClient extends RpcClient {
    private final static Logger logger = LoggerFactory.getLogger(XmlRpcClient.class);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (httpClient != null) {
            httpClient.destroy();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpcRequest createRpcRequest(String methodName) {
        return new XmlRpcRequest(methodName);
    }

    /**
     * Returns the XML-RPC url.
     */
    @Override
    protected String getRpcCallbackUrl() {
        return "http://" + config.getCallbackHost() + ":" + config.getCallbackPort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Object[] sendMessage(int port, RpcRequest request) throws IOException {
        try {
            if (TRACE_ENABLED) {
                logger.trace("Client XmlRpcRequest (port {}):\n{}", port, request);
            }

            BytesContentProvider content = new BytesContentProvider(request.createMessage());
            String url = String.format("http://%s:%s", config.getGatewayAddress(), port);
            ContentResponse response = httpClient.POST(url).content(content)
                    .timeout(config.getTimeout(), TimeUnit.SECONDS)
                    .header(HttpHeader.CONTENT_TYPE, "text/xml;charset=" + config.getEncoding()).send();

            String result = new String(response.getContent(), config.getEncoding());
            if (TRACE_ENABLED) {
                logger.trace("Client XmlRpcResponse (port {}):\n{}", port, result);
            }

            Object[] data = new XmlRpcResponse(new ByteArrayInputStream(result.getBytes())).getResponseData();
            return new RpcResponseParser(request).parse(data);
        } catch (UnknownRpcFailureException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }
}
