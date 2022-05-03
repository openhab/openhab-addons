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
package org.openhab.binding.evnotify.api;

import java.time.OffsetDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for the data from the API of the evnotify online service.
 *
 * @author Michael Schmidt - Initial contribution
 */
@NonNullByDefault
public interface ChargingData {

    Float getStateOfChargeDisplay();

    Float getStateOfChargeBms();

    OffsetDateTime getLastStateOfCharge();

    Boolean isCharging();

    Boolean isRapidChargePort();

    Boolean isNormalChargePort();

    Boolean isSlowChargePort();

    Float getStateOfHealth();

    Float getAuxBatteryVoltage();

    Float getDcBatteryVoltage();

    Float getDcBatteryCurrent();

    Float getDcBatteryPower();

    Float getCumulativeEnergyCharged();

    Float getCumulativeEnergyDischarged();

    Float getBatteryMinTemperature();

    Float getBatteryMaxTemperature();

    Float getBatteryInletTemperature();

    Float getExternalTemperature();

    OffsetDateTime getLastExtended();
}
