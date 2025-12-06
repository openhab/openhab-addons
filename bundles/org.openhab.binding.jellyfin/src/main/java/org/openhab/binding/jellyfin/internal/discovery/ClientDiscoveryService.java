/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.jellyfin.internal.Constants.DISCOVERABLE_CLIENT_THING_TYPES;
import static org.openhab.binding.jellyfin.internal.Constants.DISCOVERY_RESULT_TTL_SEC;
import static org.openhab.binding.jellyfin.internal.Constants.THING_TYPE_JELLYFIN_CLIENT;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
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
 * The {@link ClientDiscoveryService} discovers Jellyfin client devices connected to a Jellyfin server.
 *
 * This discovery service is bridge-bound and depends on {@link ServerHandler} to provide the list of active
 * client sessions. It automatically discovers clients when the server handler updates its client list and
 * supports both manual scans and automatic background discovery.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ClientDiscoveryService.class)
@NonNullByDefault
public class ClientDiscoveryService extends AbstractThingHandlerDiscoveryService<ServerHandler> {
    private static final Logger logger = LoggerFactory.getLogger(ClientDiscoveryService.class);

    /**
     * Creates a new instance of the client discovery service.
     *
     * @throws IllegalArgumentException if service initialization fails
     */
    public ClientDiscoveryService() throws IllegalArgumentException {
        super(ServerHandler.class, DISCOVERABLE_CLIENT_THING_TYPES, DISCOVERY_RESULT_TTL_SEC, false);
    }

    @Override
    public void initialize() {
        // Notify the server handler that the discovery service is now available
        // This allows the TaskManager to initialize DiscoveryTask with a valid service reference
        thingHandler.onDiscoveryServiceInitialized(this);
        super.initialize();
    }

    @Override
    protected void startScan() {
        ThingStatus serverStatus = thingHandler.getThing().getStatus();
        if (serverStatus != ThingStatus.ONLINE) {
            logger.debug("Server handler {} is not online (status: {}), skipping client discovery",
                    thingHandler.getThing().getLabel(), serverStatus);
            return;
        }

        logger.debug("Starting client discovery scan for server {}", thingHandler.getThing().getLabel());
        discoverClients();
    }

    /**
     * Discovers Jellyfin client devices from the server handler's client list.
     *
     * This method retrieves the current list of active client sessions from the server handler,
     * validates each client, and creates discovery results for valid clients. Clients with missing
     * or empty device IDs are skipped with a debug log message.
     *
     * For each valid client, a discovery result is created with:
     * - ThingUID based on the sanitized device ID
     * - Representation property using device name (with fallback to client name)
     * - Properties including serial number (device ID) and firmware version (if available)
     *
     * The openHAB Inbox framework automatically handles deduplication:
     * - First call with a ThingUID: fires ADDED event (new discovery)
     * - Subsequent calls with same ThingUID: fires UPDATED event (not ADDED)
     *
     * This method is called:
     * - When a manual scan is triggered
     * - During background discovery (every 60 seconds)
     * - When the server handler updates its client list
     */
    public void discoverClients() {
        Map<String, SessionInfoDto> clients = thingHandler.getClients();
        ThingUID bridgeUID = thingHandler.getThing().getUID();

        if (clients.isEmpty()) {
            logger.debug("No clients found for server {}", thingHandler.getThing().getLabel());
            return;
        }

        logger.debug("Processing {} client(s) for discovery", clients.size());

        for (Map.Entry<String, SessionInfoDto> entry : clients.entrySet()) {
            SessionInfoDto session = entry.getValue();
            String deviceId = session.getDeviceId();

            // Skip clients with missing or empty device ID
            if (deviceId == null || deviceId.isBlank()) {
                logger.debug("Skipping client with missing or empty device ID: sessionId={}, client={}",
                        session.getId(), session.getClient());
                continue;
            }

            // Sanitize device ID for use in ThingUID (remove special characters)
            String sanitizedDeviceId = sanitizeDeviceId(deviceId);

            // Create ThingUID for this client
            ThingUID clientUID = new ThingUID(THING_TYPE_JELLYFIN_CLIENT, bridgeUID, sanitizedDeviceId);

            // Determine label and representation property
            // Prefer deviceName for user-friendliness, fallback to client app name
            String deviceName = session.getDeviceName();
            String label;

            if (deviceName != null && !deviceName.isBlank()) {
                label = deviceName;
            } else {
                String clientName = session.getClient();
                label = clientName != null && !clientName.isBlank() ? clientName : "Jellyfin Client";
            }

            // Build discovery result
            DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(clientUID)
                    .withThingType(THING_TYPE_JELLYFIN_CLIENT).withBridge(bridgeUID).withLabel(label)
                    .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                    .withProperty(Thing.PROPERTY_SERIAL_NUMBER, deviceId);

            // Add firmware version if available
            String appVersion = session.getApplicationVersion();
            if (appVersion != null && !appVersion.isBlank()) {
                resultBuilder.withProperty(Thing.PROPERTY_FIRMWARE_VERSION, appVersion);
            }

            // Add vendor information (client application name) if available
            String client = session.getClient();
            if (client != null && !client.isBlank()) {
                resultBuilder.withProperty(Thing.PROPERTY_VENDOR, client);
            }

            // Publish discovery result
            thingDiscovered(resultBuilder.build());

            logger.info("Discovered Jellyfin client: {} [deviceId={}, client={}]", label, deviceId, client);
        }
    }

    /**
     * Sanitizes a device ID for use in a ThingUID by removing or replacing invalid characters.
     *
     * ThingUIDs have strict requirements: only alphanumeric characters, hyphens, and underscores are allowed.
     * This method replaces any other characters with hyphens to ensure valid ThingUID generation.
     *
     * @param deviceId the raw device ID from the Jellyfin session
     * @return the sanitized device ID safe for use in a ThingUID
     */
    private String sanitizeDeviceId(String deviceId) {
        // Replace any character that is not alphanumeric, hyphen, or underscore with a hyphen
        return deviceId.replaceAll("[^a-zA-Z0-9_-]", "-");
    }
}
