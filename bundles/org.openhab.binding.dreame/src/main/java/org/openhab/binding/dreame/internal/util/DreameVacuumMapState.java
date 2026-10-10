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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Reconstructs bounded vacuum I/P map frames and renders matching SVG and PNG images.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameVacuumMapState {
    private static final int MAX_DATA = 1024 * 1024;
    private static final int SESSION_RESET_FRAME_GAP = 32;
    private static final Pattern PATH = Pattern.compile("([MWSLl])(-?\\d+),(-?\\d+)");
    private static final Map<Integer, String> ROOM_TYPES = Map.ofEntries(Map.entry(1, "Living Room"),
            Map.entry(2, "Primary Bedroom"), Map.entry(3, "Study"), Map.entry(4, "Kitchen"),
            Map.entry(5, "Dining Hall"), Map.entry(6, "Bathroom"), Map.entry(7, "Balcony"), Map.entry(8, "Corridor"),
            Map.entry(9, "Utility Room"), Map.entry(10, "Closet"), Map.entry(11, "Meeting Room"),
            Map.entry(12, "Office"), Map.entry(13, "Fitness Area"), Map.entry(14, "Recreation Area"),
            Map.entry(15, "Secondary Bedroom"));

    private @Nullable Frame current;
    private @Nullable RoomInfo savedRooms;
    private @Nullable Integer savedRoomsMapId;
    private boolean needsBase = true;
    private String diagnostic = "waiting-for-base";

    public record Images(byte[] png, byte[] svg, String rooms) {
    }

    private record Point(int x, int y, boolean move) {
    }

    private record Frame(int mapId, int frameId, int type, int grid, int left, int top, int width, int height,
            int robotX, int robotY, int robotAngle, int dockX, int dockY, byte[] pixels, boolean version3,
            boolean frameMap, List<Point> path, Map<Integer, String> roomNames, String roomMetadata) {
    }

    private record RoomInfo(Map<Integer, String> names, String diagnostic) {
    }

    public boolean needsBase() {
        return needsBase;
    }

    public void clear() {
        current = null;
        savedRooms = null;
        savedRoomsMapId = null;
        needsBase = true;
        diagnostic = "waiting-for-base";
    }

    /** Development diagnostic without map contents or embedded metadata. */
    public String diagnostic() {
        return diagnostic;
    }

    public @Nullable Images accept(String encoded) {
        try {
            Frame incoming = decode(encoded);
            if (incoming == null) {
                needsBase = true;
                diagnostic = "decode-rejected/chars=" + encoded.length();
                return null;
            }
            RoomInfo cachedRooms = savedRooms;
            if (incoming.roomNames.isEmpty() && cachedRooms != null && savedRoomsMapId != null
                    && (incoming.mapId == savedRoomsMapId || roomSegmentsMatch(incoming, cachedRooms.names))) {
                incoming = withRoomNames(incoming, cachedRooms.names, cachedRooms.diagnostic);
            }
            Frame base = current;
            if (base != null && base.mapId == incoming.mapId && incoming.frameId <= base.frameId) {
                if (base.frameId - incoming.frameId > SESSION_RESET_FRAME_GAP) {
                    current = null;
                    needsBase = true;
                    base = null;
                } else {
                    diagnostic = "stale/type=" + (char) incoming.type + "/map=" + incoming.mapId + "/frame="
                            + incoming.frameId + "/current=" + base.frameId;
                    return null;
                }
            }
            if (incoming.type == 'P') {
                if (needsBase || base == null || base.mapId != incoming.mapId || base.grid != incoming.grid) {
                    needsBase = true;
                    diagnostic = "missing-base/type=P/map=" + incoming.mapId + "/frame=" + incoming.frameId + "/grid="
                            + incoming.grid;
                    return null;
                }
                incoming = merge(base, incoming);
            }
            Images images = render(incoming);
            current = incoming;
            needsBase = false;
            diagnostic = "published/type=" + (char) incoming.type + "/map=" + incoming.mapId + "/frame="
                    + incoming.frameId + "/grid=" + incoming.grid + "/size=" + incoming.width + "x" + incoming.height
                    + "/roomMetadata=" + incoming.roomMetadata;
            return images;
        } catch (RuntimeException | DataFormatException | IOException e) {
            needsBase = true;
            diagnostic = "processing-failed/type=" + e.getClass().getSimpleName() + "/chars=" + encoded.length();
            return null;
        }
    }

    /** Applies room metadata from the selected saved map without replacing the newer live geometry. */
    public @Nullable Images acceptMapList(String raw) {
        try {
            if (raw.length() > 2 * MAX_DATA || !DreameVacuumDiagnostics.boundedDepth(raw)
                    || !(JsonParser.parseString(raw) instanceof JsonObject root)
                    || !(root.get("mapstr") instanceof JsonArray maps) || maps.size() > 8) {
                diagnostic = "map-list-rejected/chars=" + raw.length();
                return null;
            }
            Integer selectedId = integer(root.get("curr_id"));
            Frame selected = null;
            for (int i = 0; i < maps.size(); i++) {
                if (maps.get(i) instanceof JsonObject entry && entry.get("map") instanceof JsonPrimitive map
                        && map.isString()) {
                    Frame candidate = decode(map.getAsString());
                    if (candidate != null && (selectedId == null || candidate.mapId == selectedId)) {
                        selected = candidate;
                        if (selectedId != null) {
                            break;
                        }
                    }
                }
            }
            if (selected == null || selected.roomNames.isEmpty()) {
                diagnostic = "map-list-no-matching-room-metadata/maps=" + maps.size();
                return null;
            }
            savedRoomsMapId = selected.mapId;
            savedRooms = new RoomInfo(selected.roomNames, selected.roomMetadata);
            Frame live = current;
            if (live == null || selected.mapId != live.mapId && !roomSegmentsMatch(live, selected.roomNames)) {
                diagnostic = "map-list-room-metadata-cached/map=" + selected.mapId + "/rooms="
                        + selected.roomNames.size();
                return null;
            }
            Frame enriched = withRoomNames(live, selected.roomNames, selected.roomMetadata);
            current = enriched;
            Images images = render(enriched);
            diagnostic = "published-map-list/map=" + live.mapId + "/rooms=" + selected.roomNames.size()
                    + "/roomMetadata=" + selected.roomMetadata;
            return images;
        } catch (RuntimeException | DataFormatException | IOException e) {
            diagnostic = "map-list-processing-failed/type=" + e.getClass().getSimpleName() + "/chars=" + raw.length();
            return null;
        }
    }

    private static @Nullable Integer integer(@Nullable JsonElement value) {
        try {
            return value instanceof JsonPrimitive primitive && primitive.isNumber() ? primitive.getAsInt() : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Frame withRoomNames(Frame frame, Map<Integer, String> names, String metadata) {
        return new Frame(frame.mapId, frame.frameId, frame.type, frame.grid, frame.left, frame.top, frame.width,
                frame.height, frame.robotX, frame.robotY, frame.robotAngle, frame.dockX, frame.dockY, frame.pixels,
                frame.version3, frame.frameMap, frame.path, names, metadata);
    }

    private static boolean roomSegmentsMatch(Frame frame, Map<Integer, String> names) {
        TreeSet<Integer> segments = new TreeSet<>();
        for (byte value : frame.pixels) {
            int room = room(frame, value & 0xFF);
            if (room > 0 && room < 61) {
                segments.add(room);
            }
        }
        return !segments.isEmpty() && segments.equals(new TreeSet<>(names.keySet()));
    }

    private static @Nullable Frame decode(String encoded) throws DataFormatException {
        if (encoded.length() > 2 * MAX_DATA || encoded.contains(",")) {
            return null;
        }
        byte[] raw = new byte[MAX_DATA + 1];
        Inflater inflater = new Inflater();
        int size = 0;
        try {
            inflater.setInput(Base64.getDecoder()
                    .decode(encoded.replace('-', '+').replace('_', '/').replaceAll("[\\t\\n\\r ]", "")));
            while (!inflater.finished()) {
                int count = inflater.inflate(raw, size, raw.length - size);
                size += count;
                if (size > MAX_DATA || count == 0 && !inflater.finished()) {
                    return null;
                }
            }
            if (inflater.getRemaining() != 0 || size < 27 || raw[4] != 'I' && raw[4] != 'P') {
                return null;
            }
        } finally {
            inflater.end();
        }
        int width = number(raw, 19), height = number(raw, 21), grid = number(raw, 17);
        if (width < 0 || height < 0 || width > 2048 || height > 2048 || grid <= 0
                || (long) width * height + 27 > size) {
            return null;
        }
        int imageEnd = 27 + width * height;
        String json = new String(raw, imageEnd, size - imageEnd, StandardCharsets.UTF_8).trim();
        if (!DreameVacuumDiagnostics.boundedDepth(json)) {
            return null;
        }
        JsonObject metadata = json.isEmpty() ? new JsonObject() : JsonParser.parseString(json).getAsJsonObject();
        // V3 cover/diff changes need their own coordinate decoder. Reject them explicitly until observed.
        if (raw[4] == 'P' && (metadata.has("cover") || metadata.has("diff"))) {
            return null;
        }
        boolean version3 = metadata.has("saveMapId") || metadata.has("curtain");
        boolean frameMap = metadata.has("fsm") && metadata.get("fsm").getAsInt() == 1;
        RoomInfo rooms = parseRoomInfo(metadata);
        return new Frame(number(raw, 0), number(raw, 2), raw[4], grid, number(raw, 23), number(raw, 25), width, height,
                number(raw, 5), number(raw, 7), number(raw, 9), number(raw, 11), number(raw, 13),
                Arrays.copyOfRange(raw, 27, imageEnd), version3, frameMap, parsePath(metadata), rooms.names(),
                rooms.diagnostic());
    }

    private static RoomInfo parseRoomInfo(JsonObject metadata) {
        if (!(metadata.get("seg_inf") instanceof JsonObject segments) || segments.size() > 64) {
            return new RoomInfo(Map.of(), "absent");
        }
        Map<Integer, String> names = new java.util.HashMap<>();
        StringJoiner diagnostic = new StringJoiner(",", "[", "]");
        segments.entrySet().forEach(entry -> {
            try {
                int id = Integer.parseInt(entry.getKey());
                if (id > 0 && id < 64 && entry.getValue() instanceof JsonObject room) {
                    int type = room.get("type") instanceof com.google.gson.JsonPrimitive typeValue
                            && typeValue.isNumber() ? typeValue.getAsInt() : -1;
                    int index = room.get("index") instanceof com.google.gson.JsonPrimitive indexValue
                            && indexValue.isNumber() ? indexValue.getAsInt() : -1;
                    diagnostic.add(id + ":type=" + type + "/index=" + index + "/name="
                            + (room.get("name") instanceof com.google.gson.JsonPrimitive nameValue
                                    && nameValue.isString() && !nameValue.getAsString().isBlank()));
                    String text = null;
                    if (room.get("name") instanceof com.google.gson.JsonPrimitive name && name.isString()) {
                        text = new String(Base64.getDecoder().decode(name.getAsString()), StandardCharsets.UTF_8)
                                .trim();
                    }
                    if (text == null || text.isEmpty()) {
                        text = ROOM_TYPES.get(type);
                        if (text != null && index > 0) {
                            text += " " + (index + 1);
                        }
                    }
                    if (text != null && !text.isEmpty() && text.length() <= 64) {
                        names.put(id, text);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Ignore a malformed optional room label without dropping the map.
            }
        });
        return new RoomInfo(names, diagnostic.toString());
    }

    private static List<Point> parsePath(JsonObject metadata) {
        List<Point> points = new ArrayList<>();
        if (!metadata.has("tr")) {
            return points;
        }
        String encoded = metadata.get("tr").getAsString();
        if (encoded.length() > 200000) {
            throw new IllegalArgumentException();
        }
        Matcher matcher = PATH.matcher(encoded);
        int x = 0;
        int y = 0;
        while (matcher.find()) {
            int nextX = Integer.parseInt(matcher.group(2));
            int nextY = Integer.parseInt(matcher.group(3));
            if ("L".equals(matcher.group(1))) {
                x = Math.addExact(x, nextX);
                y = Math.addExact(y, nextY);
            } else {
                x = nextX;
                y = nextY;
            }
            if (Math.abs((long) x) > 1000000 || Math.abs((long) y) > 1000000 || points.size() >= 10000) {
                throw new IllegalArgumentException();
            }
            points.add(new Point(x, y, "M".equals(matcher.group(1))));
        }
        return points;
    }

    private static Frame merge(Frame base, Frame update) {
        int left = Math.min(base.left, update.left);
        int top = Math.min(base.top, update.top);
        int right = Math.max(base.left + base.width * base.grid, update.left + update.width * update.grid);
        int bottom = Math.max(base.top + base.height * base.grid, update.top + update.height * update.grid);
        if ((base.left - left) % base.grid != 0 || (base.top - top) % base.grid != 0
                || (update.left - left) % base.grid != 0 || (update.top - top) % base.grid != 0) {
            throw new IllegalArgumentException();
        }
        int width = (right - left) / base.grid;
        int height = (bottom - top) / base.grid;
        if (width > 2048 || height > 2048 || (long) width * height > MAX_DATA) {
            throw new IllegalArgumentException();
        }
        byte[] pixels = new byte[width * height];
        for (int y = 0; y < base.height; y++) {
            System.arraycopy(base.pixels, y * base.width, pixels,
                    (y + (base.top - top) / base.grid) * width + (base.left - left) / base.grid, base.width);
        }
        for (int y = 0; y < update.height; y++) {
            for (int x = 0; x < update.width; x++) {
                int delta = update.pixels[y * update.width + x] & 0xFF;
                if (delta != 0) {
                    int target = (y + (update.top - top) / base.grid) * width + x + (update.left - left) / base.grid;
                    pixels[target] = (byte) (base.version3 ? delta : pixels[target] + delta);
                }
            }
        }
        List<Point> path = new ArrayList<>(base.path);
        path.addAll(update.path);
        Map<Integer, String> roomNames = update.roomNames.isEmpty() ? base.roomNames : update.roomNames;
        String roomMetadata = "absent".equals(update.roomMetadata) ? base.roomMetadata : update.roomMetadata;
        return new Frame(update.mapId, update.frameId, 'I', base.grid, left, top, width, height, update.robotX,
                update.robotY, update.robotAngle, update.dockX, update.dockY, pixels, base.version3, true, path,
                roomNames, roomMetadata);
    }

    private static Images render(Frame frame) throws IOException {
        int scale = Math.max(1, Math.min(4, 1600 / Math.max(frame.width, frame.height)));
        BufferedImage image = new BufferedImage(frame.width * scale, frame.height * scale, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        StringBuilder svg = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
                .append(frame.width).append(' ').append(frame.height).append("\"><g shape-rendering=\"crispEdges\">");
        try {
            graphics.scale(scale, scale);
            for (int y = 0; y < frame.height; y++) {
                int x = 0;
                while (x < frame.width) {
                    int start = x;
                    int color = color(frame, frame.pixels[y * frame.width + x] & 0xFF);
                    while (x < frame.width && color(frame, frame.pixels[y * frame.width + x] & 0xFF) == color) {
                        x++;
                    }
                    int displayY = frame.height - 1 - y;
                    graphics.setColor(new Color(color));
                    graphics.fillRect(start, displayY, x - start, 1);
                    svg.append("<path fill=\"#").append(String.format("%06x", color)).append("\" d=\"M").append(start)
                            .append(' ').append(displayY).append('h').append(x - start).append("v1h-").append(x - start)
                            .append("z\"/>");
                    if (svg.length() > 8 * MAX_DATA) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            svg.append("</g>");
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(0x2563EB));
            graphics.setStroke(new BasicStroke(0.8f));
            StringBuilder svgPath = new StringBuilder();
            Point previous = null;
            for (Point point : frame.path) {
                double x = x(frame, point.x);
                double y = y(frame, point.y);
                svgPath.append(previous == null || point.move ? 'M' : 'L').append(x).append(' ').append(y);
                if (previous != null && !point.move) {
                    graphics.draw(new java.awt.geom.Line2D.Double(x(frame, previous.x), y(frame, previous.y), x, y));
                }
                previous = point;
            }
            svg.append("<path fill=\"none\" stroke=\"#2563eb\" stroke-width=\"0.8\" d=\"").append(svgPath)
                    .append("\"/>");
            roomLabels(frame, graphics, svg);
            marker(frame, graphics, svg, frame.dockX, frame.dockY, 0x16A34A, "Dock");
            if (frame.robotAngle != 32767) {
                marker(frame, graphics, svg, frame.robotX, frame.robotY, 0xDC2626, "Robot");
            }
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", png)) {
            throw new IOException("PNG writer unavailable");
        }
        return new Images(png.toByteArray(), svg.append("</svg>").toString().getBytes(StandardCharsets.UTF_8),
                rooms(frame));
    }

    private static void roomLabels(Frame frame, java.awt.Graphics2D graphics, StringBuilder svg) {
        long[] xSum = new long[64];
        long[] ySum = new long[64];
        int[] count = new int[64];
        for (int y = 0; y < frame.height; y++) {
            for (int x = 0; x < frame.width; x++) {
                int room = room(frame, frame.pixels[y * frame.width + x] & 0xFF);
                if (isVisibleRoom(frame, room)) {
                    xSum[room] += x;
                    ySum[room] += frame.height - 1 - y;
                    count[room]++;
                }
            }
        }
        graphics.setColor(new Color(0x0F172A));
        graphics.setFont(graphics.getFont().deriveFont(3f));
        for (int room = 1; room < count.length; room++) {
            if (count[room] > 0) {
                double x = (double) xSum[room] / count[room];
                double y = (double) ySum[room] / count[room];
                String name = frame.roomNames.get(room);
                String label = name == null ? "Room " + room : name + " (" + room + ")";
                graphics.drawString(label, (float) x, (float) y);
                svg.append("<text x=\"").append(x).append("\" y=\"").append(y)
                        .append("\" text-anchor=\"middle\" font-size=\"3\" fill=\"#0f172a\">").append(escape(label))
                        .append("</text>");
            }
        }
    }

    private static String rooms(Frame frame) {
        TreeSet<Integer> rooms = new TreeSet<>();
        for (byte value : frame.pixels) {
            int room = room(frame, value & 0xFF);
            if (isVisibleRoom(frame, room)) {
                rooms.add(room);
            }
        }
        return rooms.stream().map(room -> {
            String name = frame.roomNames.get(room);
            return name == null ? Integer.toString(room) : room + "=" + name;
        }).collect(java.util.stream.Collectors.joining(", "));
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static int color(Frame frame, int pixel) {
        if (pixel == 0) {
            return 0xF3F4F6;
        }
        int room = room(frame, pixel);
        boolean wall = frame.version3 ? room == 31 && ((pixel >> 5) & 3) > 0
                : frame.frameMap ? room == 63 : (pixel & 0x80) != 0;
        if (wall) {
            return 0x475569;
        }
        if (room == 0 || room >= 61) {
            return 0xCBD5E1;
        }
        return Color.HSBtoRGB((room * 0.137f) % 1, 0.25f, 0.9f) & 0xFFFFFF;
    }

    private static int room(Frame frame, int pixel) {
        return frame.version3 ? pixel & 0x1F : frame.frameMap ? pixel >> 2 : pixel & 0x3F;
    }

    private static boolean isVisibleRoom(Frame frame, int room) {
        return room > 0 && room < 61 && (frame.roomNames.isEmpty() || frame.roomNames.containsKey(room));
    }

    private static void marker(Frame frame, java.awt.Graphics2D graphics, StringBuilder svg, int px, int py, int color,
            String label) {
        double x = x(frame, px);
        double y = y(frame, py);
        if (px == 32767 || py == 32767 || x < 0 || y < 0 || x >= frame.width || y >= frame.height) {
            return;
        }
        graphics.setColor(new Color(color));
        graphics.fill(new java.awt.geom.Ellipse2D.Double(x - 1.5, y - 1.5, 3, 3));
        svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y).append("\" r=\"1.5\" fill=\"#")
                .append(String.format("%06x", color)).append("\"><title>").append(label).append("</title></circle>");
    }

    private static double x(Frame frame, int x) {
        return (x - (double) frame.left) / frame.grid;
    }

    private static double y(Frame frame, int y) {
        return frame.height - 1 - (y - (double) frame.top) / frame.grid;
    }

    private static int number(byte[] data, int offset) {
        return (short) ((data[offset] & 0xFF) | (data[offset + 1] << 8));
    }
}
