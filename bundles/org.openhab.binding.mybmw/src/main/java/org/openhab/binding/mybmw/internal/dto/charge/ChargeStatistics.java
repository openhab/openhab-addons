/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.dto.charge;

/**
 * The {@link ChargeStatistics} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ChargeStatistics {
    public int totalEnergyCharged;// ": 173,
    public String totalEnergyChargedSemantics;// ": "Insgesamt circa 173 Kilowattstunden geladen",
    public String symbol;// ": "~",
    public int numberOfChargingSessions;// ": 13,
    public String numberOfChargingSessionsSemantics;// ": "13 Ladevorgänge"
}
