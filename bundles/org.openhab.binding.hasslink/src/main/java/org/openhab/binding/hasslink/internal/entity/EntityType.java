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
package org.openhab.binding.hasslink.internal.entity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * Interface defining entity type handlers for converting Home Assistant state models
 * into openHAB channels, states, and service calls.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public interface EntityType {

    /**
     * Target attribute key representing the primary state/channel of an entity.
     */
    String PRIMARY_ATTR = "";

    /**
     * Checks if the given attribute string represents the primary entity channel/state.
     *
     * @param attribute the attribute string to check
     * @return true if the attribute is null, empty, or equals {@link #PRIMARY_ATTR}
     */
    static boolean isPrimary(@Nullable String attribute) {
        return attribute == null || attribute.isBlank();
    }

    /**
     * The entity type corresponding to the Home Assistant entity prefix (e.g., "fan", "cover", "light").
     */
    String getType();

    /**
     * Determines whether the entity's domain name (e.g., "Button", "Switch", "Image") should be appended
     * as a suffix to the base channel label when formatted from a stripped object ID.
     * <p>
     * <b>Default Behavior ({@code true}):</b><br>
     * Applies to interactive controls, actuators, cameras, images, and physical components (e.g., {@code button},
     * {@code switch}, {@code light}, {@code image}, {@code camera}, {@code cover}). Object IDs in HA often represent
     * actions or target nouns (e.g., "force_refresh", "cover", "p1s_printer"), so appending the domain creates a clear,
     * natural channel label (e.g., "Force Refresh Button", "Cover Image", "Camera Switch").
     * <p>
     * <b>Telemetry & Value Selectors (Override to {@code false}):</b><br>
     * Overridden by entity handlers representing self-describing properties, measurements, or setpoints
     * (e.g., {@code sensor}, {@code binary_sensor}, {@code number}, {@code select}, {@code text}), where appending
     * internal domain names creates redundant noise (e.g., "Current Layer Sensor" &rarr; "Current Layer",
     * "Printing Speed Select" &rarr; "Printing Speed").
     *
     * @return {@code true} to append the domain name as a label suffix (default); {@code false} if the stripped
     *         object ID is already a complete property name.
     */
    default boolean appendsDomainToLabel() {
        return true;
    }

    /**
     * Returns the channel specifications required for this entity based on its current state and attributes.
     *
     * @param entityState the raw state update from Home Assistant
     * @param context execution context supplying optional bridge handler, channel link predicate, and async consumers
     * @return list of channel specifications for this entity
     */
    List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context);

    /**
     * Builds dynamic metadata overrides (min, max, step, pattern, options) for a specific channel property.
     *
     * @param entityState the current raw state from Home Assistant
     * @param attribute the channel attribute key/suffix (e.g., "target_temperature", "preset_mode")
     * @param context execution context supplying optional bridge handler and channel link state
     * @return a fragment containing state description overrides, or null if no dynamic metadata applies
     */
    default @Nullable StateDescriptionFragment getStateDescriptionFragment( //
            EntityState entityState, //
            String attribute, //
            EntityContext context) {
        return null;
    }

    /**
     * Builds dynamic command options for a specific channel property, allowing for context-sensitive command
     * descriptions.
     *
     * @param entityState the current raw state from Home Assistant
     * @param attribute the channel attribute key/suffix (e.g., "target_temperature", "preset_mode")
     * @param context execution context supplying optional bridge handler and channel link state
     * @return a list of command options for the channel, or null if no dynamic options apply
     */
    default @Nullable List<CommandOption> getCommandOptions( //
            EntityState entityState, //
            String attribute, //
            EntityContext context) {
        return null;
    }

    /**
     * Parses the entity state synchronously into a map of channel key suffixes to openHAB State objects,
     * and optionally dispatches asynchronous or out-of-band event updates.
     * <p>
     * <b>Dispatch Contract:</b>
     * <ul>
     * <li><b>Synchronous State Map:</b> Return persistent channel attributes (e.g., {@code temperature},
     * {@code battery}, {@code brightness}) in the returned map for standard openHAB state updates.</li>
     *
     * <li><b>Guarded Execution:</b> Query {@link EntityContext#isLinked(String)} before triggering heavy or expensive
     * work
     * (e.g., background HTTP image downloads) to avoid executing unnecessary background tasks for unlinked
     * channels.</li>
     *
     * <li><b>Callback Consumer:</b> Use {@link EntityContext#asyncStateConsumer()} for off-thread background updates
     * (e.g., HTTP image fetches), streaming data, or when emitting multiple events for a single attribute
     * that would otherwise overwrite a Map key.</li>
     *
     * <li><b>No Double Dispatch:</b> Any channel attribute value dispatched directly via {@code asyncStateConsumer}
     * <b>MUST NOT</b> be included in the returned map to avoid duplicate channel dispatches in openHAB.</li>
     * </ul>
     *
     * @param entityState the Home Assistant entity state payload
     * @param context execution context supplying optional bridge handler, channel link predicate, and async consumers
     * @return map of synchronously parsed property names to their corresponding openHAB states
     */
    Map<String, ParsedData> parseState(EntityState entityState, EntityContext context);

    /**
     * Converts an openHAB {@link Command} into a Home Assistant {@link ServiceCall} for a target entity attribute,
     * leveraging execution context to resolve global metadata (such as fallback unit system configurations).
     *
     * @param entityId the Home Assistant entity ID (e.g., {@code "climate.living_room"})
     * @param attribute the target channel or attribute key being commanded (e.g., {@code "temperature"},
     *            {@code "power"})
     * @param command the openHAB command received
     * @param entityState the current cached entity state, or {@code null} if unavailable
     * @param context execution context supplying optional bridge handler and channel link state
     * @return an {@link Optional} containing the mapped {@link ServiceCall}, or {@link Optional#empty()}
     *         if the command cannot be mapped or required unit metadata is missing
     */
    default Optional<ServiceCall> toServiceCall( //
            String entityId, //
            String attribute, //
            Command command, //
            @Nullable EntityState entityState, //
            EntityContext context) {
        return Optional.empty();
    }
}
