/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.discovery;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.amazonechocontrol.handler.EchoHandler;
import org.openhab.binding.amazonechocontrol.handler.FlashBriefingProfileHandler;
import org.openhab.binding.amazonechocontrol.handler.SmartHomeBaseHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevice;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonEchoDiscovery} is responsible for discovering echo devices on
 * the amazon account specified in the binding.
 *
 * @author Michael Geramb - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.amazonechocontrol")
public class AmazonEchoDiscovery extends AbstractDiscoveryService {

    public static AmazonEchoDiscovery instance;
    private final @NonNull static List<IAmazonEchoDiscovery> discoveryServices = new ArrayList<>();

    private final @NonNull Logger logger = LoggerFactory.getLogger(AmazonEchoDiscovery.class);
    private final @NonNull Map<String, ThingUID> lastDeviceInformations = new HashMap<>();
    private final @NonNull Map<String, ThingUID> lastSmartHomeDeviceInformations = new HashMap<>();
    private final @NonNull HashSet<String> discoverdFlashBriefings = new HashSet<String>();

    public static void addDiscoveryHandler(IAmazonEchoDiscovery discoveryService) {
        synchronized (discoveryServices) {
            if (!discoveryServices.contains(discoveryService)) {
                discoveryServices.add(discoveryService);
            }
        }

    }

    public static void removeDiscoveryHandler(IAmazonEchoDiscovery discoveryService) {
        synchronized (discoveryServices) {
            discoveryServices.remove(discoveryService);
        }
    }

