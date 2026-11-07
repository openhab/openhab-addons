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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Vacuum actions from the Dreame vacuum action mapping, separate from mower actions.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public enum DreameVacuumAction {
    START(2, 1),
    PAUSE(2, 2),
    DOCK(3, 1),
    STOP(4, 2),
    LOCATE(7, 1),
    AUTO_EMPTY(15, 1),
    WASH_MOPS(4, 4, 10, "2,1"),
    PAUSE_WASHING(4, 4, 10, "1,0"),
    START_DRYING(4, 4, 10, "3,1"),
    STOP_DRYING(4, 4, 10, "3,0");

    private final int serviceId;
    private final int actionId;
    private final int inputPropertyId;
    private final @Nullable String inputValue;

    DreameVacuumAction(int serviceId, int actionId) {
        this(serviceId, actionId, 0, null);
    }

    DreameVacuumAction(int serviceId, int actionId, int inputPropertyId, @Nullable String inputValue) {
        this.serviceId = serviceId;
        this.actionId = actionId;
        this.inputPropertyId = inputPropertyId;
        this.inputValue = inputValue;
    }

    public int serviceId() {
        return serviceId;
    }

    public int actionId() {
        return actionId;
    }

    public int inputPropertyId() {
        return inputPropertyId;
    }

    public @Nullable String inputValue() {
        return inputValue;
    }
}
