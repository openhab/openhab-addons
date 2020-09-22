/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zoneminder.internal.handler.Monitor;
import org.openhab.binding.zoneminder.internal.handler.ZmBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MonitorDiscoveryService} is responsible for discovering the Zoneminder monitors
 * associated with a Zoneminder server.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class MonitorDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(MonitorDiscoveryService.class);

    private @Nullable ZmBridgeHandler bridgeHandler;

    public MonitorDiscoveryService() {
        super(30);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ZmBridgeHandler) {
            ((ZmBridgeHandler) handler).setDiscoveryService(this);
            this.bridgeHandler = (ZmBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_MONITOR_THING_TYPES_UIDS;
    }

    @Override
    public void startBackgroundDiscovery() {
        logger.trace("Discovery: Performing background discovery scan for {}", getBridgeUID());
        discoverMonitors();
    }

    @Override
    public void startScan() {
        logger.debug("Discovery: Starting monitor discovery scan for {}", getBridgeUID());
        discoverMonitors();
    }

    private @Nullable ThingUID getBridgeUID() {
        ZmBridgeHandler localBridgeHandler = bridgeHandler;
        return localBridgeHandler != null ? localBridgeHandler.getThing().getUID() : null;
    }

    private synchronized void discoverMonitors() {
        ZmBridgeHandler localBridgeHandler = bridgeHandler;
        ThingUID bridgeUID = getBridgeUID();
        if (localBridgeHandler != null && bridgeUID != null) {
            Integer alarmDuration = localBridgeHandler.getDefaultAlarmDuration();
            Integer imageRefreshInterval = localBridgeHandler.getDefaultImageRefreshInterval();
            for (Monitor monitor : localBridgeHandler.getSavedMonitors()) {
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
                logger.trace("Discovery: Monitor with id '{}' and name '{}' added to Inbox with UID '{}'",
                        monitor.getId(), monitor.getName(), thingUID);
            }
        }
    }

    private DiscoveryResult createDiscoveryResult(ThingUID monitorUID, ThingUID bridgeUID, String id, String name,
            Map<String, Object> properties) {
        return DiscoveryResultBuilder.create(monitorUID).withProperties(properties).withBridge(bridgeUID)
                .withLabel(buildLabel(name)).withRepresentationProperty(CONFIG_MONITOR_ID).build();
    }

    private String buildLabel(String name) {
        return String.format("Zoneminder Monitor %s", name);
    }
}
