/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.config;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.DEFAULT_FREEBOX_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FreeboxOsConfiguration} is responsible for holding
 * configuration informations needed to access the Freebox API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsConfiguration {
    public static final String API_DOMAIN = "api_domain";
    public static final String HTTPS_AVAILABLE = "https_available";
    public static final String HTTPS_PORT = "https_port";
    public static final String APP_TOKEN = "appToken";

    private int httpsPort = 15682;
    private boolean httpsAvailable;

    public String apiDomain = DEFAULT_FREEBOX_NAME;
    public @Nullable String appToken;
    public boolean discoverNetDevice;

    public String getScheme() {
        return httpsAvailable ? "https" : "http";
    }

    public int getPort() {
        return httpsAvailable ? httpsPort : 80;
    }
}
