/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.domain;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.EnergyManagerDTO;
import org.openhab.binding.solarwatt.internal.domain.model.Device;
import org.openhab.binding.solarwatt.internal.factory.EnergyManagerDevicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of all devices known to the energy manager including the energy manager itself.
 *
 * The {@link Device}s are generated from the {@link DeviceDTO}s inside of the {@link EnergyManagerDTO}
 * 
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class EnergyManagerCollection {
    private Logger logger = LoggerFactory.getLogger(EnergyManagerCollection.class);

    private Map<String, Device> devices;

    public EnergyManagerCollection(EnergyManagerDTO energyManagerDTO) {
        this.devices = new HashMap<>();

        energyManagerDTO.getItems().forEach(deviceDTO -> {
            try {
                Device device = EnergyManagerDevicesFactory.getEnergyManagerDevice(deviceDTO);

                if (device != null) {
                    this.devices.put(device.getGuid(), device);
                }
            } catch (Exception ex) {
                this.logger.error("Error setting up initial device {}: {}", deviceDTO.getGuid(),
                        deviceDTO.getDeviceModel(), ex);
            }
        });
    }

    public Map<String, Device> getDevices() {
        return this.devices;
    }
}
