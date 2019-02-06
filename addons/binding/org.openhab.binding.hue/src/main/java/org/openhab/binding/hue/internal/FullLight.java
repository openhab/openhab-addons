/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

/**
 * Detailed light information.
 *
 * @author Q42 - Initial contribution
 * @author Thomas Höfer - added unique id and changed range check for brightness and saturation
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 * @author Samuel Leisering - added GSon Type to FullLight, refactored content to {@link FullHueObject}
 */
public class FullLight extends FullHueObject {
    public static final Type GSON_TYPE = new TypeToken<Map<String, FullLight>>() {
    }.getType();

    private State state;
    private final long fadetime = 400; // milliseconds

    FullLight() {
    }

    /**
     * Returns the current state of the light.
     *
     * @return current state
     */
    public State getState() {
        return state;
    }

    public Duration getFadeTime() {
        return Duration.ofMillis(fadetime);
    }
}
