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
package org.openhab.binding.shelly.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Typed parameters for light control commands. Profiles that don't support a particular parameter should have that
 * field set to null, and will ignore the field.
 *
 * @author Andrew Fiddian-Green - Refactored from Map<String, String> to typed parameters
 */
@NonNullByDefault
public class ShellyLightParms {
    /**
     * Light mode: "color" or "white"
     */
    @Nullable
    public String mode;

    /**
     * On/off state, represented as "on" or "off"
     */
    @Nullable
    public String onOff;

    /**
     * Red color component, 0-255
     */
    @Nullable
    public Integer red;

    /**
     * Green color component, 0-255
     */
    @Nullable
    public Integer green;

    /**
     * Blue color component, 0-255
     */
    @Nullable
    public Integer blue;

    /**
     * White color component, 0-255
     */
    @Nullable
    public Integer white;

    /**
     * Gain/saturation, 0-100
     */
    @Nullable
    public Integer gain;

    /**
     * Brightness, 0-100
     */
    @Nullable
    public Integer brightness;

    /**
     * Effect index, 0-6
     */
    @Nullable
    public Integer effect;

    /**
     * Color temperature in Kelvin
     */
    @Nullable
    public Integer colorTemp;
}
