/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeathomesystem.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeBridgeHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeDeviceList;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FreeAtHomeSystemDiscoveryService} is responsible for performing discoverz of things
 *
 * @author Andras Uhrin - Initial contribution
 */
@Component(service = DiscoveryService.class)
@NonNullByDefault
public class FreeAtHomeSystemDiscoveryService extends AbstractDiscoveryService {

    // public AbstractDiscoveryService(@Nullable Set<ThingTypeUID> supportedThingTypes, int timeout,
    // boolean backgroundDiscoveryEnabledByDefault) throws IllegalArgumentException {

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            FreeAtHomeBridgeHandler bridge = FreeAtHomeBridgeHandler.freeAtHomeSystemHandler;

            if (null != bridge) {
                ThingUID bridgeUID = bridge.getThing().getUID();

                FreeAtHomeDeviceList deviceList = bridge.getDeviceDeviceList();

                for (int i = 0; i < deviceList.getNumberOfDevices(); i++) {
                    String deviceLabel;

                    FreeAtHomeDeviceDescription device = deviceList
                            .getDeviceDescription(deviceList.getDeviceIdByIndex(i));

                    for (int ch = 0; ch < device.numberOfThings(); ch++) {

                        String deviceID = device.deviceId + "_" + String.format("%1d", ch);
                        String deviceChannelCounter;

                        if (device.numberOfThings() > 1) {
                            deviceChannelCounter = "-Ch" + (ch + 1) + "/" + device.numberOfThings();
                        } else {
                            deviceChannelCounter = "";
                        }

                        if (device.deviceLabel.contentEquals(device.listOfThings.get(ch).channelLabel)) {

                            deviceLabel = device.listOfThings.get(ch).channelTypeString + "-" + device.deviceId + "-"
                                    + device.deviceLabel + deviceChannelCounter;

                        } else {

                            deviceLabel = device.listOfThings.get(ch).channelTypeString + "-" + device.deviceId + "-"
                                    + device.deviceLabel + deviceChannelCounter + "-"
                                    + device.listOfThings.get(ch).channelLabel;
                        }

                        switch (device.listOfThings.get(ch).thingTypeOfChannel) {

                            case FreeAtHomeSystemBindingConstants.ACTUATOR_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.ACTUATOR_TYPE_UID,
                                        bridgeUID, deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.DOOROPENER_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.DOOROPENER_TYPE_UID,
                                        bridgeUID, deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.CORRIDORLIGHTSWITCH_TYPE_ID: {
                                ThingUID uid = new ThingUID(
                                        FreeAtHomeSystemBindingConstants.CORRIDORLIGHTSWITCH_TYPE_UID, bridgeUID,
                                        deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.DIMMINGACTUATOR_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.DIMMINGACTUATOR_TYPE_UID,
                                        bridgeUID, deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.SHUTTERACTUATOR_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.SHUTTERACTUATOR_TYPE_UID,
                                        bridgeUID, deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.THERMOSTAT_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.THERMOSTAT_TYPE_UID,
                                        bridgeUID, deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.WINDOWSENSOR_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.WINDOWSENSOR_TYPE_UID,
                                        bridgeUID, deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.SCENE_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.SCENE_TYPE_UID, bridgeUID,
                                        deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.RULE_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.RULE_TYPE_UID, bridgeUID,
                                        deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }

                            case FreeAtHomeSystemBindingConstants.DOORRINGSENSOR_TYPE_ID: {
                                ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.DOORRINGSENSOR_TYPE_UID,
                                        bridgeUID, deviceID);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.deviceId);
                                properties.put("interface", device.interfaceType);
                                properties.put("channelId", device.listOfThings.get(ch).channelId);

                                for (int idxOfDatapoints = 0; idxOfDatapoints < device.listOfThings.get(ch)
                                        .numberOfDatapoints(); idxOfDatapoints++) {
                                    String key = device.listOfThings.get(ch).channels.get(idxOfDatapoints);

                                    properties.put(key, device.listOfThings.get(ch).datapoints.get(idxOfDatapoints));
                                }

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(deviceLabel).withBridge(bridgeUID).withProperties(properties)
                                        .build();

                                thingDiscovered(discoveryResult);
                                break;
                            }
                        }
                    }
                }

                stopScan();
            }
        }
    };

    public FreeAtHomeSystemDiscoveryService(int timeout) {
        super(FreeAtHomeSystemBindingConstants.SUPPORTED_THING_TYPES_UIDS, 90, false);
    }

    public FreeAtHomeSystemDiscoveryService() {
        super(FreeAtHomeSystemBindingConstants.SUPPORTED_THING_TYPES_UIDS, 90, false);
    }

    @Override
    protected void startScan() {

        this.removeOlderResults(getTimestampOfLastScan());

        scheduler.execute(runnable);
    }
}
