/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.api.v2;

import java.time.OffsetDateTime;

import org.openhab.binding.evnotify.api.ChargingData;

/**
 * Represents the complete data that is returned by evnotify v2 API.
 *
 * @author Michael Schmidt - Initial contribution
 */
public class ChargingDataDTO implements ChargingData {

    private final BasicChargingDataDTO basicChargingDataDTO;

    private final ExtendedChargingDataDTO extendedChargingDataDTO;

    public ChargingDataDTO(BasicChargingDataDTO basicChargingDataDTO, ExtendedChargingDataDTO extendedChargingDataDTO) {

        if (basicChargingDataDTO == null || extendedChargingDataDTO == null) {
            throw new IllegalArgumentException("Given charging data must not be null");
        }

        this.basicChargingDataDTO = basicChargingDataDTO;
        this.extendedChargingDataDTO = extendedChargingDataDTO;
    }

    @Override
    public Float getStateOfChargeDisplay() {
        return basicChargingDataDTO.getStateOfChargeDisplay();
    }

    @Override
    public Float getStateOfChargeBms() {
        return basicChargingDataDTO.getStateOfChargeBms();
    }

    @Override
    public OffsetDateTime getLastStateOfCharge() {
        return basicChargingDataDTO.getLastStateOfCharge();
    }

    @Override
    public Boolean isCharging() {
        return extendedChargingDataDTO.isCharging();
    }

    @Override
    public Boolean isRapidChargePort() {
        return extendedChargingDataDTO.isRapidChargePort();
    }

    @Override
    public Boolean isNormalChargePort() {
        return extendedChargingDataDTO.isNormalChargePort();
    }

    @Override
    public Boolean isSlowChargePort() {
        return extendedChargingDataDTO.isSlowChargePort();
    }

    @Override
    public Float getStateOfHealth() {
        return extendedChargingDataDTO.getStateOfHealth();
    }

    @Override
    public Float getAuxBatteryVoltage() {
        return extendedChargingDataDTO.getAuxBatteryVoltage();
    }

    @Override
    public Float getDcBatteryVoltage() {
        return extendedChargingDataDTO.getDcBatteryVoltage();
    }

    @Override
    public Float getDcBatteryCurrent() {
        return extendedChargingDataDTO.getDcBatteryCurrent();
    }

    @Override
    public Float getDcBatteryPower() {
        return extendedChargingDataDTO.getDcBatteryPower();
    }

    @Override
    public Float getCumulativeEnergyCharged() {
        return extendedChargingDataDTO.getCumulativeEnergyCharged();
    }

    @Override
    public Float getCumulativeEnergyDischarged() {
        return extendedChargingDataDTO.getCumulativeEnergyDischarged();
    }

    @Override
    public Float getBatteryMinTemperature() {
        return extendedChargingDataDTO.getBatteryMinTemperature();
    }

    @Override
    public Float getBatteryMaxTemperature() {
        return extendedChargingDataDTO.getBatteryMaxTemperature();
    }

    @Override
    public Float getBatteryInletTemperature() {
        return extendedChargingDataDTO.getBatteryInletTemperature();
    }

    @Override
    public Float getExternalTemperature() {
        return extendedChargingDataDTO.getExternalTemperature();
    }

    @Override
    public OffsetDateTime getLastExtended() {
        return extendedChargingDataDTO.getLastExtended();
    }
}
