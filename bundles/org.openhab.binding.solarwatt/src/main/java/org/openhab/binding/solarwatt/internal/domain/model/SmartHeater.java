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
package org.openhab.binding.solarwatt.internal.domain.model;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Base class for a EGO Smart Heaters ... see
 * https://www.solarwatt.de/betriebsanleitungen/solarwatt-manager/solarwatt-manager-flex/geraete-verbinden/ego-smartheater
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.smartheater.SmartHeater=[
 * TemperatureSetMax,
 * PowerACInLimit,
 * Temperature,
 * DateProduction,
 * IdFingerPrint,
 * StateVisibleIsSet,
 * StateErrorList,
 * PasswordLock,
 * IdManufacturer,
 * WorkACIn,
 * PowerACInLimits,
 * TemperatureBoiler,
 * IdFirmware,
 * IdModelCode,
 * IdName,
 * IdInterfaceList,
 * TemperatureSetMin,
 * IdDriver,
 * StateDevice,
 * TemperatureSet,
 * PowerACIn,
 * IdSerialNumber,
 * PowerACInMax,
 * IdFingerPrintVersion,
 * IdLabelSet,
 * StateLockedIsSet
 * ]
 *
 * @author Thomas Schumm - Initial contribution
 */
@NonNullByDefault
public class SmartHeater extends Device {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.smartheater.SmartHeater";

    public SmartHeater(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    public void update(DeviceDTO deviceDTO) {
        super.update(deviceDTO);

        this.addWattHourQuantity(CHANNEL_WORK_AC_IN, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_AC_IN, deviceDTO);
        this.addCelsiusQuantity(CHANNEL_TEMPERATURE, deviceDTO);
        this.addCelsiusQuantity(CHANNEL_TEMPERATURE_SET_MAX, deviceDTO);
        this.addCelsiusQuantity(CHANNEL_TEMPERATURE_BOILER, deviceDTO);
        this.addCelsiusQuantity(CHANNEL_TEMPERATURE_SET_MIN, deviceDTO);
        this.addCelsiusQuantity(CHANNEL_TEMPERATURE_SET, deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "SmartHeater";
    }
}
