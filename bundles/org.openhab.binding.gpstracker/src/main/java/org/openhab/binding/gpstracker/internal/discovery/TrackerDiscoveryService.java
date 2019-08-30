/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@link TrackerDiscoveryService} class provides discovery service for the binding to discover trackers. Discovery
 * process is initiated by the tracker by sending a GPS log record. Based on the tracker id received in thin record an
 * entry is created in the Inbox for the thing representing the tracker.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class, TrackerDiscoveryService.class }, immediate = true, configurationPid = "discovery.gpstracker")
public class TrackerDiscoveryService extends AbstractDiscoveryService {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(TrackerDiscoveryService.class);

    /**
     * Discovery timeout
     */
    private static final int TIMEOUT = 1;

    /**
     * Registry of tracker to discover next time
     */
    private Set<String> trackersToDiscover = new HashSet<>();

    /**
     * Bridge UID
     */
    @Nullable
    private ThingUID life360BridgeUID;

    /**
     * Constructor.
     *
     * @throws IllegalArgumentException thrown by the super constructor
     */
    public TrackerDiscoveryService() throws IllegalArgumentException {
        super(GPSTrackerBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
    }

    public void setLife360BridgeUID(ThingUID life360BridgeUID) {
        this.life360BridgeUID = life360BridgeUID;
    }

    /**
     * Called when the source tracker is not registered as a thing. These undiscovered trackers will be registered by
     * the discovery service.
     *
     * @param trackerId Tracker id.
     */
    public void addTracker(String trackerId) {
        if (!trackersToDiscover.contains(trackerId)) {
            logger.debug("Tracker discovered: {}", trackerId);
            trackersToDiscover.add(trackerId);
            if (isBackgroundDiscoveryEnabled()) {
                createDiscoveryResult(trackerId);
            }
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
        int idx = trackerId.indexOf("@");
        boolean isLife360Tracker = idx > -1 && life360BridgeUID != null;

        String id = isLife360Tracker ? trackerId.substring(0, idx).replaceAll("[^A-Za-z0-9 ]", "_"): trackerId;

        DiscoveryResultBuilder discoveryResult;

        if (isLife360Tracker) {
            discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(GPSTrackerBindingConstants.THING_TYPE_TRACKER, life360BridgeUID, id))
                    .withProperty(ConfigHelper.CONFIG_LOGIN_EMAIL, trackerId)
                    .withBridge(life360BridgeUID);
        } else {
            discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(GPSTrackerBindingConstants.THING_TYPE_TRACKER, id));
        }

        discoveryResult
                .withProperty(ConfigHelper.CONFIG_TRACKER_ID, id)
                .withThingType(GPSTrackerBindingConstants.THING_TYPE_TRACKER)
                .withLabel("GPS Tracker " + trackerId);

        this.thingDiscovered(discoveryResult.build());
    }

    @Override
    @Activate
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, @Nullable Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    @Deactivate
    protected void deactivate() {
        removeOlderResults(new Date().getTime());
        super.deactivate();
    }

    public void clearLife360Results() {
        Set<String> tmp = new HashSet<>(trackersToDiscover);
        tmp.stream().filter(o->o.indexOf("@")>0).forEach(o->trackersToDiscover.remove(o));
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }
}
