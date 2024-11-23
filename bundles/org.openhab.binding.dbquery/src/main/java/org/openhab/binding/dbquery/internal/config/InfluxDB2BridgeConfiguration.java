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
package org.openhab.binding.dbquery.internal.config;

import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains fields mapping InfluxDB2 bridge configuration parameters.
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class InfluxDB2BridgeConfiguration {
    private String url;
    private String user;
    private String token;
    private String bucket;
    private String organization;

    public InfluxDB2BridgeConfiguration(String url, String user, String token, String organization, String bucket) {
        this.url = url;
        this.user = user;
        this.token = token;
        this.organization = organization;
        this.bucket = bucket;
    }

    public InfluxDB2BridgeConfiguration() {
        // Used only when configuration is created by reflection using ConfigMapper
        url = user = token = organization = bucket = "";
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public String getOrganization() {
        return organization;
    }

    public String getBucket() {
        return bucket;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InfluxDB2BridgeConfiguration.class.getSimpleName() + "[", "]")
                .add("url='" + url + "'").add("user='" + user + "'").add("token='" + "*".repeat(token.length()) + "'")
                .add("organization='" + organization + "'").add("bucket='" + bucket + "'").toString();
    }
}
