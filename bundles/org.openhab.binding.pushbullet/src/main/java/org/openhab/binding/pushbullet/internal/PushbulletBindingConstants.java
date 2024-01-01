/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pushbullet.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PushbulletBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hakan Tandogan - Initial contribution
 */
@NonNullByDefault
public class PushbulletBindingConstants {

    private static final String BINDING_ID = "pushbullet";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BOT = new ThingTypeUID(BINDING_ID, "bot");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BOT);

    // List of all Channel ids
    public static final String RECIPIENT = "recipient";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";

    // Binding logic constants
    public static final String API_METHOD_PUSHES = "pushes";

    public static final int TIMEOUT = 30 * 1000; // 30 seconds
}
