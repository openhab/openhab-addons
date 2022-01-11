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
package org.openhab.binding.sensibo.internal.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sensibo.internal.SensiboTemperatureUnitConverter;
import org.openhab.binding.sensibo.internal.dto.poddetails.ModeCapabilityDTO;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetailsDTO;
import org.openhab.binding.sensibo.internal.dto.poddetails.TemperatureDTO;
import org.openhab.core.thing.Thing;

/**
 * The {@link SensiboSky} represents a Sensibo Sky unit
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboSky extends Pod {
    private final String macAddress;
    private final String firmwareVersion;
    private final String firmwareType;
    private final String serialNumber;
    private final String productModel;
    private final String roomName;
    private final Unit<Temperature> temperatureUnit;
    private final String originalTemperatureUnit;
    private final Double temperature;
    private final Double humidity;
    private final boolean alive;
    private final Map<String, ModeCapabilityDTO> remoteCapabilities;
    private Schedule[] schedules = new Schedule[0];
    private Optional<AcState> acState = Optional.empty();
    private Optional<Timer> timer = Optional.empty();

    public SensiboSky(final PodDetailsDTO dto) {
        super(dto.id);
        this.macAddress = StringUtils.remove(dto.macAddress, ':');
        this.firmwareVersion = dto.firmwareVersion;
        this.firmwareType = dto.firmwareType;
        this.serialNumber = dto.serialNumber;
        this.originalTemperatureUnit = dto.temperatureUnit;
        this.temperatureUnit = SensiboTemperatureUnitConverter.parseFromSensiboFormat(dto.temperatureUnit);
        this.productModel = dto.productModel;

        if (dto.acState != null) {
            this.acState = Optional.of(new AcState(dto.acState));
        }

        if (dto.timer != null) {
            this.timer = Optional.of(new Timer(dto.timer));
        }

        this.temperature = dto.lastMeasurement.temperature;
        this.humidity = dto.lastMeasurement.humidity;

        this.alive = dto.isAlive();
        if (dto.getRemoteCapabilities() != null) {
            this.remoteCapabilities = dto.getRemoteCapabilities();
        } else {
            this.remoteCapabilities = new HashMap<>();
        }
        this.roomName = dto.getRoomName();

        if (dto.schedules != null) {
            schedules = Arrays.stream(dto.schedules).map(Schedule::new).toArray(Schedule[]::new);
        }
    }

    public String getOriginalTemperatureUnit() {
        return originalTemperatureUnit;
    }

    public String getRoomName() {
        return roomName;
    }

    public Schedule[] getSchedules() {
        return schedules;
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

    public Optional<AcState> getAcState() {
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

    public Map<String, ModeCapabilityDTO> getRemoteCapabilities() {
        return remoteCapabilities;
    }

    public Optional<ModeCapabilityDTO> getCurrentModeCapabilities() {
        if (acState.isPresent() && acState.get().getMode() != null) {
            return Optional.ofNullable(remoteCapabilities.get(acState.get().getMode()));
        } else {
            return Optional.empty();
        }
    }

    public List<Integer> getTargetTemperatures() {
        Optional<ModeCapabilityDTO> currentModeCapabilities = getCurrentModeCapabilities();
        if (currentModeCapabilities.isPresent()) {
            TemperatureDTO selectedTemperatureRange = currentModeCapabilities.get().temperatures
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
        this.acState = Optional.of(newAcState);
    }

    public Optional<Timer> getTimer() {
        return timer;
    }

    public Map<String, String> getThingProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, "Sensibo");
        properties.put("podId", id);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, macAddress);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        properties.put(Thing.PROPERTY_MODEL_ID, productModel);
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        properties.put("firmwareType", firmwareType);

        return properties;
    }
}
