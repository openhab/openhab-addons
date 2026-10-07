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
package org.openhab.binding.melcloud.internal.home.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.MelCloudBindingConstants;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaUnit;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwUnit;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeUserContext;
import org.openhab.binding.melcloud.internal.home.handler.MelCloudHomeAccountHandler;
import org.openhab.binding.melcloud.internal.logging.SensitiveDataMasker;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MelCloudHomeUnitDiscoveryService} discovers ATA/ATW units under a {@code home-account} bridge via its own
 * one-off {@code /context} fetch. Background discovery waits with a short retry until the bridge is
 * {@link ThingStatus#ONLINE}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MelCloudHomeUnitDiscoveryService.class)
public class MelCloudHomeUnitDiscoveryService extends AbstractThingHandlerDiscoveryService<MelCloudHomeAccountHandler> {

    private static final String PROPERTY_UNIT_ID = "unitId";
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private static final int BRIDGE_NOT_ONLINE_RETRY_DELAY_SECONDS = 5;
    private static final int BRIDGE_NOT_ONLINE_MAX_ATTEMPTS = 12;

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeUnitDiscoveryService.class);

    private @Nullable ScheduledFuture<?> scanTask;
    private volatile @Nullable ScheduledFuture<?> backgroundTask;
    private final AtomicInteger bridgeNotOnlineAttempts = new AtomicInteger();

    /**
     * Creates a MelCloudHomeUnitDiscoveryService with enabled autostart.
     */
    public MelCloudHomeUnitDiscoveryService() {
        super(MelCloudHomeAccountHandler.class, MelCloudBindingConstants.HOME_DISCOVERABLE_THING_TYPE_UIDS,
                DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
        // Return promptly: discovery performs a blocking network request.
        backgroundTask = scheduler.schedule(this::discoverUnits, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> task = backgroundTask;
        if (task != null) {
            task.cancel(true);
            backgroundTask = null;
        }
        super.stopBackgroundDiscovery();
    }

    @Override
    protected void startScan() {
        ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
        }
        this.scanTask = scheduler.schedule(this::discoverUnits, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        super.stopScan();

        ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    private void discoverUnits() {
        if (thingHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            int attempt = bridgeNotOnlineAttempts.incrementAndGet();
            if (attempt <= BRIDGE_NOT_ONLINE_MAX_ATTEMPTS) {
                logger.debug("MELCloud Home bridge is not online yet, deferring unit discovery by {}s (attempt {}/{})",
                        BRIDGE_NOT_ONLINE_RETRY_DELAY_SECONDS, attempt, BRIDGE_NOT_ONLINE_MAX_ATTEMPTS);
                backgroundTask = scheduler.schedule(this::discoverUnits, BRIDGE_NOT_ONLINE_RETRY_DELAY_SECONDS,
                        TimeUnit.SECONDS);
            } else {
                logger.debug("MELCloud Home bridge is still not online after {} attempts, giving up on automatic "
                        + "unit discovery for now; a manual scan will retry", BRIDGE_NOT_ONLINE_MAX_ATTEMPTS);
            }
            return;
        }
        bridgeNotOnlineAttempts.set(0);

        logger.debug("Discover MELCloud Home units");
        try {
            MelCloudHomeUserContext context = thingHandler.fetchUserContext();
            ThingUID bridgeUID = thingHandler.getThing().getUID();

            context.getAllAtaUnits().forEach(unit -> discoverAtaUnit(unit, bridgeUID));
            context.getAllAtwUnits().forEach(unit -> discoverAtwUnit(unit, bridgeUID));
        } catch (MelCloudCommException e) {
            logger.debug("Error occurred during unit discovery, reason {}. ", e.getMessage(), e);
        }
    }

    private void discoverAtaUnit(MelCloudHomeAtaUnit unit, ThingUID bridgeUID) {
        ThingTypeUID thingTypeUid = MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ATA_UNIT;
        ThingUID unitThing = new ThingUID(thingTypeUid, bridgeUID, unit.id);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_UNIT_ID, unit.id);

        String label = "MELCloud Home ATA - " + unit.givenDisplayName;
        logger.debug("Found ATA unit: {} (id={})", label, SensitiveDataMasker.maskId(unit.id));

        thingDiscovered(DiscoveryResultBuilder.create(unitThing).withLabel(label).withProperties(properties)
                .withRepresentationProperty(PROPERTY_UNIT_ID).withBridge(bridgeUID).build());
    }

    private void discoverAtwUnit(MelCloudHomeAtwUnit unit, ThingUID bridgeUID) {
        ThingTypeUID thingTypeUid = MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ATW_UNIT;
        ThingUID unitThing = new ThingUID(thingTypeUid, bridgeUID, unit.id);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_UNIT_ID, unit.id);

        String label = "MELCloud Home ATW - " + unit.givenDisplayName;
        logger.debug("Found ATW unit: {} (id={})", label, SensitiveDataMasker.maskId(unit.id));

        thingDiscovered(DiscoveryResultBuilder.create(unitThing).withLabel(label).withProperties(properties)
                .withRepresentationProperty(PROPERTY_UNIT_ID).withBridge(bridgeUID).build());
    }
}
