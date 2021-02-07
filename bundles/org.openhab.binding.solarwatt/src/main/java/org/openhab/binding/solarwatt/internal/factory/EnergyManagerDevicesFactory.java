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
package org.openhab.binding.solarwatt.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.domain.EnergyManagerCollection;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;
import org.openhab.binding.solarwatt.internal.domain.dto.EnergyManagerDTO;
import org.openhab.binding.solarwatt.internal.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to produce concrete instances which match the device structure returned by the energy manager.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class EnergyManagerDevicesFactory {
    private static final Logger logger = LoggerFactory.getLogger(EnergyManagerDevicesFactory.class);

    /**
     * Generate a concrete collection of devices which where discovered by the energy manager.
     *
     * @param energyManagerDTO raw transfer object generated from json
     * @return collection of all devices
     */
    public static EnergyManagerCollection getEnergyManagerCollection(EnergyManagerDTO energyManagerDTO) {
        return new EnergyManagerCollection(energyManagerDTO);
    }

    /**
     * Generate one concrete device instance from one raw device.
     *
     * Specific implementation to use is determined by looking at the
     * deviceModels supported by the device and prioritising them according from the bottom up.
     *
     * @param deviceDTO raw transfer object for one device
     * @return device fille from the {@link DeviceDTO}
     */
    public static @Nullable Device getEnergyManagerDevice(DeviceDTO deviceDTO) {
        try {
            // objects on level 3
            if (deviceDTO.getDeviceModelStrings().contains(MyReserve.solarWattClassname)) {
                return new MyReserve(deviceDTO);
            }
            // objects on level 2
            if (deviceDTO.getDeviceModelStrings().contains(S0Counter.solarWattClassname)) {
                return new S0Counter(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(KebaEv.solarWattClassname)) {
                return new KebaEv(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(MyReservePowerMeter.solarWattClassname)) {
                return new MyReservePowerMeter(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(SunSpecInverter.solarWattClassname)) {
                return new SunSpecInverter(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(MyReserveInverter.solarWattClassname)) {
                return new MyReserveInverter(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(BatteryConverter.solarWattClassname)) {
                return new BatteryConverter(deviceDTO);
            }
            // objects on level 1
            if (deviceDTO.getDeviceModelStrings().contains(ScheduleApp.solarWattClassname)) {
                return new ScheduleApp(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(SmartEnergyManagement.solarWattClassname)) {
                return new SmartEnergyManagement(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(EnergyManager.solarWattClassname)) {
                return new EnergyManager(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(Forecast.solarWattClassname)) {
                return new Forecast(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(Location.solarWattClassname)) {
                return new Location(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(EVStation.solarWattClassname)) {
                return new EVStation(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(PowerMeter.solarWattClassname)) {
                return new PowerMeter(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(SimpleSwitcher.solarWattClassname)) {
                return new SimpleSwitcher(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(GridFlow.solarWattClassname)) {
                return new GridFlow(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(PVPlant.solarWattClassname)) {
                return new PVPlant(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(Inverter.solarWattClassname)) {
                return new Inverter(deviceDTO);
            }
            if (deviceDTO.getDeviceModelStrings().contains(ProfileApp.solarWattClassname)) {
                return new ProfileApp(deviceDTO);
            }

            // Objects on level 0
            if (deviceDTO.getDeviceModelStrings().contains(Device.solarWattClassname)) {
                return new Device(deviceDTO);
            }

            logger.debug("Don't know how to handle device {}: {}", deviceDTO.getGuid(), deviceDTO.getDeviceModel());
        } catch (Exception ex) {
            logger.error("Error setting up initial device {}: {}", deviceDTO.getGuid(), deviceDTO.getDeviceModel(), ex);
        }

        return null;
    }
}
