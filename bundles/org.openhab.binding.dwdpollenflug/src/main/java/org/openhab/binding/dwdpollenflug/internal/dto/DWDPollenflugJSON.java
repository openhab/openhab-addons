/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dwdpollenflug.internal.dto;

import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class to hold Response of Http Request
 * 
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugJSON {
    public @Nullable String sender;

    public @Nullable String name;

    public @Nullable String next_update;

    public @Nullable String last_update;

    public @Nullable Map<String, String> legend;

    @SerializedName("content")
    public @Nullable Set<DWDRegionJSON> regions;
}
