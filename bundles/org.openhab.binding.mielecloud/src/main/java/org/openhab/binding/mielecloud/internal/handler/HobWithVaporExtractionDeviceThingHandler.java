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
package org.openhab.binding.mielecloud.internal.handler;

import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.handler.channel.DeviceChannelState;
import org.openhab.core.thing.Thing;

/**
 * ThingHandler implementation for Miele hob devices with vapor extraction.
 *
 * @author Martin Rehker - Initial contribution
 */
@NonNullByDefault
public class HobWithVaporExtractionDeviceThingHandler extends HobDeviceThingHandler {
    /**
     * Creates a new {@link HobWithVaporExtractionDeviceThingHandler}.
     *
     * @param thing The thing to handle.
     */
    public HobWithVaporExtractionDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateDeviceState(DeviceChannelState device) {
        super.updateDeviceState(device);
        updateState(channel(VENTILATION_POWER), device.getVentilationPower());
        updateState(channel(VENTILATION_POWER_RAW), device.getVentilationPowerRaw());
    }
}
