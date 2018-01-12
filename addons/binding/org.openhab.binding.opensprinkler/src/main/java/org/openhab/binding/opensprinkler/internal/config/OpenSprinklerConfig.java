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
import static org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiConstants.*;

/**
 * The {@link OpenSprinklerConfig} class defines the configuration options
 * for the OpenSprinkler Thing.
 *
 * @author Chris Graham - Initial contribution
 */
public class OpenSprinklerConfig {
    /**
     * Hostname of the OpenSprinkler API.
     */
    public String hostname = null;

    /**
     * The port the OpenSprinkler API is listening on.
     */
    public int port = DEFAULT_API_PORT;

    /**
     * The password to connect to the OpenSprinkler API.
     */
    public String password = DEFAULT_ADMIN_PASSWORD;

    /**
     * Number of seconds in between refreshes from the OpenSprinkler device.
     */
    public int refresh = DEFAULT_REFRESH_RATE;
}
