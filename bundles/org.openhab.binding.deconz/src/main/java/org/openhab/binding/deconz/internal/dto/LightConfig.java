/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LightConfig} holds a light configuration.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class LightConfig {
    public boolean hascolor = false;
    public @Nullable Integer ctmin;
    public @Nullable Integer ctmax;

    public LightConfig() {
    }

    public LightConfig(LightMessage lightMessage) {
        if (lightMessage.hascolor != null) {
            this.hascolor = lightMessage.hascolor;
        }
        this.ctmin = lightMessage.ctmin;
        this.ctmax = lightMessage.ctmax;
    }
}
