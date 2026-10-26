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
package org.openhab.binding.awtrixng.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awtrixng.internal.dto.AppPayload;
import org.openhab.binding.awtrixng.internal.dto.PlacedIcon;
import org.openhab.binding.awtrixng.internal.dto.ScrollConfig;
import org.openhab.core.library.types.StringType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * The {@link AppChannels} lists the channels of an app that are plain fields of its payload, and how each one maps to
 * its field. The channels that control the app itself (active, reset, enabled) are not part of the payload.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class AppChannels {

    /** The value of {@code textColor} that colors the text from the palette of the app. */
    static final String PALETTE = "palette";

    // Limits of the payload of NG, which rejects an app that exceeds them
    private static final int MAX_CHART_VALUES = 16;
    private static final int MAX_ICONS = 4;
    private static final int MAX_ICON_COORDINATE = 65535;

    // The words of the enum fields that NG accepts
    private static final Set<String> TEXT_CASES = Set.of("inherit", "upper", "asTyped");
    private static final Set<String> TEXT_ALIGNS = Set.of("start", "center", "end");
    private static final Set<String> SCROLL_MODES = Set.of("static", "wrap", "loop", "bounce");
    private static final Set<String> SCROLL_DIRECTIONS = Set.of("left", "right");
    private static final Set<String> SCROLL_ENTRIES = Set.of("inline", "offscreen");
    private static final Set<String> SCROLL_WHEN_FITS = Set.of("static", "scroll");
    private static final Set<String> ICON_MODES = Set.of("fixed", "pushOnce", "push");
    private static final Set<String> LIFETIME_EXPIRIES = Set.of("remove", "mark");
    private static final long MAX_INT = Integer.MAX_VALUE;

    private static final PayloadChannels<AppPayload> CHANNELS = new PayloadChannels<>();

    static {
        // Text
        CHANNELS.custom("text", p -> p.text == null ? null : new StringType(TextMarkup.toMarkup(p.text)),
                (p, command) -> {
                    if (command instanceof StringType) {
                        // NG colors the text outside of the font tags white, not with the text color
                        p.text = TextMarkup.toJson(command.toString(), null);
                        return true;
                    }
                    return false;
                });
        CHANNELS.textChannel("text-case", p -> p.textCase, (p, v) -> p.textCase = v, TEXT_CASES);
        CHANNELS.textChannel("font", p -> p.font, (p, v) -> p.font = v);
        // The color and the palette are alternatives for the color of the text: setting one switches the other off
        CHANNELS.colorChannel("text-color", p -> p.textColor, (p, v) -> p.textColor = v);
        CHANNELS.switchChannel("text-palette", p -> p.textColor == null ? null : PALETTE.equals(p.textColor),
                (p, v) -> {
                    if (v) {
                        p.textColor = PALETTE;
                    } else if (PALETTE.equals(p.textColor)) {
                        p.textColor = null;
                    }
                });
        CHANNELS.durationChannel("text-blink", p -> p.textBlinkMs, (p, v) -> p.textBlinkMs = v.intValue(), 0, MAX_INT);
        CHANNELS.durationChannel("text-fade", p -> p.textFadeMs, (p, v) -> p.textFadeMs = v.intValue(), 0, MAX_INT);
        CHANNELS.textChannel("text-align", p -> p.textAlign, (p, v) -> p.textAlign = v, TEXT_ALIGNS);
        CHANNELS.numberChannel("text-offset-x", p -> p.textOffsetX, (p, v) -> p.textOffsetX = v);
        CHANNELS.switchChannel("text-in-front", p -> p.textInFront, (p, v) -> p.textInFront = v);

        // Scrolling
        CHANNELS.textChannel("scroll-mode", p -> p.scroll == null ? null : p.scroll.mode, (p, v) -> scroll(p).mode = v,
                SCROLL_MODES);
        CHANNELS.textChannel("scroll-direction", p -> p.scroll == null ? null : p.scroll.direction,
                (p, v) -> scroll(p).direction = v, SCROLL_DIRECTIONS);
        CHANNELS.textChannel("scroll-entry", p -> p.scroll == null ? null : p.scroll.entry,
                (p, v) -> scroll(p).entry = v, SCROLL_ENTRIES);
        CHANNELS.textChannel("scroll-when-fits", p -> p.scroll == null ? null : p.scroll.whenFits,
                (p, v) -> scroll(p).whenFits = v, SCROLL_WHEN_FITS);
        CHANNELS.percentChannel("scroll-speed", p -> p.scroll == null ? null : p.scroll.speed,
                (p, v) -> scroll(p).speed = v, 0, Integer.MAX_VALUE);
        CHANNELS.numberChannel("scroll-gap", p -> p.scroll == null ? null : p.scroll.gap, (p, v) -> scroll(p).gap = v,
                0, Integer.MAX_VALUE);
        CHANNELS.durationChannel("scroll-hold", p -> p.scroll == null ? null : p.scroll.holdMs,
                (p, v) -> scroll(p).holdMs = v.intValue(), 0, MAX_INT);

        // Icons
        CHANNELS.textChannel("icon", p -> p.icon, (p, v) -> p.icon = v);
        CHANNELS.textChannel("icon-mode", p -> p.iconMode, (p, v) -> p.iconMode = v, ICON_MODES);
        CHANNELS.numberChannel("icon-offset-x", p -> p.iconOffsetX, (p, v) -> p.iconOffsetX = v);
        CHANNELS.numberChannel("icon-gap", p -> p.iconGap, (p, v) -> p.iconGap = v, 0, 128);
        CHANNELS.custom("icons", p -> p.icons == null ? null : new StringType(Helper.toJson(p.icons)), (p, command) -> {
            List<PlacedIcon> icons = command instanceof StringType ? parseIcons(command.toString()) : null;
            if (icons == null) {
                return false;
            }
            p.icons = icons.isEmpty() ? null : icons;
            return true;
        });

        // Timing
        CHANNELS.durationChannel("duration", p -> p.durationMs, (p, v) -> p.durationMs = v, Long.MIN_VALUE,
                Long.MAX_VALUE);
        CHANNELS.durationChannel("lifetime", p -> p.lifetimeMs, (p, v) -> p.lifetimeMs = v, 0, Long.MAX_VALUE);
        CHANNELS.textChannel("lifetime-expiry", p -> p.lifetimeExpiry, (p, v) -> p.lifetimeExpiry = v,
                LIFETIME_EXPIRIES);
        CHANNELS.numberChannel("repeat", p -> p.repeat, (p, v) -> p.repeat = v, 0, Integer.MAX_VALUE);

        // Charts and progress
        chartChannel("bar-chart", p -> p.barChart, (p, v) -> p.barChart = v);
        chartChannel("line-chart", p -> p.lineChart, (p, v) -> p.lineChart = v);
        CHANNELS.switchChannel("chart-autoscale", p -> p.chartAutoscale, (p, v) -> p.chartAutoscale = v);
        CHANNELS.colorChannel("chart-color", p -> p.chartColor, (p, v) -> p.chartColor = v);
        CHANNELS.percentChannel("progress", p -> p.progress, (p, v) -> p.progress = v, -1, 100);
        CHANNELS.colorChannel("progress-color", p -> p.progressColor, (p, v) -> p.progressColor = v);
        CHANNELS.colorChannel("progress-track-color", p -> p.progressTrackColor, (p, v) -> p.progressTrackColor = v);

        // Background, effects and palette
        CHANNELS.colorChannel("background-color", p -> p.backgroundColor, (p, v) -> p.backgroundColor = v);
        CHANNELS.textChannel("effect", p -> p.effect, (p, v) -> p.effect = v);
        CHANNELS.decimalChannel("effect-speed", p -> p.effectSpeed, (p, v) -> p.effectSpeed = v, 0.1, 10);
        CHANNELS.custom("palette", p -> p.palette == null ? null : new StringType(paletteToText(p.palette)),
                (p, command) -> {
                    if (command instanceof StringType) {
                        p.palette = paletteFromText(command.toString());
                        return true;
                    }
                    return false;
                });
        CHANNELS.switchChannel("palette-blend", p -> p.paletteBlend, (p, v) -> p.paletteBlend = v);
        CHANNELS.numberChannel("palette-span", p -> p.paletteSpan, (p, v) -> p.paletteSpan = v, 0, Integer.MAX_VALUE);
        CHANNELS.decimalChannel("palette-speed", p -> p.paletteSpeed, (p, v) -> p.paletteSpeed = v, 0, 10);
        CHANNELS.textChannel("overlay", p -> p.overlay, (p, v) -> p.overlay = v);

        // Drawing commands
        CHANNELS.custom("draw", p -> p.draw == null ? null : new StringType(p.draw.toString()), (p, command) -> {
            JsonArray draw = command instanceof StringType ? parseArray(command.toString()) : null;
            if (draw == null) {
                return false;
            }
            p.draw = draw.isEmpty() ? null : draw;
            return true;
        });
    }

    private AppChannels() {
    }

    /**
     * @return the mapping for the channel, or null if the channel is not a plain payload field
     */
    public static @Nullable PayloadChannel<AppPayload> get(String channelId) {
        return CHANNELS.get(channelId);
    }

    public static Iterable<Map.Entry<String, PayloadChannel<AppPayload>>> all() {
        return CHANNELS.all();
    }

    private static ScrollConfig scroll(AppPayload payload) {
        ScrollConfig scroll = payload.scroll;
        if (scroll == null) {
            scroll = new ScrollConfig();
            payload.scroll = scroll;
        }
        return scroll;
    }

    /** A chart is a comma separated list, of which NG shows at most the newest values. */
    private static void chartChannel(String id, Function<AppPayload, @Nullable List<Integer>> get,
            BiConsumer<AppPayload, @Nullable List<Integer>> set) {
        CHANNELS.custom(id, p -> {
            List<Integer> values = get.apply(p);
            return values == null ? null
                    : new StringType(values.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }, (p, command) -> {
            if (!(command instanceof StringType)) {
                return false;
            }
            try {
                List<Integer> values = command.toString().isBlank() ? List.of()
                        : Arrays.stream(command.toString().split(",")).map(String::trim).map(Integer::valueOf).toList();
                if (values.size() > MAX_CHART_VALUES) {
                    values = values.subList(values.size() - MAX_CHART_VALUES, values.size());
                }
                set.accept(p, values.isEmpty() ? null : new ArrayList<>(values));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
    }

    private static @Nullable List<PlacedIcon> parseIcons(String json) {
        if (json.isBlank()) {
            return List.of();
        }
        PlacedIcon[] icons = Helper.fromJson(json, PlacedIcon[].class);
        if (icons == null || icons.length > MAX_ICONS) {
            return null;
        }
        // NG rejects the whole app for an entry without an icon or beyond the panel's coordinates
        for (PlacedIcon icon : icons) {
            if (icon == null || icon.icon == null || icon.icon.isBlank() || !isIconCoordinate(icon.x)
                    || !isIconCoordinate(icon.y)) {
                return null;
            }
        }
        return Arrays.asList(icons);
    }

    /** A coordinate that is left out is 0 for NG. */
    private static boolean isIconCoordinate(@Nullable Integer coordinate) {
        return coordinate == null || Math.abs(coordinate) <= MAX_ICON_COORDINATE;
    }

    private static @Nullable JsonArray parseArray(String json) {
        if (json.isBlank()) {
            return new JsonArray();
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            return element.isJsonArray() ? element.getAsJsonArray() : null;
        } catch (JsonParseException e) {
            return null;
        }
    }

    /** The name of a built-in palette, or the stops of the app's own palette separated by commas. */
    private static String paletteToText(JsonElement palette) {
        if (palette.isJsonArray()) {
            List<String> stops = new ArrayList<>();
            // A stored app can hold what is not a color or a name, which has no text
            palette.getAsJsonArray().forEach(stop -> {
                if (stop.isJsonPrimitive()) {
                    stops.add(stop.getAsString());
                }
            });
            return String.join(",", stops);
        }
        return palette.isJsonPrimitive() ? palette.getAsString() : "";
    }

    private static @Nullable JsonElement paletteFromText(String text) {
        if (text.isBlank()) {
            return null;
        }
        if (!text.contains(",") && !text.contains("#")) {
            return new JsonPrimitive(text.trim());
        }
        JsonArray stops = new JsonArray();
        Arrays.stream(text.split(",")).map(String::trim).filter(stop -> !stop.isEmpty()).forEach(stops::add);
        return stops;
    }
}
