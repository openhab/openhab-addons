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
package org.openhab.binding.e3dc.internal.handler;

import static java.util.Map.entry;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelRegistry;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.config.E3DCBatteryModuleConfiguration;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.thing.Thing;

/**
 * One battery cell module of the power plant; the module index counts inside its battery's container.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBatteryModuleHandler extends E3DCThingHandler {

    private static final Map<String, String> ENERGY_SOURCES = Map.of("design-energy", "design-capacity",
            "full-charge-energy", "full-charge-capacity", "remaining-energy", "remaining-capacity");

    /** The values that describe the module rather than measuring it, published as thing properties. */
    private static final Map<String, E3DCChannelSpec> PROPERTIES = Map.ofEntries(//
            entry("deviceName", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_DEVICE_NAME)), //
            entry(Thing.PROPERTY_VENDOR, E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_MANUFACTURE_NAME)), //
            entry("manufactureDate", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_MANUFACTURE_DATE)), //
            entry(Thing.PROPERTY_SERIAL_NUMBER, E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_SERIALNO)), //
            entry("serialCode", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_SERIALCODE)), //
            entry(Thing.PROPERTY_FIRMWARE_VERSION,
                    E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_FW_VERSION)), //
            entry("protocolVersion", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_PROTOCOL_VERSION)), //
            entry("tableVersion", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_DATA_TABLE_VERSION)), //
            entry("pcbVersion", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_PCB_VERSION)));

    /** The hardware specifications of the module, which do not change while it is in use. */
    private static final Map<String, E3DCChannelSpec> VOLTAGES = Map.ofEntries(//
            entry("designVoltage", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_DESIGN_VOLTAGE)), //
            entry("maxChargeVoltage", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_MAX_CHARGE_VOLTAGE)));

    private static final Map<String, E3DCChannelSpec> CURRENTS = Map.ofEntries(//
            entry("maxChargeCurrent", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_MAX_CHARGE_CURRENT)), //
            entry("maxDischargeCurrent",
                    E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_MAX_DISCHARGE_CURRENT)));

    private static final Map<String, E3DCChannelSpec> COUNTS = Map.ofEntries(//
            entry("sensorCount", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_NR_SENSOR)), //
            entry("seriesCellCount", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_NR_SERIES_CELL)), //
            entry("parallelCellCount", E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, RscpTag.BAT_DCB_NR_PARALLEL_CELL)));

    private final E3DCChannelRegistry registry = E3DCChannelSpec.batteryModule();
    private E3DCBatteryModuleConfiguration config = new E3DCBatteryModuleConfiguration();

    public E3DCBatteryModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(E3DCBatteryModuleConfiguration.class);
        super.initialize();
    }

    @Override
    protected E3DCChannelRegistry getRegistry() {
        return registry;
    }

    @Override
    protected @Nullable Integer getDeviceIndex() {
        return config.moduleIndex;
    }

    @Override
    public void onData(RscpValue response) {
        RscpValue batteryData = batteryData(response, config.batteryIndex);
        if (batteryData == null) {
            // the modules of another battery are no substitute for the ones of this module's battery
            logger.debug("The device reports no data for battery {}, module {} is not updated", config.batteryIndex,
                    config.moduleIndex);
            return;
        }
        super.onData(batteryData);
        updateEnergies(batteryData, E3DCBindingConstants.GROUP_MODULE, "voltage", ENERGY_SOURCES);
        updateProperties(batteryData, PROPERTIES);
        updateProperties(batteryData, VOLTAGES, "V");
        updateProperties(batteryData, CURRENTS, "A");
        updateProperties(batteryData, COUNTS);
    }

    /** The data container of one battery, or {@code null} when the battery is not part of the answer. */
    static @Nullable RscpValue batteryData(RscpValue response, int batteryIndex) {
        for (RscpValue battery : response.findAll(RscpTag.BAT_DATA)) {
            Number index = battery.findNumber(RscpTag.BAT_INDEX);
            if (index != null && index.intValue() == batteryIndex) {
                return battery;
            }
        }
        return null;
    }
}
