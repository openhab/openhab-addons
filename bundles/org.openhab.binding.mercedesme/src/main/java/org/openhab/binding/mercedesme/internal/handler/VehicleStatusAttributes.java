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
 * Internal carrier for a single vehicle's status update, passed from {@link AccountHandler} to
 * {@link VehicleHandler}. The VIN is deliberately not part of the record - callers key updates by VIN.
 *
 * @param fullUpdate {@code true} if this is a full snapshot, {@code false} for a partial/delta update
 * @param attributes attribute key ({@code MB_KEY_*}) to value map
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record VehicleStatusAttributes(boolean fullUpdate, Map<String, VehicleAttributeStatus> attributes) {

    /**
     * Defensive copy - the map is handed off across a scheduler thread boundary between the two handlers.
     */
    public VehicleStatusAttributes {
        attributes = Map.copyOf(attributes);
    }
}
