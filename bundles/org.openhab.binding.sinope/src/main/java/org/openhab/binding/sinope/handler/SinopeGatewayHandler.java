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
package org.openhab.binding.sinope.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinope.SinopeBindingConstants;
import org.openhab.binding.sinope.internal.SinopeConfigStatusMessage;
import org.openhab.binding.sinope.internal.config.SinopeConfig;
import org.openhab.binding.sinope.internal.core.SinopeApiLoginAnswer;
import org.openhab.binding.sinope.internal.core.SinopeApiLoginRequest;
import org.openhab.binding.sinope.internal.core.SinopeDeviceReportAnswer;
import org.openhab.binding.sinope.internal.core.base.SinopeAnswer;
import org.openhab.binding.sinope.internal.core.base.SinopeDataAnswer;
import org.openhab.binding.sinope.internal.core.base.SinopeDataRequest;
import org.openhab.binding.sinope.internal.core.base.SinopeRequest;
import org.openhab.binding.sinope.internal.discovery.SinopeThingsDiscoveryService;
import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SinopeGatewayHandler} is responsible for handling commands for the Sinopé Gateway.
 *
 * @author Pascal Larin - Initial contribution
 */
@NonNullByDefault
public class SinopeGatewayHandler extends ConfigStatusBridgeHandler {

    private static final int FIRST_POLL_INTERVAL = 1; // In second
    private final Logger logger = LoggerFactory.getLogger(SinopeGatewayHandler.class);
    private @Nullable ScheduledFuture<?> pollFuture;
    private long refreshInterval; // In seconds
    private final List<SinopeThermostatHandler> thermostatHandlers = new CopyOnWriteArrayList<>();
    private int seq = 1;
    private @Nullable Socket clientSocket;
    private boolean searching; // In searching mode..
    private @Nullable ScheduledFuture<?> pollSearch;

    public SinopeGatewayHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Sinope Gateway");

