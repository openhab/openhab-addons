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
package org.openhab.binding.dreame.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * MIoT properties used by Dreame robotic mowers.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public enum DreameProperty {
    STATE(0, 2, 1),
    ERROR(1, 2, 2),
    BATTERY_LEVEL(2, 3, 1),
    CHARGING_STATUS(3, 3, 2),
    STATUS(5, 4, 1),
    DND(70, 5, 1),
    DND_TASK(73, 5, 4),
    DND_STATUS(74, 2, 51),
    TOTAL_MOWING_TIME(110, 12, 2),
    MOWING_SESSIONS(111, 12, 3),
    TOTAL_MOWED_AREA(112, 12, 4);

    private final int id;
    private final int serviceId;
    private final int propertyId;

    DreameProperty(int id, int serviceId, int propertyId) {
        this.id = id;
        this.serviceId = serviceId;
        this.propertyId = propertyId;
    }

    public int id() {
        return id;
    }

    public int serviceId() {
        return serviceId;
    }

    public int propertyId() {
        return propertyId;
    }
}
