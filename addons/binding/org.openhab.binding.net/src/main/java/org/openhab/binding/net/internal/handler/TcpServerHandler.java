/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.net.internal.handler;

import java.security.cert.CertificateException;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.net.internal.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

/**
 * The {@link TcpServerHandler} is responsible for handling TCP server thing functionality.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
public class TcpServerHandler extends AbstractServerBridge {

    private final Logger logger = LoggerFactory.getLogger(TcpServerHandler.class);

    private ServerConfiguration configuration;
    private DisposableServer server;

    public TcpServerHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Unsupported command '{}' received for channel '{}'", command, channelUID);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(ServerConfiguration.class);
        logger.debug("Using configuration: {}", configuration);

        try {
            startServer(configuration.tls);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Exception occurred during initalization: {}. ", e.getMessage(), e);
            shutdownServer();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Stopping thing");
        shutdownServer();
    }

    private void startServer(boolean tls) {
        logger.debug("Start TCP server");

        TcpServer tcpServer = TcpServer.create().port(configuration.port);

        if (tls) {
            tcpServer = tcpServer.secure(sslContextSpec -> {
                try {
                    sslContextSpec.sslContext(SecureContextBuilder.getInstance().getSslContextBuilder());
                } catch (CertificateException e) {
                    logger.warn("SSL context builder error: reason {}.", e.getMessage(), e);
                }
            });
        }

        server = tcpServer.handle((in, out) -> {
            in.receive().asByteArray().subscribe(bytes -> {
                sendData(configuration.convertTo, bytes);
            });
            return Flux.never();
        }).bind().block();
        logger.debug("TCP server started");
    }

    private void shutdownServer() {
        logger.debug("Shutdown TCP server");
        if (server != null) {
            server.disposeNow();
        }
        logger.debug("TCP server stopped");
    }

}
