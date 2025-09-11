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
package org.openhab.binding.amazonechocontrol.internal.discovery;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice.DriverIdentity;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDeviceAlias;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeGroups.SmartHomeGroup;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.smarthome.Constants;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lukas Knoeller - Initial contribution
 * @author Jan N. Klug - Refactored to ThingHandlerService
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SmartHomeDevicesDiscovery.class)
@NonNullByDefault
public class SmartHomeDevicesDiscovery extends AbstractThingHandlerDiscoveryService<AccountHandler> {
    private final Logger logger = LoggerFactory.getLogger(SmartHomeDevicesDiscovery.class);

    private @Nullable ScheduledFuture<?> discoveryJob;

    public SmartHomeDevicesDiscovery() {
        super(AccountHandler.class, SUPPORTED_SMART_HOME_THING_TYPES_UIDS, 10);
    }

    @Override
    protected void startScan() {
        setSmartHomeDevices(thingHandler.updateSmartHomeDeviceList(false));
    }

    @Override
    protected void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            this.discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 1, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
    }

    private synchronized void setSmartHomeDevices(List<SmartHomeBaseDevice> deviceList) {
        int smartHomeDeviceDiscoveryMode = thingHandler.getSmartHomeDevicesDiscoveryMode();
        if (smartHomeDeviceDiscoveryMode == 0) {
            return;
        }

        for (Object smartHomeDevice : deviceList) {
            ThingUID bridgeThingUID = thingHandler.getThing().getUID();
            ThingUID thingUID = null;
            String deviceName = null;
            Map<String, Object> props = new HashMap<>();

            if (smartHomeDevice instanceof JsonSmartHomeDevice shd) {
                logger.trace("Found SmartHome device: {}", shd.applianceId);

                String entityId = shd.entityId;
                if (entityId == null) {
                    // No entity id
                    continue;
                }
                String id = shd.findId();
                if (id == null) {
                    // No id
                    continue;
                }
                boolean isSkillDevice = false;
                DriverIdentity driverIdentity = shd.driverIdentity;
                isSkillDevice = driverIdentity != null && "SKILL".equals(driverIdentity.namespace);

                if (smartHomeDeviceDiscoveryMode == 1 && isSkillDevice) {
                    // Connected through skill and we want direct only
                    continue;
                }
                if (smartHomeDeviceDiscoveryMode == 2 && "openHAB".equalsIgnoreCase(shd.manufacturerName)) {
                    // openHAB device and we want non-openHAB only
                    continue;
                }

                if (shd.getCapabilities().stream()
                        .noneMatch(capability -> Constants.SUPPORTED_INTERFACES.contains(capability.interfaceName))) {
                    // No supported interface found
                    continue;
                }

                thingUID = new ThingUID(THING_TYPE_SMART_HOME_DEVICE, bridgeThingUID, entityId.replace(".", "-"));

                List<JsonSmartHomeDeviceAlias> aliases = shd.aliases;
                String manufacturerName = shd.manufacturerName;
                if (manufacturerName != null) {
                    props.put(DEVICE_PROPERTY_MANUFACTURER_NAME, manufacturerName);
                }
                if (manufacturerName != null && manufacturerName.startsWith("Amazon")) {
                    List<@Nullable String> interfaces = shd.getCapabilities().stream().map(c -> c.interfaceName)
                            .toList();
                    if (driverIdentity != null && "SonarCloudService".equals(driverIdentity.identifier)) {
                        if (interfaces.contains("Alexa.AcousticEventSensor")) {
                            deviceName = "Alexa Guard on " + shd.friendlyName;
                        } else if (interfaces.contains("Alexa.ColorController")) {
                            deviceName = "Alexa Color Controller on " + shd.friendlyName;
                        } else if (interfaces.contains("Alexa.PowerController")) {
                            deviceName = "Alexa Plug on " + shd.friendlyName;
                        } else {
                            deviceName = "Unknown Device on " + shd.friendlyName;
                        }
                    } else if (driverIdentity != null
                            && "OnGuardSmartHomeBridgeService".equals(driverIdentity.identifier)) {
                        deviceName = "Alexa Guard";
                    } else if (driverIdentity != null && "AlexaBridge".equals(driverIdentity.namespace)
                            && interfaces.contains("Alexa.AcousticEventSensor")) {
                        deviceName = "Alexa Guard on " + shd.friendlyName;
                    } else {
                        deviceName = "Unknown Device on " + shd.friendlyName;
                    }
                } else if (aliases != null && !aliases.isEmpty() && aliases.get(0).friendlyName != null) {
                    deviceName = aliases.get(0).friendlyName;
                } else {
                    deviceName = shd.friendlyName;
                }
                props.put(DEVICE_PROPERTY_ID, id);
                List<JsonSmartHomeDevice.DeviceIdentifier> alexaDeviceIdentifierList = shd.alexaDeviceIdentifierList;
                if (alexaDeviceIdentifierList != null && !alexaDeviceIdentifierList.isEmpty()) {
                    props.put(DEVICE_PROPERTY_DEVICE_IDENTIFIER_LIST,
                            alexaDeviceIdentifierList.stream()
                                    .map(d -> d.dmsDeviceSerialNumber + " @ " + d.dmsDeviceTypeId)
                                    .collect(Collectors.joining(", ")));
                }
            } else if (smartHomeDevice instanceof SmartHomeGroup shg) {
                logger.trace("Found SmartHome device: {}", shg);

                String id = shg.findId();
                if (id == null) {
                    // No id
                    continue;
                }
                Set<JsonSmartHomeDevice> supportedChildren = SmartHomeDeviceHandler.getSupportedSmartHomeDevices(shg,
                        deviceList);
                if (supportedChildren.isEmpty()) {
                    // No children with a supported interface
                    continue;
                }
                thingUID = new ThingUID(THING_TYPE_SMART_HOME_DEVICE_GROUP, bridgeThingUID, id.replace(".", "-"));
                deviceName = shg.applianceGroupName;
                props.put(DEVICE_PROPERTY_ID, id);
            }

            if (thingUID != null) {
                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(deviceName)
                        .withProperties(props).withBridge(bridgeThingUID).build();

                logger.debug("Device [{}] found.", deviceName);

                thingDiscovered(result);
            }
        }
    }
}
