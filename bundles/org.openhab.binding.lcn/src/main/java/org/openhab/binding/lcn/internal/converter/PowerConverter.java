/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.converter;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

/**
 * Converts the native LCN value of an S0 input power variable of LCN-BU4L to Watts.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class PowerConverter extends AbstractS0Converter {
    public PowerConverter(@Nullable Object parameter) {
        super(parameter);
    }

    @Override
    protected Unit<?> getUnitType() {
        return SmartHomeUnits.WATT;
    }
}
