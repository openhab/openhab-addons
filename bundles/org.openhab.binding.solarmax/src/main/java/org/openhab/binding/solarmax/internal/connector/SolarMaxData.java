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
package org.openhab.binding.solarmax.internal.connector;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * The {@link SolarMaxData} class is a POJO for storing the values returned from the SolarMax device and accessing the
 * (decoded) values
 *
 * @author Jamie Townsend - Initial contribution
 */
public class SolarMaxData {

    private ZonedDateTime dataDateTime;
    private boolean communicationSuccessful;
    private final Map<SolarMaxCommandKey, String> data = new HashMap<>();

    public State getDataDateTime() {
        return new DateTimeType(dataDateTime);
    }

    public boolean has(SolarMaxCommandKey key) {
        return data.containsKey(key);
    }

    public State get(SolarMaxCommandKey key) {
        switch (key) {
            case SoftwareVersion:
                return getSoftwareVersion();

            case BuildNumber:
                return getBuildNumber();

            case Startups:
                return getStartups();

            case AcPhase1Current:
                return getAcPhase1Current();

            case AcPhase2Current:
                return getAcPhase2Current();

            case AcPhase3Current:
                return getAcPhase3Current();

            case EnergyGeneratedToday:
                return getEnergyGeneratedToday();

            case EnergyGeneratedTotal:
                return getEnergyGeneratedTotal();

            case OperatingHours:
                return getOperatingHours();

            case EnergyGeneratedYesterday:
                return getEnergyGeneratedYesterday();

            case EnergyGeneratedLastMonth:
                return getEnergyGeneratedLastMonth();

            case EnergyGeneratedLastYear:
                return getEnergyGeneratedLastYear();

            case EnergyGeneratedThisMonth:
                return getEnergyGeneratedThisMonth();

            case EnergyGeneratedThisYear:
                return getEnergyGeneratedThisYear();

            case CurrentPowerGenerated:
                return getCurrentPowerGenerated();

            case AcFrequency:
                return getAcFrequency();

            case AcPhase1Voltage:
                return getAcPhase1Voltage();

            case AcPhase2Voltage:
                return getAcPhase2Voltage();

            case AcPhase3Voltage:
                return getAcPhase3Voltage();

            case HeatSinkTemperature:
                return getHeatSinkTemperature();

            default:
                return null;
        }
    }

    public void setDataDateTime(ZonedDateTime dataDateTime) {
        this.dataDateTime = dataDateTime;
    }

    public boolean wasCommunicationSuccessful() {
        return this.communicationSuccessful;
    }

    public void setCommunicationSuccessful(boolean communicationSuccessful) {
        this.communicationSuccessful = communicationSuccessful;
    }

    public DecimalType getSoftwareVersion() {
        return getIntegerValueFrom(SolarMaxCommandKey.SoftwareVersion);
    }

    public DecimalType getBuildNumber() {
        return getIntegerValueFrom(SolarMaxCommandKey.BuildNumber);
    }

    public DecimalType getStartups() {
        return getIntegerValueFrom(SolarMaxCommandKey.Startups);
    }

    public DecimalType getAcPhase1Current() {
        return getDecimalValueFrom(SolarMaxCommandKey.AcPhase1Current, 0.01);
    }

    public DecimalType getAcPhase2Current() {
        return getDecimalValueFrom(SolarMaxCommandKey.AcPhase2Current, 0.01);
    }

    public DecimalType getAcPhase3Current() {
        return getDecimalValueFrom(SolarMaxCommandKey.AcPhase3Current, 0.01);
    }

    public DecimalType getEnergyGeneratedToday() {
        return getIntegerValueFrom(SolarMaxCommandKey.EnergyGeneratedToday, 100);
    }

    public DecimalType getEnergyGeneratedTotal() {
        return getIntegerValueFrom(SolarMaxCommandKey.EnergyGeneratedTotal, 1000);
    }

    public DecimalType getOperatingHours() {
        return getIntegerValueFrom(SolarMaxCommandKey.OperatingHours);
    }

    public DecimalType getEnergyGeneratedYesterday() {
        return getIntegerValueFrom(SolarMaxCommandKey.EnergyGeneratedYesterday, 100);
    }

    public DecimalType getEnergyGeneratedLastMonth() {
        return getIntegerValueFrom(SolarMaxCommandKey.EnergyGeneratedLastMonth, 1000);
    }

    public DecimalType getEnergyGeneratedLastYear() {
        return getIntegerValueFrom(SolarMaxCommandKey.EnergyGeneratedLastYear, 1000);
    }

    public DecimalType getEnergyGeneratedThisMonth() {
        return getIntegerValueFrom(SolarMaxCommandKey.EnergyGeneratedThisMonth, 1000);
    }

    public DecimalType getEnergyGeneratedThisYear() {
        return getIntegerValueFrom(SolarMaxCommandKey.EnergyGeneratedThisYear, 1000);
    }

    public DecimalType getCurrentPowerGenerated() {
        return getIntegerValueFrom(SolarMaxCommandKey.CurrentPowerGenerated, 0.5);
    }

    public DecimalType getAcFrequency() {
        return getDecimalValueFrom(SolarMaxCommandKey.AcFrequency, 0.01);
    }

    public DecimalType getAcPhase1Voltage() {
        return getDecimalValueFrom(SolarMaxCommandKey.AcPhase1Voltage, 0.1);
    }

    public DecimalType getAcPhase2Voltage() {
        return getDecimalValueFrom(SolarMaxCommandKey.AcPhase2Voltage, 0.1);
    }

    public DecimalType getAcPhase3Voltage() {
        return getDecimalValueFrom(SolarMaxCommandKey.AcPhase3Voltage, 0.1);
    }

    public DecimalType getHeatSinkTemperature() {
        return getIntegerValueFrom(SolarMaxCommandKey.HeatSinkTemperature);
    }

    private DecimalType getDecimalValueFrom(SolarMaxCommandKey solarMaxCommandKey, double multiplyByFactor) {
        if (this.data.containsKey(solarMaxCommandKey)) {
            String valueString = this.data.get(solarMaxCommandKey);
            if (valueString != null) {
                int valueInt = Integer.parseInt(valueString, 16);
                return new DecimalType((float) valueInt * multiplyByFactor);
            }
            return null;
        }
        return null;
    }

    private DecimalType getIntegerValueFrom(SolarMaxCommandKey solarMaxCommandKey, double multiplyByFactor) {
        if (this.data.containsKey(solarMaxCommandKey)) {
            String valueString = this.data.get(solarMaxCommandKey);
            if (valueString != null) {
                int valueInt = Integer.parseInt(valueString, 16);
                return new DecimalType((int) (valueInt * multiplyByFactor));
            }
            return null;
        }
        return null;
    }

    private DecimalType getIntegerValueFrom(SolarMaxCommandKey solarMaxCommandKey) {
        if (this.data.containsKey(solarMaxCommandKey)) {
            String valueString = this.data.get(solarMaxCommandKey);
            if (valueString != null) {
                int valueInt = Integer.parseInt(valueString, 16);
                return new DecimalType(valueInt);
            }
            return null;
        }
        return null;
    }

    protected void setData(Map<SolarMaxCommandKey, String> data) {
        this.data.putAll(data);
    }
}
