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
package org.openhab.binding.ocpp.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for a {@code connector} — one outlet of a charger (connectorId 1..N).
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppConnectorConfiguration {

    public int connectorId = 1;

    /**
     * Always send a TxDefaultProfile for the charge-limit channel, even while a transaction is
     * active. Needed for chargers that reject a TxProfile outside a transaction (e.g. Phoenix CHARX).
     */
    public boolean forceTxDefaultProfile = false;

    /**
     * Minimum spacing (ms) between SetChargingProfile sends to this connector. Rapid limit changes
     * within the window are coalesced into a single send. 0 disables coalescing.
     */
    public int profileMinIntervalMs = 0;

    /**
     * Vendor ChangeConfiguration key backing the hardware maximum current channel. Empty means the
     * charger has no such key and the channel is inert.
     */
    public String hardwareMaxCurrentKey = "";

    /** idTag used when starting a transaction remotely (the charging channel). */
    public String remoteStartTag = "openhab";

    /**
     * Nominal line voltage used to convert an Amperes charge-limit into Watts for a charger that only
     * accepts a power limit ({@code ChargingScheduleAllowedChargingRateUnit} = Power). W = A x V x phases.
     */
    public double nominalVoltage = 230.0;

    /**
     * Number of phases assumed when converting the Amperes charge-limit into Watts for a power-only
     * charger. 1 for single-phase charging, 3 for three-phase. Only used for that conversion.
     */
    public int phases = 1;

    /** If &gt; 0, poll this connector for MeterValues every N seconds via TriggerMessage. */
    public int meterValuesPollSeconds = 0;

    /**
     * Opt-in: after this connector sits in a transient state (Preparing/Finishing) too long, send an
     * UnlockConnector to try to clear it. Off by default because those are normal states and
     * unlocking is a physical side effect; enable only for a charger known to wedge there.
     */
    public boolean stuckStateRecovery = false;
}
