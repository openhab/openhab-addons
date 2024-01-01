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
package org.openhab.binding.ecovacs.internal.api.impl;

import java.security.KeyStore;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.commands.GetCleanLogsCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetFirmwareVersionCommand;
import org.openhab.binding.ecovacs.internal.api.commands.IotDeviceCommand;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.Device;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalLoginResponse;
import org.openhab.binding.ecovacs.internal.api.model.CleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import io.netty.handler.ssl.util.SimpleTrustManagerFactory;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcovacsIotMqDevice implements EcovacsDevice {
    private final Logger logger = LoggerFactory.getLogger(EcovacsIotMqDevice.class);

    private final Device device;
    private final DeviceDescription desc;
    private final EcovacsApiImpl api;
    private final Gson gson;
    private @Nullable Mqtt3AsyncClient mqttClient;

    EcovacsIotMqDevice(Device device, DeviceDescription desc, EcovacsApiImpl api, Gson gson)
            throws EcovacsApiException {
        this.device = device;
        this.desc = desc;
        this.api = api;
        this.gson = gson;
    }

    @Override
    public String getSerialNumber() {
        return device.getName();
    }

    @Override
    public String getModelName() {
        return desc.modelName;
    }

    @Override
    public boolean hasCapability(DeviceCapability cap) {
        return desc.capabilities.contains(cap);
    }

    @Override
    public <T> T sendCommand(IotDeviceCommand<T> command) throws EcovacsApiException, InterruptedException {
        return api.sendIotCommand(device, desc, command);
    }

    @Override
    public List<CleanLogRecord> getCleanLogs() throws EcovacsApiException, InterruptedException {
        Stream<CleanLogRecord> logEntries;
        if (desc.protoVersion == ProtocolVersion.XML) {
            logEntries = sendCommand(new GetCleanLogsCommand()).stream();
        } else {
            logEntries = api.fetchCleanLogs(device).stream().map(record -> new CleanLogRecord(record.timestamp,
                    record.duration, record.area, Optional.ofNullable(record.imageUrl), record.type));
        }
        return logEntries.sorted((lhs, rhs) -> rhs.timestamp.compareTo(lhs.timestamp)).collect(Collectors.toList());
    }

    @Override
    public void connect(final EventListener listener, ScheduledExecutorService scheduler)
            throws EcovacsApiException, InterruptedException {
        EcovacsApiConfiguration config = api.getConfig();
        PortalLoginResponse loginData = api.getLoginData();
        if (loginData == null) {
            throw new EcovacsApiException("Can not connect when not logged in");
        }

        // XML message handler does not receive firmware version information with events, so fetch in advance
        if (desc.protoVersion == ProtocolVersion.XML) {
            listener.onFirmwareVersionChanged(this, sendCommand(new GetFirmwareVersionCommand()));
        }

        String userName = String.format("%s@%s", loginData.getUserId(), config.getRealm().split("\\.")[0]);
        String host = String.format("mq-%s.%s", config.getContinent(), config.getRealm());

        Mqtt3SimpleAuth auth = Mqtt3SimpleAuth.builder().username(userName).password(loginData.getToken().getBytes())
                .build();

        MqttClientSslConfig sslConfig = MqttClientSslConfig.builder().trustManagerFactory(createTrustManagerFactory())
                .build();

        final MqttClientDisconnectedListener disconnectListener = ctx -> {
            boolean expectedShutdown = ctx.getSource() == MqttDisconnectSource.USER
                    && ctx.getCause() instanceof Mqtt3DisconnectException;
            // As the client already was disconnected, there's no need to do it again in disconnect() later
            this.mqttClient = null;
            if (!expectedShutdown) {
                logger.debug("{}: MQTT disconnected (source {}): {}", getSerialNumber(), ctx.getSource(),
                        ctx.getCause().getMessage());
                listener.onEventStreamFailure(EcovacsIotMqDevice.this, ctx.getCause());
            }
        };

        final Mqtt3AsyncClient client = MqttClient.builder().useMqttVersion3()
                .identifier(userName + "/" + loginData.getResource()).simpleAuth(auth).serverHost(host).serverPort(8883)
                .sslConfig(sslConfig).addDisconnectedListener(disconnectListener).buildAsync();

        try {
            this.mqttClient = client;
            client.connect().get();

            final ReportParser parser = desc.protoVersion == ProtocolVersion.XML
                    ? new XmlReportParser(this, listener, gson, logger)
                    : new JsonReportParser(this, listener, desc.protoVersion, gson, logger);
            final Consumer<@Nullable Mqtt3Publish> eventCallback = publish -> {
                if (publish == null) {
                    return;
                }
                String receivedTopic = publish.getTopic().toString();
                String payload = new String(publish.getPayloadAsBytes());
                try {
                    String eventName = receivedTopic.split("/")[2].toLowerCase();
                    logger.trace("{}: Got MQTT message on topic {}: {}", getSerialNumber(), receivedTopic, payload);
                    parser.handleMessage(eventName, payload);
                } catch (DataParsingException e) {
                    listener.onEventStreamFailure(this, e);
                }
            };

            String topic = String.format("iot/atr/+/%s/%s/%s/+", device.getDid(), device.getDeviceClass(),
                    device.getResource());

            client.subscribeWith().topicFilter(topic).callback(eventCallback).send().get();
            logger.debug("Established MQTT connection to device {}", getSerialNumber());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            boolean isAuthFailure = cause instanceof Mqtt3ConnAckException connAckException
                    && connAckException.getMqttMessage().getReturnCode() == Mqtt3ConnAckReturnCode.NOT_AUTHORIZED;
            throw new EcovacsApiException(e, isAuthFailure);
        }
    }

    @Override
    public void disconnect(ScheduledExecutorService scheduler) {
        Mqtt3AsyncClient client = this.mqttClient;
        if (client != null) {
            client.disconnect();
        }
        this.mqttClient = null;
    }

    private TrustManagerFactory createTrustManagerFactory() {
        return new SimpleTrustManagerFactory() {
            @Override
            protected void engineInit(@Nullable KeyStore keyStore) throws Exception {
            }

            @Override
            protected void engineInit(@Nullable ManagerFactoryParameters managerFactoryParameters) throws Exception {
            }

            @Override
            protected TrustManager[] engineGetTrustManagers() {
                return new TrustManager[] { TrustAllTrustManager.getInstance() };
            }
        };
    }
}
