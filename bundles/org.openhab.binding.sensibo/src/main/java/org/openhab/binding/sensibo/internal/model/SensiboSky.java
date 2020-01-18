/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sensibo.internal.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.openhab.binding.sensibo.internal.dto.poddetails.Measurement;
import org.openhab.binding.sensibo.internal.dto.poddetails.ModeCapability;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetails;
import org.openhab.binding.sensibo.internal.dto.poddetails.Schedule;

/**
 * The {@link SensiboSky} represents a Sensibo Sky unit
 *
 * @author Arne Seime - Initial contribution
 */
public class SensiboSky extends Pod {

    private final String macAddress;
    private final String firmwareVersion;
    private final String firmwareType;
    private final String serialNumber;
    private Unit<Temperature> temperatureUnit;
    private final String originalTemperatureUnit;
    private final String productModel;
    private Boolean smartMode;
    private AcState acState = null;
    private Double temperature = null;
    private Double humidity = null;
    private boolean alive = false;
    private final String roomName;
    private Schedule[] schedules;

    public String getOriginalTemperatureUnit() {
        return originalTemperatureUnit;
    }

    public String getRoomName() {
        return roomName;
    }

    public Schedule[] getSchedules() {
        return schedules;
    }

    private final Map<String, ModeCapability> remoteCapabilities;
    private Timer timer;

    public SensiboSky(final PodDetails dto) {
        this.id = dto.id;
        this.macAddress = StringUtils.remove(dto.macAddress, ':');
        this.firmwareVersion = dto.firmwareVersion;
        this.firmwareType = dto.firmwareType;
        this.serialNumber = dto.serialNumber;
        this.originalTemperatureUnit = dto.temperatureUnit;
        if (originalTemperatureUnit != null) {
            switch (originalTemperatureUnit) {
                case "C":
                    this.temperatureUnit = SIUnits.CELSIUS;
                    break;
                case "F":
                    this.temperatureUnit = ImperialUnits.FAHRENHEIT;
                    break;
                default:
                    throw new IllegalArgumentException("Do not understand temperature unit " + temperatureUnit);

            }
        }
        this.productModel = dto.productModel;

        if (dto.acState != null) {
            this.acState = new AcState(dto.acState);
        }

        if (dto.timer != null) {
            this.timer = new Timer(dto.timer);
        }

        final Measurement lastMeasurement = dto.lastMeasurement;
        if (lastMeasurement != null) {
            this.temperature = lastMeasurement.temperature;
            this.humidity = lastMeasurement.humidity;
        }
        this.alive = dto.isAlive();
        this.remoteCapabilities = dto.getRemoteCapabilities();
        this.roomName = dto.getRoomName();

    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getFirmwareType() {
        return firmwareType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Unit<Temperature> getTemperatureUnit() {
        return temperatureUnit;
    }

    public String getProductModel() {
        return productModel;
    }

    public Boolean getSmartMode() {
        return smartMode;
    }

    public AcState getAcState() {
        return acState;
    }

    public String getProductName() {
        switch (productModel) {
            case "skyv2":
                return String.format("Sensibo Sky %s", roomName);
            default:
                return String.format("%s %s", productModel, roomName);
        }
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public boolean isAlive() {
        return alive;
    }

    public Map<String, ModeCapability> getRemoteCapabilities() {
        return remoteCapabilities;
    }

    public ModeCapability getCurrentModeCapabilities() {
        if (remoteCapabilities != null && acState != null && acState.getMode() != null) {
            return remoteCapabilities.get(acState.getMode());
        } else {
            return null;
        }

    }

    public List<Integer> getTargetTemperatures() {
        if (getCurrentModeCapabilities() != null && originalTemperatureUnit != null) {
            org.openhab.binding.sensibo.internal.dto.poddetails.Temperature selectedTemperatureRange = getCurrentModeCapabilities().temperatures
                    .get(originalTemperatureUnit);
            if (selectedTemperatureRange != null) {
                return selectedTemperatureRange.validValues;
            }
        }
        return Collections.emptyList();
    }

    /**
     * @param newAcState an updated ac state
     */
    public void updateAcState(AcState newAcState) {
        this.acState = newAcState;
    }

    public Timer getTimer() {
        return timer;
    }

}
