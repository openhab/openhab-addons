/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SensorState} is send by the websocket connection as well as the Rest API.
 * It is part of a {@link SensorMessage}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorState {
    /** Some presence sensors, the daylight sensor and all light sensors provide the "dark" boolean. */
    public @Nullable Boolean dark;
    /** The daylight sensor provides the "daylight" boolean. */
    public @Nullable Boolean daylight;
    /** Light sensors provide a lux value. */
    public @Nullable Integer lux;
    /** Presence sensors provide this boolean. */
    public @Nullable Boolean presence;
    /** Power sensors provide this value in Watts. */
    public @Nullable Integer power;
    /** Light sensors and the daylight sensor provide a status integer that can have various semantics. */
    public @Nullable Integer status;
    /** Switches provide this value. */
    public @Nullable Integer buttonevent;
    /** deCONZ sends a last update string with every event. */
    public @Nullable String lastupdated;
}
