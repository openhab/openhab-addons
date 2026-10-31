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
package org.openhab.binding.matter.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.ws.OtaUpdateInfo;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.firmware.Firmware;
import org.openhab.core.thing.binding.firmware.FirmwareBuilder;
import org.openhab.core.thing.firmware.FirmwareProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides firmware information for Matter devices.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = { FirmwareProvider.class,
        MatterFirmwareProvider.class }, scope = ServiceScope.SINGLETON, immediate = true, configurationPid = "org.openhab.binding.matter.firmware")
@NonNullByDefault
public class MatterFirmwareProvider implements FirmwareProvider {

    private static final Duration CACHE_EXPIRY = Duration.ofDays(1);
    private static final Duration QUERY_RETRY_DELAY = Duration.ofMinutes(15);

    private final Logger logger = LoggerFactory.getLogger(MatterFirmwareProvider.class);
    private final Map<ThingUID, OtaFirmwareEntry> firmwareCache = new ConcurrentHashMap<>();
    private final Map<ThingUID, Instant> lastQueryStarted = new ConcurrentHashMap<>();

    @Activate
    public MatterFirmwareProvider() {
        logger.debug("MatterFirmwareProvider activated");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("MatterFirmwareProvider deactivated");
        firmwareCache.clear();
        lastQueryStarted.clear();
    }

    @Override
    public @Nullable Firmware getFirmware(Thing thing, String version) {
        return getFirmware(thing, version, null);
    }

    @Override
    public @Nullable Firmware getFirmware(Thing thing, String version, @Nullable Locale locale) {
        logger.debug("Getting firmware for thing {} with version {}", thing.getUID(), version);
        Set<Firmware> firmwares = getFirmwares(thing, locale);
        if (firmwares != null) {
            return firmwares.stream().filter(firmware -> firmware.getVersion().equals(version)).findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public @Nullable Set<Firmware> getFirmwares(Thing thing) {
        return getFirmwares(thing, null);
    }

    @Override
    public @Nullable Set<Firmware> getFirmwares(Thing thing, @Nullable Locale locale) {
        logger.debug("Getting firmwares for thing {}", thing.getUID());
        if (thing.getHandler() instanceof NodeHandler nodeHandler) {
            ThingUID thingUID = thing.getUID();
            OtaFirmwareEntry firmwareEntry = firmwareCache.get(thingUID);
            if (firmwareEntry == null || firmwareEntry.isExpired()) {
                Instant now = Instant.now();
                Instant lastStarted = lastQueryStarted.get(thingUID);
                // A failed query stores nothing, so wait before asking the device again
                if (lastStarted == null || now.isAfter(lastStarted.plus(QUERY_RETRY_DELAY))) {
                    lastQueryStarted.put(thingUID, now);
                    nodeHandler.checkForOTAUpdate();
                }
            }

            OtaUpdateInfo updateInfo = firmwareEntry != null ? firmwareEntry.updateInfo() : null;
            if (updateInfo != null) {
                FirmwareBuilder builder = FirmwareBuilder.create(thing.getThingTypeUID(),
                        updateInfo.softwareVersionString);
                builder.withVendor(String.valueOf(updateInfo.vendorId))
                        .withDescription(updateInfo.softwareVersionString);
                String releaseNotesUrl = updateInfo.releaseNotesUrl;
                if (releaseNotesUrl != null && !releaseNotesUrl.isBlank()) {
                    try {
                        URL url = new URI(releaseNotesUrl).toURL();
                        builder.withOnlineChangelog(url);
                    } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
                        logger.debug("Failed to create URL for release notes URL {}", releaseNotesUrl, e);
                    }
                }
                return Collections.singleton(builder.build());
            } else {
                // No OTA update available - return current firmware version so the
                // framework reports UP_TO_DATE instead of UNKNOWN
                String currentVersion = thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION);
                if (currentVersion != null && !currentVersion.isBlank()) {
                    return Collections
                            .singleton(FirmwareBuilder.create(thing.getThingTypeUID(), currentVersion.trim()).build());
                }
            }
        }
        return null;
    }

    /**
     * Updates the OTA update information for the given node Thing.
     * 
     * @param thingUID the UID of the node Thing to update the OTA update information for
     * @param updateInfo the OTA update information, or null if no update is available
     */
    public void updateFirmwareInfo(ThingUID thingUID, @Nullable OtaUpdateInfo updateInfo) {
        firmwareCache.put(thingUID, new OtaFirmwareEntry(updateInfo, Instant.now().plus(CACHE_EXPIRY)));
    }

    /**
     * Removes the OTA update information for the given node Thing.
     * 
     * @param thingUID the UID of the node Thing to remove the OTA update information for
     */
    public void removeFirmwareInfo(ThingUID thingUID) {
        firmwareCache.remove(thingUID);
        lastQueryStarted.remove(thingUID);
    }

    private record OtaFirmwareEntry(@Nullable OtaUpdateInfo updateInfo, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
