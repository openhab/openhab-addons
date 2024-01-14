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
package org.openhab.binding.jellyfin.internal.discovery;

import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.DISCOVERY_RESULT_TTL_SEC;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.THING_TYPE_CLIENT;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.THING_TYPE_SERVER;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jellyfin.sdk.api.client.exception.ApiClientException;
import org.jellyfin.sdk.api.client.exception.InvalidStatusException;
import org.jellyfin.sdk.model.api.SessionInfo;
import org.openhab.binding.jellyfin.internal.handler.JellyfinServerHandler;
import org.openhab.binding.jellyfin.internal.util.SyncCallback;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JellyfinClientDiscoveryService} discover Jellyfin clients connected to the server.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = JellyfinClientDiscoveryService.class)
@NonNullByDefault
public class JellyfinClientDiscoveryService extends AbstractThingHandlerDiscoveryService<JellyfinServerHandler> {
    private final Logger logger = LoggerFactory.getLogger(JellyfinClientDiscoveryService.class);

    public JellyfinClientDiscoveryService() throws IllegalArgumentException {
        super(JellyfinServerHandler.class, Set.of(THING_TYPE_SERVER), 60);
    }

    @Override
    protected void startScan() {
        if (!thingHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.warn("Server handler {} is not online.", thingHandler.getThing().getLabel());
            return;
        }
        logger.debug("Searching devices for server {}", thingHandler.getThing().getLabel());
        try {
            thingHandler.getControllableSessions().forEach(this::discoverDevice);
        } catch (SyncCallback.SyncCallbackError syncCallbackError) {
            logger.error("Unexpected error: {}", syncCallbackError.getMessage());
        } catch (InvalidStatusException e) {
            logger.warn("Api client error with status{}: {}", e.getStatus(), e.getMessage());
        } catch (ApiClientException e) {
            logger.warn("Api client error: {}", e.getMessage());
        }
    }

    public void discoverDevice(SessionInfo info) {
        var id = info.getDeviceId();
        if (id == null) {
            logger.warn("missing device id aborting");
            return;
        }
        logger.debug("Client discovered: [{}] {}", id, info.getDeviceName());
        var bridgeUID = thingHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, id);
        var appVersion = info.getApplicationVersion();
        if (appVersion != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, appVersion);
        }
        var client = info.getApplicationVersion();
        if (client != null) {
            properties.put(Thing.PROPERTY_VENDOR, client);
        }
        thingDiscovered(
                DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_CLIENT, bridgeUID, id)).withBridge(bridgeUID)
                        .withTTL(DISCOVERY_RESULT_TTL_SEC).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                        .withProperties(properties).withLabel(info.getDeviceName()).build());
    }
}
