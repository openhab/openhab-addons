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
package org.openhab.binding.danfossairunit.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DanfossAirUnitBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ralf Duckstein - Initial contribution
 * @author Robert Bach - heavy refactorings
 */
@NonNullByDefault
public class DanfossAirUnitBindingConstants {

    public static String BINDING_ID = "danfossairunit";

    // The only thing type UIDs
    public static ThingTypeUID THING_TYPE_AIRUNIT = new ThingTypeUID(BINDING_ID, "airunit");

    // The thing type as a set
    public static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_AIRUNIT);
}
