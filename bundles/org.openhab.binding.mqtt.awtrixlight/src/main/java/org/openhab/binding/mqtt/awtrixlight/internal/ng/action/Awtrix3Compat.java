/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.awtrixlight.internal.ng.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Awtrix3Compat} translates what the rules of AWTRIX 3 clocks pass to the actions into what AWTRIX NG
 * takes, following the
 * <a href="https://blueforcer.github.io/awtrix-ng/esp32/guides/migrating-from-awtrix3/">migration guide of AWTRIX
 * NG</a>. This keeps the rules working after a clock was flashed with AWTRIX NG.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
final class Awtrix3Compat {

    // Keys of the notification of AWTRIX 3 that AWTRIX NG has no equivalent for
    private static final Set<String> NO_EQUIVALENT = Set.of("topText", "pos", "barBC", "clients", "save", "draw");

    private static final String[] TEXT_CASES = { "inherit", "upper", "asTyped" };
    private static final String[] ICON_MODES = { "fixed", "pushOnce", "push" };
    private static final String[] LIFETIME_EXPIRIES = { "remove", "mark" };

    // AWTRIX 3 gave the color of a text part without the #
    private static final Pattern BARE_HEX_COLOR = Pattern.compile("[0-9A-Fa-f]{6}");

    private Awtrix3Compat() {
    }

    /**
     * @return the color as {@code #RRGGBB}, or null if it is not three values from 0 to 255
     */
    static @Nullable String toHex(int[] rgb) {
        if (rgb.length != 3) {
            return null;
        }
        for (int component : rgb) {
            if (component < 0 || component > 255) {
                return null;
            }
        }
        return String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Translates the keys and values of an AWTRIX 3 notification to the ones of AWTRIX NG. Keys that are no key of
     * AWTRIX 3 are passed on as they are, as AWTRIX NG names (such as {@code textColor}) were accepted before. A key
     * without a value is left out.
     *
     * @param params the notification of AWTRIX 3
     * @param withoutEquivalent receives the keys that were left out as AWTRIX NG has no equivalent
     * @return the notification of AWTRIX NG
     */
    static Map<String, Object> toNotification(Map<String, ?> params, Set<String> withoutEquivalent) {
        Map<String, Object> notification = new LinkedHashMap<>();
        Map<String, Object> scroll = new LinkedHashMap<>();
        Object gradient = null;
        boolean rainbow = false;
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (NO_EQUIVALENT.contains(key)) {
                withoutEquivalent.add(key);
                continue;
            }
            if (value == null) {
                continue;
            }
            switch (key) {
                case "textCase" -> notification.put("textCase", fromOrdinal(value, TEXT_CASES));
                case "textOffset" -> notification.put("textOffsetX", value);
                case "center" -> notification.put("textAlign", Boolean.TRUE.equals(value) ? "center" : "start");
                case "color" -> notification.put("textColor", color(value));
                case "gradient" -> gradient = value;
                case "rainbow" -> rainbow = Boolean.TRUE.equals(value);
                case "blinkText" -> notification.put("textBlinkMs", value);
                case "fadeText" -> notification.put("textFadeMs", value);
                case "background" -> notification.put("backgroundColor", color(value));
                case "noScroll" -> {
                    if (Boolean.TRUE.equals(value)) {
                        scroll.put("mode", "static");
                    }
                }
                case "scrollSpeed" -> scroll.put("speed", value);
                case "pushIcon" -> notification.put("iconMode", fromOrdinal(value, ICON_MODES));
                case "duration" -> notification.put("durationMs", seconds(value));
                case "lifetime" -> notification.put("lifetimeMs", seconds(value));
                case "lifetimeMode" -> notification.put("lifetimeExpiry", fromOrdinal(value, LIFETIME_EXPIRIES));
                case "bar" -> notification.put("barChart", value);
                case "line" -> notification.put("lineChart", value);
                case "autoscale" -> notification.put("chartAutoscale", value);
                case "progressC" -> notification.put("progressColor", color(value));
                case "progressBC" -> notification.put("progressTrackColor", color(value));
                case "overlay" ->
                    notification.put("overlay", "clear".equalsIgnoreCase(String.valueOf(value)) ? "" : value);
                case "wakeUp" -> notification.put("wakeup", value);
                case "text" -> notification.put("text", text(value));
                case "effectSettings" -> effectSettings(value, notification);
                default -> notification.put(key, value);
            }
        }
        if (!scroll.isEmpty()) {
            notification.put("scroll", scroll);
        }
        // The palette of the text goes before the one of the effect, as both are the one palette of AWTRIX NG
        if (rainbow) {
            notification.put("palette", "Rainbow");
            notification.put("textColor", "palette");
        } else if (gradient instanceof Collection<?> colors && !colors.isEmpty()) {
            List<String> hexColors = new ArrayList<>();
            for (Object gradientColor : colors) {
                if (gradientColor != null) {
                    hexColors.add(String.valueOf(color(gradientColor)));
                }
            }
            notification.put("palette", String.join(",", hexColors));
            notification.put("textColor", "palette");
        }
        return notification;
    }

