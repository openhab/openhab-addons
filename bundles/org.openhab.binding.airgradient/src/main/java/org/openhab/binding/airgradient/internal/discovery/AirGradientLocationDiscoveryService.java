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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airgradient.internal.handler.AirGradientAPIHandler;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
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

    public AirGradientLocationDiscoveryService() {
        super(Set.of(THING_TYPE_LOCATION), SEARCH_TIME, BACKGROUND_DISCOVERY);
        logger.debug("Constructing discovery service");
    }

    @Override
    protected void startScan() {
        ThingUID bridgeUid = apiHandler.getThing().getHandler().getThing().getUID();
        logger.debug("Starting Location discovery for bridge {}", bridgeUid);

        List<Measure> measures = apiHandler.getMeasures();
        Set<String> registeredLocationIds = new HashSet<>(apiHandler.getRegisteredLocationIds());

        for (Measure measure : measures) {
            if (!registeredLocationIds.contains(measure.getLocationId())) {
                Map<String, Object> properties = new HashMap<>(1);
                properties.put(PROPERTY_NAME, measure.getLocationName());
                properties.put(PROPERTY_FIRMWARE_VERSION, measure.getFirmwareVersion());
                properties.put(PROPERTY_SERIAL_NO, measure.getSerialNo());
                properties.put(CONFIG_LOCATION, measure.getLocationId());

                ThingUID thingUID = new ThingUID(THING_TYPE_LOCATION, bridgeUid, measure.getLocationId());

                logger.debug("Adding location {} with id {} to bridge {} with location id {}",
                        measure.getLocationName(), thingUID, bridgeUid, measure.getLocationId());
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUid).withLabel(measure.getLocationName())
                        .withRepresentationProperty(CONFIG_LOCATION).build();

                thingDiscovered(discoveryResult);
            }
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
