/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumeration of all lock actions Nuki Opener accepts
 *
 * @author Jan Vybíral - Initial contribution
 */
@NonNullByDefault
public enum OpenerAction {
    ACTIVATE_RING_TO_OPEN(1),
    DEACTIVATE_RING_TO_OPEN(2),
    ELECTRIC_STRIKE_ACTUATION(3),
    ACTIVATE_CONTINUOUS_MODE(4),
    DEACTIVATE_CONTINUOUS_MODE(5);

    private final int action;

    OpenerAction(int action) {
        this.action = action;
    }

    public static @Nullable OpenerAction fromAction(int action) {
        for (OpenerAction value : values()) {
            if (value.action == action) {
                return value;
            }
        }
        return null;
    }

    public int getAction() {
        return action;
    }
}
