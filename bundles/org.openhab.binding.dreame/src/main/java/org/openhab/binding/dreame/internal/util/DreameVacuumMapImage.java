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
package org.openhab.binding.dreame.internal.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameVacuumCapabilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Renders a bounded monochrome preview of complete vacuum map frames.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameVacuumMapImage {
    private static final int LIMIT = 1024 * 1024;

    private DreameVacuumMapImage() {
    }

    public static byte @Nullable [] fromMessage(byte[] payload, String model) {
        String encoded = encodedFromMessage(payload, model);
        return encoded == null ? null : render(encoded);
    }

    public static @Nullable String encodedFromMessage(byte[] payload, String model) {
        return stringPropertyFromMessage(payload, model, 1);
    }

    /** Returns the map-list object name without exposing the accompanying checksum. */
    public static @Nullable String mapListObjectNameFromMessage(byte[] payload, String model) {
        String value = stringPropertyFromMessage(payload, model, 8);
        return value == null ? null : mapListObjectName(value);
    }

    /** Returns the object name from a MAP_LIST property value. */
    public static @Nullable String mapListObjectName(String value) {
        if (value.length() > 2048 || !DreameVacuumDiagnostics.boundedDepth(value)) {
            return null;
        }
        try {
            JsonElement parsed = JsonParser.parseString(value);
            // Some firmwares wrap the descriptor in a second JSON string.
            if (parsed instanceof JsonPrimitive wrapper && wrapper.isString()) {
                String nested = wrapper.getAsString();
                if (nested.length() > 2048 || !DreameVacuumDiagnostics.boundedDepth(nested)) {
                    return null;
                }
                parsed = JsonParser.parseString(nested);
            }
            if (parsed instanceof JsonObject descriptor
                    && descriptor.get("object_name") instanceof JsonPrimitive objectName && objectName.isString()) {
                String result = objectName.getAsString();
                return result.isBlank() || result.length() > 1024 ? null : result;
            }
        } catch (RuntimeException e) {
            // Treat malformed device metadata as absent.
        }
        return null;
    }

    private static @Nullable String stringPropertyFromMessage(byte[] payload, String model, int propertyId) {
        if (!DreameVacuumCapabilities.isSupported(model) || payload.length > 65536) {
            return null;
        }
        String json = new String(payload, StandardCharsets.UTF_8);
        if (!DreameVacuumDiagnostics.boundedDepth(json)) {
            return null;
        }
        try {
            if (!(JsonParser.parseString(json) instanceof JsonObject root)) {
                return null;
            }
            JsonObject data = root.get("data") instanceof JsonObject nested ? nested : root;
            if (!(data.get("method") instanceof JsonPrimitive method) || !method.isString()
                    || !"properties_changed".equals(method.getAsString())
                    || !(data.get("params") instanceof JsonArray params)) {
                return null;
            }
            for (int i = 0; i < Math.min(params.size(), 32); i++) {
                if (params.get(i) instanceof JsonObject property && number(property, "siid", 6)
                        && number(property, "piid", propertyId)
                        && (!property.has("code") || number(property, "code", 0))
                        && property.get("value") instanceof JsonPrimitive value && value.isString()) {
                    return value.getAsString();
                }
            }
        } catch (RuntimeException e) {
            // Untrusted envelopes must not expose their contents through diagnostics.
        }
        return null;
    }

    private static boolean number(JsonObject object, String key, int expected) {
        return object.get(key) instanceof JsonPrimitive value && value.isNumber()
                && value.getAsBigDecimal().intValueExact() == expected;
    }

    static byte @Nullable [] render(String encoded) {
        if (encoded.length() > 65536 || encoded.contains(",")) {
            return null;
        }
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(Base64.getDecoder()
                    .decode(encoded.replace('-', '+').replace('_', '/').replaceAll("[\\t\\n\\r ]", "")));
            byte[] raw = new byte[LIMIT + 1];
            int size = 0;
            while (!inflater.finished()) {
                int count = inflater.inflate(raw, size, raw.length - size);
                size += count;
                if (size > LIMIT || count == 0 && !inflater.finished()) {
                    return null;
                }
            }
            // Reference: Tasshack/dreame-vacuum, map.py and MapFrameType.I in types.py.
            // P frames need a matching baseline and are never rendered as standalone maps.
            if (inflater.getRemaining() != 0 || size < 27 || raw[4] != 73) {
                return null;
            }
            int width = signedShort(raw, 19);
            int height = signedShort(raw, 21);
            if (width <= 0 || height <= 0 || width > 2048 || height > 2048 || (long) width * height + 27 > size
                    || signedShort(raw, 17) <= 0) {
                return null;
            }
            // This preview deliberately distinguishes only zero/nonzero pixels. Room and wall
            // classifications vary between map versions and require device validation first.
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    image.setRGB(x, height - 1 - y, raw[27 + y * width + x] == 0 ? 0xF3F4F6 : 0x456B85);
                }
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            return ImageIO.write(image, "png", output) ? output.toByteArray() : null;
        } catch (IllegalArgumentException | DataFormatException | IOException e) {
            return null;
        } finally {
            inflater.end();
        }
    }

    /** Converts the internally rendered PNG into vector runs using the same pixel coordinates. */
    public static byte @Nullable [] toSvg(byte[] png) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
            if (image == null) {
                return null;
            }
            StringBuilder svg = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                    .append(image.getWidth()).append("\" height=\"").append(image.getHeight())
                    .append("\" viewBox=\"0 0 ").append(image.getWidth()).append(' ').append(image.getHeight())
                    .append("\" shape-rendering=\"crispEdges\"><rect width=\"100%\" height=\"100%\" fill=\"#f3f4f6\"/>")
                    .append("<path fill=\"#456b85\" d=\"");
            for (int y = 0; y < image.getHeight(); y++) {
                int x = 0;
                while (x < image.getWidth()) {
                    if ((image.getRGB(x, y) & 0xFFFFFF) == 0xF3F4F6) {
                        x++;
                        continue;
                    }
                    int start = x++;
                    while (x < image.getWidth() && (image.getRGB(x, y) & 0xFFFFFF) != 0xF3F4F6) {
                        x++;
                    }
                    int length = x - start;
                    svg.append('M').append(start).append(' ').append(y).append('h').append(length).append("v1h-")
                            .append(length).append("z");
                    if (svg.length() > 4 * LIMIT) {
                        return null;
                    }
                }
            }
            return svg.append("\"/></svg>").toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static int signedShort(byte[] data, int offset) {
        return (short) ((data[offset] & 0xFF) | (data[offset + 1] << 8));
    }
}
