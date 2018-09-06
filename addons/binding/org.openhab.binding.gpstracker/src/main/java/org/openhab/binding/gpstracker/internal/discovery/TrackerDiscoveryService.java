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
import org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.CONFIG_TRACKER_ID;

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
     * Registry of tracker to discover next time
     */
    private Set<String> trackersToDiscover = new HashSet<>();

    /**
     * Constructor.
     *
     * @throws IllegalArgumentException thrown by the super constructor
     */
    public TrackerDiscoveryService() throws IllegalArgumentException {
        super(GPSTrackerBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, false);
    }

    /**
     * Called when the source tracker is not registered as a thing. These undiscovered tracker will be registered by
     * the discovery service.
     *
     * @param trackerId Tracker id.
     */
    public void addTracker(String trackerId) {
        trackersToDiscover.add(trackerId);
    }

    public void removeTracker(String trackerId) {
        trackersToDiscover.remove(trackerId);
    }

    private String getTrackerLabel(String trackerId) {
        return String.format("GPS Tracker %s", trackerId);
    }

    @Override
    protected void startScan() {
        trackersToDiscover.stream().forEach(trackerId -> {
            ThingUID id = new ThingUID(GPSTrackerBindingConstants.THING_TYPE_TRACKER, trackerId);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(id)
                    .withLabel(getTrackerLabel(trackerId))
                    .withProperty(CONFIG_TRACKER_ID,trackerId)
                    .build();
            this.thingDiscovered(discoveryResult);
        });
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        super.deactivate();
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }
}
