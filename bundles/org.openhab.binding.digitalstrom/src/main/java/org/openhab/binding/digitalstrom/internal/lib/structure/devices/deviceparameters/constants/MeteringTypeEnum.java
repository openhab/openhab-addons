/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import java.util.Arrays;
import java.util.List;

/**
 * The {@link MeteringTypeEnum} lists all available digitalSTROM metering types.
 *
 * @author Alexander Betker - initial contributer
 * @author Michael Ochel - add MeteringUnitEnum list
 * @author Matthias Siegele - add MeteringUnitEnum list
 */
public enum MeteringTypeEnum {
    ENERGY(Arrays.asList(MeteringUnitsEnum.WH, MeteringUnitsEnum.WS)),
    // currently null by request getLast
    // energyDelta(Lists.newArrayList(MeteringUnitsEnum.Wh, MeteringUnitsEnum.Ws)),
    CONSUMPTION(Arrays.asList(MeteringUnitsEnum.WH));

    private final List<MeteringUnitsEnum> meteringUnits;

    private MeteringTypeEnum(List<MeteringUnitsEnum> meteringUnits) {
        this.meteringUnits = meteringUnits;
    }

    /**
     * Returns the available units as {@link List} for this {@link MeteringTypeEnum}.
     *
     * @return units
     */
    public List<MeteringUnitsEnum> getMeteringUnitList() {
        return meteringUnits;
    }
}
