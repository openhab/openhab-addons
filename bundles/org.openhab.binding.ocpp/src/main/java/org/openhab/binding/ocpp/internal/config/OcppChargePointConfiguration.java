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
 * Configuration for a {@code chargepoint} — one physical charger.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppChargePointConfiguration {

    /**
     * The OCPP charge point identity — the path of the WebSocket URL the charger dials, without the
     * leading slash (e.g. {@code ws://host:8887/<chargePointId>}, so {@code charger} or
     * {@code site/charger}). Immutable per charger.
     */
    public String chargePointId = "";

    /**
     * Seconds to wait after BootNotification before sending the configuration burst. Some chargers
     * are not ready to answer ChangeConfiguration immediately after announcing themselves.
     */
    public int configSettleSeconds = 0;

    /**
     * The charger has no internal meter. Only the periodic clock-aligned emission is disabled; the
     * meter measurand configuration is skipped.
     */
    public boolean meterless = false;

    /**
     * Per-charger heartbeat interval (seconds) returned in the BootNotification response, overriding
     * the server default. Also sizes this charger's liveness window. 0 uses the server default.
     */
    public int heartbeat = 0;
}
