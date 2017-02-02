/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.dsmr.DSMRBindingConstants;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterConstants;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements the discovery service for new DSMR Meters
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class DSMRMeterDiscoveryService extends AbstractDiscoveryService implements DSMRMeterDiscoveryListener {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(DSMRMeterDiscoveryService.class);

    // The Bridge ThingUID
    private ThingUID dsmrBridgeUID = null;

    /**
     * Constructs a new DSMRMeterDiscoveryService with the specified DSMR Bridge ThingUID
     *
     * @param dsmrBridgeUID ThingUID for the DSMR Bridges
     */
    public DSMRMeterDiscoveryService(ThingUID dsmrBridgeUID) {
        super(DSMRMeterType.METER_THING_TYPES, DSMRBindingConstants.DSMR_DISCOVERY_TIMEOUT, false);
        this.dsmrBridgeUID = dsmrBridgeUID;
    }

    /**
     * Manual scanning is not supported for meters. The bridge will handle this automatically
     */
    @Override
    protected void startScan() {
        // Manual scanning is not supported
        logger.debug("Started Discovery Scan (Not supported for DSMR meter Thing types)");
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
     * @return true (meter is always accepted)
     */
    @Override
    public boolean meterDiscovered(DSMRMeterDescriptor meterDescriptor) {
        DSMRMeterType meterType = meterDescriptor.getMeterType();

        ThingTypeUID thingTypeUID = meterType.getThingTypeUID();
        String thingId = "dsmr:" + meterType.name().toLowerCase() + ":"
                + (meterDescriptor.getChannel() == DSMRMeterConstants.UNKNOWN_CHANNEL ? "default"
                        : meterDescriptor.getChannel());
        ThingUID thingUID = new ThingUID(thingId);

        // Construct the configuration for this meter
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("meterType", meterType.name());
        properties.put("channel", meterDescriptor.getChannel());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withBridge(dsmrBridgeUID).withProperties(properties).withLabel(meterType.meterKind.toString()).build();

        logger.debug("{} for meterDescriptor {}", discoveryResult, meterDescriptor);
        thingDiscovered(discoveryResult);

        return true;
    }
}