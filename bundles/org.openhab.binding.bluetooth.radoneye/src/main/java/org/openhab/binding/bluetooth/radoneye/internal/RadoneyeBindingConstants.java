/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.radoneye.internal;

import java.math.BigInteger;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.ProductUnit;
import tech.units.indriya.unit.TransformedUnit;

/**
 * The {@link RadoneyeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public class RadoneyeBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RADONEYE = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "radoneye_rd200");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_RADONEYE);

    // Channel IDs
    public static final String CHANNEL_ID_RADON = "radon";

    public static final Unit<Dimensionless> PARTS_PER_BILLION = new TransformedUnit<>(Units.ONE,
            MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000000000)));
    public static final Unit<Density> BECQUEREL_PER_CUBIC_METRE = new ProductUnit<>(
            Units.BECQUEREL.divide(SIUnits.CUBIC_METRE));

    static {
        SimpleUnitFormat.getInstance().label(PARTS_PER_BILLION, "ppb");
        SimpleUnitFormat.getInstance().label(BECQUEREL_PER_CUBIC_METRE, "Bq/m³");
    }
}
