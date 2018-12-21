/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link VeluxKLF200BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author mkf - Initial contribution
 */
@NonNullByDefault
public class VeluxKLF200BindingConstants {

    /** Constant BINDING_ID. */
    private static final String BINDING_ID = "veluxklf200";

    // List of all Thing Type UIDs
    /** BRIDGE. */
    public static final ThingTypeUID THING_TYPE_VELUX_KLF200 = new ThingTypeUID(BINDING_ID, "klf200-bridge");

    /** SCENES */
    public static final ThingTypeUID THING_TYPE_VELUX_SCENE = new ThingTypeUID(BINDING_ID, "velux_scene");

    /** VERTICAL INTERIOR BLINDS. */
    public static final ThingTypeUID THING_TYPE_VELUX_BLIND = new ThingTypeUID(BINDING_ID, "velux_blind");

    // List of all Channel ids
    /** Trigger a scene */
    public static final String KLF200_TRIGGER_SCENE = "trigger_scene";

    /** Position of a blind. */
    public static final String VELUX_BLIND_POSITION = "blind_position";

    /** Indicates whether the bridge is connected to the KLF200 */
    public static final String BRIDGE_CONNECTIVITY = "connection_status";

    /** All the supported thing types */
    public static final Set<ThingTypeUID> SUPPORTED_VELUX_KLF200_THING_TYPES_UIDS = ImmutableSet
            .of(THING_TYPE_VELUX_KLF200, THING_TYPE_VELUX_SCENE, THING_TYPE_VELUX_BLIND);
}
