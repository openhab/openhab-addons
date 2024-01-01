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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class IotProduct {
    @SerializedName("classid")
    private final String classId;

    @SerializedName("product")
    private final ProductDefinition productDef;

    public IotProduct(String classId, ProductDefinition productDef) {
        this.classId = classId;
        this.productDef = productDef;
    }

    public String getClassId() {
        return classId;
    }

    public ProductDefinition getDefinition() {
        return productDef;
    }

    public static class ProductDefinition {
        @SerializedName("_id")
        public final String id;

        @SerializedName("materialNo")
        public final String materialNumber;

        @SerializedName("name")
        public final String name;

        @SerializedName("icon")
        public final String icon;

        @SerializedName("iconUrl")
        public final String iconUrl;

        @SerializedName("model")
        public final String model;

        @SerializedName("UILogicId")
        public final String uiLogicId;

        @SerializedName("ota")
        public final boolean otaCapable;

        @SerializedName("supportType")
        public final SupportFlags supportFlags;

        public ProductDefinition(String id, String materialNumber, String name, String icon, String iconUrl,
                String model, String uiLogicId, boolean otaCapable, SupportFlags supportFlags) {
            this.id = id;
            this.materialNumber = materialNumber;
            this.name = name;
            this.icon = icon;
            this.iconUrl = iconUrl;
            this.model = model;
            this.uiLogicId = uiLogicId;
            this.otaCapable = otaCapable;
            this.supportFlags = supportFlags;
        }
    }

    public static class SupportFlags {
        @SerializedName("share")
        public final boolean canShare;

        @SerializedName("tmjl")
        public final boolean tmjl; // ???

        @SerializedName("assistant")
        public final boolean canUseAssistant;

        @SerializedName("alexa")
        public final boolean canUseAlexa;

        public SupportFlags(boolean share, boolean tmjl, boolean assistant, boolean alexa) {
            this.canShare = share;
            this.tmjl = tmjl;
            this.canUseAssistant = assistant;
            this.canUseAlexa = alexa;
        }
    }
}
