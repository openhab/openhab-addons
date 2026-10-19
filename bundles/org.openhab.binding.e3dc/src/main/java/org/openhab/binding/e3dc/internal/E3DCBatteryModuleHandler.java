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
 * One battery cell module of the power plant.
 *
 * <p>
 * A module reports its values in its own container inside the battery data, so the handler selects that
 * container by the module index rather than by position.
 *
 * <p>
 * The module index is unique within one battery. A system with several batteries is not addressed
 * separately here: the device numbers the modules of all batteries consecutively in the response.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBatteryModuleHandler extends E3DCThingHandler {

    /** The energy channels and the capacity channel each one is calculated from. */
    private static final Map<String, String> ENERGY_SOURCES = Map.of("design-energy", "design-capacity",
            "full-charge-energy", "full-charge-capacity", "remaining-energy", "remaining-capacity");

    private final E3DCChannelRegistry registry = E3DCChannelSpec.batteryModule();
    private final E3DCBatteryModuleConfiguration config;

    public E3DCBatteryModuleHandler(Thing thing) {
        super(thing);
        this.config = thing.getConfiguration().as(E3DCBatteryModuleConfiguration.class);
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
        super.onData(response);
        updateEnergies(response, E3DCBindingConstants.GROUP_MODULE, "voltage", ENERGY_SOURCES);
    }
}
