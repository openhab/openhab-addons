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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxDiscoveryResponse} is the Java class used to map the
 * structure used by the response of the request authorization API
 * https://dev.freebox.fr/sdk/os/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxDiscoveryResponse {
    private String uid;
    private String deviceName;
    private String apiVersion;
    private String apiBaseUrl;
    private String deviceType;
    private String apiDomain;
    private Boolean httpsAvailable;
    private Integer httpsPort;

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

    public Boolean isHttpsAvailable() {
        return httpsAvailable;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }
}
