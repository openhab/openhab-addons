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
package org.openhab.binding.amazonechocontrol.internal.discovery;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDeviceAlias;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.DriverIdentity;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroups.SmartHomeGroup;
import org.openhab.binding.amazonechocontrol.internal.jsons.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.smarthome.Constants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lukas Knoeller - Initial contribution
 */
@NonNullByDefault
public class SmartHomeDevicesDiscovery extends AbstractDiscoveryService {
    private AccountHandler accountHandler;
    private Logger logger = LoggerFactory.getLogger(SmartHomeDevicesDiscovery.class);

    private @Nullable ScheduledFuture<?> startScanStateJob;
    private @Nullable Long activateTimeStamp;

    public SmartHomeDevicesDiscovery(AccountHandler accountHandler) {
        super(SUPPORTED_SMART_HOME_THING_TYPES_UIDS, 10);
        this.accountHandler = accountHandler;
    }

    public void activate() {
        activate(new Hashtable<String, Object>());
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        stopScanJob();
        Long activateTimeStamp = this.activateTimeStamp;
        if (activateTimeStamp != null) {
            removeOlderResults(activateTimeStamp);
        }
        setSmartHomeDevices(accountHandler.updateSmartHomeDeviceList(false));
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
        ScheduledFuture<?> currentStartScanStateJob = startScanStateJob;
        if (currentStartScanStateJob != null) {
            currentStartScanStateJob.cancel(false);
            startScanStateJob = null;
        }
        super.stopScan();
    }

    @Override
    @Activate
    public void activate(@Nullable Map<String, Object> config) {
        super.activate(config);
        if (config != null) {
            modified(config);
        }
        Long activateTimeStamp = this.activateTimeStamp;
        if (activateTimeStamp == null) {
            this.activateTimeStamp = new Date().getTime();
        }
    };

    synchronized void setSmartHomeDevices(List<SmartHomeBaseDevice> deviceList) {
        int smartHomeDeviceDiscoveryMode = accountHandler.getSmartHomeDevicesDiscoveryMode();
        if (smartHomeDeviceDiscoveryMode == 0) {
            return;
        }

        for (Object smartHomeDevice : deviceList) {
            ThingUID bridgeThingUID = this.accountHandler.getThing().getUID();
            ThingUID thingUID = null;
            String deviceName = null;
            Map<String, Object> props = new HashMap<>();

            if (smartHomeDevice instanceof SmartHomeDevice shd) {
                logger.trace("Found SmartHome device: {}", shd);

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
                    // Connected through skill
                    continue;
                }
                if (smartHomeDeviceDiscoveryMode != 2 && "openHAB".equalsIgnoreCase(shd.manufacturerName)) {
                    // OpenHAB device
                    continue;
                }

                if (shd.getCapabilities().stream()
                        .noneMatch(capability -> Constants.SUPPORTED_INTERFACES.contains(capability.interfaceName))) {
                    // No supported interface found
                    continue;
                }

                thingUID = new ThingUID(THING_TYPE_SMART_HOME_DEVICE, bridgeThingUID, entityId.replace(".", "-"));

                List<JsonSmartHomeDeviceAlias> aliases = shd.aliases;
                if ("Amazon".equals(shd.manufacturerName) && driverIdentity != null
                        && "SonarCloudService".equals(driverIdentity.identifier)) {
                    List<@Nullable String> interfaces = shd.getCapabilities().stream().map(c -> c.interfaceName)
                            .collect(Collectors.toList());
                    if (interfaces.contains("Alexa.AcousticEventSensor")) {
                        deviceName = "Alexa Guard on " + shd.friendlyName;
                    } else if (interfaces.contains("Alexa.ColorController")) {
                        deviceName = "Alexa Color Controller on " + shd.friendlyName;
                    } else if (interfaces.contains("Alexa.PowerController")) {
                        deviceName = "Alexa Plug on " + shd.friendlyName;
                    } else if (interfaces.contains("Alexa.ThermostatController")) {
                        deviceName = "Alexa Smart " + shd.friendlyName;
                    } else {
                        deviceName = "Unknown Device on " + shd.friendlyName;
                    }
                } else if ("Amazon".equals(shd.manufacturerName) && driverIdentity != null
                        && "OnGuardSmartHomeBridgeService".equals(driverIdentity.identifier)) {
                    deviceName = "Alexa Guard";
                } else if (aliases != null && !aliases.isEmpty() && aliases.get(0).friendlyName != null) {
                    deviceName = aliases.get(0).friendlyName;
                } else {
                    deviceName = shd.friendlyName;
                }
                props.put(DEVICE_PROPERTY_ID, id);
            } else if (smartHomeDevice instanceof SmartHomeGroup shg) {
                logger.trace("Found SmartHome device: {}", shg);

                String id = shg.findId();
                if (id == null) {
                    // No id
                    continue;
                }
                Set<SmartHomeDevice> supportedChildren = SmartHomeDeviceHandler.getSupportedSmartHomeDevices(shg,
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
