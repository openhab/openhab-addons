/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airgradient.internal.discovery;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.BACKGROUND_DISCOVERY;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.BACKGROUND_DISCOVERY_DELAY;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.BACKGROUND_DISCOVERY_INTERVAL;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.CONFIG_LOCATION;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.PROPERTY_NAME;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.PROPERTY_SERIAL_NO;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.SEARCH_TIME;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.THING_TYPE_LOCATION;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airgradient.internal.communication.AirGradientCommunicationException;
import org.openhab.binding.airgradient.internal.handler.AirGradientAPIHandler;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirGradientLocationDiscoveryService} is responsible for discovering new locations
 * that are not bound to any items.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AirGradientLocationDiscoveryService.class)
@NonNullByDefault
public class AirGradientLocationDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(AirGradientLocationDiscoveryService.class);

    private @NonNullByDefault({}) AirGradientAPIHandler apiHandler;
    private @Nullable ScheduledFuture<?> discoveryFuture;

    public AirGradientLocationDiscoveryService() {
        super(Set.of(THING_TYPE_LOCATION), (int) SEARCH_TIME.getSeconds(), BACKGROUND_DISCOVERY);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start AirGradient background discovery");

        ScheduledFuture<?> localDiscoveryFuture = this.discoveryFuture;
        if (localDiscoveryFuture == null || localDiscoveryFuture.isCancelled()) {
            this.discoveryFuture = scheduler.scheduleWithFixedDelay(this::startScan,
                    BACKGROUND_DISCOVERY_DELAY.getSeconds(), BACKGROUND_DISCOVERY_INTERVAL.getSeconds(),
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping AirGradient background discovery");

        ScheduledFuture<?> localDiscoveryFuture = this.discoveryFuture;
        if (localDiscoveryFuture != null) {
            localDiscoveryFuture.cancel(true);
            this.discoveryFuture = null;
        }
    }

    @Override
    protected void startScan() {
        BridgeHandler bridge = apiHandler.getThing().getHandler();
        if (bridge == null) {
            logger.debug("Missing bridge, can't start scan.");
            return;
        }

        ThingUID bridgeUid = bridge.getThing().getUID();
        logger.debug("Starting Location discovery for bridge {}", bridgeUid);

        try {
            List<Measure> measures = apiHandler.getApiController().getMeasures();
            Set<String> registeredLocationIds = new HashSet<>(apiHandler.getRegisteredLocationIds());

            for (Measure measure : measures) {
                String id = measure.getLocationId();
                if (id.isEmpty()) {
                    // Local devices don't have location ID.
                    id = measure.getSerialNo();
                }

                String name = measure.getLocationName();
                if (name.isEmpty()) {
                    name = "Sensor_" + measure.getSerialNo();
                }

                if (!registeredLocationIds.contains(id)) {
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put(PROPERTY_NAME, name);
                    properties.put(PROPERTY_FIRMWARE_VERSION, measure.getFirmwareVersion());
                    properties.put(PROPERTY_SERIAL_NO, measure.getSerialNo());
                    properties.put(CONFIG_LOCATION, id);

                    ThingUID thingUID = new ThingUID(THING_TYPE_LOCATION, bridgeUid, id);

                    logger.debug("Adding location {} with id {} to bridge {} with location id {}", name, thingUID,
                            bridgeUid, measure.getLocationId());
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeUid).withLabel(name).withRepresentationProperty(CONFIG_LOCATION).build();

                    thingDiscovered(discoveryResult);
                }
            }
        } catch (AirGradientCommunicationException agce) {
            logger.warn("Failed discovery due to communication exception: {}", agce.getMessage());
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof AirGradientAPIHandler airGradientAPIHandler) {
            this.apiHandler = airGradientAPIHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return apiHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
