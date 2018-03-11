/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onewiregpio;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
     * The refresh time in seconds.
     */
    public static final String REFRESH_TIME = "refresh_time";

    /**
     * The default auto refresh time in seconds.
     */
    public static final Integer DEFAULT_REFRESH_TIME = Integer.valueOf(120);

    public static final String GPIO_BUS_FILE = "gpio_bus_file";

    public static final String FILE_TEMP_MARKER = "t=";

}
