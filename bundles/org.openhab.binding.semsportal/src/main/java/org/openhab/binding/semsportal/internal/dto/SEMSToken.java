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
package org.openhab.binding.semsportal.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The session token of the portal. It is sent as a header with every request. Before logging in it is an empty token
 * that only names the client type. The token received at login also tells the gateway to send the other requests to.
 * <p>
 * The portal gives the token as JSON and expects it back as it was received, like its web application does. Gson reads
 * and writes all fields, which is why most of them have no getter. As far as tested, the portal only requires uid,
 * token and client; the other fields are returned for compatibility.
 *
 * @author Iwan Bron - Initial contribution
 * @author Julio Vilmar Gesser - Migrated to the SEMS+ API
 */
@SuppressWarnings("unused") // the fields are used by Gson
@NonNullByDefault
public class SEMSToken {
    private String uid = "";
    private long timestamp;
    private String token = "";
    private String client;
    private String version = "";
    private String language = "en";
    private @Nullable String api;
    private @Nullable String region;

    public SEMSToken(String client) {
        this.client = client;
    }

    public String getUid() {
        return uid;
    }

    public String getToken() {
        return token;
    }

    public String getClient() {
        return client;
    }

    /**
     * @return the base url of the regional gateway that the other requests have to be sent to. Only available in the
     *         token received at login.
     */
    public @Nullable String getApi() {
        return api;
    }
}
