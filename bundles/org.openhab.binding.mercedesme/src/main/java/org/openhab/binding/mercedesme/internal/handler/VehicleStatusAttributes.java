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
package org.openhab.binding.mercedesme.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;

/**
 * {@link VehicleStatusAttributes} is the internal carrier for a single vehicle's status update. It is the
 * sole type passed between {@link AccountHandler} and {@link VehicleHandler}.
 * <p>
 * {@link AccountHandler} builds it from the typed {@code VehicleStatusUpdate} push, converted via
 * {@link org.openhab.binding.mercedesme.internal.utils.Mapper#fromVehicleStatusUpdate}. It originally
 * replaced the protobuf-generated {@code VEPUpdate} in this role, back when both the legacy and the typed
 * push were still normalized into it; the legacy {@code VEPUpdate} WebSocket ingress branch (and everything
 * downstream of it) has since been removed entirely - see ADR-002,
 * {@code docs/ADR/002-unified-vehicle-status-update-carrier.md}, and its addendum.
 * <p>
 * The vehicle identification number is deliberately not part of this record: every existing caller keys
 * updates by VIN externally (map key / {@code VehicleHandler.config.vin}).
 *
 * @param fullUpdate {@code true} if this is a full snapshot, {@code false} for a partial/delta update
 * @param attributes attribute key ({@code MB_KEY_*}) to value map
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record VehicleStatusAttributes(boolean fullUpdate, Map<String, VehicleAttributeStatus> attributes) {

    /**
     * Defensive copy so neither {@link org.openhab.binding.mercedesme.internal.handler.AccountHandler} nor
     * {@link org.openhab.binding.mercedesme.internal.handler.VehicleHandler} can accidentally mutate a map
     * that is handed off across the scheduler thread boundary between them.
     */
    public VehicleStatusAttributes {
        attributes = Map.copyOf(attributes);
    }
}
