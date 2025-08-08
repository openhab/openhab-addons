/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

    public static final String BINDING_ID = "danfossairunit";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AIRUNIT = new ThingTypeUID(BINDING_ID, "airunit");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIRUNIT);

    // List of all Thing Type configuration parameters
    public static final String PARAMETER_HOST = "host";

    // List of all Thing Type properties
    public static final String PROPERTY_CCM_SERIAL_NUMBER = "ccmSerialNumber";
}
