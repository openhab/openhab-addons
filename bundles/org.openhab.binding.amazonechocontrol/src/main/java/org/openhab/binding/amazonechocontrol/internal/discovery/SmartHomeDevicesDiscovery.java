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
package org.openhab.binding.amazonechocontrol.internal.discovery;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lukas Knoeller
 */
@NonNullByDefault
public class SmartHomeDevicesDiscovery extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    AccountHandler accountHandler;
    private final Logger logger = LoggerFactory.getLogger(SmartHomeDevicesDiscovery.class);
    private final ArrayList<SmartHomeDevice> smartHomeDevices = new ArrayList<SmartHomeDevice>();

    @Nullable
    ScheduledFuture<?> startScanStateJob;
    long activateTimeStamp;

    private @Nullable DiscoveryServiceCallback discoveryServiceCallback;

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    public SmartHomeDevicesDiscovery(AccountHandler accountHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 10);
        this.accountHandler = accountHandler;
    }

    public void activate() {
        activate(new Hashtable<String, @Nullable Object>());
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        stopScanJob();
        removeOlderResults(activateTimeStamp);

        setSmartHomeDevices(accountHandler.updateSmartHomeDeviceList());
    }

    protected void startAutomaticScan() {
        if (!this.accountHandler.getThing().getThings().isEmpty()) {
            stopScanJob();
            return;
        }
        Connection connection = this.accountHandler.findConnection();
        if (connection == null) {
            return;
        }
        Date verifyTime = connection.tryGetVerifyTime();
        if (verifyTime == null) {
            return;
        }
        if (new Date().getTime() - verifyTime.getTime() < 10000) {
            return;
        }
        startScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopScanJob();
        startScanStateJob = scheduler.scheduleWithFixedDelay(this::startAutomaticScan, 3000, 1000,
                TimeUnit.MILLISECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScanJob();
    }

    void stopScanJob() {
        @Nullable
        ScheduledFuture<?> currentStartScanStateJob = startScanStateJob;
        if (currentStartScanStateJob != null) {
            currentStartScanStateJob.cancel(false);
            startScanStateJob = null;
        }
    }

    @Override
    @Activate
    public void activate(@Nullable Map<String, @Nullable Object> config) {
        super.activate(config);
        if (config != null) {
            modified(config);
        }
        activateTimeStamp = new Date().getTime();
    };

    synchronized void setSmartHomeDevices(List<SmartHomeDevice> deviceList) {
        DiscoveryServiceCallback discoveryServiceCallback = this.discoveryServiceCallback;

        if (discoveryServiceCallback == null) {
            return;
        }

        Configuration config = accountHandler.getThing().getConfiguration();
        boolean discoverSmartHome = (boolean) config.getProperties().get("discoverSmartHome");

        if (discoverSmartHome == false) {
            return;
        }

        for (SmartHomeDevice smartHomeDevice : deviceList) {
            ThingUID bridgeThingUID = this.accountHandler.getThing().getUID();
            ThingTypeUID thingTypeId = smartHomeDevice.groupDevices != null ? THING_TYPE_LIGHT_GROUP : THING_TYPE_LIGHT;
            ThingUID thingUID = new ThingUID(thingTypeId, bridgeThingUID, smartHomeDevice.entityId);
            if (discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
                continue;
            }
            if (discoveryServiceCallback.getExistingThing(thingUID) != null) {
                continue;
            }

            String lightName = null;
            if (smartHomeDevice.alias != null && smartHomeDevice.alias[0] != null) {
                lightName = smartHomeDevice.alias[0].friendlyName;
            } else {
                lightName = smartHomeDevice.friendlyName;
            }

            Map<String, Object> props = new HashMap<String, Object>();
            if (smartHomeDevice.entityId != null) {
                props.put(DEVICE_PROPERTY_LIGHT_ENTITY_ID, smartHomeDevice.entityId);
            }

            if (smartHomeDevice.applianceId != null) {
                props.put(DEVICE_PROPERTY_APPLIANCE_ID, smartHomeDevice.applianceId);
            }

            if (smartHomeDevice.groupDevices != null) {
                int subDeviceCounter = 0;
                if (smartHomeDevice.groupDevices != null) {
                    for (SmartHomeDevice d : smartHomeDevice.groupDevices) {
                        if (d != null && d.entityId != null) {
                            props.put(DEVICE_PROPERTY_LIGHT_SUBDEVICE + subDeviceCounter, d.entityId);
                        }

                        if (d != null && d.applianceId != null) {
                            props.put(DEVICE_PROPERTY_APPLIANCE_ID + subDeviceCounter, d.applianceId);
                        }
                        ++subDeviceCounter;
                    }
                }
            }

            if (smartHomeDevice.brightness == true) {
                props.put(INTERFACE_BRIGHTNESS, "true");
            } else if (smartHomeDevice.colorTemperature == true) {
                props.put(INTERFACE_COLOR_TEMPERATURE, "true");
            } else if (smartHomeDevice.color == true) {
                props.put(INTERFACE_COLOR, "true");
            }

            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(lightName).withProperties(props)
                    .withBridge(bridgeThingUID).build();

            logger.debug("Device[{]: {}] found. Mapped to thing type {}", smartHomeDevice.friendlyName,
                    smartHomeDevice.applianceId, thingTypeId.getAsString());

            thingDiscovered(result);
        }
    }
}
