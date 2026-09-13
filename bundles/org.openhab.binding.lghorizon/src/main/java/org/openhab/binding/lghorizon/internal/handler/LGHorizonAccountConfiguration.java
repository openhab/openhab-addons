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
package org.openhab.binding.lghorizon.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for the {@code account} bridge thing.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public class LGHorizonAccountConfiguration {

    // Known provider from {@link org.openhab.binding.lghorizon.internal.api.ProviderPresets}, e.g. {@code telenet} or
    // left empty to configure an unlisted provider (or a provider's preprod/test backend) manually using the {@link
    // #country}, {@link #apiUrl} and {@link #useRefreshToken} fields instead.
    public @Nullable String provider;

    // Two-letter locale code, known automatically when {@link #provider} is set, required if it is left empty.
    public String country = "";

    // Base URL of the provider's REST API. Known automatically when {@link #provider} is set; required if it is left
    // empty.
    public String apiUrl = "";

    // Whether the provider requires refresh-token auth instead of username/password. Known
    // automatically when {@link #provider} is set; only consulted if it is left empty.
    public boolean useRefreshToken = false;

    // Only used for password-based providers (e.g. Ziggo NL).
    public @Nullable String username;

    // Only used for password-based providers (e.g. Ziggo NL).
    public @Nullable String password;

    // Only used for token-based providers (e.g. Telenet BE, UPC/Sunrise CH, Virgin Media GB).
    public String refreshToken = "";
}
