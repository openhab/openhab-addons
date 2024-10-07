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
 * @author Jeremy Setton - Add link and file push type support
 */
@NonNullByDefault
public class PushbulletBindingConstants {

    public static final String BINDING_ID = "pushbullet";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BOT = new ThingTypeUID(BINDING_ID, "bot");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BOT);

    // Thing properties
    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_NAME = "name";

    // Binding logic constants
    public static final String API_BASE_URL = "https://api.pushbullet.com/v2";

    public static final String API_ENDPOINT_PUSHES = "/pushes";
    public static final String API_ENDPOINT_UPLOAD_REQUEST = "/upload-request";
    public static final String API_ENDPOINT_USERS_ME = "/users/me";

    public static final String HEADER_RATELIMIT_RESET = "X-Ratelimit-Reset";

    public static final String IMAGE_FILE_NAME = "image.jpg";

    public static final int MAX_UPLOAD_SIZE = 26214400;
}
