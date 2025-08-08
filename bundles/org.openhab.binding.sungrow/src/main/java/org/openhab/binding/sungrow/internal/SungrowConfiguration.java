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

/**
 * The {@link SungrowConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Kemper - Initial contribution
 */
@NonNullByDefault
public class SungrowConfiguration {

    private String hostname = "";

    private String appKey = "";

    private String appSecret = "";

    private String username = "";

    private String password = "";

    private Integer interval = 30;

    public String getHostname() {
        return hostname;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppSecret() {
        return appSecret;
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
}
