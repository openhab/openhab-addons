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
package org.openhab.binding.russound.internal.rio.models;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple model of a RIO Preset and it's attributes. Please note this class is used to
 * serialize/deserialize to JSON.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioPreset {
    /**
     * The preset id
     */
    private final int id;

    /**
     * Whether the preset is valid or not
     */
    private final AtomicBoolean valid = new AtomicBoolean(false);

    /**
     * The preset name
     */
    private final AtomicReference<String> name = new AtomicReference<>(null);

    /**
     * Simply creates the preset from the given ID. The preset will not be valid and the name will default to
     * "Preset " + id
     *
     * @param id a preset ID between 1 and 36
     * @throws IllegalArgumentException if id {@literal < 1} or > 36
     */
    public RioPreset(int id) {
        this(id, false, "Preset " + id);
    }

    /**
     * Creates the preset from the given ID, validity and name. If the name is empty or null, it will default to
     * "Preset " + id
     *
     * @param id a preset ID between 1 and 36
     * @param valid true if the preset is valid, false otherwise
     * @param name a possibly null, possibly empty preset name
     * @throws IllegalArgumentException if id {@literal < 1} or > 32
     */
    public RioPreset(int id, boolean valid, @Nullable String name) {
        if (id < 1 || id > 36) {
            throw new IllegalArgumentException("Preset ID can only be between 1 and 36");
        }

        this.id = id;
        this.valid.set(valid);
        this.name.set(name == null || name.isEmpty() ? "Preset " + id : name);
    }

    /**
     * Returns the bank identifier this preset is for
     *
     * @return bank identifier between 1 and 6
     */
    public int getBank() {
        return ((getId() - 1) / 6) + 1;
    }

    /**
     * Returns the bank preset identifier this preset is for
     *
     * @return bank preset identifier between 1 and 6
     */
    public int getBankPreset() {
        return ((getId() - 1) % 6) + 1;
    }

    /**
     * Returns the preset identifier
     *
     * @return the preset identifier between 1 and 36
     */
    public int getId() {
        return id;
    }

    /**
     * Returns true if the preset is valid, false otherwise
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
    }

    /**
     * Sets whether the preset is valid (true) or not (false)
     *
     * @param presetValid true if valid, false otherwise
     */
    public void setValid(boolean presetValid) {
        valid.set(presetValid);
    }

    /**
     * Set's the preset name. If null or empty, will default to "Preset " + getId()
     *
     * @param presetName a possibly null, possibly empty preset name
     */
    public void setName(@Nullable String presetName) {
        name.set(presetName == null || presetName.isEmpty() ? "Preset " + getId() : presetName);
    }

    /**
     * Returns the preset name
     *
     * @return a non-null, non-empty preset name
     */
    public String getName() {
        return name.get();
    }
}
