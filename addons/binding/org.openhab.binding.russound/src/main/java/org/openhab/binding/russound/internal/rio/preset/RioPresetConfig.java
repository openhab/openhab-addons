/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.preset;

/**
 * Configuration class for the {@link RioPresetHandler}
 *
 * @author Tim Roberts
 */
public class RioPresetConfig {
    /**
     * ID of the preset (1-6 for a bank, 1-36 for a zone)
     */
    private int preset;

    /**
     * Gets the preset identifier
     *
     * @return the preset identifier
     */
    public int getPreset() {
        return preset;
    }

    /**
     * Sets the preset identifier
     *
     * @param preset the preset identifier
     */
    public void setPreset(int preset) {
        this.preset = preset;
    }
}
