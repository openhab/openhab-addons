/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.openhab.io.neeo.internal.NeeoUtil;

import com.google.gson.annotations.SerializedName;

/**
 * The model representing an NEEO recipe (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoRecipe {

    /** The recipe identifier */
    private final String uid;

    /** The recipe URLs */
    @SerializedName(value = "url", alternate = { "urls" })
    private final NeeoRecipeUrls url;

    // may be used in the future
    // private final String type;
    // private final boolean isCustom;
    // private final boolean isPowered;
    // private final String powerKey;
    // private final NeeoRecipeDetail detail;

    /**
     * Creates a new recipe from the identifier and urls
     *
     * @param uid the non-empty uid
     * @param url the non-null url
     */
    public NeeoRecipe(String uid, NeeoRecipeUrls url) {
        NeeoUtil.requireNotEmpty(uid, "uid cannot be empty");
        Objects.requireNonNull(url, "urls cannot be empty");
        this.uid = uid;
        this.url = url;
    }

    /**
     * Gets the recipe identifier.
     *
     * @return the recipe identifier
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the urls
     *
     * @return the possibly null urls
     */
    public NeeoRecipeUrls getUrls() {
        return url;
    }

    @Override
    public String toString() {
        return "NeeoRecipe [urls=" + url + ", uid=" + uid + "]";
    }
}
