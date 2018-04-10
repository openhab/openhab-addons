/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal.models;

/**
 * Class for different Nibe heat pump models.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public enum PumpModel {
    F1X45("F1X45");

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
