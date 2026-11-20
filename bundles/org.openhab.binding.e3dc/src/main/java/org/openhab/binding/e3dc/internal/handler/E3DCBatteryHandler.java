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
import org.openhab.binding.e3dc.internal.config.E3DCBatteryConfiguration;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.thing.Thing;

/**
 * One battery of the power plant, read from the battery container addressed by the battery index.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBatteryHandler extends E3DCThingHandler {

    private static final Map<String, String> ENERGY_SOURCES = Map.of("design-energy", "design-capacity",
            "usable-energy", "usable-capacity", "usable-remaining-energy", "usable-remaining-capacity");

    /** The values that describe the battery rather than measuring it, published as thing properties. */
    private static final Map<String, E3DCChannelSpec> PROPERTIES = Map.ofEntries(//
            entry("deviceName", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_DEVICE_NAME)), //
            entry("moduleCount", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_DCB_COUNT)));

    private final E3DCChannelRegistry registry = E3DCChannelSpec.battery();
    private E3DCBatteryConfiguration config = new E3DCBatteryConfiguration();

    public E3DCBatteryHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(E3DCBatteryConfiguration.class);
        super.initialize();
    }

    @Override
    protected E3DCChannelRegistry getRegistry() {
        return registry;
    }

    @Override
    protected @Nullable Integer getDeviceIndex() {
        return config.batteryIndex;
    }

    @Override
    public void onData(RscpValue response) {
        super.onData(response);
        updateEnergies(response, E3DCBindingConstants.GROUP_PACK, "voltage", ENERGY_SOURCES);
        updateProperties(response, PROPERTIES);
    }
}
