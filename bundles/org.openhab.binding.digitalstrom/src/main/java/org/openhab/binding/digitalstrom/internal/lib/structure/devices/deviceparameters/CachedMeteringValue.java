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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters;

import java.util.Date;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;

/**
 * The {@link CachedMeteringValue} saves the metering value of an digitalSTROM-Circuit.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - add methods getDateAsDate(), getMeteringType() and getMeteringUnit(); add missing java-doc
 * @author Matthias Siegele - add methods getDateAsDate(), getMeteringType() and getMeteringUnit(); add missing java-doc
 */
public interface CachedMeteringValue {

    /**
     * Returns the {@link DSID} of the digitalSTROM-Circuit.
     *
     * @return dSID of circuit
     */
    DSID getDsid();

    /**
     * Returns the saved sensor value.
     *
     * @return sensor value
     */
    double getValue();

    /**
     * Returns the timestamp when the sensor value was read out as {@link String}.
     *
     * @return read out timestamp
     */
    String getDate();

    /**
     * Returns the timestamp when the sensor value was read out as {@link Date}.
     *
     * @return read out timestamp
     */
    Date getDateAsDate();

    /**
     * Returns the {@link MeteringTypeEnum} of this {@link CachedMeteringValue}.
     *
     * @return metering type as {@link MeteringTypeEnum}
     */
    MeteringTypeEnum getMeteringType();

    /**
     * Returns the {@link MeteringUnitsEnum} of this {@link CachedMeteringValue}.
     *
     * @return metering unit as {@link MeteringUnitsEnum}
     */
    MeteringUnitsEnum getMeteringUnit();
}
