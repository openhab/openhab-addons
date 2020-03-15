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