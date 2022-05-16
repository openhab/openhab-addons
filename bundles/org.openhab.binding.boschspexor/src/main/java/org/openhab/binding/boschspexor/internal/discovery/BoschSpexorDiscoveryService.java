/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal.discovery;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschspexor.internal.api.model.Spexor;
import org.openhab.binding.boschspexor.internal.api.service.SpexorBridgeHandler;
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
 * Discovery Service to determine spexors
 *
 * @author Marc Fischer - Initial contribution *
 */
@NonNullByDefault
public class BoschSpexorDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(BoschSpexorDiscoveryService.class);

    private static final int DISCOVERY_TIME_SECONDS = 10;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(SPEXOR_THING_TYPE);

    private Optional<SpexorBridgeHandler> bridgeHandler = Optional.empty();
    private @NonNullByDefault({}) ThingUID bridgeUID;

    private Optional<ScheduledFuture<?>> backgroundFuture;

    public BoschSpexorDiscoveryService() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
        backgroundFuture = Optional.empty();
    }

    public void init() {
        logger.debug("bridgeHandler is {}", (bridgeHandler.isEmpty() ? "'null'" : "available"));
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void activate() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        bridgeHandler.get().setDiscoveryService(null);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof SpexorBridgeHandler) {
            bridgeHandler = Optional.of((SpexorBridgeHandler) handler);
            bridgeUID = bridgeHandler.get().getUID();
            bridgeHandler.get().setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler.isEmpty() ? null : bridgeHandler.get();
    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        backgroundFuture = Optional.of(scheduler.scheduleWithFixedDelay(this::startScan,
                BACKGROUND_SCAN_REFRESH_MINUTES, BACKGROUND_SCAN_REFRESH_MINUTES, TimeUnit.MINUTES));
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        if (backgroundFuture.isPresent()) {
            backgroundFuture.get().cancel(true);
            backgroundFuture = Optional.empty();
        }
    }

    @Override
    protected void startScan() {
        if (bridgeHandler.isPresent() && bridgeHandler.get().isAuthorized()) {
            logger.debug("Starting spexor discovery for bridge {}", bridgeUID);
            bridgeHandler.get().listSpexors().forEach(this::thingDiscovered);
        }
    }

    private void thingDiscovered(Spexor spexor) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(PROPERTY_SPEXOR_NAME, spexor.getName());
        properties.put(PROPERTY_SPEXOR_ID, spexor.getId());
        ThingUID thing = new ThingUID(SPEXOR_THING_TYPE, bridgeUID, spexor.getId());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thing).withBridge(bridgeUID)
                .withProperties(properties).withRepresentationProperty(PROPERTY_SPEXOR_NAME).withLabel(spexor.getName())
                .build();

        thingDiscovered(discoveryResult);
    }
}
