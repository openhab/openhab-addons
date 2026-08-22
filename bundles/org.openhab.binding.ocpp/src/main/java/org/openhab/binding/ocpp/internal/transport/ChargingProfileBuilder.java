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
 * <p>
 * OCPP 1.6 offers two relevant profile purposes: a {@code TxProfile} limits the current transaction
 * and must carry its transaction id; a {@code TxDefaultProfile} applies with or without a running
 * transaction and must not carry one. A charge point with no active transaction (or one, like the
 * Phoenix CHARX, that rejects a TxProfile outside a transaction) needs the default profile — hence
 * {@code forceTxDefault}.
 *
 * <p>
 * The schedule is {@code Relative}: the cap applies from the start of charging for the whole
 * session, so it needs no absolute {@code startSchedule} (an {@code Absolute} profile without one is
 * invalid per OCPP 1.6 and a compliant charger may reject it). The profile id is derived from the
 * connector and the purpose so limits on different connectors of a multi-connector charger stay
 * independent — a charge point identifies an installed profile by id and installing one with an
 * existing id replaces it.
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

    /**
     * @param connectorId the connector to limit
     * @param amps the current cap in amperes (0 pauses charging)
     * @param forceTxDefault always use a TxDefaultProfile, even when a transaction is active
     * @param transactionId the active transaction id, or {@code null} if none
     */
    public static SetChargingProfileRequest currentLimit(int connectorId, double amps, boolean forceTxDefault,
            @Nullable Integer transactionId) {
        return limit(connectorId, ChargingRateUnitType.A, amps, null, forceTxDefault, transactionId);
    }

    /**
     * Build a SetChargingProfile capping the connector at {@code value} in {@code unit} — Amperes for a
     * charger that limits by current, Watts for one whose
     * {@code ChargingScheduleAllowedChargingRateUnit} is Power only. {@code numberPhases}, when non-null,
     * requests charging on that many phases (1/2/3; OCPP assumes 3 when omitted). Same profile shape
     * otherwise; only the schedule's unit, value and phase count differ.
     */
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

    /**
     * Builds a ClearChargingProfile that removes this binding's cap from a connector, returning it to
     * the charge point's own configured maximum. This is how a pause is lifted, or a limit cleared,
     * without leaving a 0 A profile behind — a charger reads 0 A as "suspend" (it reports
     * SuspendedEVSE), not "charge at full". Clearing is done by connector and stack level rather than a
     * single profile id, so it removes the cap whichever purpose — {@code TxProfile} or
     * {@code TxDefaultProfile} — installed it.
     *
     * @param connectorId the connector to lift the cap from
     */
    public static ClearChargingProfileRequest clearLimit(int connectorId) {
        ClearChargingProfileRequest request = new ClearChargingProfileRequest();
        request.setConnectorId(connectorId);
        request.setStackLevel(STACK_LEVEL);
        return request;
    }

    /**
     * A charge-point-wide id for the installed profile, unique per (connector, purpose).
     */
    static int profileId(int connectorId, boolean txProfile) {
        return connectorId * PROFILE_ID_STRIDE + (txProfile ? TX_PROFILE_SUFFIX : TX_DEFAULT_SUFFIX);
    }
}
