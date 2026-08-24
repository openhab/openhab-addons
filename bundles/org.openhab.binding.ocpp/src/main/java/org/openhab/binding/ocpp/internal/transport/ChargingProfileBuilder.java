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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import eu.chargetime.ocpp.model.core.ChargingProfile;
import eu.chargetime.ocpp.model.core.ChargingProfileKindType;
import eu.chargetime.ocpp.model.core.ChargingProfilePurposeType;
import eu.chargetime.ocpp.model.core.ChargingRateUnitType;
import eu.chargetime.ocpp.model.core.ChargingSchedule;
import eu.chargetime.ocpp.model.core.ChargingSchedulePeriod;
import eu.chargetime.ocpp.model.smartcharging.ClearChargingProfileRequest;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;

/**
 * Builds a SetChargingProfile request that caps a connector to a fixed current.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ChargingProfileBuilder {

    static final int TX_DEFAULT_SUFFIX = 1;
    static final int TX_PROFILE_SUFFIX = 2;
    private static final int PROFILE_ID_STRIDE = 10;
    private static final int STACK_LEVEL = 0;

    private ChargingProfileBuilder() {
    }

    public static SetChargingProfileRequest currentLimit(int connectorId, double amps, boolean forceTxDefault,
            @Nullable Integer transactionId) {
        return limit(connectorId, ChargingRateUnitType.A, amps, null, forceTxDefault, transactionId);
    }

    /** Build a SetChargingProfile capping the connector at {@code value} in {@code unit}. */
    public static SetChargingProfileRequest limit(int connectorId, ChargingRateUnitType unit, double value,
            @Nullable Integer numberPhases, boolean forceTxDefault, @Nullable Integer transactionId) {
        boolean useTxProfile = transactionId != null && !forceTxDefault;
        ChargingProfilePurposeType purpose = useTxProfile ? ChargingProfilePurposeType.TxProfile
                : ChargingProfilePurposeType.TxDefaultProfile;

        ChargingSchedulePeriod period = new ChargingSchedulePeriod(0, value);
        if (numberPhases != null) {
            period.setNumberPhases(numberPhases);
        }
        ChargingSchedule schedule = new ChargingSchedule(unit, new ChargingSchedulePeriod[] { period });
        ChargingProfile profile = new ChargingProfile(profileId(connectorId, useTxProfile), STACK_LEVEL, purpose,
                ChargingProfileKindType.Relative, schedule);
        if (useTxProfile) {
            profile.setTransactionId(transactionId);
        }
        return new SetChargingProfileRequest(connectorId, profile);
    }

    /** Removes this binding's cap from a connector, by connector and stack level. */
    public static ClearChargingProfileRequest clearLimit(int connectorId) {
        ClearChargingProfileRequest request = new ClearChargingProfileRequest();
        request.setConnectorId(connectorId);
        request.setStackLevel(STACK_LEVEL);
        return request;
    }

    static int profileId(int connectorId, boolean txProfile) {
        return connectorId * PROFILE_ID_STRIDE + (txProfile ? TX_PROFILE_SUFFIX : TX_DEFAULT_SUFFIX);
    }
}
