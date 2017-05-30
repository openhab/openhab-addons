/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

/**
 * The model representing Neeo Macros (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoMacros {

    /** The macros. */
    private final NeeoMacro[] macros;

    /**
     * Instantiates a new neeo macros.
     *
     * @param macros the macros
     */
    public NeeoMacros(NeeoMacro[] macros) {
        this.macros = macros;
    }

    /**
     * Gets the macros.
     *
     * @return the macros
     */
    public NeeoMacro[] getMacros() {
        return macros;
    }

    /**
     * Gets the macro.
     *
     * @param key the key
     * @return the macro
     */
    public NeeoMacro getMacro(String key) {
        for (NeeoMacro macro : macros) {
            if (StringUtils.equalsIgnoreCase(key, macro.getKey())) {
                return macro;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "NeeoMacro [macros=" + Arrays.toString(macros) + "]";
    }
}
