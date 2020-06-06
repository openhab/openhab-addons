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
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

/**
 * Converts the native LCN value of I-Port periphery like LCN-GBL, LCN-GUS, LCN-WIH to the lux value.
 * Profile doesn't support the LCN-LS on the T-Port.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class LightConverter extends AbstractVariableValueConverter {
    @Override
    protected Unit<?> getUnitType() {
        return SmartHomeUnits.LUX;
    }

    @Override
    public int toNative(double value) {
        return (int) Math.round(Math.log(value) * 100);
    }

    @Override
    public double toHumanReadable(long value) {
        // Max. value hardware can deliver is 100klx. Apply hard limit, because higher native values lead to very big
        // lux values.
        if (value > toNative(100e3)) {
            return Double.NaN;
        }
        return Math.exp(value / 100d);
    }
}
