/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.binding.jellyfin.internal.Constants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.discovery.DeviceIdSanitizer;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
     * Registry of all configured Things. Used to detect when a Jellyfin client has regenerated
     * its device ID, so the existing Thing's configuration can be updated instead of creating
     * a duplicate inbox entry.
     *
     * <p>
     * Declared {@code @NonNullByDefault({})} because OSGi DS guarantees injection before activation
     * and the field would otherwise require @Nullable despite being effectively non-null at runtime.
     */
    @NonNullByDefault({})
    private volatile ThingRegistry thingRegistry;

    @Reference
    public void setThingRegistry(ThingRegistry registry) {
        this.thingRegistry = registry;
    }

    public void unsetThingRegistry(ThingRegistry registry) {
        // Intentionally left unset — field is only used during active discovery runs
    }

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
     * - Properties including serial number (device ID), device name, and firmware version (if available)
     *
     * The openHAB Inbox framework automatically handles deduplication:
     * - First call with a ThingUID: fires ADDED event (new discovery)
     * - Subsequent calls with same ThingUID: fires UPDATED event (not ADDED)
     *
     * <p>
     * Device ID regeneration: Jellyfin mobile clients can regenerate their device IDs (e.g. after
     * an app reinstall). When this happens, this method detects the existing configured Thing by
     * matching {@code deviceName} + {@code client} and updates its {@code serialNumber} configuration
     * instead of emitting a new inbox entry.
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

        // ----------------------------------------------------------------
        // Pre-pass: canonical ID expansion (Android short-ID migration)
        //
        // Newer Jellyfin Android apps send only ANDROID_ID (16 hex chars) as deviceId,
        // whereas older versions sent ANDROID_ID + userId (48 hex chars). If a configured
        // Thing still has the old full ID as serialNumber, detect it here by computing the
        // candidate full ID (shortId + userId_hex) and update its config to the short ID so
        // that the ClientHandler re-subscribes on the correct key.
        // ----------------------------------------------------------------
        Set<String> migratedDeviceIds = new HashSet<>();
        for (SessionInfoDto session : clients.values()) {
            String shortId = session.getDeviceId();
            UUID userId = session.getUserId();
            if (shortId == null || shortId.isBlank() || userId == null) {
                continue;
            }
            String candidateFullId = computeCandidateFullId(shortId, userId);
            Thing existingThing = findThingBySerialNumber(bridgeUID, candidateFullId);
            if (existingThing != null) {
                logger.info(
                        "[MIGRATION] Canonical ID expansion: updating serialNumber '{}' → '{}' for device '{}' (client: {})",
                        candidateFullId, shortId, session.getDeviceName(), session.getClient());
                Map<String, Object> updatedConfig = new HashMap<>(existingThing.getConfiguration().getProperties());
                updatedConfig.put("serialNumber", shortId);
                thingRegistry.updateConfiguration(existingThing.getUID(), updatedConfig);
                migratedDeviceIds.add(shortId);
            }
        }

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

            // Skip devices whose serialNumber was just migrated — their Thing config is up to date;
            // no new inbox entry is needed.
            if (migratedDeviceIds.contains(deviceId)) {
                logger.debug("Skipping migrated device '{}' (serialNumber config updated)", deviceId);
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

            // Migration: things created by old binding versions stored serialNumber as a Thing
            // property rather than a configuration parameter. When the new binding XML declares
            // serialNumber as a required <parameter>, the framework sets HANDLER_CONFIGURATION_PENDING
            // and never calls initialize(). Detect this here and promote the value to configuration
            // so the handler can initialize correctly.
            Thing existingThing = thingRegistry.get(clientUID);
            if (existingThing != null && existingThing.getConfiguration().get("serialNumber") == null) {
                String legacySerial = existingThing.getProperties().get(Thing.PROPERTY_SERIAL_NUMBER);
                if (legacySerial != null && !legacySerial.isBlank()) {
                    logger.info(
                            "[MIGRATION] Promoting serialNumber '{}' from property to configuration for {} (legacy thing)",
                            legacySerial, clientUID);
                    Map<String, Object> updatedConfig = new HashMap<>(existingThing.getConfiguration().getProperties());
                    updatedConfig.put("serialNumber", legacySerial);
                    thingRegistry.updateConfiguration(clientUID, updatedConfig);
                    continue; // thing already exists and is now properly configured; skip inbox entry
                }
            }

            // Detect device ID regeneration: if an existing Thing matches by deviceName + client but
            // carries a different serialNumber, update its config rather than emitting a new inbox entry.
            String client = session.getClient();
            if (deviceName != null && !deviceName.isBlank()
                    && handleDeviceIdChange(bridgeUID, deviceId, deviceName, client)) {
                logger.info("Device '{}' (client: {}) regenerated its device ID to {}; updated existing Thing config",
                        deviceName, client, deviceId);
                continue;
            }

            // Build discovery result
            DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(clientUID)
                    .withThingType(THING_TYPE_JELLYFIN_CLIENT).withBridge(bridgeUID).withLabel(label)
                    .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                    .withProperty(Thing.PROPERTY_SERIAL_NUMBER, deviceId);

            // Store deviceName as a thing property so future device-ID-change detection can match it
            if (deviceName != null && !deviceName.isBlank()) {
                resultBuilder.withProperty(PROPERTY_DEVICE_NAME, deviceName);
            }

            // Add firmware version if available
            String appVersion = session.getApplicationVersion();
            if (appVersion != null && !appVersion.isBlank()) {
                resultBuilder.withProperty(Thing.PROPERTY_FIRMWARE_VERSION, appVersion);
            }

            // Add vendor information (client application name) if available
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
     * Searches the Thing registry for an existing configured client Thing under the given bridge that matches
     * the supplied {@code deviceName} and {@code client} (app name).
     *
     * <p>
     * The match is intentionally strict: both {@code deviceName} and {@code client} must be equal
     * (case-insensitive) to the corresponding properties stored on the Thing. Things created before
     * {@link org.openhab.binding.jellyfin.internal.Constants#PROPERTY_DEVICE_NAME} was introduced as a stored
     * property will have no {@code deviceName} entry and are therefore skipped, avoiding false positives.
     *
     * <p>
     * If more than one Thing matches (e.g. two identically-named devices of the same client app), this method
     * returns {@code null} and logs a warning — the caller should fall through to normal discovery.
     *
     * @param bridgeUID the UID of the server bridge
     * @param deviceName the device-name string from the current Jellyfin session
     * @param client the Jellyfin client application name (may be {@code null})
     * @return the single matching Thing, or {@code null} if no unique match was found
     */
    @Nullable
    Thing findExistingThingByIdentity(ThingUID bridgeUID, String deviceName, @Nullable String client) {
        List<Thing> matches = thingRegistry.getAll().stream()
                .filter(t -> THING_TYPE_JELLYFIN_CLIENT.equals(t.getThingTypeUID()))
                .filter(t -> bridgeUID.equals(t.getBridgeUID())).filter(t -> {
                    String storedName = t.getProperties().get(PROPERTY_DEVICE_NAME);
                    if (storedName == null || storedName.isBlank()) {
                        return false; // legacy Thing without deviceName property — skip
                    }
                    if (!deviceName.equalsIgnoreCase(storedName)) {
                        return false;
                    }
                    // Apply client app name check when both sides are known
                    String storedClient = t.getProperties().get(Thing.PROPERTY_VENDOR);
                    if (client != null && !client.isBlank() && storedClient != null && !storedClient.isBlank()) {
                        return client.equalsIgnoreCase(storedClient);
                    }
                    return true;
                }).collect(Collectors.toList());

        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.size() > 1) {
            logger.warn(
                    "Found {} Things matching deviceName='{}' client='{}' — cannot uniquely identify device; skipping ID update",
                    matches.size(), deviceName, client);
        }
        return null;
    }

    /**
     * Handles a potential Jellyfin device ID regeneration event.
     *
     * <p>
     * When a Jellyfin mobile client reinstalls or clears its local storage, it generates a fresh device ID.
     * This method detects the case by looking for an existing configured Thing with the same
     * {@code deviceName} and {@code client} (app name) but a different {@code serialNumber}.
     * If found, the existing Thing's {@code serialNumber} configuration is updated to the new device ID,
     * which triggers the {@link org.openhab.binding.jellyfin.internal.handler.ClientHandler} lifecycle
     * ({@code dispose()} + {@code initialize()}) and causes it to re-subscribe to the
     * {@link org.openhab.binding.jellyfin.internal.events.SessionEventBus} with the new device ID.
     *
     * @param bridgeUID the UID of the server bridge
     * @param newDeviceId the newly observed Jellyfin device ID
     * @param deviceName the device-name label from the current Jellyfin session
     * @param client the Jellyfin client application name (may be {@code null})
     * @return {@code true} if a stale device ID was detected and the existing Thing was updated;
     *         {@code false} if no match was found or the ID is already current
     */
    boolean handleDeviceIdChange(ThingUID bridgeUID, String newDeviceId, String deviceName, @Nullable String client) {
        Thing existing = findExistingThingByIdentity(bridgeUID, deviceName, client);
        if (existing == null) {
            return false;
        }

        Object storedId = existing.getConfiguration().get("serialNumber");
        if (newDeviceId.equals(storedId)) {
            return false; // device ID unchanged — normal update path
        }

        logger.info("Detected device ID change for '{}' (client: {}): {} -> {}", deviceName, client, storedId,
                newDeviceId);

        // Copy the full existing configuration to preserve any future parameters, then update serialNumber
        Map<String, Object> updatedConfig = new HashMap<>(existing.getConfiguration().getProperties());
        updatedConfig.put("serialNumber", newDeviceId);
        thingRegistry.updateConfiguration(existing.getUID(), updatedConfig);
        return true;
    }

    /**
     * Computes the candidate full device ID by appending the user's UUID (hex, no hyphens) to the short ID.
     *
     * <p>
     * Older Jellyfin Android apps sent {@code ANDROID_ID + userId_hex} as the device ID.
     * Newer versions send only the 16-hex-char {@code ANDROID_ID}. This method reconstructs the
     * legacy full ID so callers can check whether a configured Thing still carries the old form.
     *
     * @param shortId the short device ID reported by the Jellyfin server
     * @param userId the user UUID from the session
     * @return the candidate full device ID ({@code shortId} + {@code userId} without hyphens)
     */
    static String computeCandidateFullId(String shortId, UUID userId) {
        return shortId + userId.toString().replace("-", "");
    }

    /**
     * Searches the Thing registry for a configured client Thing under the given bridge whose
     * {@code serialNumber} configuration parameter exactly matches the given value.
     *
     * @param bridgeUID the UID of the server bridge
     * @param serialNumber the serialNumber value to search for
     * @return the matching Thing, or {@code null} if none found
     */
    @Nullable
    Thing findThingBySerialNumber(ThingUID bridgeUID, String serialNumber) {
        return thingRegistry.getAll().stream().filter(t -> THING_TYPE_JELLYFIN_CLIENT.equals(t.getThingTypeUID()))
                .filter(t -> bridgeUID.equals(t.getBridgeUID()))
                .filter(t -> serialNumber.equals(t.getConfiguration().get("serialNumber"))).findFirst().orElse(null);
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
