/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@code Plant} class defines the dto for Smarther API plant object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Plant {

    private String id;
    private String name;
    private List<Module> modules;

    /**
     * Returns the identifier of the plant.
     *
     * @return a string containing the plant identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the plant reference label (i.e. the plant "name").
     *
     * @return a string containing the plant reference label
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of chronothermostat modules of the plant.
     *
     * @return the list of chronothermostat modules of the plant, or {@code null} in case the plant has no modules
     */
    public @Nullable List<Module> getModules() {
        return modules;
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s", id, name);
    }
}
