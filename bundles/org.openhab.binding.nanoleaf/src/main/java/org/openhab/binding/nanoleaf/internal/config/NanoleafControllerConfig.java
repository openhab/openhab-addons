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
package org.openhab.binding.nanoleaf.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NanoleafControllerConfig} class contains fields mapping controller configuration parameters.
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class NanoleafControllerConfig {

    /** IP address or hostname of the light panels controller */
    public static final String ADDRESS = "address";
    public String address = "";
    /** Port number of the light panels controller */
    public static final String PORT = "port";
    public int port = 16021;
    /** Authorization token for controller API */
    public static final String AUTH_TOKEN = "authToken";
    public @Nullable String authToken;
    /** Light panels status refresh interval */
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public int refreshInterval = 60;
    /** Nanoleaf device type: Light panels or Canvas */
    public static final String DEVICE_TYPE = "deviceType";
    public String deviceType = "lightPanels";
}
