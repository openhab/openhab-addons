/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PlayerStateMainArtTO} encapsulates the art section of a player info
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PlayerStateMainArtTO {
    public String altText;
    public String artType;
    public String contentType;
    @SerializedName(value = "url", alternate = { "fullUrl" })
    public String url;

    @Override
    public @NonNull String toString() {
        return "PlayerStateMainArtTO{altText='" + altText + "', artType='" + artType + "', contentType='" + contentType
                + "', url='" + url + "'}";
    }
}
