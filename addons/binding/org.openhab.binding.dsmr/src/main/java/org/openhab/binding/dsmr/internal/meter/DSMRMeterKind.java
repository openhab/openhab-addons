/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.meter;

/**
 * This class describes the kind of meters the binding supports
 *
 * @author M. Volaart - Initial contribution
 */
public enum DSMRMeterKind {
    INVALID,
    DEVICE,
    MAIN_ELECTRICITY,
    GAS,
    HEATING,
    COOLING,
    WATER,
    GENERIC,
    GJ,
    M3,
    SLAVE_ELECTRICITY1,
    SLAVE_ELECTRICITY2;

    /**
     * @return Returns the i18n label key for this meter.
     */
    public String getLabelKey() {
        return "@text/meterKind." + name().toLowerCase() + ".label";
    }
}
