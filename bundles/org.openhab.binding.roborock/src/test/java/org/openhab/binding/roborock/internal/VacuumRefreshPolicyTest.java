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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault({})
class VacuumRefreshPolicyTest {

    @Test
    void stateIdChangeRequestsImmediateStatusOnlyWhenKnownAndChanged() {
        assertFalse(VacuumRefreshPolicy.shouldRequestImmediateStatus(null, 5));
        assertFalse(VacuumRefreshPolicy.shouldRequestImmediateStatus(Integer.valueOf(5), 5));
        assertTrue(VacuumRefreshPolicy.shouldRequestImmediateStatus(Integer.valueOf(5), 6));
    }

    @Test
    void statusPollDelayUsesFastIntervalWhenVacuumIsOn() {
        assertEquals(Integer.valueOf(15),
                VacuumRefreshPolicy.getStatusPollDelaySeconds(RoborockCommunicationMode.DIRECT, true, 15));
    }

    @Test
    void statusPollDelayDisabledWhenVacuumIsOff() {
        assertNull(VacuumRefreshPolicy.getStatusPollDelaySeconds(RoborockCommunicationMode.DIRECT, false, 15));
    }

    @Test
    void statusPollDelayDisabledInCloudMode() {
        assertNull(VacuumRefreshPolicy.getStatusPollDelaySeconds(RoborockCommunicationMode.CLOUD, true, 15));
    }

    @Test
    void normalizeLegacyRefreshMinutesToSecondsUsesDefaultAndMinimum() {
        assertEquals(300, VacuumRefreshPolicy.normalizeLegacyRefreshMinutesToSeconds(0, 5, 60));
        assertEquals(60, VacuumRefreshPolicy.normalizeLegacyRefreshMinutesToSeconds(1, 5, 60));
        assertEquals(180, VacuumRefreshPolicy.normalizeLegacyRefreshMinutesToSeconds(3, 5, 60));
    }

    @Test
    void normalizeIntervalSecondsUsesDefaultAndMinimum() {
        assertEquals(30, VacuumRefreshPolicy.normalizeIntervalSeconds(0, 30, 30));
        assertEquals(30, VacuumRefreshPolicy.normalizeIntervalSeconds(10, 30, 30));
        assertEquals(45, VacuumRefreshPolicy.normalizeIntervalSeconds(45, 30, 30));
    }

    @Test
    void isRefreshDueChecksTimestampAndInterval() {
        assertTrue(VacuumRefreshPolicy.isRefreshDue(10_000L, 0L, 30));
        assertFalse(VacuumRefreshPolicy.isRefreshDue(20_000L, 10_000L, 30));
        assertTrue(VacuumRefreshPolicy.isRefreshDue(40_000L, 10_000L, 30));
    }
}
