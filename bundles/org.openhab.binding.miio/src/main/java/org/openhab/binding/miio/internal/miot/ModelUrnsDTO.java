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
package org.openhab.binding.miio.internal.miot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Model Urns DTO for miot spec file.
 *
 * To read http://miot-spec.org/miot-spec-v2/instances?status=released or status=all
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class ModelUrnsDTO {
    @SerializedName("model")
    @Expose
    private String model = "";
    @SerializedName("version")
    @Expose
    private @Nullable Integer version;
    @SerializedName("type")
    @Expose
    private @Nullable String type;
    @SerializedName("status")
    @Expose
    private @Nullable String status;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public @Nullable Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public @Nullable String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the status of the spec (released, preview or debug), null if the list does not contain the status
     */
    public @Nullable String getStatus() {
        return status;
    }
}
