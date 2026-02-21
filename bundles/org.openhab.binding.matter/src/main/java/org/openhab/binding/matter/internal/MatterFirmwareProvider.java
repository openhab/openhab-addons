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

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.ws.OtaUpdateInfo;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.thing.Thing;
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

    private final Logger logger = LoggerFactory.getLogger(MatterFirmwareProvider.class);
    // Cache the OTA update information for 1 day
    private final ExpiringCacheMap<BigInteger, OtaFirmwareEntry> firmwareCache = new ExpiringCacheMap<>(
            Duration.ofDays(1));

    @Activate
    public MatterFirmwareProvider() {
        logger.debug("MatterFirmwareProvider activated");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("MatterFirmwareProvider deactivated");
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
            BigInteger nodeId = nodeHandler.getNodeId();
            OtaFirmwareEntry firmwareEntry = firmwareCache.putIfAbsentAndGet(nodeId, () -> {
                try {
                    OtaUpdateInfo updateInfo = nodeHandler.checkForOTAUpdate().get(30, TimeUnit.SECONDS);
                    return new OtaFirmwareEntry(updateInfo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("Failed to check for firmware update for device {}", nodeId, e);
                    return new OtaFirmwareEntry(null);
                } catch (ExecutionException | TimeoutException e) {
                    logger.debug("Failed to check for firmware update for device {}", nodeId, e);
                    return new OtaFirmwareEntry(null);
                }
            });

            if (firmwareEntry != null && firmwareEntry.updateInfo != null) {
                OtaUpdateInfo updateInfo = firmwareEntry.updateInfo;
                FirmwareBuilder builder = FirmwareBuilder.create(thing.getThingTypeUID(),
                        String.valueOf(updateInfo.softwareVersion));
                builder.withVendor(String.valueOf(updateInfo.vendorId))
                        .withDescription(updateInfo.softwareVersionString);
                if (updateInfo.releaseNotesUrl != null && !updateInfo.releaseNotesUrl.isBlank()) {
                    try {
                        URL url = new URI(updateInfo.releaseNotesUrl).toURL();
                        builder.withOnlineChangelog(url);
                    } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
                        logger.debug("Failed to create URL for release notes URL {}", updateInfo.releaseNotesUrl, e);
                    }
                }
                return Collections.singleton(builder.build());
            }
        }
        return null;
    }

    /**
     * Updates the OTA update information for the given node ID.
     * 
     * @param nodeId the node ID to update the OTA update information for
     * @param updateInfo the OTA update information to update
     */
    public void updateFirmwareInfo(BigInteger nodeId, OtaUpdateInfo updateInfo) {
        firmwareCache.putValue(nodeId, new OtaFirmwareEntry(updateInfo));
    }

    /**
     * OTA firmware entry for a node.
     * We wrap the OtaUpdateInfo so we can check if the entry is expired or not and if it has update information
     * separately.
     */
    class OtaFirmwareEntry {
        public @Nullable OtaUpdateInfo updateInfo;

        public OtaFirmwareEntry(@Nullable OtaUpdateInfo updateInfo) {
            this.updateInfo = updateInfo;
        }
    }
}