        try {
            SinopeConfig config = getConfigAs(SinopeConfig.class);
            refreshInterval = config.refresh;
            if (config.hostname == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Gateway hostname must be set");
            } else if (config.port == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Gateway port must be set");
            } else if (config.gatewayId == null || SinopeConfig.convert(config.gatewayId) == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Gateway Id must be set");
            } else if (config.apiKey == null || SinopeConfig.convert(config.apiKey) == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Api Key must be set");
            } else if (connectToBridge()) {
                schedulePoll();
                updateStatus(ThingStatus.ONLINE);
                return;
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Can't connect to gateway. Please make sure that another instance is not connected.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warn("Unexpected error when closing connection to gateway", e);
            }
        }
    }

    synchronized void schedulePoll() {
        if (searching) {
            return;
        }
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for {} s out, then every {} s", FIRST_POLL_INTERVAL, refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(() -> poll(), FIRST_POLL_INTERVAL, refreshInterval,
                TimeUnit.SECONDS);
    }

    synchronized void stopPoll() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
    }

    private synchronized void poll() {
        if (!thermostatHandlers.isEmpty()) {
            logger.debug("Polling for state");
            try {
                if (connectToBridge()) {
                    logger.debug("Connected to bridge");
                    for (SinopeThermostatHandler sinopeThermostatHandler : thermostatHandlers) {
                        sinopeThermostatHandler.update();
                    }
                }
            } catch (IOException e) {
                setCommunicationError(true);
                logger.debug("Polling issue", e);
            }
        } else {
            logger.debug("nothing to poll");
        }
    }

    boolean connectToBridge() throws UnknownHostException, IOException {
        SinopeConfig config = getConfigAs(SinopeConfig.class);
        if (this.clientSocket == null || !this.clientSocket.isConnected() || this.clientSocket.isClosed()) {
            this.clientSocket = new Socket(config.hostname, config.port);
            SinopeApiLoginRequest loginRequest = new SinopeApiLoginRequest(SinopeConfig.convert(config.gatewayId),
                    SinopeConfig.convert(config.apiKey));
            SinopeApiLoginAnswer loginAnswer = (SinopeApiLoginAnswer) execute(loginRequest);
            setCommunicationError(false);
            return loginAnswer.getStatus() == 0;
        }
        return true;
    }

    public synchronized byte[] newSeq() {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(seq++).array();
    }

    synchronized SinopeAnswer execute(SinopeRequest command) throws UnknownHostException, IOException {
        Socket clientSocket = this.getClientSocket();
        OutputStream outToServer = clientSocket.getOutputStream();
        InputStream inputStream = clientSocket.getInputStream();
        outToServer.write(command.getPayload());
        outToServer.flush();
        return command.getReplyAnswer(inputStream);
    }

    synchronized SinopeAnswer execute(SinopeDataRequest command) throws UnknownHostException, IOException {
        Socket clientSocket = this.getClientSocket();

        OutputStream outToServer = clientSocket.getOutputStream();
        InputStream inputStream = clientSocket.getInputStream();
        if (logger.isDebugEnabled()) {
            int leftBytes = inputStream.available();
            if (leftBytes > 0) {
                logger.debug("Hum... some leftovers: {} bytes", leftBytes);
            }
        }
        outToServer.write(command.getPayload());

        SinopeDataAnswer answ = command.getReplyAnswer(inputStream);

        while (answ.getMore() == 0x01) {
            answ = command.getReplyAnswer(inputStream);
        }
        return answ;
    }

    public boolean registerThermostatHandler(SinopeThermostatHandler thermostatHandler) {
        return thermostatHandlers.add(thermostatHandler);
    }

    public boolean unregisterThermostatHandler(SinopeThermostatHandler thermostatHandler) {
        return thermostatHandlers.remove(thermostatHandler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new LinkedList<>();

        SinopeConfig config = getConfigAs(SinopeConfig.class);
        if (config.hostname == null) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SinopeBindingConstants.CONFIG_PROPERTY_HOST)
                    .withMessageKeySuffix(SinopeConfigStatusMessage.HOST_MISSING.getMessageKey())
                    .withArguments(SinopeBindingConstants.CONFIG_PROPERTY_HOST).build());
        }
        if (config.port == null) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SinopeBindingConstants.CONFIG_PROPERTY_PORT)
                    .withMessageKeySuffix(SinopeConfigStatusMessage.PORT_MISSING.getMessageKey())
                    .withArguments(SinopeBindingConstants.CONFIG_PROPERTY_PORT).build());
        }

        if (config.gatewayId == null || SinopeConfig.convert(config.gatewayId) == null) {
            configStatusMessages
                    .add(ConfigStatusMessage.Builder.error(SinopeBindingConstants.CONFIG_PROPERTY_GATEWAY_ID)
                            .withMessageKeySuffix(SinopeConfigStatusMessage.GATEWAY_ID_INVALID.getMessageKey())
                            .withArguments(SinopeBindingConstants.CONFIG_PROPERTY_GATEWAY_ID).build());
        }
        if (config.apiKey == null || SinopeConfig.convert(config.apiKey) == null) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SinopeBindingConstants.CONFIG_PROPERTY_API_KEY)
                    .withMessageKeySuffix(SinopeConfigStatusMessage.API_KEY_INVALID.getMessageKey())
                    .withArguments(SinopeBindingConstants.CONFIG_PROPERTY_API_KEY).build());
        }

        return configStatusMessages;
    }

    public void startSearch(final SinopeThingsDiscoveryService sinopeThingsDiscoveryService)
            throws UnknownHostException, IOException {
        // Stopping current polling
        stopPoll();
        this.searching = true;
        pollSearch = scheduler.schedule(() -> search(sinopeThingsDiscoveryService), FIRST_POLL_INTERVAL,
                TimeUnit.SECONDS);
    }

    private void search(final SinopeThingsDiscoveryService sinopeThingsDiscoveryService) {
        try {
            if (connectToBridge()) {
                logger.debug("Successful login");
                try {
                    while (clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed()) {
                        SinopeDeviceReportAnswer answ;
                        answ = new SinopeDeviceReportAnswer(clientSocket.getInputStream());
                        logger.debug("Got report answer: {}", answ);
                        logger.debug("Your device id is: {}", ByteUtil.toString(answ.getDeviceId()));
                        sinopeThingsDiscoveryService.newThermostat(answ.getDeviceId());
                    }
                } finally {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                        clientSocket = null;
                    }
                }
            }
        } catch (UnknownHostException e) {
            logger.warn("Unexpected error when searching for new devices", e);
        } catch (IOException e) {
            logger.debug("Network connection error, expected when ending search", e);
        } finally {
            schedulePoll();
        }
    }

    public void stopSearch() throws IOException {
        this.searching = false;
        if (this.pollSearch != null && !this.pollSearch.isCancelled()) {
            this.pollSearch.cancel(true);
            this.pollSearch = null;
        }
        if (this.clientSocket != null && this.clientSocket.isConnected()) {
            this.clientSocket.close();
            this.clientSocket = null;
        }

        schedulePoll();
    }

    public @Nullable Socket getClientSocket() throws UnknownHostException, IOException {
        if (connectToBridge()) {
            return clientSocket;
        }
        throw new IOException("Could not create a socket to the gateway. Check host/ip/gateway Id");
    }

    public void setCommunicationError(boolean hasError) {
        if (hasError) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            clientSocket = null;
        } else {
            updateStatus(ThingStatus.ONLINE);
            schedulePoll();
        }
    }
}
