/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Enumeration of all lock actions Nuki Smart Lock accepts
 *
 * @author Jan Vybíral - Initial contribution
 */
@NonNullByDefault
public enum SmartLockAction {
    UNLOCK(1),
    LOCK(2),
    UNLATCH(3),
    LOCK_N_GO(4),
    LOCK_N_GO_WITH_UNLATCH(5);

    private final int action;

    SmartLockAction(int action) {
        this.action = action;
    }

    public static @Nullable SmartLockAction fromAction(int action) {
        for (SmartLockAction value : values()) {
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
