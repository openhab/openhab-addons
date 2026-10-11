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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;

/**
 * One power meter attached to the power plant.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCPowerMeterHandler extends E3DCThingHandler {

    private final E3DCChannelRegistry registry = E3DCChannelSpec.powerMeter();
    private final E3DCPowerMeterConfiguration config;

    public E3DCPowerMeterHandler(Thing thing) {
        super(thing);
        this.config = thing.getConfiguration().as(E3DCPowerMeterConfiguration.class);
    }

    @Override
    protected E3DCChannelRegistry getRegistry() {
        return registry;
    }

    @Override
    protected @Nullable Integer getDeviceIndex() {
        return config.meterIndex;
    }
}
