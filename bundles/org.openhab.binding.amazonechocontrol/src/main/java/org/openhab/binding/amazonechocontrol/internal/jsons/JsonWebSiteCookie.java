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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JsonWebSiteCookie} encapsulate the GSON data of register cookie array
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonWebSiteCookie {
    public JsonWebSiteCookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @SerializedName("Value")
    public @Nullable String value;
    @SerializedName("Name")
    public @Nullable String name;
}
