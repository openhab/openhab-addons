/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
}
