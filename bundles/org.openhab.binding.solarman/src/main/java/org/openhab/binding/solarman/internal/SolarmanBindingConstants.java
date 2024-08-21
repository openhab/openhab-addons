/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solarman.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolarmanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class SolarmanBindingConstants {

    public static final String SOLARMAN_BINDING_ID = "solarman";
    public static final ThingTypeUID THING_TYPE_SOLARMAN_LOGGER = new ThingTypeUID(SOLARMAN_BINDING_ID, "logger");
    public static final String DYNAMIC_CHANNEL = "dynamic-channel";
}
