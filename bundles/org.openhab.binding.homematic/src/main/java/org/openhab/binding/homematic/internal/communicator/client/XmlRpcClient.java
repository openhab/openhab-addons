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
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
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
        IOException reason = new IOException();
        for (int rpcRetryCounter = 1; rpcRetryCounter <= MAX_RPC_RETRY; rpcRetryCounter++) {
            try {
                byte[] response = send(port, request);
                Object[] data = new XmlRpcResponse(new ByteArrayInputStream(response), config.getEncoding())
                        .getResponseData();
                return new RpcResponseParser(request).parse(data);
            } catch (UnknownRpcFailureException | UnknownParameterSetException ex) {
                throw ex;
            } catch (SAXException | ParserConfigurationException ex) {
                throw new IOException(ex);
            } catch (IOException ex) {
                reason = ex;
                if ("init".equals(request.getMethodName())) { // no retries for "init" request
                    break;
                }
                logger.debug("XmlRpcMessage failed, sending message again {}/{}", rpcRetryCounter, MAX_RPC_RETRY);
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
            try {
                ret = req.send().getContent();
            } catch (IllegalArgumentException e) { // Returned buffer too large
                logger.info("Blocking XmlRpcRequest failed: {}, trying non-blocking request", e.getMessage());
                InputStreamResponseListener respListener = new InputStreamResponseListener();
                req.send(respListener);
                Response resp = respListener.get(config.getTimeout(), TimeUnit.SECONDS);
                ByteArrayOutputStream respData = new ByteArrayOutputStream(RESP_BUFFER_SIZE);

                int httpStatus = resp.getStatus();
                if (httpStatus == HttpStatus.OK_200) {
                    byte[] recvBuffer = new byte[RESP_BUFFER_SIZE];
                    try (InputStream input = respListener.getInputStream()) {
                        while (true) {
                            int read = input.read(recvBuffer);
                            if (read == -1) {
                                break;
                            }
                            respData.write(recvBuffer, 0, read);
                        }
                        ret = respData.toByteArray();
                    }
                } else {
                    logger.warn("Non-blocking XmlRpcRequest failed, status code: {} / request: {}", httpStatus,
                            request);
                    resp.abort(new IOException());
                }
            }
            if (logger.isTraceEnabled()) {
                String result = new String(ret, config.getEncoding());
                logger.trace("Client XmlRpcResponse (port {}):\n{}", port, result);
            }
        } catch (UnsupportedEncodingException | ExecutionException | TimeoutException | InterruptedException e) {
            throw new IOException(e);
        }
        return ret;
    }
}
