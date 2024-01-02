/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;

/**
 * The {@link SolarMaxData} class is a POJO for storing the values returned from the SolarMax device and accessing the
 * (decoded) values
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.FIELD, DefaultLocation.TYPE_BOUND,
        DefaultLocation.TYPE_ARGUMENT })
public class SolarMaxData {

    private ZonedDateTime dataDateTime = ZonedDateTime.now();

    private boolean communicationSuccessful;

    private final Map<SolarMaxCommandKey, @Nullable String> data = new HashMap<>();

    public State getDataDateTime() {
        return new DateTimeType(dataDateTime);
    }

    public boolean has(SolarMaxCommandKey key) {
        return data.containsKey(key);
    }

    public Number get(SolarMaxCommandKey key) {
        switch (key) {
            case softwareVersion:
                return getSoftwareVersion();

            case buildNumber:
                return getBuildNumber();

            case startups:
                return getStartups();

            case acPhase1Current:
                return getAcPhase1Current();

            case acPhase2Current:
                return getAcPhase2Current();

            case acPhase3Current:
                return getAcPhase3Current();

            case energyGeneratedToday:
                return getEnergyGeneratedToday();

            case energyGeneratedTotal:
                return getEnergyGeneratedTotal();

            case operatingHours:
                return getOperatingHours();

            case energyGeneratedYesterday:
                return getEnergyGeneratedYesterday();

            case energyGeneratedLastMonth:
                return getEnergyGeneratedLastMonth();

            case energyGeneratedLastYear:
                return getEnergyGeneratedLastYear();

            case energyGeneratedThisMonth:
                return getEnergyGeneratedThisMonth();

            case energyGeneratedThisYear:
                return getEnergyGeneratedThisYear();

            case currentPowerGenerated:
                return getCurrentPowerGenerated();

            case acFrequency:
                return getAcFrequency();

            case acPhase1Voltage:
                return getAcPhase1Voltage();

            case acPhase2Voltage:
                return getAcPhase2Voltage();

            case acPhase3Voltage:
                return getAcPhase3Voltage();

            case heatSinkTemperature:
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

    public Number getSoftwareVersion() {
        return getIntegerValueFrom(SolarMaxCommandKey.softwareVersion);
    }

    public Number getBuildNumber() {
        return getIntegerValueFrom(SolarMaxCommandKey.buildNumber);
    }

    public Number getStartups() {
        return getIntegerValueFrom(SolarMaxCommandKey.startups);
    }

    public Number getAcPhase1Current() {
        return getDecimalValueFrom(SolarMaxCommandKey.acPhase1Current, 0.01);
    }

    public Number getAcPhase2Current() {
        return getDecimalValueFrom(SolarMaxCommandKey.acPhase2Current, 0.01);
    }

    public Number getAcPhase3Current() {
        return getDecimalValueFrom(SolarMaxCommandKey.acPhase3Current, 0.01);
    }

    public Number getEnergyGeneratedToday() {
        return getIntegerValueFrom(SolarMaxCommandKey.energyGeneratedToday, 100);
    }

    public Number getEnergyGeneratedTotal() {
        return getIntegerValueFrom(SolarMaxCommandKey.energyGeneratedTotal, 1000);
    }

    public Number getOperatingHours() {
        return getIntegerValueFrom(SolarMaxCommandKey.operatingHours);
    }

    public Number getEnergyGeneratedYesterday() {
        return getIntegerValueFrom(SolarMaxCommandKey.energyGeneratedYesterday, 100);
    }

    public Number getEnergyGeneratedLastMonth() {
        return getIntegerValueFrom(SolarMaxCommandKey.energyGeneratedLastMonth, 1000);
    }

    public Number getEnergyGeneratedLastYear() {
        return getIntegerValueFrom(SolarMaxCommandKey.energyGeneratedLastYear, 1000);
    }

    public Number getEnergyGeneratedThisMonth() {
        return getIntegerValueFrom(SolarMaxCommandKey.energyGeneratedThisMonth, 1000);
    }

    public Number getEnergyGeneratedThisYear() {
        return getIntegerValueFrom(SolarMaxCommandKey.energyGeneratedThisYear, 1000);
    }

    public Number getCurrentPowerGenerated() {
        return getIntegerValueFrom(SolarMaxCommandKey.currentPowerGenerated, 0.5);
    }

    Number getAcFrequency() {
        return getDecimalValueFrom(SolarMaxCommandKey.acFrequency, 0.01);
    }

    public Number getAcPhase1Voltage() {
        return getDecimalValueFrom(SolarMaxCommandKey.acPhase1Voltage, 0.1);
    }

    public Number getAcPhase2Voltage() {
        return getDecimalValueFrom(SolarMaxCommandKey.acPhase2Voltage, 0.1);
    }

    public Number getAcPhase3Voltage() {
        return getDecimalValueFrom(SolarMaxCommandKey.acPhase3Voltage, 0.1);
    }

    public Number getHeatSinkTemperature() {
        return getIntegerValueFrom(SolarMaxCommandKey.heatSinkTemperature);
    }

    @Nullable
    private Number getDecimalValueFrom(SolarMaxCommandKey solarMaxCommandKey, double multiplyByFactor) {
        if (this.data.containsKey(solarMaxCommandKey)) {
            String valueString = this.data.get(solarMaxCommandKey);

            if (valueString != null) {
                int valueInt = Integer.parseInt(valueString, 16);
                return (float) valueInt * multiplyByFactor;
            }
            return null;
        }
        return null;
    }

    @Nullable
    private Number getIntegerValueFrom(SolarMaxCommandKey solarMaxCommandKey, double multiplyByFactor) {
        if (this.data.containsKey(solarMaxCommandKey)) {
            String valueString = this.data.get(solarMaxCommandKey);

            if (valueString != null) {
                int valueInt = Integer.parseInt(valueString, 16);
                return (int) (valueInt * multiplyByFactor);
            }
            return null;
        }
        return null;
    }

    @Nullable
    private Number getIntegerValueFrom(SolarMaxCommandKey solarMaxCommandKey) {
        if (this.data.containsKey(solarMaxCommandKey)) {
            String valueString = this.data.get(solarMaxCommandKey);
            if (valueString != null) {
                return Integer.parseInt(valueString, 16);
            }
            return null;
        }
        return null;
    }

    protected void setData(Map<SolarMaxCommandKey, @Nullable String> data) {
        this.data.putAll(data);
    }
}