    /**
     * @return the sound of AWTRIX NG for the sound arguments of AWTRIX 3, or null if there is none
     */
    static @Nullable Map<String, Object> toSound(@Nullable String rtttl, @Nullable String sound, boolean loop) {
        Map<String, Object> spec = new LinkedHashMap<>();
        if (rtttl != null && !rtttl.isBlank()) {
            spec.put("rtttl", rtttl);
        } else if (sound != null && !sound.isBlank()) {
            spec.put("file", sound);
        } else {
            return null;
        }
        if (loop) {
            spec.put("loop", true);
        }
        return spec;
    }

    private static Object fromOrdinal(Object value, String[] names) {
        if (value instanceof Number number && number.intValue() >= 0 && number.intValue() < names.length) {
            return names[number.intValue()];
        }
        // Not one of the numbers, so AWTRIX NG tells what is wrong with it
        return value;
    }

    private static Object seconds(Object value) {
        return value instanceof Number number ? Math.round(number.doubleValue() * 1000) : value;
    }

    private static Object color(Object value) {
        if (value instanceof int[] rgb) {
            String hex = toHex(rgb);
            return hex != null ? hex : value;
        }
        if (value instanceof Collection<?> components && components.size() == 3) {
            int[] rgb = new int[3];
            int index = 0;
            for (Object component : components) {
                if (!(component instanceof Number number)) {
                    return value;
                }
                rgb[index++] = number.intValue();
            }
            String hex = toHex(rgb);
            return hex != null ? hex : value;
        }
        return value;
    }

    /** A list of parts, as AWTRIX 3 named them, becomes the parts of AWTRIX NG. */
    private static Object text(Object value) {
        if (!(value instanceof Collection<?> parts)) {
            return value;
        }
        List<Object> translated = new ArrayList<>();
        for (Object part : parts) {
            if (part instanceof Map<?, ?> fragment) {
                Map<String, Object> converted = new LinkedHashMap<>();
                for (Map.Entry<?, ?> property : fragment.entrySet()) {
                    Object name = property.getKey();
                    Object propertyValue = property.getValue();
                    if (name == null || propertyValue == null) {
                        continue;
                    }
                    if ("t".equals(name)) {
                        converted.put("text", propertyValue);
                    } else if ("c".equals(name)) {
                        converted.put("color", bareColor(propertyValue));
                    } else {
                        converted.put(name.toString(), propertyValue);
                    }
                }
                translated.add(converted);
            } else if (part != null) {
                translated.add(part);
            }
        }
        return translated;
    }

    private static Object bareColor(Object value) {
        if (value instanceof String hex && BARE_HEX_COLOR.matcher(hex).matches()) {
            return "#" + hex;
        }
        return color(value);
    }

    private static void effectSettings(Object value, Map<String, Object> notification) {
        if (!(value instanceof Map<?, ?> settings)) {
            return;
        }
        Object speed = settings.get("speed");
        if (speed != null) {
            notification.put("effectSpeed", speed);
        }
        Object blend = settings.get("blend");
        if (blend != null) {
            notification.put("paletteBlend", blend);
        }
        Object palette = settings.get("palette");
        if (palette != null && !"None".equalsIgnoreCase(palette.toString())) {
            notification.putIfAbsent("palette", palette);
        }
    }
}
