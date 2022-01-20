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
package org.openhab.binding.icloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icloud.internal.json.response.ICloudAccountDataResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Extracts iCloud device information from a given JSON string
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
@NonNullByDefault
public class ICloudDeviceInformationParser {
    private final Gson gson = new GsonBuilder().create();

    public @Nullable ICloudAccountDataResponse parse(String json) throws JsonSyntaxException {
        return gson.fromJson(json, ICloudAccountDataResponse.class);
    }
}
