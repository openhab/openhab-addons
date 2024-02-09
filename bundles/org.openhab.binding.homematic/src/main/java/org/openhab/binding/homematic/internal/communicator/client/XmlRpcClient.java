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
package org.openhab.binding.homematic.internal.communicator.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.FutureResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcRequest;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcResponse;
import org.openhab.binding.homematic.internal.communicator.parser.RpcResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
        IOException reason = new IOException();
        for (int rpcRetryCounter = 1; rpcRetryCounter <= MAX_RPC_RETRY; rpcRetryCounter++) {
            try {
                byte[] response = send(port, request);
                if (response.length == 0 && "setInstallMode".equals(request.getMethodName())) {
                    return new Object[] {};
                }
                Object[] data = new XmlRpcResponse(new ByteArrayInputStream(response), config.getEncoding())
                        .getResponseData();
                return new RpcResponseParser(request).parse(data);
            } catch (UnknownRpcFailureException | UnknownParameterSetException ex) {
                throw ex;
            } catch (SAXException | ParserConfigurationException ex) {
                throw new IOException(ex);
            } catch (IOException ex) {
                reason = ex;
                // no retries for "init" request or if connection is refused
                if ("init".equals(request.getMethodName()) || ex.getCause() instanceof ExecutionException) {
                    break;
                }
                logger.debug("XmlRpcMessage failed({}), sending message again {}/{}", ex.getMessage(), rpcRetryCounter,
                        MAX_RPC_RETRY);
            }
        }
        throw reason;
    }

    private byte[] send(int port, RpcRequest<String> request) throws IOException {
        byte[] ret = new byte[0];
        try {
            BytesContentProvider content = new BytesContentProvider(
                    request.createMessage().getBytes(config.getEncoding()));
            String url = String.format("http://%s:%s", config.getGatewayAddress(), port);
            if (port == config.getGroupPort()) {
                url += "/groups";
            }
            Request req = httpClient.POST(url).content(content).timeout(config.getTimeout(), TimeUnit.SECONDS)
                    .header(HttpHeader.CONTENT_TYPE, "text/xml;charset=" + config.getEncoding());
            FutureResponseListener listener = new FutureResponseListener(req, config.getBufferSize() * 1024);
            req.send(listener);
            ContentResponse response = listener.get(config.getTimeout(), TimeUnit.SECONDS);
            ret = response.getContent();
            if (ret == null || ret.length == 0) {
                throw new IOException("Received no data from the Homematic gateway");
            }
            if (logger.isTraceEnabled()) {
                String result = new String(ret, config.getEncoding());
                logger.trace("Client XmlRpcResponse (port {}):\n{}", port, result);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException | IllegalArgumentException e) {
            throw new IOException(e);
        }
        return ret;
    }
}
