/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VelbusVMBGPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusVMBGPHandler extends VelbusThermostatHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMBGP1, THING_TYPE_VMBGP1_2, THING_TYPE_VMBGP2, THING_TYPE_VMBGP2_2,
                    THING_TYPE_VMBGP4, THING_TYPE_VMBGP4_2, THING_TYPE_VMBGP4PIR, THING_TYPE_VMBGP4PIR_2));

    public VelbusVMBGPHandler(Thing thing) {
        super(thing, 4, new ChannelUID(thing.getUID(), "input", "CH9"));
    }
}
