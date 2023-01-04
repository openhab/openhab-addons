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
package org.openhab.binding.solarwatt.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.domain.EnergyManagerCollection;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;
import org.openhab.binding.solarwatt.internal.domain.dto.EnergyManagerDTO;
import org.openhab.binding.solarwatt.internal.domain.model.BatteryConverter;
import org.openhab.binding.solarwatt.internal.domain.model.Device;
import org.openhab.binding.solarwatt.internal.domain.model.EVStation;
import org.openhab.binding.solarwatt.internal.domain.model.EnergyManager;
import org.openhab.binding.solarwatt.internal.domain.model.Forecast;
import org.openhab.binding.solarwatt.internal.domain.model.GridFlow;
import org.openhab.binding.solarwatt.internal.domain.model.Inverter;
import org.openhab.binding.solarwatt.internal.domain.model.KebaEv;
import org.openhab.binding.solarwatt.internal.domain.model.Location;
import org.openhab.binding.solarwatt.internal.domain.model.MyReserve;
import org.openhab.binding.solarwatt.internal.domain.model.MyReserveInverter;
import org.openhab.binding.solarwatt.internal.domain.model.MyReservePowerMeter;
import org.openhab.binding.solarwatt.internal.domain.model.PVPlant;
import org.openhab.binding.solarwatt.internal.domain.model.PowerMeter;
import org.openhab.binding.solarwatt.internal.domain.model.ProfileApp;
import org.openhab.binding.solarwatt.internal.domain.model.S0Counter;
import org.openhab.binding.solarwatt.internal.domain.model.ScheduleApp;
import org.openhab.binding.solarwatt.internal.domain.model.SimpleSwitcher;
import org.openhab.binding.solarwatt.internal.domain.model.SmartEnergyManagement;
import org.openhab.binding.solarwatt.internal.domain.model.SunSpecInverter;

/**
 * Factory to produce concrete instances which match the device structure returned by the energy manager.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class EnergyManagerDevicesFactory {
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
        // objects on level 3
        if (deviceDTO.getDeviceModelStrings().contains(MyReserve.SOLAR_WATT_CLASSNAME)) {
            return new MyReserve(deviceDTO);
        }
        // objects on level 2
        if (deviceDTO.getDeviceModelStrings().contains(S0Counter.SOLAR_WATT_CLASSNAME)) {
            return new S0Counter(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(KebaEv.SOLAR_WATT_CLASSNAME)) {
            return new KebaEv(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(MyReservePowerMeter.SOLAR_WATT_CLASSNAME)) {
            return new MyReservePowerMeter(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(SunSpecInverter.SOLAR_WATT_CLASSNAME)) {
            return new SunSpecInverter(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(MyReserveInverter.SOLAR_WATT_CLASSNAME)) {
            return new MyReserveInverter(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(BatteryConverter.SOLAR_WATT_CLASSNAME)) {
            return new BatteryConverter(deviceDTO);
        }
        // objects on level 1
        if (deviceDTO.getDeviceModelStrings().contains(ScheduleApp.SOLAR_WATT_CLASSNAME)) {
            return new ScheduleApp(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(SmartEnergyManagement.SOLAR_WATT_CLASSNAME)) {
            return new SmartEnergyManagement(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(EnergyManager.SOLAR_WATT_CLASSNAME)) {
            return new EnergyManager(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(Forecast.SOLAR_WATT_CLASSNAME)) {
            return new Forecast(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(Location.SOLAR_WATT_CLASSNAME)) {
            return new Location(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(EVStation.SOLAR_WATT_CLASSNAME)) {
            return new EVStation(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(PowerMeter.SOLAR_WATT_CLASSNAME)) {
            return new PowerMeter(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(SimpleSwitcher.SOLAR_WATT_CLASSNAME)) {
            return new SimpleSwitcher(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(GridFlow.SOLAR_WATT_CLASSNAME)) {
            return new GridFlow(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(PVPlant.SOLAR_WATT_CLASSNAME)) {
            return new PVPlant(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(Inverter.SOLAR_WATT_CLASSNAME)) {
            return new Inverter(deviceDTO);
        }
        if (deviceDTO.getDeviceModelStrings().contains(ProfileApp.SOLAR_WATT_CLASSNAME)) {
            return new ProfileApp(deviceDTO);
        }

        // Objects on level 0
        if (deviceDTO.getDeviceModelStrings().contains(Device.SOLAR_WATT_CLASSNAME)) {
            return new Device(deviceDTO);
        }

        return null;
    }
}
