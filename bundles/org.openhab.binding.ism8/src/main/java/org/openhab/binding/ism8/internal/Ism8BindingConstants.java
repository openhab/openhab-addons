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
package org.openhab.binding.ism8.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Ism8BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class Ism8BindingConstants {
    // Binding ID
    private static final String BINDING_ID = "ism8";

    // List of all Thing Type UIDs

    /**
     * Defines the thing type UID
     *
     */
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // Thing Configuration parameters

    /**
     * The port number configuration parameter
     *
     */
    public static final String PORT_NUMBER = "portNumber";
}
