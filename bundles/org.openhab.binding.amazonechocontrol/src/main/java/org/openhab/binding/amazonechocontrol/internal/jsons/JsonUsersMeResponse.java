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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonUsersMeResponse} encapsulate the GSON data of the users me response
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonUsersMeResponse {
    public @Nullable String countryOfResidence;
    public @Nullable String effectiveMarketPlaceId;
    public @Nullable String email;
    public @Nullable Boolean eulaAcceptance;
    public @Nullable List<String> features;
    public @Nullable String fullName;
    public @Nullable Boolean hasActiveDopplers;
    public @Nullable String id;
    public @Nullable String marketPlaceDomainName;
    public @Nullable String marketPlaceId;
    public @Nullable String marketPlaceLocale;
}
