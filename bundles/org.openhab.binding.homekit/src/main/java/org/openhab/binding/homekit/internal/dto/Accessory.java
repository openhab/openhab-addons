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
package org.openhab.binding.homekit.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.AccessoryType;

/**
 * HomeKit accessory DTO
 * Used to deserialize individual accessories from the /accessories endpoint of a HomeKit bridge.
 * Each accessory has an accessory ID (aid) and a list of services.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Accessory {
    public @Nullable Integer aid; // e.g. 1
    public @Nullable List<Service> services;

    public AccessoryType getAccessoryType() {
        Integer aid = this.aid;
        if (aid == null) {
            return AccessoryType.OTHER;
        }
        return AccessoryType.from(aid);
    }
}
