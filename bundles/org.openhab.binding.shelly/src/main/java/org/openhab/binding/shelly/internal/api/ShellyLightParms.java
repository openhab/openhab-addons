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
 * @param mode Light mode: "color" or "white"
 * @param onOff On/off state, represented as "on" or "off"
 * @param red Red color component, 0-255
 * @param green Green color component, 0-255
 * @param blue Blue color component, 0-255
 * @param white White color component, 0-255
 * @param gain Gain/saturation, 0-100
 * @param brightness Brightness, 0-100
 * @param effect Effect index, 0-6
 * @param colorTemp Color temperature in Kelvin
 */
@NonNullByDefault
public record ShellyLightParms(
        @Nullable String mode,
        @Nullable String onOff,
        @Nullable Integer red,
        @Nullable Integer green,
        @Nullable Integer blue,
        @Nullable Integer white,
        @Nullable Integer gain,
        @Nullable Integer brightness,
        @Nullable Integer effect,
        @Nullable Integer colorTemp) {
}
