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
package org.openhab.binding.opensprinkler.internal.config;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenSprinklerPiConfig} class defines the configuration options
 * for the OpenSprinkler PI Thing.
 *
 * @author Chris Graham - Initial contribution
 */
@NonNullByDefault
public class OpenSprinklerPiConfig {
    /**
     * Number of stations to control.
     */
    public int stations = DEFAULT_STATION_COUNT;

    /**
     * Number of seconds in between refreshes from the OpenSprinkler device.
     */
    public int refresh = DEFAULT_REFRESH_RATE;
}
