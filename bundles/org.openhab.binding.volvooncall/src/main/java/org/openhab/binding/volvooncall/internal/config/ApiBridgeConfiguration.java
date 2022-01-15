/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApiBridgeConfiguration} is responsible for holding
 * configuration informations needed to access VOC API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiBridgeConfiguration {
    public String username = "";
    public String password = "";

    public String getAuthorization() {
        byte[] authorization = Base64.getEncoder().encode((String.format("%s:%s", username, password)).getBytes());
        return "Basic " + new String(authorization, StandardCharsets.UTF_8);
    }
}
