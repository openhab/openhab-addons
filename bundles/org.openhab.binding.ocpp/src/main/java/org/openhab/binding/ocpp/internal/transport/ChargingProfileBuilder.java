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
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;

/**
 * Builds a SetChargingProfile request that caps a connector to a fixed current.
 *
 * <p>
 * OCPP 1.6 offers two relevant profile purposes. A {@code TxProfile} limits the current
 * transaction and must carry its transaction id; a {@code TxDefaultProfile} applies whether or not a
 * transaction is running and must not carry one. A charge point that has no transaction active (or
 * one, like the Phoenix CHARX, that rejects a TxProfile outside a transaction) needs the default
 * profile — hence {@code forceTxDefault}. The two purposes use distinct, stable profile ids so a
 * charger keeps them apart across sessions.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ChargingProfileBuilder {

    public static final int TX_DEFAULT_PROFILE_ID = 1;
    public static final int TX_PROFILE_ID = 2;
    private static final int STACK_LEVEL = 0;

    private ChargingProfileBuilder() {
    }

    /**
     * @param connectorId the connector to limit
     * @param amps the current cap in amperes (0 pauses charging)
     * @param forceTxDefault always use a TxDefaultProfile, even when a transaction is active
     * @param transactionId the active transaction id, or {@code null} if none
     */
    public static SetChargingProfileRequest currentLimit(int connectorId, double amps, boolean forceTxDefault,
            @Nullable Integer transactionId) {
        boolean useTxProfile = transactionId != null && !forceTxDefault;
        ChargingProfilePurposeType purpose = useTxProfile ? ChargingProfilePurposeType.TxProfile
                : ChargingProfilePurposeType.TxDefaultProfile;
        int profileId = useTxProfile ? TX_PROFILE_ID : TX_DEFAULT_PROFILE_ID;

        ChargingSchedulePeriod period = new ChargingSchedulePeriod(0, amps);
        ChargingSchedule schedule = new ChargingSchedule(ChargingRateUnitType.A,
                new ChargingSchedulePeriod[] { period });
        ChargingProfile profile = new ChargingProfile(profileId, STACK_LEVEL, purpose, ChargingProfileKindType.Absolute,
                schedule);
        if (useTxProfile) {
            profile.setTransactionId(transactionId);
        }
        return new SetChargingProfileRequest(connectorId, profile);
    }
}
