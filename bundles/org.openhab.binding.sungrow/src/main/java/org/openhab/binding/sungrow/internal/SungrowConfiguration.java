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
package org.openhab.binding.sungrow.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import de.afrouper.server.sungrow.api.SungrowClientBuilder;

/**
 * The {@link SungrowConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Kemper - Initial contribution
 */
@NonNullByDefault
public class SungrowConfiguration {

    private String appKey = "";

    private String secretKey = "";

    private String username = "";

    private String password = "";

    private Integer interval = Integer.valueOf(60);

    private SungrowClientBuilder.Region region = SungrowClientBuilder.Region.EUROPE;

    private String hostname = "";

    public String getAppKey() {
        return appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getInterval() {
        return interval;
    }

    public SungrowClientBuilder.Region getRegion() {
        return region;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isValid() {
        return appKey != null && secretKey != null && username != null && password != null && interval != null
                && region != null;
    }

    @Override
    public String toString() {
        return "SungrowConfiguration{" + "appKey='" + logFormat(appKey) + '\'' + ", appSecret='" + logFormat(secretKey)
                + '\'' + ", username='" + username + '\'' + ", password='" + logFormat(password) + '\'' + ", interval="
                + interval + ", region=" + region + ", hostname='" + hostname + '\'' + '}';
    }

    private String logFormat(String s) {
        if (s == null) {
            return "N/A";
        } else if (s.length() == 0) {
            return "''";
        } else {
            return s.substring(0, s.length() / 4) + "...";
        }
    }
}
