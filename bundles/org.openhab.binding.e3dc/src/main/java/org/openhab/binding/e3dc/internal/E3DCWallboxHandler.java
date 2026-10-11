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
 * One wallbox attached to the power plant.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCWallboxHandler extends E3DCThingHandler {

    private final E3DCChannelRegistry registry = E3DCChannelSpec.wallbox();
    private final E3DCCommandRegistry commandRegistry = E3DCCommandSpec.wallbox();
    private final E3DCWallboxConfiguration config;

    public E3DCWallboxHandler(Thing thing) {
        super(thing);
        this.config = thing.getConfiguration().as(E3DCWallboxConfiguration.class);
    }

    @Override
    protected E3DCChannelRegistry getRegistry() {
        return registry;
    }

    @Override
    protected @Nullable E3DCCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    protected @Nullable Integer getDeviceIndex() {
        return config.wallboxIndex;
    }
}
