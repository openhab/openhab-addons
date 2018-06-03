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

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.amazonechocontrol.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonEchoDiscovery} is responsible for discovering echo devices on
 * the amazon account specified in the binding.
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class AmazonEchoDiscovery extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    AccountHandler accountHandler;
    private final Logger logger = LoggerFactory.getLogger(AmazonEchoDiscovery.class);
    private final HashSet<String> discoverdFlashBriefings = new HashSet<String>();

    @Nullable
    ScheduledFuture<?> startScanStateJob;
    long activateTimeStamp;

    private @Nullable DiscoveryServiceCallback discoveryServiceCallback;

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    public AmazonEchoDiscovery(AccountHandler accountHandler) {
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

        setDevices(accountHandler.updateDeviceList());

        String currentFlashBriefingConfiguration = accountHandler.getNewCurrentFlashbriefingConfiguration();
        discoverFlashBriefingProfiles(currentFlashBriefingConfiguration);
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

    synchronized void setDevices(List<Device> deviceList) {
        DiscoveryServiceCallback discoveryServiceCallback = this.discoveryServiceCallback;
        if (discoveryServiceCallback == null) {
            return;
        }
        for (Device device : deviceList) {
            String serialNumber = device.serialNumber;
            if (serialNumber != null) {
                String deviceFamily = device.deviceFamily;
                if (deviceFamily != null) {
                    ThingTypeUID thingTypeId;
                    if (deviceFamily.equals("ECHO")) {
                        thingTypeId = THING_TYPE_ECHO;
                    } else if (deviceFamily.equals("ROOK")) {
                        thingTypeId = THING_TYPE_ECHO_SPOT;
                    } else if (deviceFamily.equals("KNIGHT")) {
                        thingTypeId = THING_TYPE_ECHO_SHOW;
                    } else if (deviceFamily.equals("WHA")) {
                        thingTypeId = THING_TYPE_ECHO_WHA;
                    } else {
                        logger.debug("Unknown thing type '{}'", deviceFamily);
                        continue;
                    }

                    ThingUID brigdeThingUID = this.accountHandler.getThing().getUID();
                    ThingUID thingUID = new ThingUID(thingTypeId, brigdeThingUID, serialNumber);
                    if (discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
                        continue;
                    }
                    if (discoveryServiceCallback.getExistingThing(thingUID) != null) {
                        continue;
                    }
                    DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(device.accountName)
                            .withProperty(DEVICE_PROPERTY_SERIAL_NUMBER, serialNumber)
                            .withProperty(DEVICE_PROPERTY_FAMILY, deviceFamily)
                            .withRepresentationProperty(DEVICE_PROPERTY_SERIAL_NUMBER).withBridge(brigdeThingUID)
                            .build();

                    logger.debug("Device [{}: {}] found. Mapped to thing type {}", device.deviceFamily, serialNumber,
                            thingTypeId.getAsString());

                    thingDiscovered(result);
                }
            }
        }
    }

    public synchronized void discoverFlashBriefingProfiles(String currentFlashBriefingJson) {
        if (currentFlashBriefingJson.isEmpty()) {
            return;
        }
        DiscoveryServiceCallback discoveryServiceCallback = this.discoveryServiceCallback;
        if (discoveryServiceCallback == null) {
            return;
        }

        if (!discoverdFlashBriefings.contains(currentFlashBriefingJson)) {

            ThingUID freeThingUID = null;
            int freeIndex = 0;
            for (int i = 1; i < 1000; i++) {
                String id = Integer.toString(i);
                ThingUID brigdeThingUID = this.accountHandler.getThing().getUID();
                ThingUID thingUID = new ThingUID(THING_TYPE_FLASH_BRIEFING_PROFILE, brigdeThingUID, id);
                if (discoveryServiceCallback.getExistingThing(thingUID) == null
                        && discoveryServiceCallback.getExistingDiscoveryResult(thingUID) == null) {
                    freeThingUID = thingUID;
                    freeIndex = i;
                    break;
                }
            }
            if (freeThingUID == null) {
                logger.debug("No more free flashbriefing thing ID found");
                return;
            }
            DiscoveryResult result = DiscoveryResultBuilder.create(freeThingUID).withLabel("FlashBriefing " + freeIndex)
                    .withProperty(DEVICE_PROPERTY_FLASH_BRIEFING_PROFILE, currentFlashBriefingJson)
                    .withBridge(accountHandler.getThing().getUID()).build();
            logger.debug("Flash Briefing {} discovered", currentFlashBriefingJson);
            thingDiscovered(result);
            discoverdFlashBriefings.add(currentFlashBriefingJson);
        }
    }

    public synchronized void removeExistingFlashBriefingProfile(@Nullable String currentFlashBriefingJson) {
        if (currentFlashBriefingJson != null) {
            discoverdFlashBriefings.remove(currentFlashBriefingJson);
        }
    }
}
