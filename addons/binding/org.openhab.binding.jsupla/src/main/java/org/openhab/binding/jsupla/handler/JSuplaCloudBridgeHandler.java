/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla.handler;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.jsupla.internal.SuplaDeviceRegistry;
import org.openhab.binding.jsupla.internal.discovery.JSuplaDiscoveryService;
import org.openhab.binding.jsupla.internal.server.JSuplaChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.protocol.impl.calltypes.CallTypeParserImpl;
import pl.grzeslowski.jsupla.protocol.impl.decoders.DecoderFactoryImpl;
import pl.grzeslowski.jsupla.protocol.impl.decoders.PrimitiveDecoderImpl;
import pl.grzeslowski.jsupla.protocol.impl.encoders.EncoderFactoryImpl;
import pl.grzeslowski.jsupla.protocol.impl.encoders.PrimitiveEncoderImpl;
import pl.grzeslowski.jsupla.server.api.Channel;
import pl.grzeslowski.jsupla.server.api.Server;
import pl.grzeslowski.jsupla.server.api.ServerFactory;
import pl.grzeslowski.jsupla.server.api.ServerProperties;
import pl.grzeslowski.jsupla.server.netty.api.NettyServerFactory;

import javax.net.ssl.SSLException;
import java.math.BigDecimal;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.CONFIG_PORT;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.CONFIG_SERVER_ACCESS_ID;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.CONFIG_SERVER_ACCESS_ID_PASSWORD;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.CONNECTED_DEVICES_CHANNEL_ID;
import static pl.grzeslowski.jsupla.server.api.ServerProperties.fromList;
import static pl.grzeslowski.jsupla.server.netty.api.NettyServerFactory.PORT;
import static pl.grzeslowski.jsupla.server.netty.api.NettyServerFactory.SSL_CTX;

/**
 * @author Grzeslowski - Initial contribution
 */
public class JSuplaCloudBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(JSuplaCloudBridgeHandler.class);
    private final SuplaDeviceRegistry suplaDeviceRegistry;
    private ScheduledExecutorService scheduledPool;
    private Server server;
    private JSuplaDiscoveryService jSuplaDiscoveryService;

    private int numberOfConnectedDevices = 0;

    private int port;
    private int serverAccessId;
    private char[] serverAccessIdPassword;

    public JSuplaCloudBridgeHandler(final Bridge bridge, final SuplaDeviceRegistry suplaDeviceRegistry) {
        super(bridge);
        this.suplaDeviceRegistry = suplaDeviceRegistry;
    }

    @Override
    public void initialize() {
        updateConnectedDevices();
        scheduledPool = ThreadPoolManager.getScheduledPool(this.getClass() + "." + port);
        final ServerFactory factory = buildServerFactory();
        try {
            final Configuration config = this.getConfig();
            serverAccessId = ((BigDecimal) config.get(CONFIG_SERVER_ACCESS_ID)).intValue();
            serverAccessIdPassword = String.valueOf(((BigDecimal) config.get(CONFIG_SERVER_ACCESS_ID_PASSWORD)).intValue()).toCharArray();
            port = ((BigDecimal) config.get(CONFIG_PORT)).intValue();
            server = factory.createNewServer(buildServerProperties(port));
            server.getNewChannelsPipe().subscribe(
                    this::channelConsumer,
                    this::errorOccurredInChannel);

            logger.debug("jSuplaServer running on port {}", port);
            updateStatus(ONLINE);
        } catch (CertificateException | SSLException ex) {
            logger.error("Cannot start server!", ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "Cannot start server! " + ex.getLocalizedMessage());
        }
    }

    private void channelConsumer(Channel channel) {
        logger.debug("Device connected to {}", toString());
        changeNumberOfConnectedDevices(1);
        newChannel(channel, serverAccessId, serverAccessIdPassword);
    }

    private void errorOccurredInChannel(Throwable ex) {
        logger.error("Error occurred in server pipe", ex);
        updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Error occurred in server pipe. Message: " + ex.getLocalizedMessage());
    }

    public void completedChannel() {
        logger.debug("Device disconnected from {}", toString());
        changeNumberOfConnectedDevices(-1);
    }

    private void changeNumberOfConnectedDevices(int delta) {
        numberOfConnectedDevices += delta;
        updateConnectedDevices();
    }

    private void updateConnectedDevices() {
        updateState(CONNECTED_DEVICES_CHANNEL_ID, new DecimalType(numberOfConnectedDevices));
    }

    @SuppressWarnings("unchecked")
    private ServerFactory buildServerFactory() {
        return new NettyServerFactory(
                new CallTypeParserImpl(),
                new DecoderFactoryImpl(new PrimitiveDecoderImpl()),
                new EncoderFactoryImpl(new PrimitiveEncoderImpl()));
    }

    private ServerProperties buildServerProperties(int port)
            throws CertificateException, SSLException {
        return fromList(Arrays.asList(PORT, port, SSL_CTX, buildSslContext()));
    }

    private SslContext buildSslContext() throws CertificateException, SSLException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }

    private void newChannel(final Channel channel, int serverAccessId, char[] serverAccessIdPassword) {
        logger.debug("New channel {}", channel);
        final JSuplaChannel jSuplaChannel = new JSuplaChannel(
                this,
                serverAccessId,
                serverAccessIdPassword,
                jSuplaDiscoveryService,
                channel,
                scheduledPool,
                suplaDeviceRegistry);

        channel.getMessagePipe().subscribe(
                jSuplaChannel::onNext,
                jSuplaChannel::onError,
                jSuplaChannel::onComplete);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            server.close();
        } catch (Exception ex) {
            logger.error("Could not close server!", ex);
            updateStatus(OFFLINE, ThingStatusDetail.NONE,
                    "Could not close server! It's possible that restart of your RPi is required. " + ex.getLocalizedMessage());
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // no commands in this bridge
    }

    public void setJSuplaDiscoveryService(final JSuplaDiscoveryService jSuplaDiscoveryService) {
        logger.trace("setJSuplaDiscoveryService#{}", jSuplaDiscoveryService.hashCode());
        this.jSuplaDiscoveryService = jSuplaDiscoveryService;
    }

    @Override
    public String toString() {
        return "JSuplaCloudBridgeHandler{" +
                       "port=" + port +
                       ", serverAccessId=" + serverAccessId +
                       "} " + super.toString();
    }
}
