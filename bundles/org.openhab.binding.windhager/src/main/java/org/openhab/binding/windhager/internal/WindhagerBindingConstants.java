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
package org.openhab.binding.windhager.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WindhagerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author BenjiU - Initial contribution
 */
@NonNullByDefault
public class WindhagerBindingConstants {

    private static final String BINDING_ID = "windhager";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BIOWIN2 = new ThingTypeUID(BINDING_ID, "biowin2");
}
