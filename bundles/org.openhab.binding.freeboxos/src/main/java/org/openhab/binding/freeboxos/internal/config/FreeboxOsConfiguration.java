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
package org.openhab.binding.freeboxos.internal.config;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxTlsCertificateProvider;

/**
 * The {@link FreeboxOsConfiguration} is responsible for holding configuration informations needed to access the Freebox
 * API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsConfiguration {
    public static final String API_DOMAIN = "apiDomain";
    public static final String APP_TOKEN = "appToken";
    public static final String HTTPS_PORT = "httpsPort";
    public static final String HTTPS_AVAILABLE = "httpsAvailable";

    private String apiDomain = FreeboxTlsCertificateProvider.DEFAULT_NAME;
    public String appToken = "";
    public boolean discoverNetDevice;
    public int discoveryInterval = 10;
    public int wsReconnectInterval = 60;

    private int httpsPort = 15682;
    private boolean httpsAvailable;

    private String getScheme() {
        return httpsAvailable ? "https" : "http";
    }

    private int getPort() {
        return httpsAvailable ? httpsPort : 80;
    }

    public UriBuilder getUriBuilder(String path) {
        return UriBuilder.fromPath("/").scheme(getScheme()).port(getPort()).host(apiDomain).path(path).clone();
    }
}
