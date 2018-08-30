/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.discovery;


import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.gpstracker.internal.BindingConstants;

/**
 * Tracker discovery service.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TrackerDiscoveryService extends AbstractDiscoveryService {
    /**
     * Discovery timeout
     */
    private static final int TIMEOUT = 5;

    /**
     * Constructor.
     *
     * @throws IllegalArgumentException thrown by the super constructor
     */
    public TrackerDiscoveryService() throws IllegalArgumentException {
        super(BindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
    }

    /**
     * Called when the source tracker is not registered as a thing. These undiscovered tracker will be registered by
     * the discovery service.
     *
     * @param trackerId Tracker id.
     */
    public void addTracker(String trackerId) {
        ThingUID id = new ThingUID(BindingConstants.THING_TYPE_TRACKER, trackerId);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(id).withLabel(getTrackerLabel(trackerId)).build();
        this.thingDiscovered(discoveryResult);
    }

    private String getTrackerLabel(String trackerId) {
        return String.format("GPS Tracker %s", trackerId);
    }

    @Override
    protected void startScan() {
    }
}
