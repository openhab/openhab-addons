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
package org.openhab.binding.bticinosmarther.internal.api.dto;

/**
 * The {@code PlantRef} class defines the dto for Smarther API plant reference object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class PlantRef {

    private String id;
    private ModuleRef module;

    /**
     * Returns the identifier of the plant.
     *
     * @return a string containing the plant identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the chronothermostat reference inside the plant.
     *
     * @return a {@link ModuleRef} object representing the chronothermostat module reference
     */
    public ModuleRef getModule() {
        return module;
    }

    @Override
    public String toString() {
        return String.format("id=%s, module=[%s]", id, module);
    }
}
