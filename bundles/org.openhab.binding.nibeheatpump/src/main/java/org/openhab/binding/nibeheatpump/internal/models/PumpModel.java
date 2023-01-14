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
package org.openhab.binding.nibeheatpump.internal.models;

/**
 * Class for different Nibe heat pump models.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public enum PumpModel {
    F1X45("F1X45"),
    F1X55("F1X55"),
    SMO40("SMO40"),
    F750("F750"),
    F470("F470");

    private final String pumpModel;

    PumpModel(String pumpModel) {
        this.pumpModel = pumpModel;
    }

    @Override
    public String toString() {
        return pumpModel;
    }

    public static PumpModel getPumpModel(String pumpModel) throws IllegalArgumentException {
        try {
            return PumpModel.valueOf(pumpModel.toUpperCase());
        } catch (Exception e) {
            String description = String.format("Illegal pump model '%s'", pumpModel);
            throw new IllegalArgumentException(description);
        }
    }
}
