/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.powermax.internal.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to store the settings of an X10 device
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxX10Settings {

    private final @Nullable String name;
    private final boolean enabled;

    public PowermaxX10Settings(@Nullable String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    /**
     * @return the name of the X10 device
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * @return true if the X10 device is enabled; false if not
     */
    public boolean isEnabled() {
        return enabled;
    }
}
