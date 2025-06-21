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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link UsersMeTO} encapsulate the response of /api/users/me
 *
 * @author Michael Geramb - Initial contribution
 */
public class UsersMeTO {
    public String countryOfResidence;
    public String effectiveMarketPlaceId;
    public String email;
    public Boolean eulaAcceptance;
    public List<String> features = List.of();
    public String fullName;
    public Boolean hasActiveDopplers;
    public String id;
    public String marketPlaceDomainName;
    public String marketPlaceId;
    public String marketPlaceLocale;

    @Override
    public @NonNull String toString() {
        return "UsersMeTO{countryOfResidence='" + countryOfResidence + "', effectiveMarketPlaceId='"
                + effectiveMarketPlaceId + "', email='" + email + "', eulaAcceptance=" + eulaAcceptance + ", features="
                + features + ", fullName='" + fullName + "'" + ", hasActiveDopplers=" + hasActiveDopplers + ", id='"
                + id + "', marketPlaceDomainName='" + marketPlaceDomainName + "', marketPlaceId='" + marketPlaceId
                + "', marketPlaceLocale='" + marketPlaceLocale + "'}";
    }
}
