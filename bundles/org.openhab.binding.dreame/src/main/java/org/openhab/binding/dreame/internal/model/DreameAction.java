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
 * Actions supported by the mower control service.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public enum DreameAction {
    START(5, 1),
    STOP(5, 2),
    DOCK(5, 3),
    PAUSE(5, 4);

    private final int serviceId;
    private final int actionId;

    DreameAction(int serviceId, int actionId) {
        this.serviceId = serviceId;
        this.actionId = actionId;
    }

    public int serviceId() {
        return serviceId;
    }

    public int actionId() {
        return actionId;
    }
}
