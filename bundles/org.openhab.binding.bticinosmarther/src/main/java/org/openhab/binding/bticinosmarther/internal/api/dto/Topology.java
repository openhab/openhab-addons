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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@code Topology} class defines the dto for Smarther API topology object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Topology {

    private Plant plant;

    /**
     * Returns a {@link Plant} object representing the plant contained in this topology.
     *
     * @return the plant contained in this topology, or {@code null} if the topology has no plant
     */
    public @Nullable Plant getPlant() {
        return plant;
    }

    /**
     * Returns the list of chronothermostat modules contained in this topology.
     *
     * @return the list of chronothermostat modules contained in this topology, or an empty list in case the topology
     *         has no modules
     */
    public List<Module> getModules() {
        return (plant != null) ? plant.getModules() : new ArrayList<>();
    }
}
