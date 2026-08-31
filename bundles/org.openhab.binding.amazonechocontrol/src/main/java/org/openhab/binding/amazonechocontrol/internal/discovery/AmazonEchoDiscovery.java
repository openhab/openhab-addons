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
package org.openhab.binding.amazonechocontrol.internal.discovery;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.EnabledFeedTO;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonEchoDiscovery} is responsible for discovering echo devices on
 * the amazon account specified in the binding.
 *
 * @author Michael Geramb - Initial contribution
 * @author Jan N. Klug - Refactored to ThingHandlerService
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AmazonEchoDiscovery.class)
@NonNullByDefault
public class AmazonEchoDiscovery extends AbstractThingHandlerDiscoveryService<AccountHandler> {
    private static final int BACKGROUND_INTERVAL = 10; // in seconds
    private final Logger logger = LoggerFactory.getLogger(AmazonEchoDiscovery.class);
    private final Set<List<EnabledFeedTO>> discoveredFlashBriefings = new HashSet<>();

    private @Nullable ScheduledFuture<?> startScanStateJob;
    private @Nullable Instant activateTimeStamp;

    public AmazonEchoDiscovery() {
        super(AccountHandler.class, SUPPORTED_ECHO_THING_TYPES_UIDS, 5);
    }

    @Override
    protected void startScan() {
        stopScanJob();
        final Instant activateTimeStamp = this.activateTimeStamp;
        if (activateTimeStamp != null) {
            removeOlderResults(activateTimeStamp);
        }
        Connection connection = thingHandler.getConnection();
        List<DeviceTO> devices = thingHandler.updateDeviceList();
        devices.forEach(device -> {
            if (device.macAddress == null) {
                device.macAddress = connection.getDeviceMacAddress(device);
            }
        });
        setDevices(devices);

        List<EnabledFeedTO> currentFlashBriefingConfiguration = thingHandler.updateFlashBriefingHandlers();
        discoverFlashBriefingProfiles(currentFlashBriefingConfiguration);
    }

    protected void startAutomaticScan() {
        if (!thingHandler.getThing().getThings().isEmpty()) {
            stopScanJob();
            return;
        }
        Connection connection = thingHandler.getConnection();
        // do discovery only if logged in and last login is more than 10 s ago
        Date verifyTime = connection.getVerifyTime();
        if (verifyTime == null || System.currentTimeMillis() < (verifyTime.getTime() + 10000)) {
            return;
        }

        startScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopScanJob();
        startScanStateJob = scheduler.scheduleWithFixedDelay(this::startAutomaticScan, BACKGROUND_INTERVAL,
                BACKGROUND_INTERVAL, TimeUnit.SECONDS);
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
    public void initialize() {
        if (activateTimeStamp == null) {
            activateTimeStamp = Instant.now();
        }
        super.initialize();
    }

    private synchronized void setDevices(List<DeviceTO> deviceList) {
        for (DeviceTO device : deviceList) {
            String serialNumber = device.serialNumber;
            if (serialNumber != null) {
                String deviceFamily = device.deviceFamily;
                if (deviceFamily != null) {
                    ThingTypeUID thingTypeId;
                    switch (deviceFamily) {
                        case "ECHO":
                            thingTypeId = THING_TYPE_ECHO;
                            break;
                        case "ROOK":
                            thingTypeId = THING_TYPE_ECHO_SPOT;
                            break;
                        case "KNIGHT":
                            thingTypeId = THING_TYPE_ECHO_SHOW;
                            break;
                        case "WHA":
                            thingTypeId = THING_TYPE_ECHO_WHA;
                            break;
                        default:
                            logger.debug("Unknown thing type '{}'", deviceFamily);
                            continue;
                    }

                    ThingUID bridgeThingUID = thingHandler.getThing().getUID();
                    ThingUID thingUID = new ThingUID(thingTypeId, bridgeThingUID, serialNumber);

                    DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(thingUID)
                            .withLabel(device.accountName).withProperty(DEVICE_PROPERTY_SERIAL_NUMBER, serialNumber)
                            .withProperty(DEVICE_PROPERTY_FAMILY, deviceFamily)
                            .withProperty(DEVICE_PROPERTY_DEVICE_TYPE_ID,
                                    Objects.requireNonNullElse(device.deviceType, "<unknown>"))
                            .withRepresentationProperty(DEVICE_PROPERTY_SERIAL_NUMBER).withBridge(bridgeThingUID);

                    addMacAddressProperty(resultBuilder, device.macAddress);
                    DiscoveryResult result = resultBuilder.build();

                    logger.debug("Device [{}: {}] found. Mapped to thing type {}", device.deviceFamily, serialNumber,
                            thingTypeId.getAsString());

                    thingDiscovered(result);
                }
            }
        }
    }

    static void addMacAddressProperty(DiscoveryResultBuilder resultBuilder, @Nullable String macAddress) {
        String normalizedMacAddress = normalizeMacAddress(macAddress);
        if (!normalizedMacAddress.isEmpty()) {
            resultBuilder.withProperty(Thing.PROPERTY_MAC_ADDRESS, normalizedMacAddress);
        }
    }

    private static String normalizeMacAddress(@Nullable String macAddress) {
        if (macAddress == null) {
            return "";
        }

        String hex = macAddress.replace(":", "").replace("-", "").trim();
        if (!hex.matches("(?i)[0-9a-f]{12}") || "000000000000".equals(hex)) {
            return "";
        }

        String normalized = hex.toLowerCase(Locale.ROOT);
        return String.join(":", normalized.substring(0, 2), normalized.substring(2, 4), normalized.substring(4, 6),
                normalized.substring(6, 8), normalized.substring(8, 10), normalized.substring(10, 12));
    }

    private synchronized void discoverFlashBriefingProfiles(List<EnabledFeedTO> enabledFeeds) {
        if (enabledFeeds.isEmpty()) {
            return;
        }

        if (!discoveredFlashBriefings.contains(enabledFeeds)) {
            ThingUID bridgeThingUID = thingHandler.getThing().getUID();
            ThingUID freeThingUID = new ThingUID(THING_TYPE_FLASH_BRIEFING_PROFILE, bridgeThingUID,
                    Integer.toString(enabledFeeds.hashCode()));
            DiscoveryResult result = DiscoveryResultBuilder.create(freeThingUID).withLabel("FlashBriefing")
                    .withProperty(DEVICE_PROPERTY_FLASH_BRIEFING_PROFILE, enabledFeeds)
                    .withBridge(thingHandler.getThing().getUID()).build();
            logger.debug("Flash Briefing {} discovered", enabledFeeds);
            thingDiscovered(result);
            discoveredFlashBriefings.add(enabledFeeds);
        }
    }
}
