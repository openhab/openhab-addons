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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ApiVersion} is the Java class used to map the api_version answer
 * http://mafreebox.freebox.fr/api_version
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiVersion {
    private String apiBaseUrl = "/api/";
    @SerializedName(value = "api_version", alternate = { "api_ver" })
    private @NonNullByDefault({}) String apiVersion;

    /**
     * @return a string like eg : '/api/v8'
     */
    public String baseUrl() {
        return String.format("%sv%s", apiBaseUrl, apiVersion.split("\\.")[0]);
    }
}
