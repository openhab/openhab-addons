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
package org.openhab.binding.digiplex.internal.communication.events;

import static org.openhab.binding.digiplex.internal.DigiplexBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Trouble event type.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public enum TroubleType {
    TLM_TROUBLE(BRIDGE_TLM_TROUBLE),
    AC_FAILURE(BRIDGE_AC_FAILURE),
    BATTERY_FAILURE(BRIDGE_BATTERY_FAILURE),
    AUXILIARY_CURRENT_LIMIT(BRIDGE_AUX_CURRENT_LIMIT),
    BELL_CURRENT_LIMIT(BRIDGE_BELL_CURRENT_LIMIT),
    BELL_ABSENT(BRIDGE_BELL_ABSENT),
    CLOCK_TROUBLE(BRIDGE_CLOCK_TROUBLE),
    GLOBAL_FIRE_LOOP(BRIDGE_GLOBAL_FIRE_LOOP);

    private String bridgeChannel;

    private TroubleType(String bridgeChannel) {
        this.bridgeChannel = bridgeChannel;
    }

    public String getBridgeChannel() {
        return bridgeChannel;
    }

    public static TroubleType fromEventNumber(int eventNumber) {
        return TroubleType.values()[eventNumber];
    }
}
