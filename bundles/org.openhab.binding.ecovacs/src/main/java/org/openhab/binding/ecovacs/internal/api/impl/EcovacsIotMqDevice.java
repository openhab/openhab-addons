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
package org.openhab.binding.ecovacs.internal.api.impl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.commands.GetCleanLogsCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetFirmwareVersionCommand;
import org.openhab.binding.ecovacs.internal.api.commands.IotDeviceCommand;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.Device;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalCleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.model.CleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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
    private EcovacsApiImpl.@Nullable MqttSubscriptionHandle subscription;

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
            List<PortalCleanLogRecord> log = hasCapability(DeviceCapability.USES_CLEAN_RESULTS_LOG_API)
                    ? api.fetchCleanResultsLog(device)
                    : api.fetchCleanLogs(device);
            logEntries = log.stream().map(record -> new CleanLogRecord(record.timestamp, record.duration, record.area,
                    Optional.ofNullable(record.imageUrl), record.type));
        }
        return logEntries.sorted((lhs, rhs) -> rhs.timestamp.compareTo(lhs.timestamp)).collect(Collectors.toList());
    }

    @Override
    public Optional<byte[]> downloadCleanMapImage(CleanLogRecord record)
            throws EcovacsApiException, InterruptedException {
        if (record.mapImageUrl.isEmpty()) {
            return Optional.empty();
        }
        boolean needsSigning = hasCapability(DeviceCapability.USES_CLEAN_RESULTS_LOG_API);
        return api.downloadCleanMapImage(device, record.mapImageUrl.get(), needsSigning);
    }

    @Override
    public void connect(final EventListener listener, ScheduledExecutorService scheduler)
            throws EcovacsApiException, InterruptedException {
        // XML message handler does not receive firmware version information with events, so fetch in advance
        if (desc.protoVersion == ProtocolVersion.XML) {
            listener.onFirmwareVersionChanged(this, sendCommand(new GetFirmwareVersionCommand()));
        }

        final ReportParser parser = desc.protoVersion == ProtocolVersion.XML
                ? new XmlReportParser(this, listener, gson, logger)
                : new JsonReportParser(this, listener, desc.protoVersion, gson, logger);

        final MqttEventReceiver receiver = new MqttEventReceiver() {
            @Override
            public void onEvent(String eventName, String payload) {
                try {
                    parser.handleMessage(eventName, payload);
                } catch (Exception e) {
                    listener.onEventStreamFailure(EcovacsIotMqDevice.this, e);
                }
            }

            @Override
            public void onEventStreamDisconnected(boolean userInitiated, Throwable cause) {
                if (!userInitiated) {
                    listener.onEventStreamFailure(EcovacsIotMqDevice.this, cause);
                }
            }
        };

        this.subscription = api.subscribeForMqttEvents(device, receiver);
        logger.debug("Established MQTT connection to device {}", getSerialNumber());
    }

    @Override
    public void disconnect(ScheduledExecutorService scheduler) {
        EcovacsApiImpl.MqttSubscriptionHandle subscription = this.subscription;
        this.subscription = null;
        if (subscription != null) {
            scheduler.submit(() -> {
                try {
                    subscription.unsubscribe();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (EcovacsApiException e) {
                    logger.debug("{}: Could not unsubscribe from MQTT events", getSerialNumber(), e);
                }
            });
        }
    }
}
