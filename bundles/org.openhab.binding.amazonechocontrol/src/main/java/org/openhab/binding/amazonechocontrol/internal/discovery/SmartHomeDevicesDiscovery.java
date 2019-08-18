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

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_APPLIANCE_ID;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_LIGHT_ENTITY_ID;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_LIGHT_SUBDEVICE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_BRIGHTNESS;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_COLOR_TEMPERATURE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_POWER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_LIGHT;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_LIGHT_GROUP;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_SMART_PLUG;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroups.SmartHomeGroup;
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
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
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

    synchronized void setSmartHomeDevices(List<Object> deviceList) {
        DiscoveryServiceCallback discoveryServiceCallback = this.discoveryServiceCallback;

        if (discoveryServiceCallback == null) {
            return;
        }

        Configuration config = accountHandler.getThing().getConfiguration();
        boolean discoverSmartHome = (boolean) config.getProperties().get("discoverSmartHome");

        if (discoverSmartHome == false) {
            return;
        }

        Boolean discoverOpenHabSmartHomeDevices = (Boolean) config.getProperties()
                .get("discoverOpenHabSmartHomeDevices");

        for (Object smartHomeDevice : deviceList) {
            ThingUID bridgeThingUID = this.accountHandler.getThing().getUID();
            ThingTypeUID thingTypeId = null;
            ThingUID thingUID = null;
            String lightName = null;
            Map<String, Object> props = new HashMap<String, Object>();

            if (smartHomeDevice instanceof SmartHomeDevice) {
                SmartHomeDevice shd = (SmartHomeDevice) smartHomeDevice;
                if (discoverOpenHabSmartHomeDevices == null || discoverOpenHabSmartHomeDevices == false) {
                    if ("OpenHab".equalsIgnoreCase(shd.manufacturerName)) {
                        continue;
                    }
                }

                if (!Arrays.asList(shd.applianceTypes).contains("SMARTPLUG")) {
                    thingTypeId = THING_TYPE_LIGHT;
                    thingUID = new ThingUID(thingTypeId, bridgeThingUID, shd.entityId);
                } else if (Arrays.asList(shd.applianceTypes).contains("SMARTPLUG")) {
                    thingTypeId = THING_TYPE_SMART_PLUG;
                    thingUID = new ThingUID(thingTypeId, bridgeThingUID, shd.entityId);
                }

                if (shd.aliases != null && shd.aliases.length > 0 && shd.aliases[0].friendlyName != null) {
                    lightName = shd.aliases[0].friendlyName;
                } else {
                    lightName = shd.friendlyName;
                }

                if (shd != null && shd.entityId != null) {
                    props.put(DEVICE_PROPERTY_LIGHT_ENTITY_ID, shd.entityId);
                }

                if (shd != null && shd.applianceId != null) {
                    props.put(DEVICE_PROPERTY_APPLIANCE_ID, shd.applianceId);
                }

                if (((SmartHomeDevice) smartHomeDevice).brightness == true) {
                    props.put(INTERFACE_BRIGHTNESS, "true");
                } else if (((SmartHomeDevice) smartHomeDevice).colorTemperature == true) {
                    props.put(INTERFACE_COLOR_TEMPERATURE, "true");
                } else if (((SmartHomeDevice) smartHomeDevice).color == true) {
                    props.put(INTERFACE_COLOR, "true");
                }

                for (SmartHomeCapability capability : shd.capabilities) {
                    if (capability.interfaceName.equals(INTERFACE_POWER)) {
                        props.put("power", INTERFACE_POWER);
                    }

                    if (capability.interfaceName.equals(INTERFACE_BRIGHTNESS)) {
                        props.put("brightness", INTERFACE_BRIGHTNESS);
                    }

                    if (capability.interfaceName.equals(INTERFACE_COLOR_TEMPERATURE)) {
                        props.put("colorTemperature", INTERFACE_COLOR_TEMPERATURE);
                    }

                    if (capability.interfaceName.equals(INTERFACE_COLOR)) {
                        props.put("color", INTERFACE_COLOR);
                    }
                }
            }

            if (smartHomeDevice instanceof SmartHomeGroup) {
                SmartHomeGroup shg = (SmartHomeGroup) smartHomeDevice;
                thingTypeId = THING_TYPE_LIGHT_GROUP;
                lightName = shg.applianceGroupName;
                String groupIdentifier = shg.applianceGroupIdentifier.value.replace(".", "_");
                thingUID = new ThingUID(thingTypeId, bridgeThingUID, groupIdentifier);

                int subDeviceCounter = 0;
                for (Object smartDevice : deviceList) {
                    if (smartDevice instanceof SmartHomeDevice) {
                        SmartHomeDevice shd = (SmartHomeDevice) smartDevice;
                        if (shd.tags != null && shd.tags.tagNameToValueSetMap != null
                                && shd.tags.tagNameToValueSetMap.groupIdentity != null
                                && Arrays.asList(shd.tags.tagNameToValueSetMap.groupIdentity)
                                        .contains(shg.applianceGroupIdentifier.value)) {
                            if (shd.entityId != null) {
                                props.put(DEVICE_PROPERTY_LIGHT_SUBDEVICE + subDeviceCounter, shd.entityId);
                            }

                            if (shd.applianceId != null) {
                                props.put(DEVICE_PROPERTY_APPLIANCE_ID + subDeviceCounter, shd.applianceId);
                            }
                            ++subDeviceCounter;
                        }

                        for (SmartHomeCapability capability : shd.capabilities) {
                            if (capability.interfaceName.equals(INTERFACE_POWER) && !props.containsKey("power")) {
                                props.put("power", INTERFACE_POWER);
                            }

                            if (capability.interfaceName.equals(INTERFACE_BRIGHTNESS)
                                    && !props.containsKey("brightness")) {
                                props.put("brightness", INTERFACE_BRIGHTNESS);
                            }

                            if (capability.interfaceName.equals(INTERFACE_COLOR_TEMPERATURE)
                                    && !props.containsKey("colorTemperature")) {
                                props.put("colorTemperature", INTERFACE_COLOR_TEMPERATURE);
                            }

                            if (capability.interfaceName.equals(INTERFACE_COLOR) && !props.containsKey("color")) {
                                props.put("color", INTERFACE_COLOR);
                            }
                        }
                    }
                }
            }

            if (thingUID != null) {
                if (discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
                    continue;
                }

                if (discoveryServiceCallback.getExistingThing(thingUID) != null) {
                    continue;
                }

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(lightName)
                        .withProperties(props).withBridge(bridgeThingUID).build();

                logger.debug("Device[{]: {}] found. Mapped to thing type {}", lightName, thingTypeId.getAsString());

                thingDiscovered(result);
            }
        }
    }
}
