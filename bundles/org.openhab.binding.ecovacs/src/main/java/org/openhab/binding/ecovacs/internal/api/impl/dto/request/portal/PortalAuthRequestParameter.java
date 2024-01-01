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
package org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class PortalAuthRequestParameter {

    @SerializedName("with")
    final String with;

    @SerializedName("userid")
    final String userId;

    @SerializedName("realm")
    final String realm;

    @SerializedName("token")
    final String token;

    @SerializedName("resource")
    final String resource;

    public PortalAuthRequestParameter(String with, String userid, String realm, String token, String resource) {
        this.with = with;
        this.userId = userid;
        this.realm = realm;
        this.token = token;
        this.resource = resource;
    }
}
