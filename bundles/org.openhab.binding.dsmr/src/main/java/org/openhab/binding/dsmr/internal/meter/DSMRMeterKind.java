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
