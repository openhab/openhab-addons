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

import static org.openhab.binding.sungrow.internal.SungrowBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openhab.binding.sungrow.internal.client.operations.ApiOperationsFactory;
import org.openhab.binding.sungrow.internal.client.operations.BasicPlantInfo;
import org.openhab.binding.sungrow.internal.client.operations.DeviceList;
import org.openhab.binding.sungrow.internal.client.operations.RealtimeData;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SungrowPlantHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Kemper - Initial contribution
 */
public class SungrowPlantHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SungrowPlantHandler.class);

    private volatile PlantConfiguration plantConfiguration = new PlantConfiguration();

    private SungrowBridgeHandler sungrowBridgeHandler;

    public SungrowPlantHandler(Thing thing) {
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
        plantConfiguration = getConfigAs(PlantConfiguration.class);
        if (!plantConfiguration.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration.");
            return;
        }
        sungrowBridgeHandler = getSungrowBridgeHandler();
        if (sungrowBridgeHandler != null) {
            Integer interval = sungrowBridgeHandler.getConfiguration().getInterval();
            scheduler.scheduleWithFixedDelay(this::updatePlant, interval, interval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void updatePlant() {
        try {
            DeviceList deviceList = ApiOperationsFactory.getDeviceList(plantConfiguration.getPlantId());
            sungrowBridgeHandler.getSungrowClient().execute(deviceList);

            DeviceList.Response deviceListResponse = deviceList.getResponse();
            logger.info("Handling {} devices for plant {}", deviceListResponse.getDevices().size(),
                    plantConfiguration.getPlantId());
            deviceListResponse.getDevices().forEach(this::handleDevice);

            queryRealtimeData(deviceListResponse);
        } catch (IOException e) {
            logger.error("Unable to update sungrow plant", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void handleDevice(DeviceList.Device device) {
        try {
            logger.debug("Handling device {} with serial {}. Type: '{}', TypeName: '{}'", device.getDeviceName(),
                    device.getSerial(), device.getDeviceType(), device.getDeviceTypeName());

            BasicPlantInfo basicPlantInfo = ApiOperationsFactory.getBasicPlantInfo(device.getSerial());
            sungrowBridgeHandler.getSungrowClient().execute(basicPlantInfo);

            BasicPlantInfo.Response response = basicPlantInfo.getResponse();
            logger.info("Installed Power: {}", response.getInstalledPower());
        } catch (IOException e) {
            logger.error("Unable to handle device {}", device.getDeviceName(), e);
        }
    }

    private void queryRealtimeData(DeviceList.Response deviceListResponse) throws IOException {
        List<String> serials = deviceListResponse.getDevices().stream().map(DeviceList.Device::getSerial)
                .collect(Collectors.toList());
        RealtimeData realtimeData = ApiOperationsFactory.getRealtimeData(serials);
        sungrowBridgeHandler.getSungrowClient().execute(realtimeData);
        System.out.println(realtimeData.getResponse());
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
