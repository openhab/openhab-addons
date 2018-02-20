/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.models;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;

/**
 * Simple model of a RIO Favorite (both system and zone) and it's attributes. Please note this class is used to
 * serialize/deserialize to JSON.
 *
 * @author Tim Roberts
 *
 */
public class RioFavorite {
    /**
     * The favorite ID
     */
    private final int id;

    /**
     * Whether the favorite is valid or not
     */
    private final AtomicBoolean valid = new AtomicBoolean(false);

    /**
     * The favorite name
     */
    private final AtomicReference<String> name = new AtomicReference<String>(null);

    /**
     * Simply creates the favorite from the given ID. The favorite will not be valid and the name will default to
     * "Favorite " + id
     *
     * @param id a favorite ID between 1 and 32
     * @throws IllegalArgumentException if id < 1 or > 32
     */
    public RioFavorite(int id) {
        this(id, false, null);
    }

    /**
     * Creates the favorite from the given ID, validity and name. If the name is empty or null, it will default to
     * "Favorite " + id
     *
     * @param id a favorite ID between 1 and 32
     * @param isValid true if the favorite is valid, false otherwise
     * @param name a possibly null, possibly empty favorite name
     * @throws IllegalArgumentException if id < 1 or > 32
     */
    public RioFavorite(int id, boolean isValid, String name) {
        if (id < 1 || id > 32) {
            throw new IllegalArgumentException("Favorite ID must be between 1 and 32");
        }

        if (StringUtils.isEmpty(name)) {
            name = "Favorite " + id;
        }

        this.id = id;
        this.valid.set(isValid);
        this.name.set(name);
    }

    /**
     * Returns the favorite identifier
     *
     * @return a favorite id between 1 and 32
     */
    public int getId() {
        return id;
    }

    /**
     * Returns true if the favorite is valid, false otherwise
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
    }

    /**
     * Sets whether the favorite is valid or not
     *
     * @param favValid true if valid, false otherwise
     */
    public void setValid(boolean favValid) {
        valid.set(favValid);
    }

    /**
     * Set's the favorite name. If null or empty, will default to "Favorite " + getId()
     *
     * @param favName a possibly null, possibly empty favorite name
     */
    public void setName(String favName) {
        name.set(StringUtils.isEmpty(favName) ? "Favorite " + getId() : favName);
    }

    /**
     * Returns the favorite name
     *
     * @return a non-null, non-empty favorite name
     */
    public String getName() {
        return name.get();
    }
}
