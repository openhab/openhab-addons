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
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The model representing a NEEO recipe (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoRecipe {

    /** The recipe identifier */
    @Nullable
    private String uid;

    /** The recipe URLs */
    @Nullable
    @SerializedName(value = "url", alternate = { "urls" })
    private NeeoRecipeUrls url;

    // may be used in the future
    // private final String type;
    // private final boolean isCustom;
    // private final boolean isPowered;
    // private final String powerKey;
    // private final NeeoRecipeDetail detail;

    /**
     * Gets the recipe identifier.
     *
     * @return the recipe identifier
     */
    @Nullable
    public String getUid() {
        return uid;
    }

    /**
     * Gets the urls
     *
     * @return the possibly null urls
     */
    @Nullable
    public NeeoRecipeUrls getUrls() {
        return url;
    }

    @Override
    public String toString() {
        return "NeeoRecipe [urls=" + url + ", uid=" + uid + "]";
    }
}
