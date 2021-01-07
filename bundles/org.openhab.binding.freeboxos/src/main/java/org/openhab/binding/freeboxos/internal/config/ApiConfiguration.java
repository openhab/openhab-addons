/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApiConfiguration} is responsible for holding
 * configuration informations needed to access/poll the Freebox API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiConfiguration {
    public static final String API_DOMAIN = "apiDomain";
    public static final String HTTPS_AVAILABLE = "httpsAvailable";
    public static final String HTTPS_PORT = "httpsPort";
    public static final String APP_TOKEN = "appToken";
    public static final String API_VERSION = "apiVersion";
    public static final String BASE_URL = "baseUrl";

    public String apiDomain = "mafreebox.freebox.fr";
    public int httpsPort = 15682;
    public boolean discoverNetDevice;
    public boolean httpsAvailable;
    public String apiVersion = "5.0";
    public String baseUrl = "/api/";
    public String appToken = "";

    public String apiMajorVersion() {
        String[] elements = apiVersion.split("\\.");
        return elements[0];
    }
}
