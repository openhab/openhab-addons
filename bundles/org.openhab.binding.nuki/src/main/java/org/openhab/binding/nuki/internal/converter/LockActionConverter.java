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

import org.openhab.binding.nuki.internal.NukiBindingConstants;

/**
 * The {@link LockActionConverter} is responsible for mapping Binding Lock States to Bridge HTTP-API Lock Actions.
 *
 * @author Markus Katter - Initial contribution
 */
public abstract class LockActionConverter {

    private static Map<Integer, Integer> mapping;

    private static void setupMapping() {
        mapping = new HashMap<>();
        mapping.put(NukiBindingConstants.LOCK_STATES_UNLOCKING, NukiBindingConstants.LOCK_ACTIONS_UNLOCK);
        mapping.put(NukiBindingConstants.LOCK_STATES_LOCKING, NukiBindingConstants.LOCK_ACTIONS_LOCK);
        mapping.put(NukiBindingConstants.LOCK_STATES_UNLATCHING, NukiBindingConstants.LOCK_ACTIONS_UNLATCH);
        mapping.put(NukiBindingConstants.LOCK_STATES_UNLOCKING_LOCKNGO,
                NukiBindingConstants.LOCK_ACTIONS_LOCKNGO_UNLOCK);
        mapping.put(NukiBindingConstants.LOCK_STATES_UNLATCHING_LOCKNGO,
                NukiBindingConstants.LOCK_ACTIONS_LOCKNGO_UNLATCH);
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
