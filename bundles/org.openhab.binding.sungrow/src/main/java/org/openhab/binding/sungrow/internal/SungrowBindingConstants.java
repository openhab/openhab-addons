/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SungrowBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Kemper - Initial contribution
 */
@NonNullByDefault
public class SungrowBindingConstants {

    private static final String BINDING_ID = "sungrow";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "sungrow-bridge");
    public static final ThingTypeUID THING_TYPE_PLANT = new ThingTypeUID(BINDING_ID, "sungrow-plant");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "sungrow-device");

    public static final String CHANNEL_GROUP_PLANT = "plant";

    public static final int TIMEOUT = 30;
}
