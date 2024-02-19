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
package org.openhab.binding.cloudrain.internal.discovery;

import static org.openhab.binding.cloudrain.internal.CloudrainBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cloudrain.internal.api.model.Zone;
import org.openhab.binding.cloudrain.internal.handler.CloudrainAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CloudrainDiscoveryService} is responsible for discovering Cloudrain irrigation zones defined in a user's
 * Cloudrain account.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(CloudrainDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;

    /**
     * A reference to the account handler to retrieve zones
     */
    private @Nullable CloudrainAccountHandler accountHandler;

    /**
     * The scan future handle created by the scheduler for the discovery job
     */
    private @Nullable Future<?> scanFuture;

    public CloudrainDiscoveryService() {
        super(Collections.unmodifiableSet(Stream.of(new ThingTypeUID(BINDING_ID, "-")).collect(Collectors.toSet())),
                DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler != null && handler instanceof CloudrainAccountHandler) {
            CloudrainAccountHandler accountHandlerLocal = (CloudrainAccountHandler) handler;
            accountHandlerLocal.setDiscoveryService(this);
            this.accountHandler = accountHandlerLocal;
        }
    }

    /**
     * Called on component activation.
     */
    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        loadZones();
    }

    @Override
    public void stopScan() {
        final Future<?> scanFuture = this.scanFuture;
        if (scanFuture != null) {
            scanFuture.cancel(true);
        }
        super.stopScan();
    }

    /**
     * Waits for the discovery scan to finish and then returns.
     */
    public void waitForScanFinishing() {
        final Future<?> scanFuture = this.scanFuture;
        if (scanFuture != null) {
            try {
                scanFuture.get();
            } catch (CancellationException ex) {
                // ignore
            } catch (Exception ex) {
                logger.warn(ERROR_MSG_DISCOVERY_SCAN, ex.getMessage());
            }
        }
    }

    /**
     * Starts a thread which loads all Cloudrain Zones registered in the account.
     */
    private void loadZones() {
        CloudrainAccountHandler handler = accountHandler;
        if (scanFuture == null) {
            scanFuture = scheduler.submit(() -> {
                if (handler != null) {
                    List<Zone> zones = handler.getZones();
                    if (zones != null && !zones.isEmpty()) {
                        for (Zone zone : zones) {
                            zoneDiscovered(zone);
                        }
                    }
                    scanFuture = null;
                    removeOlderResults(getTimestampOfLastScan());
                } else {
                    logger.warn(ERROR_MSG_DISCOVERY_FAILED);
                }
            });
        }
    }

    /**
     * Generates the DiscoveryResult from a Cloudrain zone.
     */
    public void zoneDiscovered(Zone zone) {
        String zoneName = zone.getNameWithDefault();
        String zoneId = zone.getId();

        if (zoneId != null && !zoneId.isBlank()) {
            CloudrainAccountHandler handler = accountHandler;
            if (handler != null) {
                ThingUID accountUID = handler.getThing().getUID();
                ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, accountUID, zone.getUID());

                try {
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(accountUID)
                            .withLabel(LABEL_ZONE + zoneName)
                            .withProperty(PROPERTY_CTRL_ID, zone.getControllerIdWithDefault())
                            .withProperty(PROPERTY_CTRL_NAME, zone.getControllerNameWithDefault())
                            .withProperty(PROPERTY_ZONE_NAME, zoneName).withProperty(PROPERTY_ZONE_ID, zoneId)
                            .withProperty(PROPERTY_UID, zone.getUID()).withRepresentationProperty(PROPERTY_UID).build();
                    thingDiscovered(discoveryResult);
                } catch (Exception ex) {
                    logger.error(ERROR_MSG_DISCOVERY_FAILED_CREATE_RESULT, zoneId);
                }
            } else {
                logger.error(ERROR_MSG_DISCOVERY_FAILED_ZONE, zoneId);
            }
        } else {
            logger.warn(ERROR_MSG_DISCOVERY_FAILED_ZONE_ID);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return null;
    }
}
