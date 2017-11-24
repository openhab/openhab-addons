/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.internal.effects;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.NonNull;

import org.eclipse.smarthome.config.core.ConfigConstants;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;

import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;

/**
 * Create new instances of {@link LightifyEffect} classes based on
 * the requested name.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyEffectFactory {

    private static final String effectsFolderName = "org.openhab.binding.osramlightify.effects";

    @SuppressWarnings("serial")
    private static final Map<String, byte[]> PRESET_COLOR = Collections
            .unmodifiableMap(new HashMap<String, byte[]>() {{
                put("candy", new byte[] {
                    (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0xe3, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xaa, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x55, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xa3, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xfb, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xb1, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x71, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xbf, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xea, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xc6, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x98, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x55, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xea, (byte) 0xff, (byte) 0xff, (byte) 0x92
                });
                put("evening", new byte[] {
                    (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x0c, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x0d, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x10, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x07, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x0a, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x09, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x0b, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x0c, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x0e, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x10, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x0c, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x0d, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x09, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x0e, (byte) 0xdc, (byte) 0xff, (byte) 0xb3,
                    (byte) 0x12, (byte) 0xdc, (byte) 0xff, (byte) 0xa6
                });
                put("loop", new byte[] {
                    (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x15, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x2b, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x40, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x55, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x63, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x71, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x87, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0x95, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xa3, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xb1, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xc6, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xdc, (byte) 0xff, (byte) 0xff, (byte) 0x27,
                    (byte) 0xf0, (byte) 0xff, (byte) 0xff, (byte) 0xd0
                });
                put("ocean", new byte[] {
                    (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x9e, (byte) 0xff, (byte) 0xff, (byte) 0x28,
                    (byte) 0xa5, (byte) 0xff, (byte) 0xff, (byte) 0x18,
                    (byte) 0xa3, (byte) 0xff, (byte) 0xff, (byte) 0x2a,
                    (byte) 0x9d, (byte) 0xff, (byte) 0xff, (byte) 0x1a,
                    (byte) 0x9c, (byte) 0xff, (byte) 0xff, (byte) 0x1b,
                    (byte) 0x9b, (byte) 0xff, (byte) 0xff, (byte) 0x13,
                    (byte) 0xad, (byte) 0xff, (byte) 0xff, (byte) 0x18,
                    (byte) 0xac, (byte) 0xff, (byte) 0xff, (byte) 0x21,
                    (byte) 0xaa, (byte) 0xff, (byte) 0xff, (byte) 0x1e,
                    (byte) 0xaa, (byte) 0xff, (byte) 0xff, (byte) 0x11,
                    (byte) 0xaa, (byte) 0xff, (byte) 0xff, (byte) 0x20,
                    (byte) 0xaa, (byte) 0xff, (byte) 0xff, (byte) 0x1e,
                    (byte) 0xa7, (byte) 0xff, (byte) 0xff, (byte) 0x14,
                    (byte) 0xa7, (byte) 0xff, (byte) 0xff, (byte) 0x14,
                    (byte) 0xa0, (byte) 0xff, (byte) 0xff, (byte) 0xf4
                });
                put("polar", new byte[] {
                    (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x95, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0xa3, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x8e, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x6a, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x7c, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0xb1, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x87, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x98, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x91, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x98, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0xb5, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x9c, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0xb1, (byte) 0xff, (byte) 0xff, (byte) 0x54,
                    (byte) 0x98, (byte) 0xff, (byte) 0xff, (byte) 0xc6
                });
            }});

    @SuppressWarnings("serial")
    private static final Map<String, byte[]> PRESET_WHITE = Collections
            .unmodifiableMap(new HashMap<String, byte[]>() {{
                put("activate", new byte[] {
                    (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x00, (byte) 0xa6, (byte) 0xff, (byte) 0x03,
                    (byte) 0x00, (byte) 0xaa, (byte) 0xff, (byte) 0x03,
                    (byte) 0x00, (byte) 0xb5, (byte) 0xff, (byte) 0x03,
                    (byte) 0x00, (byte) 0xc9, (byte) 0xfe, (byte) 0x03,
                    (byte) 0x00, (byte) 0xe6, (byte) 0xee, (byte) 0x03,
                    (byte) 0x01, (byte) 0x0a, (byte) 0xdf, (byte) 0x03,
                    (byte) 0x01, (byte) 0x30, (byte) 0xd3, (byte) 0x03,
                    (byte) 0x01, (byte) 0x49, (byte) 0xcd, (byte) 0x03,
                    (byte) 0x01, (byte) 0x49, (byte) 0xcd, (byte) 0x03,
                    (byte) 0x01, (byte) 0x30, (byte) 0xd3, (byte) 0x03,
                    (byte) 0x01, (byte) 0x0a, (byte) 0xdf, (byte) 0x03,
                    (byte) 0x00, (byte) 0xe6, (byte) 0xee, (byte) 0x03,
                    (byte) 0x00, (byte) 0xc9, (byte) 0xfe, (byte) 0x03,
                    (byte) 0x00, (byte) 0xb5, (byte) 0xff, (byte) 0x03,
                    (byte) 0x00, (byte) 0xaa, (byte) 0xff, (byte) 0x3c
                });
                put("chilldown", new byte[] {
                    (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x00, (byte) 0xa6, (byte) 0xff, (byte) 0x03,
                    (byte) 0x00, (byte) 0xaa, (byte) 0xff, (byte) 0x03,
                    (byte) 0x00, (byte) 0xb5, (byte) 0xff, (byte) 0x03,
                    (byte) 0x00, (byte) 0xc9, (byte) 0xfe, (byte) 0x03,
                    (byte) 0x00, (byte) 0xe6, (byte) 0xee, (byte) 0x03,
                    (byte) 0x01, (byte) 0x0a, (byte) 0xdf, (byte) 0x03,
                    (byte) 0x01, (byte) 0x30, (byte) 0xd3, (byte) 0x03,
                    (byte) 0x01, (byte) 0x49, (byte) 0xcd, (byte) 0x03,
                    (byte) 0x01, (byte) 0x49, (byte) 0xcd, (byte) 0x03,
                    (byte) 0x01, (byte) 0x30, (byte) 0xd3, (byte) 0x03,
                    (byte) 0x01, (byte) 0x0a, (byte) 0xdf, (byte) 0x03,
                    (byte) 0x00, (byte) 0xe6, (byte) 0xee, (byte) 0x03,
                    (byte) 0x00, (byte) 0xc9, (byte) 0xfe, (byte) 0x03,
                    (byte) 0x00, (byte) 0xb5, (byte) 0xff, (byte) 0x03,
                    (byte) 0x00, (byte) 0xaa, (byte) 0xff, (byte) 0x3c
                });
                put("daylight", new byte[] {
                    (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x00, (byte) 0x99, (byte) 0xff, (byte) 0x00,
                    (byte) 0x00, (byte) 0x99, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x00, (byte) 0x99, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x00, (byte) 0x99, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x00, (byte) 0xa7, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x00, (byte) 0xe8, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x01, (byte) 0x60, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x02, (byte) 0x4c, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x02, (byte) 0x9a, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x02, (byte) 0x9a, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x02, (byte) 0x9a, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x02, (byte) 0x9a, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x02, (byte) 0x9a, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x02, (byte) 0x9a, (byte) 0xff, (byte) 0x3c,
                    (byte) 0x02, (byte) 0x9a, (byte) 0xff, (byte) 0x2d
                });
                put("goodnight", new byte[] {
                    (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x01, (byte) 0x4d, (byte) 0xff, (byte) 0x04,
                    (byte) 0x01, (byte) 0x5e, (byte) 0xa8, (byte) 0x05,
                    (byte) 0x01, (byte) 0x65, (byte) 0x80, (byte) 0x04,
                    (byte) 0x01, (byte) 0x6b, (byte) 0x68, (byte) 0x04,
                    (byte) 0x01, (byte) 0x72, (byte) 0x56, (byte) 0x04,
                    (byte) 0x01, (byte) 0x98, (byte) 0x48, (byte) 0x05,
                    (byte) 0x01, (byte) 0xbc, (byte) 0x3d, (byte) 0x04,
                    (byte) 0x01, (byte) 0xdc, (byte) 0x34, (byte) 0x04,
                    (byte) 0x02, (byte) 0x00, (byte) 0x2c, (byte) 0x05,
                    (byte) 0x02, (byte) 0x2b, (byte) 0x25, (byte) 0x04,
                    (byte) 0x02, (byte) 0x4c, (byte) 0x1e, (byte) 0x04,
                    (byte) 0x02, (byte) 0x71, (byte) 0x18, (byte) 0x04,
                    (byte) 0x02, (byte) 0x85, (byte) 0x12, (byte) 0x05,
                    (byte) 0x02, (byte) 0x85, (byte) 0x0c, (byte) 0x04,
                    (byte) 0x02, (byte) 0x9a, (byte) 0x00, (byte) 0xd7
                });
                put("white and white", new byte[] {
                    (byte) 0x01, (byte) 0xff, (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,
                    (byte) 0x01, (byte) 0x72, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x4d, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x42, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x00, (byte) 0xa6, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x00, (byte) 0xc8, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x72, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x00, (byte) 0xfa, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x4d, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x0e, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x00, (byte) 0xde, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x00, (byte) 0x99, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x4d, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x72, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x1d, (byte) 0xff, (byte) 0x0e,
                    (byte) 0x01, (byte) 0x72, (byte) 0xff, (byte) 0x4f
                });
            }});

    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends LightifyEffect>> EFFECT_MAP = Collections
        .unmodifiableMap(new HashMap<String, Class<? extends LightifyEffect>>() {{
            put("raw", LightifyEffectRaw.class);

            put("active", LightifyEffectActive.class);
            put("plant light", LightifyEffectPlantLight.class);
            put("relax", LightifyEffectRelax.class);

            put("flamecolour", LightifyEffectFlameColor.class);
            put("flamecolor", LightifyEffectFlameColor.class);
            put("fireplace", LightifyEffectFlameColor.class);
        }});

    public static @NonNull LightifyEffect create(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name) throws LightifyException {
        String givenName = name;
        String params = "";

        // If a file exists for the given name we read it, replace the given name with the
        // effect name from the file and make the parameter list the concatenation of the
        // parameters from the file plus the previous parameters.
        try {
            Set<String> seen = new HashSet<String>();

            while (true) {
                seen.add(name);

                Path filePath = Paths.get(ConfigConstants.getUserDataFolder(), effectsFolderName, name);

                String[] fileSpec = (new String(Files.readAllBytes(filePath))).split("(?::|\\r?\\n)\\s*", 2);

                name = fileSpec[0];

                // If there is a loop in the definition chain we cannot continue.
                if (seen.contains(name)) {
                    throw new LightifyException("Effect \"" + givenName + "\" contains a loop. \"" + name + "\" occurred twice.");
                }

                if (fileSpec.length == 2) {
                    params = fileSpec[1].trim()
                                 // Remove comments.
                                 .replaceAll("(?://|#)[^\\r\\n]*\\r?\\n", "")
                                 // Collapse blank lines.
                                 .replaceAll("\\r?\\n(?:\\s*\\r?\\n)+", "\n")
                                 // Replace separating newlines with separating commas.
                                 .replaceAll("\\r?\\n(?!\\s)", ", ")
                                 // Collapse continuation lines.
                                 .replaceAll("\\r?\\n\\s+", " ")
                                 + params;
                }
            }
        } catch (IOException e) {
            // The file doesn't exist or isn't readable. We don't care. We'll just try
            // the given name and parameters anyway.
        }

        byte[] data;

        // All effects MUST end up using a preset or built-in.
        if ((data = PRESET_COLOR.get(name)) != null) {
            LightifyEffect effect = new LightifyEffectRaw(bridgeHandler, deviceHandler, givenName, true, data);
            effect.parseParams(params);
            return effect;

        } else if ((data = PRESET_WHITE.get(name)) != null) {
            LightifyEffect effect = new LightifyEffectRaw(bridgeHandler, deviceHandler, givenName, false, data);
            effect.parseParams(params);
            return effect;

        } else {
            try {
                Class<? extends LightifyEffect> cl = EFFECT_MAP.get(name);
                if (cl != null) {
                    Constructor<?> c = cl.getConstructor(LightifyBridgeHandler.class, LightifyDeviceHandler.class, String.class);
                    LightifyEffect effect = (LightifyEffect) c.newInstance(bridgeHandler, deviceHandler, givenName);
                    effect.parseParams(params);
                    return effect;
                }

                throw new LightifyException("Effect \"" + name + "\" does not exist.");
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
                throw new LightifyException("Failed creating effect \"" + name + "\":", e);
            }
        }
    }
}
