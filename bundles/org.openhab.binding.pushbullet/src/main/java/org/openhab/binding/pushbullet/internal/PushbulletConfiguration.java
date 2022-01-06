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
package org.openhab.binding.pushbullet.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PushbulletConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Hakan Tandogan - Initial contribution
 */
@NonNullByDefault
public class PushbulletConfiguration {

    private @Nullable String name;

    private String token = "invalid";

    private String apiUrlBase = "invalid";

    public @Nullable String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getApiUrlBase() {
        return apiUrlBase;
    }

    public void setApiUrlBase(String apiUrlBase) {
        this.apiUrlBase = apiUrlBase;
    }
}
