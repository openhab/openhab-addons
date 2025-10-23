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
package org.openhab.binding.unifiaccess.internal.dto;

/**
 * Lock rule payload and value object used for both setting and reading lock rules.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class DoorLockRule {
    public DoorState.DoorLockRuleType type;
    /** minutes, only for type=custom */
    public Integer interval;

    public DoorLockRule(DoorState.DoorLockRuleType type, Integer interval) {
        this.type = type;
        this.interval = interval;
    }

    public static DoorLockRule keepUnlock() {
        return new DoorLockRule(DoorState.DoorLockRuleType.KEEP_UNLOCK, null);
    }

    public static DoorLockRule keepLock() {
        return new DoorLockRule(DoorState.DoorLockRuleType.KEEP_LOCK, null);
    }

    public static DoorLockRule customMinutes(int minutes) {
        return new DoorLockRule(DoorState.DoorLockRuleType.CUSTOM, minutes);
    }

    public static DoorLockRule reset() {
        return new DoorLockRule(DoorState.DoorLockRuleType.RESET, null);
    }

    public static DoorLockRule lockEarly() {
        return new DoorLockRule(DoorState.DoorLockRuleType.LOCK_EARLY, null);
    }
}
