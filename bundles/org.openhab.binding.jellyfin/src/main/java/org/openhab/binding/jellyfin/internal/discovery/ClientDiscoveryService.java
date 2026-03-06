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

import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_ANDROID;
import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_ANDROID_TV;
import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_INFUSE;
import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_IOS;
import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_JELLYCON;
import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_KODI;
import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_ROKU;
import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_SWIFTFIN;
import static org.openhab.binding.jellyfin.internal.Constants.CLIENT_FILTER_WEB;
import static org.openhab.binding.jellyfin.internal.Constants.DISCOVERABLE_CLIENT_THING_TYPES;
import static org.openhab.binding.jellyfin.internal.Constants.DISCOVERY_RESULT_TTL_SEC;
import static org.openhab.binding.jellyfin.internal.Constants.THING_TYPE_JELLYFIN_CLIENT;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.discovery.DeviceIdSanitizer;
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

        // Deduplicate device IDs that are prefix variants of the same client.
        // Prefer the longest deviceId when one is a prefix of another.
        Map<String, SessionInfoDto> deduped = new LinkedHashMap<>();

        // First pass: build deduplicated map
        for (Map.Entry<String, SessionInfoDto> entry : clients.entrySet()) {
            SessionInfoDto session = entry.getValue();
            String deviceId = session.getDeviceId();

            // Skip clients with missing or empty device ID
            if (deviceId == null || deviceId.isBlank()) {
                logger.debug("Skipping client with missing or empty device ID: sessionId={}, client={}",
                        session.getId(), session.getClient());
                continue;
            }

            // Deduplication: if a previous id is a prefix of this id, prefer the longer id
            boolean handled = false;
            for (String existing : new ArrayList<>(deduped.keySet())) {
                if (existing.startsWith(deviceId)) {
                    // existing is longer or equal -> keep existing
                    handled = true;
                    break;
                }
                if (deviceId.startsWith(existing)) {
                    // new id is longer -> replace existing
                    deduped.remove(existing);
                    deduped.put(deviceId, session);
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                deduped.put(deviceId, session);
            }
        }

        // Second pass: publish discovery results for deduplicated clients
        Configuration config = thingHandler.getThing().getConfiguration().as(Configuration.class);
        for (Map.Entry<String, SessionInfoDto> entry : deduped.entrySet()) {
            SessionInfoDto session = entry.getValue();
            String deviceId = entry.getKey();

            // Apply client category filter
            if (!isClientCategoryEnabled(config, session.getClient())) {
                logger.debug("Skipping client '{}' (category disabled by configuration)", session.getClient());
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

            logger.debug("Discovered Jellyfin client: {} [deviceId={}, client={}]", label, deviceId, client);
        }
    }

    /**
     * Sanitizes a device ID for use in a ThingUID by removing or replacing invalid characters.
     *
     * @param deviceId the raw device ID from the Jellyfin session
     * @return the sanitized device ID safe for use in a ThingUID
     * @see DeviceIdSanitizer#sanitize(String)
     */
    String sanitizeDeviceId(String deviceId) {
        return DeviceIdSanitizer.sanitize(deviceId);
    }

    /**
     * Returns whether discovery of the given client is enabled by the current configuration.
     *
     * <p>
     * The category is determined by matching the client name (case-insensitive) against
     * known client name substrings. Android TV is checked before Android to prevent false matches.
     * A {@code null} or blank client name falls back to the "other" category.
     *
     * @param config the current binding configuration
     * @param clientName the client application name as reported by the Jellyfin server (may be null)
     * @return {@code true} if this client category is enabled for discovery
     */
    private boolean isClientCategoryEnabled(Configuration config, @Nullable String clientName) {
        if (clientName == null || clientName.isBlank()) {
            return config.discoverOtherClients;
        }
        String lower = clientName.toLowerCase();
        if (lower.contains(CLIENT_FILTER_WEB)) {
            return config.discoverWebClients;
        }
        // Check Android TV before Android to avoid false positive matches
        if (lower.contains(CLIENT_FILTER_ANDROID_TV)) {
            return config.discoverAndroidTvClients;
        }
        if (lower.contains(CLIENT_FILTER_ANDROID)) {
            return config.discoverAndroidClients;
        }
        if (lower.contains(CLIENT_FILTER_IOS) || lower.contains(CLIENT_FILTER_SWIFTFIN)
                || lower.contains(CLIENT_FILTER_INFUSE)) {
            return config.discoverIosClients;
        }
        if (lower.contains(CLIENT_FILTER_KODI) || lower.contains(CLIENT_FILTER_JELLYCON)) {
            return config.discoverKodiClients;
        }
        if (lower.contains(CLIENT_FILTER_ROKU)) {
            return config.discoverRokuClients;
        }
        return config.discoverOtherClients;
    }
}
