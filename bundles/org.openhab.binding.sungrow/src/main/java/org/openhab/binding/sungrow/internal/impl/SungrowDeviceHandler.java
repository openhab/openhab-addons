/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.impl;

import static org.openhab.binding.sungrow.internal.SungrowBindingConstants.CHANNEL_1;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.afrouper.server.sungrow.api.dto.*;

/**
 * The {@link SungrowDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Kemper - Initial contribution
 */
public class SungrowDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SungrowDeviceHandler.class);

    private volatile DeviceConfiguration deviceConfiguration = new DeviceConfiguration();

    private SungrowBridgeHandler sungrowBridgeHandler;

    DevicePointInfoList openPointInfo;
    List<String> devicePointIds;

    public SungrowDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        deviceConfiguration = getConfigAs(DeviceConfiguration.class);
        if (!deviceConfiguration.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration.");
            return;
        }
        sungrowBridgeHandler = getSungrowBridgeHandler();
        if (sungrowBridgeHandler != null) {
            Integer interval = sungrowBridgeHandler.getConfiguration().getInterval();
            scheduler.scheduleWithFixedDelay(this::updateDevice, interval, interval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void readPoints() {
        Device device = deviceConfiguration.getDevice();
        openPointInfo = getSungrowBridgeHandler().getSungrowClient().getOpenPointInfo(device.deviceType(),
                device.deviceModelId());
        devicePointIds = openPointInfo.devicePointInfoList().stream().filter(Objects::nonNull)
                .map(DevicePointInfo::pointId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void updateDevice() {
        try {
            Device device = deviceConfiguration.getDevice();
            DevicePointList devicePointList = getSungrowBridgeHandler().getSungrowClient().getDeviceRealTimeData(
                    device.deviceType(), Collections.singletonList(device.plantDeviceId()), devicePointIds);
            devicePointList.devicePointList().stream().map(DevicePoint::pointIds).filter(Objects::nonNull)
                    .forEach(e -> handlePoints(e, openPointInfo));
        } catch (SungrowApiException e) {
            logger.error("Unable to update sungrow plant", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void handlePoints(Map<String, String> dataPoints, DevicePointInfoList openPointInfo) {
        dataPoints.forEach((key, value) -> {
            DevicePointInfo pointInfo = openPointInfo.getDevicePointInfo(key.substring(1));
            logger.debug("{}: {} {}", pointInfo.pointName(), value, pointInfo.showUnit());
            // ToDo: Update Channels
        });
    }

    private SungrowBridgeHandler getSungrowBridgeHandler() {
        BridgeHandler bridgeHandler = getBridge().getHandler();
        if (bridgeHandler instanceof SungrowBridgeHandler) {
            return (SungrowBridgeHandler) bridgeHandler;
        } else {
            logger.error("Sungrow Bridge not initialized");
            return null;
        }
    }
}
