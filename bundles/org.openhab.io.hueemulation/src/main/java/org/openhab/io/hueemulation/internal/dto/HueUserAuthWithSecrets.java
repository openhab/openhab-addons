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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.common.registry.Identifiable;

/**
 * Hue user object. This is stored on disk for hue users, but not send via the rest api
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueUserAuthWithSecrets extends HueUserAuth implements Identifiable<String> {
    public String apiKey = "";

    /**
     * For de-serialization.
     */
    HueUserAuthWithSecrets() {
    }

    /**
     * Create a new user with credentials
     */
    public HueUserAuthWithSecrets(String appName, String deviceName, String apiKey, String clientKey) {
        super(appName, deviceName);
        this.apiKey = apiKey;
        this.clientKey = clientKey;
    }

    @Override
    public String getUID() {
        return apiKey;
    }
}
