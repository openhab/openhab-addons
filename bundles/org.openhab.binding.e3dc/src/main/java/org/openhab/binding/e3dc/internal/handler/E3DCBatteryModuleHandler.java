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
 * One battery cell module of the power plant.
 *
 * <p>
 * A module reports its values in its own container inside the data of its battery, so the handler selects the
 * data container of its battery first and the module container inside it by the module index rather than by
 * position.
 *
 * <p>
 * The module index counts the modules of one battery: a system with several batteries reports module 0 once per
 * battery, so the battery is part of the identity of a module.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBatteryModuleHandler extends E3DCThingHandler {

    /** The energy channels and the capacity channel each one is calculated from. */
    private static final Map<String, String> ENERGY_SOURCES = Map.of("design-energy", "design-capacity",
            "full-charge-energy", "full-charge-capacity", "remaining-energy", "remaining-capacity");

    private final E3DCChannelRegistry registry = E3DCChannelSpec.batteryModule();
    private E3DCBatteryModuleConfiguration config = new E3DCBatteryModuleConfiguration();

    public E3DCBatteryModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // read here rather than in the constructor: an updated index reaches an already created handler only
        // through initialize(), which the framework calls again after a configuration change
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
