/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kvv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KVVBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Maximilian Hess - Initial contribution
 */
@NonNullByDefault
public class KVVBindingConstants {

    /** the id of the binding */
    private static final String BINDING_ID = "kvv";

    /** List of all Thing Type UIDs */
    public static final ThingTypeUID THING_TYPE_KVVSTATION = new ThingTypeUID(BINDING_ID, "kvvstation");

    /** List of all Channel ids */
    public static final String CHANNEL_1 = "channel1";

    /** URL of the KVV API */
    public static final String API_URL = "https://live.kvv.de/webapp";

    /** API key of the KVV API */
    public static final String API_KEY = "377d840e54b59adbe53608ba1aad70e8";
}
