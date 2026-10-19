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
package org.openhab.binding.e3dc.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.thing.Thing;

/**
 * One battery of the power plant, read from the battery container addressed by the battery index.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBatteryHandler extends E3DCThingHandler {

    /** The energy channels and the capacity channel each one is calculated from. */
    private static final Map<String, String> ENERGY_SOURCES = Map.of("design-energy", "design-capacity",
            "usable-energy", "usable-capacity", "usable-remaining-energy", "usable-remaining-capacity");

    private final E3DCChannelRegistry registry = E3DCChannelSpec.battery();
    private final E3DCBatteryConfiguration config;

    public E3DCBatteryHandler(Thing thing) {
        super(thing);
        this.config = thing.getConfiguration().as(E3DCBatteryConfiguration.class);
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
    }
}
