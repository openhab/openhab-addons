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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.lcn.internal.common.LcnException;

/**
 * Converts the value 1:1.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class IdentityConverter extends AbstractVariableValueConverter {
    private IdentityConverter() {
        // nothing
    }

    @NonNullByDefault({})
    private static class LazyHolder {
        static final IdentityConverter INSTANCE = new IdentityConverter();
    }

    public static IdentityConverter getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    protected Unit<?> getUnitType() {
        return SmartHomeUnits.VOLT; // not used
    }

    @Override
    public DecimalType onCommandFromItem(QuantityType<?> quantityType) throws LcnException {
        return onCommandFromItem(quantityType.doubleValue());
    }

    @Override
    public int toNative(double value) {
        return (int) Math.round(value);
    }

    @Override
    public double toHumanReadable(long value) {
        return value;
    }
}
