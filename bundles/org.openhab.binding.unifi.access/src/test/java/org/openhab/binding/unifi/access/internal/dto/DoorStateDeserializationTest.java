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
package org.openhab.binding.unifi.access.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests Gson deserialization of {@link DoorState} enums with their
 * {@code @SerializedName} alternatives.
 *
 * @author Dan Cunningham - Initial contribution
 */
class DoorStateDeserializationTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    /** Helper that wraps a JSON value so Gson can target the enum field. */
    private <T> T deserialize(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    // --- LockState ---

    @Test
    void lockStateLockDeserializesToLocked() {
        DoorState.LockState result = deserialize("\"lock\"", DoorState.LockState.class);
        assertEquals(DoorState.LockState.LOCKED, result);
    }

    @Test
    void lockStateLockedDeserializesToLocked() {
        DoorState.LockState result = deserialize("\"locked\"", DoorState.LockState.class);
        assertEquals(DoorState.LockState.LOCKED, result);
    }

    @Test
    void lockStateUnlockDeserializesToUnlocked() {
        DoorState.LockState result = deserialize("\"unlock\"", DoorState.LockState.class);
        assertEquals(DoorState.LockState.UNLOCKED, result);
    }

    @Test
    void lockStateUnlockedDeserializesToUnlocked() {
        DoorState.LockState result = deserialize("\"unlocked\"", DoorState.LockState.class);
        assertEquals(DoorState.LockState.UNLOCKED, result);
    }

    // --- DoorPosition ---

    @Test
    void doorPositionOpenDeserializesToOpen() {
        DoorState.DoorPosition result = deserialize("\"open\"", DoorState.DoorPosition.class);
        assertEquals(DoorState.DoorPosition.OPEN, result);
    }

    @Test
    void doorPositionCloseDeserializesToClose() {
        DoorState.DoorPosition result = deserialize("\"close\"", DoorState.DoorPosition.class);
        assertEquals(DoorState.DoorPosition.CLOSE, result);
    }

    // --- DoorLockRuleType ---

    @Test
    void doorLockRuleTypeCustom() {
        DoorState.DoorLockRuleType result = deserialize("\"custom\"", DoorState.DoorLockRuleType.class);
        assertEquals(DoorState.DoorLockRuleType.CUSTOM, result);
    }

    @Test
    void doorLockRuleTypeKeepUnlock() {
        DoorState.DoorLockRuleType result = deserialize("\"keep_unlock\"", DoorState.DoorLockRuleType.class);
        assertEquals(DoorState.DoorLockRuleType.KEEP_UNLOCK, result);
    }

    @Test
    void doorLockRuleTypeKeepLock() {
        DoorState.DoorLockRuleType result = deserialize("\"keep_lock\"", DoorState.DoorLockRuleType.class);
        assertEquals(DoorState.DoorLockRuleType.KEEP_LOCK, result);
    }

    @Test
    void doorLockRuleTypeReset() {
        DoorState.DoorLockRuleType result = deserialize("\"reset\"", DoorState.DoorLockRuleType.class);
        assertEquals(DoorState.DoorLockRuleType.RESET, result);
    }

    @Test
    void doorLockRuleTypeLockEarly() {
        DoorState.DoorLockRuleType result = deserialize("\"lock_early\"", DoorState.DoorLockRuleType.class);
        assertEquals(DoorState.DoorLockRuleType.LOCK_EARLY, result);
    }

    @Test
    void doorLockRuleTypeLockNow() {
        DoorState.DoorLockRuleType result = deserialize("\"lock_now\"", DoorState.DoorLockRuleType.class);
        assertEquals(DoorState.DoorLockRuleType.LOCK_NOW, result);
    }

    @Test
    void doorLockRuleTypeSchedule() {
        DoorState.DoorLockRuleType result = deserialize("\"schedule\"", DoorState.DoorLockRuleType.class);
        assertEquals(DoorState.DoorLockRuleType.SCHEDULE, result);
    }

    @Test
    void doorLockRuleTypeEmptyStringDeserializesToNone() {
        DoorState.DoorLockRuleType result = deserialize("\"\"", DoorState.DoorLockRuleType.class);
        assertEquals(DoorState.DoorLockRuleType.NONE, result);
    }
}
