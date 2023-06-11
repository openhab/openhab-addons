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
package org.openhab.binding.iaqualink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Heater States returned by iAquaLink API
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public enum HeaterState {
    OFF("0", "off"),
    HEATING("1", "on"),
    ENABLED("3", "enabled");

    String value;
    String label;

    HeaterState(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static @Nullable HeaterState fromValue(String value) {
        for (HeaterState state : HeaterState.values()) {
            if (state.value.equals(value)) {
                return state;
            }
        }
        return null;
    }
}
