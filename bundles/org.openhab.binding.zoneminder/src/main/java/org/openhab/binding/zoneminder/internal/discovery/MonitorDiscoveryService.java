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
package org.openhab.binding.zoneminder.internal.discovery;

import static org.openhab.binding.zoneminder.internal.ZmBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zoneminder.internal.handler.Monitor;
import org.openhab.binding.zoneminder.internal.handler.ZmBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MonitorDiscoveryService} is responsible for discovering the Zoneminder monitors
 * associated with a Zoneminder server.
 *
 * @author Mark Hilbush - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MonitorDiscoveryService.class)
@NonNullByDefault
public class MonitorDiscoveryService extends AbstractThingHandlerDiscoveryService<ZmBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(MonitorDiscoveryService.class);

    private static final int DISCOVERY_INTERVAL_SECONDS = 300;
    private static final int DISCOVERY_INITIAL_DELAY_SECONDS = 10;
    private static final int DISCOVERY_TIMEOUT_SECONDS = 6;

    private @Nullable Future<?> discoveryJob;

    public MonitorDiscoveryService() {
        super(ZmBridgeHandler.class, SUPPORTED_MONITOR_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_MONITOR_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            logger.debug("ZoneminderDiscovery: Starting background discovery job");
            discoveryJob = scheduler.scheduleWithFixedDelay(this::backgroundDiscoverMonitors,
                    DISCOVERY_INITIAL_DELAY_SECONDS, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            logger.debug("ZoneminderDiscovery: Stopping background discovery job");
            localDiscoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void startScan() {
        logger.debug("ZoneminderDiscovery: Running discovery scan");
        discoverMonitors();
    }

    private void backgroundDiscoverMonitors() {
        if (!thingHandler.isBackgroundDiscoveryEnabled()) {
            return;
        }
        logger.debug("ZoneminderDiscovery: Running background discovery scan");
        discoverMonitors();
    }

    private synchronized void discoverMonitors() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        Integer alarmDuration = thingHandler.getDefaultAlarmDuration();
        Integer imageRefreshInterval = thingHandler.getDefaultImageRefreshInterval();
        for (Monitor monitor : thingHandler.getSavedMonitors()) {
            String id = monitor.getId();
            String name = monitor.getName();
            ThingUID thingUID = new ThingUID(UID_MONITOR, bridgeUID, monitor.getId());
            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_MONITOR_ID, id);
            properties.put(CONFIG_ALARM_DURATION, alarmDuration);
            if (imageRefreshInterval != null) {
                properties.put(CONFIG_IMAGE_REFRESH_INTERVAL, imageRefreshInterval);
            }
            thingDiscovered(createDiscoveryResult(thingUID, bridgeUID, id, name, properties));
            logger.debug("ZoneminderDiscovery: Monitor with id '{}' and name '{}' added to Inbox with UID '{}'",
                    monitor.getId(), monitor.getName(), thingUID);
        }
    }

    private DiscoveryResult createDiscoveryResult(ThingUID monitorUID, ThingUID bridgeUID, String id, String name,
            Map<String, Object> properties) {
        return DiscoveryResultBuilder.create(monitorUID).withProperties(properties).withBridge(bridgeUID)
                .withLabel(String.format("Zoneminder Monitor %s", name)).withRepresentationProperty(CONFIG_MONITOR_ID)
                .build();
    }
}
