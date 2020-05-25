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
package org.openhab.binding.comfoair.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ComfoAirBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans Böhm - Initial contribution
 */
@NonNullByDefault
public class ComfoAirBindingConstants {

    private static final String BINDING_ID = "comfoair";

    public static final ThingTypeUID THING_TYPE_COMFOAIR_GENERIC = new ThingTypeUID(BINDING_ID, "comfoair");

    public static final String PROPERTY_SOFTWARE_MAIN_VERSION = "softwareMainVersion";
    public static final String PROPERTY_SOFTWARE_MINOR_VERSION = "softwareMinorVersion";
    public static final String PROPERTY_SOFTWARE_BETA_VERSION = "softwareBetaVersion";
}
