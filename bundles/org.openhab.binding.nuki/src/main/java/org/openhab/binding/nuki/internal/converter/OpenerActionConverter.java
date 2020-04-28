/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nuki.internal.NukiBindingConstants;

/**
 * The {@link LockActionConverter} is responsible for mapping Binding Lock States to Bridge HTTP-API Lock Actions.
 *
 * @author Markus Katter - Initial contribution
 * @author Alexander Koch - Add Nuki Opener Support
 */

@NonNullByDefault
public abstract class OpenerActionConverter {

    private static @Nullable Map<Integer, Integer> mapping;

    private static void setupMapping() {
        mapping = new HashMap<>();
        mapping.put(NukiBindingConstants.OPENER_STATES_OPENING, NukiBindingConstants.OPENER_ACTIONS_OPEN);
        mapping.put(NukiBindingConstants.OPENER_STATES_RTO_ACTIVE, NukiBindingConstants.OPENER_ACTIONS_ACTIVATE_RTO);
        mapping.put(NukiBindingConstants.OPENER_STATES_ONLINE, NukiBindingConstants.OPENER_ACTIONS_DEACTIVATE_RTO);
        mapping.put(NukiBindingConstants.OPENER_STATES_OPEN, NukiBindingConstants.OPENER_ACTIONS_ACTIVATE_CM);
        mapping.put(NukiBindingConstants.OPENER_STATES_UNTRAINED, NukiBindingConstants.OPENER_ACTIONS_DEACTIVATE_CM);
    }

    public static int getLockActionFor(int lockState) {
        if (mapping == null) {
            setupMapping();
        }
        return mapping.get(lockState);
    }

    public static int getLockStateFor(int lockAction) {
        if (mapping == null) {
            setupMapping();
        }
        for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
            if (entry.getValue() == lockAction) {
                return entry.getKey();
            }
        }
        return 0;
    }
}
