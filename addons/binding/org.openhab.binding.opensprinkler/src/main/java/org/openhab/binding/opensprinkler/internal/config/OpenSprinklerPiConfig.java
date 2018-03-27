/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.internal.config;

import static org.openhab.binding.opensprinkler.OpenSprinklerBindingConstants.DEFAULT_REFRESH_RATE;
import static org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiConstants.DEFAULT_STATION_COUNT;

/**
 * The {@link OpenSprinklerPiConfig} class defines the configuration options
 * for the OpenSprinkler PI Thing.
 *
 * @author Chris Graham - Initial contribution
 */
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
