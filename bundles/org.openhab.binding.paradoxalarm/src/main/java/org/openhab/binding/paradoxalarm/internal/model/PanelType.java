/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.model;

/**
 * The {@link PanelType} Enum of all panel types
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public enum PanelType {
    EVO48,
    EVO192,
    EVOHD,
    SP5500,
    SP6000,
    SP7000,
    MG5000,
    MG5050,
    SP4000,
    SP65,
    UNKNOWN;

    @Override
    public String toString() {
        return this.name();
    }

    public static PanelType from(String panelTypeStr) {
        if (panelTypeStr == null) {
            return PanelType.UNKNOWN;
        }

        try {
            return PanelType.valueOf(panelTypeStr);
        } catch (IllegalArgumentException e) {
            return PanelType.UNKNOWN;
        }
    }
}
