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

/**
 * The {@code Plants} class defines the dto for Smarther API list of plants.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Plants {

    private List<Plant> plants;

    /**
     * Returns the list of plants contained in this object.
     *
     * @return the list of plants
     */
    public List<Plant> getPlants() {
        return plants;
    }
}
