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
package org.openhab.binding.bluelink.internal.api;

/**
 * Abstracts from the region-specific vehicle information representations.
 * 
 * @author Florian Hotze - Initial contribution
 */
public record Vehicle(
        // generic
        String registrationId, String name, String model, String vin, String engineType, boolean electric,
        // US only:
        Integer generation, Double odometer,
        // EU only:
        Boolean ccs2ProtocolSupport) {
    public String getDisplayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return model;
    }
}
