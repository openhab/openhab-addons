/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.benqprojector.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BenqProjectorBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author René Treffer - Initial contribution
 */
@NonNullByDefault
public class BenqProjectorBindingConstants {

    private static final String BINDING_ID = "benqprojector";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BENQPROJECTOR = new ThingTypeUID(BINDING_ID, "projector");

    // Mapping of channels to projector command names
    public static final Map<String, String> CHANNEL_PROJECTOR_MAPPING = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;
                {
                    put("aspect", "asp");
                    put("blank", "blank");
                    put("brightness", "bri");
                    put("bqversion", "fwver");
                    put("colortemp", "ct");
                    put("contrast", "con");
                    put("gamma", "gamma");
                    put("hue", "hue");
                    put("model", "modelname");
                    put("mute", "mute");
                    put("picturemode", "appmod");
                    put("power", "pow");
                    put("saturation", "color");
                    put("serial", "sn");
                    put("sharpness", "sharp");
                    put("source", "sour");
                    put("swversion", "swver");
                    put("volume", "vol");
                }
            });
}
