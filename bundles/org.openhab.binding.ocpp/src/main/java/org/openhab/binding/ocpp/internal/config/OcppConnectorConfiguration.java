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
    public boolean forceTxDefaultProfile = false;
    public int profileMinIntervalMs = 0;
    public String hardwareMaxCurrentKey = "";
    public String remoteStartTag = "openhab";
    public double nominalVoltage = 230.0;
    public int phases = 1;
    public int refreshInterval = 0;
    public boolean stuckStateRecovery = false;
    public int remoteStartRetries = 0;
}
