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
package org.openhab.binding.onewiregpio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OneWireGPIOBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Anatol Ogorek - Initial contribution
 */
@NonNullByDefault
public class OneWireGPIOBindingConstants {

    public static final String BINDING_ID = "onewiregpio";

    public static final ThingTypeUID THING_TYPE = new ThingTypeUID(BINDING_ID, "sensor");

    public static final String TEMPERATURE = "temperature";

    /**
     * The default auto refresh time in seconds.
     */
    public static final Integer DEFAULT_REFRESH_TIME = Integer.valueOf(120);

    public static final int MAX_PRECISION_VALUE = 3;

    public static final String FILE_TEMP_MARKER = "t=";
}
