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
package org.openhab.binding.ihc.internal.ws.http;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcTlsExecption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTTP Client for IHC / ELKO LS Controller connection purposes.
 *
 * @author Pauli Anttila - Initial contribution
 */
public abstract class IhcHttpsClient {

    private final Logger logger = LoggerFactory.getLogger(IhcHttpsClient.class);

    private IhcConnectionPool ihcConnectionPool;
    private HttpClient client;
    private HttpPost postReq;
    private AtomicInteger counter = new AtomicInteger();

    public IhcHttpsClient(IhcConnectionPool ihcConnectionPool) {
        this.ihcConnectionPool = ihcConnectionPool;
    }

    /**
     * Init HTTP connection.
     *
     * @param url Url to connect.
     */
    private void initConnection(String url) throws IhcExecption {
        if (client == null) {
            client = ihcConnectionPool.getHttpClient();
        }
        postReq = new HttpPost(url);
    }

    /**
     * Send HTTP request and wait response from the server.
     *
     */
    public String sendQuery(String url, Map<String, String> requestProperties, String query, int timeout)
            throws IhcExecption {
        initConnection(url);
        setRequestProperty(requestProperties);
        try {
            return sendQ(query, timeout);
        } catch (SSLHandshakeException e) {
            throw new IhcTlsExecption(e);
        } catch (NoHttpResponseException | SocketTimeoutException e) {
            try {
                logger.debug("No response received, resend query");
                return sendQ(query, timeout);
            } catch (IOException ee) {
                throw new IhcExecption(ee);
            }
        } catch (IOException e) {
            throw new IhcExecption(e);
        }
    }

    private String sendQ(String query, int timeout)
            throws ClientProtocolException, IOException, NoHttpResponseException {
        postReq.setEntity(new StringEntity(query, StandardCharsets.UTF_8.name()));
        postReq.addHeader("content-type", "text/xml");

        int requestId = 0;

        if (logger.isTraceEnabled()) {
            requestId = counter.getAndIncrement();
            logger.trace("Send query (url={}, connectionPool={}, clientId={} requestId={}, timeout={}, headers={}): {}",
                    postReq.getURI(), ihcConnectionPool.hashCode(), client.hashCode(), requestId, timeout,
                    postReq.getAllHeaders(), query);
        }

        final RequestConfig params = RequestConfig.custom().setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
        postReq.setConfig(params);

        // Execute POST
        LocalDateTime start = LocalDateTime.now();
        try {
            HttpResponse response = client.execute(postReq, ihcConnectionPool.getHttpContext());
            String resp = EntityUtils.toString(response.getEntity());
            if (logger.isTraceEnabled()) {
                logger.trace("Received response (connectionPool={}, clientId={} requestId={}, in {}, headers={}): {}",
                        ihcConnectionPool.hashCode(), client.hashCode(), requestId,
                        Duration.between(start, LocalDateTime.now()), response.getAllHeaders(), resp);
            }
            return resp;
        } catch (Exception e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Exception occured (connectionPool={}, clientId={} requestId={}, in {}): {}",
                        ihcConnectionPool.hashCode(), client.hashCode(), requestId,
                        Duration.between(start, LocalDateTime.now()), e.getMessage());
            }
            throw (e);
        }
    }

    /**
     * Set request properties.
     *
     */
    private void setRequestProperty(Map<String, String> requestProperties) {
        if (postReq != null && requestProperties != null) {
            requestProperties.forEach((k, v) -> postReq.setHeader(k, v));
        }
    }
}
