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
package org.openhab.binding.pushbullet.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PushbulletConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Jeremy Setton - Add link and file push type support
 */
@NonNullByDefault
public class PushbulletConfiguration {

    private @Nullable String name;

    private String token = "";

    private String apiUrlBase = "https://api.pushbullet.com/v2";

    public @Nullable String getName() {
        return name;
    }

    public String getAccessToken() {
        return token;
    }

    public String getApiUrlBase() {
        return apiUrlBase;
    }
}
