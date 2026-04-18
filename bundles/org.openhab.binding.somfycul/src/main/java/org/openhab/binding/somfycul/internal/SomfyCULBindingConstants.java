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
package org.openhab.binding.somfycul.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SomfyCULBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marc Klasser - Initial contribution
 */
@NonNullByDefault
public class SomfyCULBindingConstants {

    private static final String BINDING_ID = "somfycul";

    // List of all Thing Type UIDs
    /**
     * CUL stick
     */
    public static final ThingTypeUID CUL_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "cul-device");

    /**
     * Somfy RTS device (e.g. rollershutter)
     */
    public static final ThingTypeUID SOMFY_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "somfy-device");

    // List of all Channel ids
    /**
     * Rollershutter's position
     */
    public static final String POSITION = "position";

    /**
     * Rollershutter's program
     */
    public static final String PROGRAM = "program";
}
