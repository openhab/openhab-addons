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
package org.openhab.binding.gpstracker.internal.discovery;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

/**
 * The {@link TrackerDiscoveryService} class provides discovery service for the binding to discover trackers. Discovery
 * process is initiated by the tracker by sending a GPS log record. Based on the tracker id received in thin record an
 * entry is created in the Inbox for the thing representing the tracker.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class,
        TrackerDiscoveryService.class }, configurationPid = "discovery.gpstracker")
public class TrackerDiscoveryService extends AbstractDiscoveryService {
    /**
     * Discovery timeout
     */
    private static final int TIMEOUT = 1;

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
        super(GPSTrackerBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
    }

    /**
     * Called when the source tracker is not registered as a thing. These undiscovered trackers will be registered by
     * the discovery service.
     *
     * @param trackerId Tracker id.
     */
    public void addTracker(String trackerId) {
        trackersToDiscover.add(trackerId);
        if (isBackgroundDiscoveryEnabled()) {
            createDiscoveryResult(trackerId);
        }
    }

    /**
     * Unregister the tracker after the thing handles is created.
     *
     * @param trackerId Tracker id to unregister
     */
    public void removeTracker(String trackerId) {
        trackersToDiscover.remove(trackerId);
    }

    @Override
    protected void startScan() {
        trackersToDiscover.forEach(this::createDiscoveryResult);
    }

    /**
     * Create discovery result form the tracker id.
     *
     * @param trackerId Tracker id.
     */
    private void createDiscoveryResult(String trackerId) {
        ThingUID id = new ThingUID(GPSTrackerBindingConstants.THING_TYPE_TRACKER, trackerId);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(id)
                .withProperty(ConfigHelper.CONFIG_TRACKER_ID, trackerId)
                .withThingType(GPSTrackerBindingConstants.THING_TYPE_TRACKER).withLabel("GPS Tracker " + trackerId)
                .build();
        this.thingDiscovered(discoveryResult);
    }

    @Override
    @Activate
    protected void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    @Deactivate
    protected void deactivate() {
        removeOlderResults(Instant.now().toEpochMilli());
        super.deactivate();
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }
}
