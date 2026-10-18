/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.service.websocket;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONSCRYPT_PROVIDER;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.conscrypt.Conscrypt;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.GZIPContentDecoder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.exception.WebSocketClientServiceException;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket client service using TLS with Conscrypt.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class WebSocketTlsConscryptClientService extends AbstractWebSocketClientService {

    private static final String PSK_IDENTITY = "HCCOM_Local_App";
    private static final String TLSV_1_2 = "TLSv1.2";
    private static final String TLS_ECDHE_PSK_WITH_CHACHA_20_POLY_1305_SHA_256 = "TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256";
    private static final String TLS = "TLS";
    private static final Provider PROVIDER = Conscrypt.newProviderBuilder().setName(CONSCRYPT_PROVIDER).build();

    private final Logger logger;

    public WebSocketTlsConscryptClientService(Thing thing, URI uri, String base64EncodedKey,
            WebSocketHandler webSocketHandler, ScheduledExecutorService scheduler)
            throws WebSocketClientServiceException {
        super(thing, uri, webSocketHandler, scheduler);
        try {
            logger = LoggerFactory.getLogger(WebSocketTlsConscryptClientService.class);
            byte[] key = Base64.getUrlDecoder().decode(base64EncodedKey);

            // http client setup
            SSLContext sslContext = SSLContext.getInstance(TLS, PROVIDER);
            sslContext.init(new KeyManager[] { new ConscryptPskKeyManager(PSK_IDENTITY, key) }, new TrustManager[0],
                    new SecureRandom());

            SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
            sslContextFactory.setSslContext(sslContext);
            sslContextFactory.setIncludeCipherSuites(TLS_ECDHE_PSK_WITH_CHACHA_20_POLY_1305_SHA_256);
            sslContextFactory.setIncludeProtocols(TLSV_1_2);

            var httpClient = new HttpClient(sslContextFactory);
            httpClient.getContentDecoderFactories().add(new GZIPContentDecoder.Factory());

            // websocket
            setWebSocketClient(new WebSocketClient(httpClient));
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new WebSocketClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void send(String message) {
        try {
            var session = getSession();
            if (session != null && session.isOpen()) {
                logger.debug(">> {} ({})", message, getThingUID());
                session.getRemote().sendString(message);
            }
        } catch (IOException e) {
            logger.error("Failed to send message! error={} thingUID={}", e.getMessage(), getThingUID());
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        logger.debug("<< {} ({})", message, getThingUID());
        getWebSocketHandler().onWebSocketMessage(message, this);
    }
}
