/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonActivity} encapsulate the GSON data of the users me response
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonUsersMeResponse {
    @Nullable
    public String countryOfResidence;
    @Nullable
    public String effectiveMarketPlaceId;
    @Nullable
    public String email;
    @Nullable
    public Boolean eulaAcceptance;
    @Nullable
    public String @Nullable [] features;
    @Nullable
    public String fullName;
    @Nullable
    public Boolean hasActiveDopplers;
    @Nullable
    public String id;
    @Nullable
    public String marketPlaceDomainName;
    @Nullable
    public String marketPlaceId;
    @Nullable
    public String marketPlaceLocale;
}
