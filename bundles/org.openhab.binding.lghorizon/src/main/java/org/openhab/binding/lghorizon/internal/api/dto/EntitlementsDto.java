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
package org.openhab.binding.lghorizon.internal.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Response body of {@code GET /purchaseService/v2/customers/{householdId}/entitlements}.
 *
 * @author Mark - Initial contribution
 */
public class EntitlementsDto {

    @SerializedName("entitlements")
    public List<EntitlementDto> entitlements;

    @SerializedName("features")
    public List<String> features;

    public List<String> getEntitlementIds() {
        List<EntitlementDto> list = entitlements;
        if (list == null) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (EntitlementDto e : list) {
            String id = e.id;
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    public boolean hasPvr() {
        List<String> f = features;
        return f != null && f.contains("PVR");
    }

    public boolean hasLocalDvr() {
        List<String> f = features;
        return f != null && f.contains("LOCALDVR");
    }

    @Override
    public String toString() {
        return "EntitlementsDto [entitlements=" + entitlements + ", features=" + features + "]";
    }

    public static class EntitlementDto {
        @SerializedName("id")
        public String id;

        @Override
        public String toString() {
            return "EntitlementDto [id=" + id + "]";
        }
    }
}
