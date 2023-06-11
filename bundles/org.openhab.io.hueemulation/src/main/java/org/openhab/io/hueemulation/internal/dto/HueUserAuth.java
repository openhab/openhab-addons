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
package org.openhab.io.hueemulation.internal.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Hue user object. Used by {@link HueAuthorizedConfig}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueUserAuth {
    public String name = "";
    public String createDate = "";
    public String lastUseDate = "";

    public String clientKey = "";

    /**
     * For de-serialization.
     */
    HueUserAuth() {
    }

    /**
     * Create a new user
     *
     * @param apikey The hue "username"
     */
    public HueUserAuth(String appName, String deviceName) {
        this.name = appName + "#" + deviceName;
        this.createDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
