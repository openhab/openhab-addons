/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bydbatterybox.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;

/**
 * The {@link BydBatteryBoxBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BydBatteryBoxBindingConstants {

    public static final String BINDING_ID = "bydbatterybox";

    public static final ThingTypeUID THING_TYPE_BATTERY = new ThingTypeUID(BINDING_ID, "battery");

    // static channel groups
    public static final String GROUP_STATUS = "status";
    public static final String GROUP_EVENTS = "events";
    public static final String GROUP_DIAGNOSTICS = "diagnostics";
    public static final ChannelGroupTypeUID GROUP_TYPE_STATUS = new ChannelGroupTypeUID(BINDING_ID, "status");
    public static final ChannelGroupTypeUID GROUP_TYPE_EVENTS = new ChannelGroupTypeUID(BINDING_ID, "events");
    public static final ChannelGroupTypeUID GROUP_TYPE_DIAGNOSTICS = new ChannelGroupTypeUID(BINDING_ID, "diagnostics");

    // tower and module channel groups, declared for the largest topology and reduced to the reported one
    public static final String GROUP_TOWER_PREFIX = "tower-";
    public static final String GROUP_MODULE_INFIX = "-module-";
    public static final ChannelGroupTypeUID GROUP_TYPE_TOWER = new ChannelGroupTypeUID(BINDING_ID, "tower");
    public static final ChannelGroupTypeUID GROUP_TYPE_MODULE = new ChannelGroupTypeUID(BINDING_ID, "module");

    // status, tower and module channels
    public static final String CHANNEL_STATE_OF_CHARGE = "state-of-charge";
    public static final String CHANNEL_STATE_OF_HEALTH = "state-of-health";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_OUTPUT_VOLTAGE = "output-voltage";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MAX_CELL_VOLTAGE = "max-cell-voltage";
    public static final String CHANNEL_MAX_CELL_VOLTAGE_CELL = "max-cell-voltage-cell";
    public static final String CHANNEL_MIN_CELL_VOLTAGE = "min-cell-voltage";
    public static final String CHANNEL_MIN_CELL_VOLTAGE_CELL = "min-cell-voltage-cell";
    public static final String CHANNEL_AVERAGE_CELL_VOLTAGE = "average-cell-voltage";
    public static final String CHANNEL_CELL_VOLTAGE_SPREAD = "cell-voltage-spread";
    public static final String CHANNEL_MAX_CELL_TEMPERATURE = "max-cell-temperature";
    public static final String CHANNEL_MAX_CELL_TEMPERATURE_SENSOR = "max-cell-temperature-sensor";
    public static final String CHANNEL_MIN_CELL_TEMPERATURE = "min-cell-temperature";
    public static final String CHANNEL_MIN_CELL_TEMPERATURE_SENSOR = "min-cell-temperature-sensor";
    public static final String CHANNEL_BMU_TEMPERATURE = "bmu-temperature";
    public static final String CHANNEL_CHARGED_ENERGY = "charged-energy";
    public static final String CHANNEL_DISCHARGED_ENERGY = "discharged-energy";
    public static final String CHANNEL_EFFICIENCY = "efficiency";
    public static final String CHANNEL_ERROR = "error";
    public static final String CHANNEL_ERRORS = "errors";
    public static final String CHANNEL_BALANCING_CELLS = "balancing-cells";
    public static final String CHANNEL_WARNING = "warning";
    public static final String CHANNEL_WARNINGS = "warnings";
    public static final String CHANNEL_CELL_VOLTAGE_PREFIX = "cell-voltage-";
    public static final String CHANNEL_CELL_TEMPERATURE_PREFIX = "cell-temperature-";

    // event and diagnostics channels
    public static final String CHANNEL_LAST_EVENT = "last-event";
    public static final String CHANNEL_LAST_EVENT_TIME = "last-event-time";
    public static final String CHANNEL_EVENT = "event";
    public static final String CHANNEL_BMU_CLOCK_OFFSET = "bmu-clock-offset";
    public static final String CHANNEL_LAST_DETAIL_UPDATE = "last-detail-update";

    // properties
    public static final String PROPERTY_BMU_FIRMWARE_A = "bmuFirmwareA";
    public static final String PROPERTY_BMU_FIRMWARE_B = "bmuFirmwareB";
    public static final String PROPERTY_BMS_FIRMWARE = "bmsFirmware";
    public static final String PROPERTY_TOWERS = "towers";
    public static final String PROPERTY_MODULES_PER_TOWER = "modulesPerTower";
    public static final String PROPERTY_CELLS_PER_MODULE = "cellsPerModule";
    public static final String PROPERTY_TEMPERATURE_SENSORS_PER_MODULE = "temperatureSensorsPerModule";
    public static final String PROPERTY_PARAMETER_TABLE = "parameterTable";
    public static final String PROPERTY_INVERTER_TYPE = "inverterType";
    public static final String PROPERTY_APPLICATION = "application";
    public static final String PROPERTY_TOWER_SERIAL_NUMBER = "tower%dSerialNumber";
    public static final String PROPERTY_TOWER_FIRMWARE = "tower%dFirmware";
}
