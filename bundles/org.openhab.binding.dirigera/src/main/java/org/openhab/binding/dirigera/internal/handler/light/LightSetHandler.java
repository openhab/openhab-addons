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
package org.openhab.binding.dirigera.internal.handler.light;

import static org.openhab.binding.dirigera.internal.interfaces.Model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.DirigeraStateDescriptionProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LightSetHandler} controls a DIRIGERA light set (multiple lights as one logical unit).
 * Commands are sent to the hub via the /devices/set/{id} endpoint instead of /devices/{id}.
 * All four light capabilities are supported: on/off, brightness, color temperature, and color.
 *
 * The handler registers under the set ID (config.id) as well as each member device ID so the
 * gateway routes websocket updates to handleUpdate for both set-level and member-level events.
 * The set is ONLINE if at least one member reports isReachable=true.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - add device set handling
 */
@NonNullByDefault
public class LightSetHandler extends ColorLightHandler {
    private final Logger logger = LoggerFactory.getLogger(LightSetHandler.class);

    /**
     * Tracks per-member reachability: memberId -> isReachable.
     * ConcurrentHashMap because handleUpdate() is called from the WebSocket thread
     * while initializeDevice() / dispose() run on the openHAB framework thread.
     */
    private final Map<String, Boolean> memberReachability = new ConcurrentHashMap<>();
    /**
     * Member device IDs belonging to this set.
     * Wrapped for thread-safety (see memberReachability note above).
     */
    private final List<String> memberDeviceIds = Collections.synchronizedList(new ArrayList<>());

    public LightSetHandler(Thing thing, Map<String, String> mapping, DirigeraStateDescriptionProvider stateProvider) {
        super(thing, mapping, stateProvider);
        super.setChildHandler(this);
    }

    @Override
    public void initializeDevice() {
        // 1) Get all member device IDs for this set from the model
        memberDeviceIds.clear();
        memberDeviceIds.addAll(gateway().model().getMemberDeviceIds(config.id));
        if (memberDeviceIds.isEmpty()) {
            logger.warn("DIRIGERA LIGHT_SET {} no member devices found for set id {}", thing.getLabel(), config.id);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No member devices found for light set");
            return;
        }
        if (customDebug) {
            logger.info("DIRIGERA LIGHT_SET {} member devices: {}", thing.getLabel(), memberDeviceIds);
        }

        // initialize all members as not reachable
        memberReachability.clear();
        memberDeviceIds.forEach(id -> memberReachability.put(id, false));

        updateProperties();

        // 2) Initialize the set's own customName from the model so that subsequent member
        // updates (which carry the member's own customName) cannot overwrite it.
        // getPropertiesFor() returns a map with ATTRIBUTES_KEY_CUSTOM_NAME = set name for light sets.
        Object setName = gateway().model().getPropertiesFor(config.id).get(ATTRIBUTES_KEY_CUSTOM_NAME);
        if (setName instanceof String nameStr && !nameStr.isBlank()) {
            JSONObject nameInit = new JSONObject();
            JSONObject attributes = new JSONObject();
            attributes.put(ATTRIBUTES_KEY_CUSTOM_NAME, nameStr);
            nameInit.put(JSON_KEY_ATTRIBUTES, attributes);
            super.handleUpdate(nameInit);
        }

        // 3) Register under the set ID itself (for future set-level events from the hub)
        // and under each member device ID so the gateway routes member websocket updates.
        // registerDevice() does not throw checked exceptions; any gateway NPE is prevented
        // by the null-guard in BaseHandler.gateway(), so no try-catch is needed here.
        gateway().registerDevice(child, config.id);
        memberDeviceIds.forEach(memberId -> gateway().registerDevice(child, memberId));

        // 4) Poll current state for each reachable member so the handler reaches ONLINE
        // immediately without waiting for the first websocket event.
        // Only call handleUpdate for reachable members — unreachable ones stay false in
        // memberReachability (initialized above) and must not overwrite channel state.
        for (String memberId : memberDeviceIds) {
            JSONObject deviceState = gateway().api().readDevice(memberId);
            if (deviceState.optBoolean(JSON_KEY_REACHABLE, false)) {
                handleUpdate(deviceState);
            }
        }

        // 5) If no member reported isReachable=true, go OFFLINE explicitly.
        // This covers the case where readDevice returned empty/error for all members.
        boolean anyReachable = memberReachability.values().stream().anyMatch(r -> r);
        if (!anyReachable) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/dirigera.device.status.not-reachable");
        }
    }

    /**
     * Receives websocket updates from all registered member devices.
     * Aggregates isReachable across all members: ONLINE if at least one is reachable.
     * Delegates attribute updates (brightness, color, etc.) to the parent only when online.
     */
    @Override
    public void handleUpdate(JSONObject update) {
        if (customDebug) {
            logger.info("DIRIGERA LIGHT_SET {} handleUpdate {}", thing.getLabel(), update);
        }
        JSONObject stripped = new JSONObject(update, update.keySet().toArray(new String[0]));

        // strip customName for each member update
        if (update.has(JSON_KEY_ATTRIBUTES)) {
            stripped.getJSONObject(JSON_KEY_ATTRIBUTES).remove(ATTRIBUTES_KEY_CUSTOM_NAME);
        }

        // handle reachable flag for deviceSet
        if (update.has(JSON_KEY_REACHABLE)) {
            // identify which member sent this update and track its reachability
            String sourceId = update.optString(JSON_KEY_DEVICE_ID, "");
            if (memberReachability.containsKey(sourceId)) {
                memberReachability.put(sourceId, update.getBoolean(JSON_KEY_REACHABLE));
            }

            boolean anyReachable = memberReachability.values().stream().anyMatch(r -> r);
            if (anyReachable) {
                online = true;
                updateStatus(ThingStatus.ONLINE);
            } else {
                online = false;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/dirigera.device.status.not-reachable");
            }

            // Strip isReachable so the parent handleUpdate does not override our status.
            // Shallow copy via keySet() avoids the expensive toString()/parse round-trip.
            stripped.remove(JSON_KEY_REACHABLE);
            // Also strip customName from member attributes: each member carries its own
            // device name, which must not overwrite the set name initialized in initializeDevice().
            if (stripped.has(JSON_KEY_ATTRIBUTES)) {
                stripped.getJSONObject(JSON_KEY_ATTRIBUTES).remove(ATTRIBUTES_KEY_CUSTOM_NAME);
            }
        }
        super.handleUpdate(stripped);
    }

    /**
     * Unregister from all member device IDs on dispose.
     * The set ID (config.id) is unregistered by super.dispose().
     *
     * Ordering rationale: member IDs are unregistered BEFORE super.dispose() so that
     * no further WebSocket events are routed to this handler while BaseHandler tears down.
     * super.dispose() is called last to ensure config.id is also unregistered cleanly.
     */
    @Override
    public void dispose() {
        memberDeviceIds.forEach(memberId -> {
            try {
                gateway().unregisterDevice(child, memberId);
            } catch (Exception e) {
                logger.debug("DIRIGERA LIGHT_SET {} unregister {} failed: {}", thing.getLabel(), memberId,
                        e.getMessage());
            }
        });
        memberDeviceIds.clear();
        memberReachability.clear();
        super.dispose();
    }

    /**
     * Override sendAttributes to route all commands to /devices/set/{id}.
     */
    @Override
    protected int sendAttributes(JSONObject attributes) {
        logger.trace("DIRIGERA LIGHT_SET {} sending set attributes {}", thing.getLabel(), attributes);
        return super.sendSetAttributes(attributes);
    }
}
