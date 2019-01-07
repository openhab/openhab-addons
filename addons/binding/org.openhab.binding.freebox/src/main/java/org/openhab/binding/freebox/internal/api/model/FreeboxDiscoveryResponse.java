/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
