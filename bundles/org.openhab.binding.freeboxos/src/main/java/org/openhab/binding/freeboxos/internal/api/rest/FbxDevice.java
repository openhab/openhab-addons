/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FbxDevice} is the Java class used to describe most of
 * Free equipments via API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FbxDevice extends ApiVersion {

    private int id;

    @SerializedName(value = "mac", alternate = { "main_mac" })
    private @NonNullByDefault({}) String mac;

    @SerializedName(value = "device_name", alternate = { "name" })
    private @Nullable String name;

    @SerializedName(value = "device_model", alternate = { "model" })
    private String model = "";

    public int getId() {
        return id;
    }

    public String getMac() {
        return mac.toLowerCase();
    }

    public @Nullable String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    /**
     * @return a string like eg : '17/api/v8'
     */
    @Override
    public String baseUrl() {
        return String.format("%d%s/", id, super.baseUrl());
    }
}
