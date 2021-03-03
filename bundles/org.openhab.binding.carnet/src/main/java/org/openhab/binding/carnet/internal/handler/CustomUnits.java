/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.handler;

import java.math.BigInteger;

import javax.measure.Unit;
import javax.measure.quantity.Volume;
import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

import tec.uom.se.AbstractSystemOfUnits;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.function.RationalConverter;
import tec.uom.se.unit.TransformedUnit;

/**
 * The {@link CustomUnits} implements missing unit definitions.
 *
 * @author Markus Michels - Initial contribution
 *
 */
@NonNullByDefault
public final class CustomUnits extends AbstractSystemOfUnits {

    private static final CustomUnits INSTANCE = new CustomUnits();

    private CustomUnits() {
        // avoid external instantiation
    }

    /** Additionally defined units to be used in openHAB **/
    public static final Unit<Volume> GALLON = addUnit(new TransformedUnit<>("gal", Units.LITRE,
            new RationalConverter(BigInteger.valueOf(3785), BigInteger.valueOf(1000))));
    /**
     * Add unit symbols for imperial units.
     */
    static {
        SimpleUnitFormat.getInstance().label(GALLON, GALLON.getSymbol());
    }

    /**
     * Returns the unique instance of this class.
     *
     * @return the Units instance.
     */
    public static SystemOfUnits getInstance() {
        return INSTANCE;
    }

    /**
     * Adds a new unit not mapped to any specified quantity type.
     *
     * @param unit the unit being added.
     * @return <code>unit</code>.
     */
    private static <U extends Unit<?>> U addUnit(U unit) {
        INSTANCE.units.add(unit);
        return unit;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
