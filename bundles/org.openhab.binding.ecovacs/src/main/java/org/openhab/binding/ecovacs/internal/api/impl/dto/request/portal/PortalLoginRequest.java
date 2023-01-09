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
package org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal;

import org.openhab.binding.ecovacs.internal.api.impl.PortalTodo;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class PortalLoginRequest {

    @SerializedName("todo")
    final PortalTodo todo;

    @SerializedName("country")
    final String country;

    @SerializedName("last")
    final String last;

    @SerializedName("org")
    final String org;

    @SerializedName("resource")
    final String resource;

    @SerializedName("realm")
    final String realm;

    @SerializedName("token")
    final String token;

    @SerializedName("userid")
    final String userId;

    @SerializedName("edition")
    final String edition;

    public PortalLoginRequest(PortalTodo todo, String country, String last, String org, String resource, String realm,
            String token, String userId, String edition) {
        this.todo = todo;
        this.country = country;
        this.last = last;
        this.org = org;
        this.resource = resource;
        this.realm = realm;
        this.token = token;
        this.userId = userId;
        this.edition = edition;
    }
}
