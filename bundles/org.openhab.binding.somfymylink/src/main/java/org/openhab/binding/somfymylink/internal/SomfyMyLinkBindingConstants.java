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
package org.openhab.binding.somfymylink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Chris Johnson - Initial contribution
 */
@NonNullByDefault
public class SomfyMyLinkBindingConstants {

    private static final String BINDING_ID = "somfymylink";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SHADE = new ThingTypeUID(BINDING_ID, "shade");
    public static final ThingTypeUID THING_TYPE_SCENE = new ThingTypeUID(BINDING_ID, "scene");
    public static final ThingTypeUID THING_TYPE_MYLINK = new ThingTypeUID(BINDING_ID, "mylink");

    // List of all Channel ids
    public static final String CHANNEL_SHADELEVEL = "shadelevel";
    public static final String CHANNEL_SCENECONTROL = "scenecontrol";
    public static final String CHANNEL_SCENES = "sceneid";

    // Thing config properties
    public static final String TARGET_ID = "targetId";
    public static final String SCENE_ID = "sceneId";
}
