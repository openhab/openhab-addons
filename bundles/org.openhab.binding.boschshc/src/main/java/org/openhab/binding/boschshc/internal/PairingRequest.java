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
package org.openhab.binding.boschshc.internal;

import com.google.gson.annotations.SerializedName;

// Modeled after: BoschClientData in model/bosch-client-data.ts
class PairRequest {

    PairRequest(String name, String id) {

        this.type = "client";
        this.id = "oss_" + id;
        this.name = "OSS " + name;

        this.primaryRole = "ROLE_RESTRICTED_CLIENT";
        this.deleted = false;
    }

    @SerializedName("@type")
    String type;

    String id;
    String name;

    String primaryRole;
    boolean deleted;
}
