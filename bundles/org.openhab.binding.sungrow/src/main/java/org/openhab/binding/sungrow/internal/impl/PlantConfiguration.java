/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.impl;

import org.eclipse.jdt.annotation.Nullable;

import de.afrouper.server.sungrow.api.dto.Plant;

/**
 * @author Christian Kemper - Initial contribution
 */
public class PlantConfiguration {

    @Nullable
    private Plant plant;

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public boolean isValid() {
        return plant != null;
    }
}
