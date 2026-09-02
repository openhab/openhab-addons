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
package org.openhab.binding.atmofrance.internal.api;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link AtmoFranceApi} class defines paths used to interact with server api
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class AtmoFranceApi {
    public static final Duration TOKEN_VALIDITY = Duration.ofHours(24).minusMinutes(30);
    private static final UriBuilder ATMO_BUILDER = UriBuilder.fromPath("api").scheme("https")
            .host("admindata.atmo-france.org");
    public static final URI LOGIN_URI = ATMO_BUILDER.clone().path("login").build();
    private static final UriBuilder API_V2 = ATMO_BUILDER.clone().path("v2").path("data").path("indices");
    private static final UriBuilder ATMO_URI = API_V2.clone().path("atmo");
    private static final UriBuilder POLLENS_URI = API_V2.clone().path("pollens");

    public static URI getAtmoUri(LocalDate date, String insee) {
        return ATMO_URI.clone().queryParam("date", date).queryParam("code_zone", insee).build();
    }

    public static URI getPollensUri(LocalDate date, String insee) {
        return POLLENS_URI.clone().queryParam("date", date).queryParam("code_zone", insee).build();
    }
}
