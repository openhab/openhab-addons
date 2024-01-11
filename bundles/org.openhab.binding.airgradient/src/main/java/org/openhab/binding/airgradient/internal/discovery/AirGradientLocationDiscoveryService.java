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
import org.openhab.binding.airgradient.internal.handler.AirGradientAPIHandler;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
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
public class AirGradientLocationDiscoveryService extends AbstractThingHandlerDiscoveryService<AirGradientAPIHandler> {

    private final Logger logger = LoggerFactory.getLogger(AirGradientLocationDiscoveryService.class);

    private @NonNullByDefault({}) ThingUID bridgeUid;

    public AirGradientLocationDiscoveryService() {
        super(AirGradientAPIHandler.class, Set.of(THING_TYPE_LOCATION), SEARCH_TIME, false);
        logger.debug("Constructing discovery service");
    }

    @Override
    public void activate() {
        logger.debug("Activating discovery service");
        super.activate();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing discovery service");
        bridgeUid = thingHandler.getThing().getUID();
        super.initialize();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Location discovery for bridge {}", bridgeUid);
        List<Measure> measures = thingHandler.getMeasures();
        Set<String> registeredLocationIds = new HashSet<>(thingHandler.getRegisteredLocationIds());

        for (Measure measure : measures) {
            if (measure != null && !registeredLocationIds.contains(measure.getLocationId())) {
                Map<String, Object> properties = new HashMap<>(1);
                properties.put(PROPERTY_NAME, measure.getLocationName());
                properties.put(PROPERTY_FIRMWARE_VERSION, measure.getFirmwareVersion());
                properties.put(PROPERTY_SERIAL_NO, measure.getSerialNo());

                ThingUID thingUID = new ThingUID(THING_TYPE_LOCATION, measure.getLocationId());

                logger.debug("Adding location {} with id {}", measure.getLocationName(), thingUID);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUid).withLabel(measure.getLocationName()).build();

                thingDiscovered(discoveryResult);
            }
        }
    }
}