    public AmazonEchoDiscovery() {
        super(SUPPORTED_THING_TYPES_UIDS, 10);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    static boolean discoverAccount = true;

    public static void setHandlerExist() {
        discoverAccount = false;
    }

    @Override
    protected void startScan() {
        startScan(true);
    }

    protected void startScan(boolean manual) {

        if (startScanStateJob != null) {
            startScanStateJob.cancel(false);
            startScanStateJob = null;
        }
        if (discoverAccount) {

            discoverAccount = false;
            // No accounts created yet, create one
            ThingUID thingUID = new ThingUID(THING_TYPE_ACCOUNT, "account1");

            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel("Amazon Account").build();

            logger.debug("Device [Amazon Account] found.");

            thingDiscovered(result);
        }

        IAmazonEchoDiscovery[] accounts;

        synchronized (discoveryServices) {
            accounts = new IAmazonEchoDiscovery[discoveryServices.size()];
            accounts = discoveryServices.toArray(accounts);
        }

        for (IAmazonEchoDiscovery discovery : accounts) {
            discovery.updateDeviceList(manual);
        }

    }

    ScheduledFuture<?> startScanStateJob;

    @Override
    protected void startBackgroundDiscovery() {
        AmazonEchoDiscovery.instance = this;
        if (startScanStateJob != null) {
            startScanStateJob.cancel(false);
            startScanStateJob = null;
        }

        startScanStateJob = scheduler.schedule(() -> {

            startScan(false);
        }, 3000, TimeUnit.MILLISECONDS);

    }

    @Override
    protected void stopBackgroundDiscovery() {
        AmazonEchoDiscovery.instance = null;
        if (startScanStateJob != null) {
            startScanStateJob.cancel(false);
            startScanStateJob = null;
        }

    }

    @Override
    @Activate
    public void activate(Map<String, Object> config) {
        super.activate(config);
        if (config != null) {
            modified(config);
        }
    };

    public synchronized void setSmartHomeDevices(ThingUID brigdeThingUID,
            List<JsonSmartHomeDevice> deviceInformations) {
        Set<String> toRemove = new HashSet<String>(lastSmartHomeDeviceInformations.keySet());
        for (JsonSmartHomeDevice deviceInformation : deviceInformations) {
            if (deviceInformation.manufacturerName != null && deviceInformation.manufacturerName.equals("openHAB")) {
                // Ignore devices provided by the openHAB skill
                continue;
            }
            String entityId = deviceInformation.entityId;
            if (entityId != null) {
                boolean alreadyfound = toRemove.remove(entityId);
                if (!alreadyfound && deviceInformation.actions != null) {
                    List<String> actions = Arrays.asList(deviceInformation.actions);
                    if (actions.contains("turnOn") && actions.contains("turnOff")) {

                        ThingTypeUID thingTypeId;
                        if (actions.contains("setPercentage")) {
                            thingTypeId = THING_TYPE_SMART_HOME_DIMMER;
                        } else {
                            thingTypeId = THING_TYPE_SMART_HOME_SWITCH;
                        }

                        ThingUID thingUID = new ThingUID(thingTypeId, brigdeThingUID, entityId);

                        // Check if already created
                        if (SmartHomeBaseHandler.find(thingUID) == null) {

                            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                                    .withLabel(deviceInformation.friendlyName)
                                    .withProperty(DEVICE_PROPERTY_ENTITY_ID, entityId)
                                    .withRepresentationProperty(DEVICE_PROPERTY_ENTITY_ID).withBridge(brigdeThingUID)
                                    .build();

                            logger.debug("Device [{}: {}] found. Mapped to thing type {}",
                                    deviceInformation.friendlyName, entityId, thingTypeId.getAsString());

                            thingDiscovered(result);
                            lastSmartHomeDeviceInformations.put(entityId, thingUID);
                        }
                    }

                }
            }
        }
    }

    public synchronized void setDevices(ThingUID brigdeThingUID, Device[] deviceInformations) {

        Set<String> toRemove = new HashSet<String>(lastDeviceInformations.keySet());
        for (Device deviceInformation : deviceInformations) {
            String serialNumber = deviceInformation.serialNumber;
            if (serialNumber != null) {
                boolean alreadyfound = toRemove.remove(serialNumber);
                // new
                if (!alreadyfound && deviceInformation.deviceFamily != null) {
                    ThingTypeUID thingTypeId;
                    if (deviceInformation.deviceFamily.equals("ECHO")) {
                        thingTypeId = THING_TYPE_ECHO;
                    } else if (deviceInformation.deviceFamily.equals("ROOK")) {
                        thingTypeId = THING_TYPE_ECHO_SPOT;
                    } else if (deviceInformation.deviceFamily.equals("KNIGHT")) {
                        thingTypeId = THING_TYPE_ECHO_SHOW;
                    } else if (deviceInformation.deviceFamily.equals("WHA")) {
                        thingTypeId = THING_TYPE_ECHO_WHA;
                    } else {
                        thingTypeId = THING_TYPE_UNKNOWN;
                    }

                    ThingUID thingUID = new ThingUID(thingTypeId, brigdeThingUID, serialNumber);

                    // Check if already created
                    if (EchoHandler.find(thingUID) == null) {

                        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                                .withLabel(deviceInformation.accountName)
                                .withProperty(DEVICE_PROPERTY_SERIAL_NUMBER, serialNumber)
                                .withProperty(DEVICE_PROPERTY_FAMILY, deviceInformation.deviceFamily)
                                .withRepresentationProperty(DEVICE_PROPERTY_SERIAL_NUMBER).withBridge(brigdeThingUID)
                                .build();

                        logger.debug("Device [{}: {}] found. Mapped to thing type {}", deviceInformation.deviceFamily,
                                serialNumber, thingTypeId.getAsString());

                        thingDiscovered(result);
                        lastDeviceInformations.put(serialNumber, thingUID);
                    }
                }
            }
        }
    }

    public synchronized void discoverFlashBriefingProfiles(ThingUID brigdeThingUID, String currentFlashBriefingJson) {
        if (currentFlashBriefingJson.isEmpty()) {
            return;
        }
        if (discoverdFlashBriefings.contains(currentFlashBriefingJson)) {
            return;
        }
        if (!FlashBriefingProfileHandler.exist(currentFlashBriefingJson)) {
            if (!discoverdFlashBriefings.contains(currentFlashBriefingJson)) {

                String id = UUID.randomUUID().toString();
                ThingUID thingUID = new ThingUID(THING_TYPE_FLASH_BRIEFING_PROFILE, brigdeThingUID, id);

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel("FlashBriefing")
                        .withProperty(DEVICE_PROPERTY_FLASH_BRIEFING_PROFILE, currentFlashBriefingJson)
                        .withBridge(brigdeThingUID).build();
                logger.debug("Flash Briefing {} discovered", currentFlashBriefingJson);

                thingDiscovered(result);
                discoverdFlashBriefings.add(currentFlashBriefingJson);
            }
        }
    }

    public synchronized void removeExistingEchoHandler(@NonNull ThingUID uid) {
        for (String id : lastDeviceInformations.keySet()) {
            if (lastDeviceInformations.get(id).equals(uid)) {
                lastDeviceInformations.remove(id);
            }
        }
    }

    public synchronized void removeExistingSmartHomeHandler(@NonNull ThingUID uid) {
        for (String id : lastSmartHomeDeviceInformations.keySet()) {
            if (lastSmartHomeDeviceInformations.get(id).equals(uid)) {
                lastSmartHomeDeviceInformations.remove(id);
            }
        }
    }

    public synchronized void removeExistingFlashBriefingProfile(String currentFlashBriefingJson) {
        if (currentFlashBriefingJson != null) {
            discoverdFlashBriefings.remove(currentFlashBriefingJson);
        }
    }

}
