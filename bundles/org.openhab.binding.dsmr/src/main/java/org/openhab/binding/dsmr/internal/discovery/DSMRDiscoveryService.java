/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for discovery services.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored code to detect meters during actual discovery phase.
 */
@NonNullByDefault
public abstract class DSMRDiscoveryService extends AbstractDiscoveryService {
    /**
     * Timeout for discovery time.
     */
    private static final int DSMR_DISCOVERY_TIMEOUT_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(DSMRDiscoveryService.class);

    /**
     * Meter Detector instance.
     */
    protected final DSMRMeterDetector meterDetector = new DSMRMeterDetector();

    /**
     * Constructs a new DSMRMeterDiscoveryService with the specified DSMR Bridge ThingUID
     */
    public DSMRDiscoveryService() {
        super(DSMRMeterType.METER_THING_TYPES, DSMR_DISCOVERY_TIMEOUT_SECONDS, false);
    }

    /**
     * Callback when a new meter is discovered
     * The new meter is described by the {@link DSMRMeterDescriptor}
     *
     * There will be a DiscoveryResult created and sent to the framework.
     *
     * At this moment there are no reasons why a new meter will not be accepted.
     *
     * Therefore this callback will always return true.
     *
     * @param meterDescriptor the descriptor of the new detected meter
     * @param dsmrBridgeUID ThingUID for the DSMR Bridges
     * @return true (meter is always accepted)
     */
    public boolean meterDiscovered(DSMRMeterDescriptor meterDescriptor, ThingUID dsmrBridgeUID) {
        DSMRMeterType meterType = meterDescriptor.getMeterType();
        ThingTypeUID thingTypeUID = meterType.getThingTypeUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, dsmrBridgeUID, meterDescriptor.getChannelId());

        // Construct the configuration for this meter
        Map<String, Object> properties = new HashMap<>();
        properties.put("meterType", meterType.name());
        properties.put("channel", meterDescriptor.getChannel());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withBridge(dsmrBridgeUID).withProperties(properties).withLabel(meterType.meterKind.getLabelKey())
                .build();

        logger.debug("{} for meterDescriptor {}", discoveryResult, meterDescriptor);
        thingDiscovered(discoveryResult);

        return true;
    }
}
