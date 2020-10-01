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
package org.openhab.binding.freebox.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DiscoveryResponse} is the Java class used to map the
 * structure used by the response of the request authorization API
 * https://dev.freebox.fr/sdk/os/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class DiscoveryResponse {

    private String uid = ""; // The device unique id
    private String deviceName = "";
    private String apiVersion = ""; // The current API version on the Freebox
    private String apiBaseUrl = ""; // The API root path on the HTTP server
    private String deviceType = ""; // “FreeboxServer1,1” for the Freebox Server revision 1,1
    private String apiDomain = ""; // The domain to use in place of hardcoded Freebox ip
    private boolean httpsAvailable; // Tells if https has been configured on the Freebox
    private int httpsPort = -1; // Port to use for remote https access to the Freebox Api

    public String getUid() {
        return uid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getApiDomain() {
        return apiDomain;
    }

    public boolean isHttpsAvailable() {
        return httpsAvailable;
    }

    public int getHttpsPort() {
        return httpsPort;
    }
}
