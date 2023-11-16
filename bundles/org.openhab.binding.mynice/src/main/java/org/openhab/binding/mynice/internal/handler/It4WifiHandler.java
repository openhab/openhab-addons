/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mynice.internal.handler;

import static org.openhab.core.thing.Thing.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.config.It4WifiConfiguration;
import org.openhab.binding.mynice.internal.discovery.MyNiceDiscoveryService;
import org.openhab.binding.mynice.internal.xml.MyNiceXStream;
import org.openhab.binding.mynice.internal.xml.RequestBuilder;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.Device;
import org.openhab.binding.mynice.internal.xml.dto.Event;
import org.openhab.binding.mynice.internal.xml.dto.Response;
import org.openhab.binding.mynice.internal.xml.dto.T4Command;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link It4WifiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class It4WifiHandler extends BaseBridgeHandler {
    private static final int SERVER_PORT = 443;
    private static final int MAX_HANDSHAKE_ATTEMPTS = 3;
    private static final int KEEPALIVE_DELAY_S = 235; // Timeout seems to be at 6 min

    private final Logger logger = LoggerFactory.getLogger(It4WifiHandler.class);
    private final List<MyNiceDataListener> dataListeners = new CopyOnWriteArrayList<>();
    private final MyNiceXStream xstream = new MyNiceXStream();
    private final SSLSocketFactory socketFactory;

    private @NonNullByDefault({}) RequestBuilder reqBuilder;
    private List<Device> devices = new ArrayList<>();
    private int handshakeAttempts = 0;
    private Optional<ScheduledFuture<?>> keepAliveJob = Optional.empty();
    private Optional<It4WifiConnector> connector = Optional.empty();
    private Optional<SSLSocket> sslSocket = Optional.empty();

    public It4WifiHandler(Bridge thing, SSLSocketFactory socketFactory) {
        super(thing);
        this.socketFactory = socketFactory;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MyNiceDiscoveryService.class);
    }

    public void registerDataListener(MyNiceDataListener dataListener) {
        dataListeners.add(dataListener);
        notifyListeners(devices);
    }

    public void unregisterDataListener(MyNiceDataListener dataListener) {
        dataListeners.remove(dataListener);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            sendCommand(CommandType.INFO);
        }
    }

    @Override
    public void initialize() {
        if (getConfigAs(It4WifiConfiguration.class).username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-username");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> startConnector());
    }

    @Override
    public void dispose() {
        dataListeners.clear();

        freeKeepAlive();

        sslSocket.ifPresent(socket -> {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("Error closing sslsocket : {}", e.getMessage());
            }
        });
        sslSocket = Optional.empty();

        connector.ifPresent(c -> scheduler.execute(() -> c.interrupt()));
        connector = Optional.empty();
    }

    private void startConnector() {
        It4WifiConfiguration config = getConfigAs(It4WifiConfiguration.class);
        freeKeepAlive();
        try {
            logger.debug("Initiating connection to IT4Wifi {} on port {}...", config.hostname, SERVER_PORT);

            SSLSocket localSocket = (SSLSocket) socketFactory.createSocket(config.hostname, SERVER_PORT);
            sslSocket = Optional.of(localSocket);
            localSocket.startHandshake();

            It4WifiConnector localConnector = new It4WifiConnector(this, localSocket);
            connector = Optional.of(localConnector);
            localConnector.start();

            reqBuilder = new RequestBuilder(config.macAddress, config.username);
            handShaked();
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-hostname");
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error-handshake-init");
        }
    }

    private void freeKeepAlive() {
        keepAliveJob.ifPresent(job -> job.cancel(true));
        keepAliveJob = Optional.empty();
    }

    public void received(String command) {
        logger.debug("Received : {}", command);
        Event event = xstream.deserialize(command);
        if (event.error != null) {
            logger.warn("Error code {} received : {}", event.error.code, event.error.info);
        } else {
            if (event instanceof Response responseEvent) {
                handleResponse(responseEvent);
            } else {
                notifyListeners(event.getDevices());
            }
        }
    }

    private void handleResponse(Response response) {
        switch (response.type) {
            case PAIR:
                Configuration thingConfig = editConfiguration();
                thingConfig.put(It4WifiConfiguration.PASSWORD, response.authentication.pwd);
                updateConfiguration(thingConfig);
                logger.info("Pairing key updated in Configuration.");
                sendCommand(CommandType.VERIFY);
                return;
            case VERIFY:
                if (keepAliveJob.isEmpty()) { // means we are not connected
                    switch (response.authentication.perm) {
                        case admin, user:
                            sendCommand(CommandType.CONNECT);
                            return;
                        case wait:
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                    "@text/conf-pending-validation");
                            scheduler.schedule(() -> handShaked(), 15, TimeUnit.SECONDS);
                            return;
                    }
                }
                return;
            case CONNECT:
                String sc = response.authentication.sc;
                if (sc != null) {
                    It4WifiConfiguration config = getConfigAs(It4WifiConfiguration.class);
                    reqBuilder.setChallenges(sc, response.authentication.id, config.password);
                    keepAliveJob = Optional.of(scheduler.scheduleWithFixedDelay(() -> sendCommand(CommandType.VERIFY),
                            KEEPALIVE_DELAY_S, KEEPALIVE_DELAY_S, TimeUnit.SECONDS));
                    sendCommand(CommandType.INFO);
                }
                return;
            case INFO:
                updateStatus(ThingStatus.ONLINE);
                if (thing.getProperties().isEmpty()) {
                    updateProperties(Map.of(PROPERTY_VENDOR, response.intf.manuf, PROPERTY_MODEL_ID, response.intf.prod,
                            PROPERTY_SERIAL_NUMBER, response.intf.serialNr, PROPERTY_HARDWARE_VERSION,
                            response.intf.versionHW, PROPERTY_FIRMWARE_VERSION, response.intf.versionFW));
                }
                notifyListeners(response.getDevices());
                return;
            case STATUS:
                notifyListeners(response.getDevices());
                return;
            case CHANGE:
                logger.debug("Change command accepted");
                return;
            default:
                logger.warn("Unhandled response type : {}", response.type);
        }
    }

    public void handShaked() {
        handshakeAttempts = 0;
        It4WifiConfiguration config = getConfigAs(It4WifiConfiguration.class);
        sendCommand(config.password.isBlank() ? CommandType.PAIR : CommandType.VERIFY);
    }

    private void notifyListeners(List<Device> list) {
        devices = list;
        dataListeners.forEach(listener -> listener.onDataFetched(devices));
    }

    private void sendCommand(String command) {
        connector.ifPresentOrElse(c -> c.sendCommand(command),
                () -> logger.warn("Tried to send a command when IT4WifiConnector is not initialized."));
    }

    public void sendCommand(CommandType command) {
        sendCommand(reqBuilder.buildMessage(command));
    }

    public void sendCommand(String id, String command) {
        sendCommand(reqBuilder.buildMessage(id, command.toLowerCase()));
    }

    public void sendCommand(String id, T4Command t4) {
        sendCommand(reqBuilder.buildMessage(id, t4));
    }

    public void communicationError(String message) {
        // avoid a status update that would generates a WARN while we're already disconnecting
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            dispose();
            if (handshakeAttempts++ <= MAX_HANDSHAKE_ATTEMPTS) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
                startConnector();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error-handshake-limit");
            }
        }
    }
}
