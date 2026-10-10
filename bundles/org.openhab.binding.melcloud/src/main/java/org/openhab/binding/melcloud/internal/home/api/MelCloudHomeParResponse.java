/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.home.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Gson deserialization target for the JSON body returned by {@code POST auth.melcloudhome.com/connect/par}
 * (RFC 9126 Pushed Authorization Request), used by {@link MelCloudHomeAuthService}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeParResponse {

    @SerializedName("request_uri")
    public @Nullable String requestUri;

    @SerializedName("expires_in")
    public long expiresIn;
}
