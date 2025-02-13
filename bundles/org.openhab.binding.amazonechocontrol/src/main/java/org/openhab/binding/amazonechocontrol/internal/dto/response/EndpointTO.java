/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link DoNotDisturbDeviceStatusesTO} encapsulate the response of /api/endpoints
 *
 * @author Jan N. Klug - Initial contribution
 */
public class EndpointTO {
    public String alexaApiUrl;
    public String awsRegion;
    public String retailDomain;
    public String retailUrl;
    public String skillsStoreUrl;
    public String websiteApiUrl;
    public String websiteUrl;

    @Override
    public @NonNull String toString() {
        return "EndpointTO{alexaApiUrl='" + alexaApiUrl + "', awsRegion='" + awsRegion + "', retailDomain='"
                + retailDomain + "', retailUrl='" + retailUrl + "', skillsStoreUrl='" + skillsStoreUrl
                + "', websiteApiUrl='" + websiteApiUrl + "', websiteUrl='" + websiteUrl + "'}";
    }
}
