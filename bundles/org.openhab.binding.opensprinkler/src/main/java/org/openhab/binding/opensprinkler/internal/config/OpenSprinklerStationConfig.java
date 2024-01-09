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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenSprinklerStationConfig} class defines the configuration options
 * for the OpenSprinkler Thing.
 *
 * @author Chris Graham - Initial contribution
 */
@NonNullByDefault
public class OpenSprinklerStationConfig {
    /**
     * The index of the station the thing is configured to control, starting with 0.
     */
    public int stationIndex = 0;
}
