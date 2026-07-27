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
package org.openhab.binding.ocpp.internal.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import eu.chargetime.ocpp.model.core.ChargingProfile;
import eu.chargetime.ocpp.model.core.ChargingProfilePurposeType;
import eu.chargetime.ocpp.model.core.ChargingRateUnitType;
import eu.chargetime.ocpp.model.core.ChargingSchedulePeriod;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;

/**
 * Tests the TxProfile / TxDefaultProfile decision and schedule shape of {@link ChargingProfileBuilder}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ChargingProfileBuilderTest {

    @Test
    void noTransactionGivesATxDefaultProfile() {
        SetChargingProfileRequest request = ChargingProfileBuilder.currentLimit(1, 16.0, false, null);
        assertEquals(1, request.getConnectorId().intValue());

        ChargingProfile profile = request.getCsChargingProfiles();
        assertEquals(ChargingProfileBuilder.TX_DEFAULT_PROFILE_ID, profile.getChargingProfileId().intValue());
        assertEquals(ChargingProfilePurposeType.TxDefaultProfile, profile.getChargingProfilePurpose());
        assertNull(profile.getTransactionId());
        assertEquals(0, profile.getStackLevel().intValue());

        assertEquals(ChargingRateUnitType.A, profile.getChargingSchedule().getChargingRateUnit());
        ChargingSchedulePeriod[] periods = profile.getChargingSchedule().getChargingSchedulePeriod();
        assertEquals(1, periods.length);
        assertEquals(0, periods[0].getStartPeriod().intValue());
        assertEquals(16.0, periods[0].getLimit().doubleValue());
    }

    @Test
    void activeTransactionGivesATxProfileCarryingTheTransactionId() {
        SetChargingProfileRequest request = ChargingProfileBuilder.currentLimit(2, 10.0, false, 42);
        ChargingProfile profile = request.getCsChargingProfiles();
        assertEquals(ChargingProfileBuilder.TX_PROFILE_ID, profile.getChargingProfileId().intValue());
        assertEquals(ChargingProfilePurposeType.TxProfile, profile.getChargingProfilePurpose());
        assertEquals(42, profile.getTransactionId().intValue());
    }

    @Test
    void forceTxDefaultKeepsTheDefaultProfileEvenDuringATransaction() {
        SetChargingProfileRequest request = ChargingProfileBuilder.currentLimit(1, 6.0, true, 42);
        ChargingProfile profile = request.getCsChargingProfiles();
        assertEquals(ChargingProfilePurposeType.TxDefaultProfile, profile.getChargingProfilePurpose());
        assertNull(profile.getTransactionId());
    }

    @Test
    void zeroAmpsPausesViaLimitZero() {
        SetChargingProfileRequest request = ChargingProfileBuilder.currentLimit(1, 0.0, true, null);
        ChargingSchedulePeriod period = request.getCsChargingProfiles().getChargingSchedule()
                .getChargingSchedulePeriod()[0];
        assertEquals(0.0, period.getLimit().doubleValue());
    }
}
