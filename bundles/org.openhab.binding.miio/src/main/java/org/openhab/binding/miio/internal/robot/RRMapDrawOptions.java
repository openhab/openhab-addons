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
package org.openhab.binding.miio.internal.robot;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class provides the configuration for the vacuum map drawing
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class RRMapDrawOptions {

    private static final Color COLOR_MAP_INSIDE = new Color(32, 115, 185);
    private static final Color COLOR_MAP_OUTSIDE = new Color(19, 87, 148);
    private static final Color COLOR_MAP_WALL = new Color(100, 196, 254);
    private static final Color COLOR_CARPET = new Color(0xDF, 0xDF, 0xDF, 0xA0);
    private static final Color COLOR_GREY_WALL = new Color(93, 109, 126);
    private static final Color COLOR_PATH = new Color(147, 194, 238);
    private static final Color COLOR_ZONES = new Color(0xAD, 0xD8, 0xFF, 0x8F);
    private static final Color COLOR_NO_GO_ZONES = new Color(255, 33, 55, 127);
    private static final Color COLOR_CHARGER_HALO = new Color(0x66, 0xfe, 0xda, 0x7f);
    private static final Color COLOR_ROBO = new Color(75, 235, 149);
    private static final Color COLOR_SCAN = new Color(0xDF, 0xDF, 0xDF);
    private static final Color ROOM1 = new Color(240, 178, 122);
    private static final Color ROOM2 = new Color(133, 193, 233);
    private static final Color ROOM3 = new Color(217, 136, 128);
    private static final Color ROOM4 = new Color(52, 152, 219);
    private static final Color ROOM5 = new Color(205, 97, 85);
    private static final Color ROOM6 = new Color(243, 156, 18);
    private static final Color ROOM7 = new Color(88, 214, 141);
    private static final Color ROOM8 = new Color(245, 176, 65);
    private static final Color ROOM9 = new Color(0xFc, 0xD4, 0x51);
    private static final Color ROOM10 = new Color(72, 201, 176);
    private static final Color ROOM11 = new Color(84, 153, 199);
    private static final Color ROOM12 = new Color(255, 213, 209);
    private static final Color ROOM13 = new Color(228, 228, 215);
    private static final Color ROOM14 = new Color(82, 190, 128);
    private static final Color ROOM15 = new Color(72, 201, 176);
    private static final Color ROOM16 = new Color(165, 105, 189);
    private static final Color[] ROOM_COLORS = { ROOM1, ROOM2, ROOM3, ROOM4, ROOM5, ROOM6, ROOM7, ROOM8, ROOM9, ROOM10,
            ROOM11, ROOM12, ROOM13, ROOM14, ROOM15, ROOM16 };

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Color.class, new JsonSerializer<Color>() {
                @Override
                public JsonElement serialize(Color src, @Nullable Type typeOfSrc,
                        @Nullable JsonSerializationContext context) {
                    JsonObject colorSave = new JsonObject();
                    colorSave.addProperty("red", src.getRed());
                    colorSave.addProperty("green", src.getGreen());
                    colorSave.addProperty("blue", src.getBlue());
                    colorSave.addProperty("alpha", src.getAlpha());
                    return colorSave;
                }
            }).registerTypeAdapter(Color.class, new JsonDeserializer<Color>() {
                @Override
                @Nullable
                public Color deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
                        @Nullable JsonDeserializationContext context) throws JsonParseException {
                    if (json == null) {
                        throw new JsonParseException("missing json text");
                    }
                    JsonObject colorSave = json.getAsJsonObject();
                    Color color = new Color(colorSave.get("red").getAsInt(), colorSave.get("green").getAsInt(),
                            colorSave.get("blue").getAsInt(), colorSave.get("alpha").getAsInt());
                    return color;
                }
            }).create();

    @SerializedName("colorMapInside")
    @Expose
    private Color colorMapInside = COLOR_MAP_INSIDE;
    @SerializedName("colorMapOutside")
    @Expose
    private Color colorMapOutside = COLOR_MAP_OUTSIDE;
    @SerializedName("colorMapWall")
    @Expose
    private Color colorMapWall = COLOR_MAP_WALL;
    @SerializedName("colorCarpet")
    @Expose
    private Color colorCarpet = COLOR_CARPET;
    @SerializedName("colorGreyWall")
    @Expose
    private Color colorGreyWall = COLOR_GREY_WALL;
    @SerializedName("colorPath")
    @Expose
    private Color colorPath = COLOR_PATH;
    @SerializedName("colorZones")
    @Expose
    private Color colorZones = COLOR_ZONES;
    @SerializedName("colorNoGoZones")
    @Expose
    private Color colorNoGoZones = COLOR_NO_GO_ZONES;
    @SerializedName("colorChargerHalo")
    @Expose
    private Color colorChargerHalo = COLOR_CHARGER_HALO;
    @SerializedName("colorRobo")
    @Expose
    private Color colorRobo = COLOR_ROBO;
    @SerializedName("colorScan")
    @Expose
    private Color colorScan = COLOR_SCAN;

    @SerializedName("roomColors")
    @Expose
    private Color[] roomColors = ROOM_COLORS;

    @SerializedName("showLogo")
    @Expose
    private boolean showLogo = true;
    @SerializedName("text")
    @Expose
    private String text = "Openhab rocks your Xiaomi vacuum!";
    @SerializedName("textFontSize")
    @Expose
    private int textFontSize = 12;
    @SerializedName("scale")
    @Expose
    private float scale = 2.0f;
    @SerializedName("cropBorder")
    @Expose
    private int cropBorder = 10;

    public Color getColorMapInside() {
        return colorMapInside;
    }

    public void setColorMapInside(Color colorMapInside) {
        this.colorMapInside = colorMapInside;
    }

    public Color getColorMapOutside() {
        return colorMapOutside;
    }

    public void setColorMapOutside(Color colorMapOutside) {
        this.colorMapOutside = colorMapOutside;
    }

    public Color getColorMapWall() {
        return colorMapWall;
    }

    public void setColorMapWall(Color colorMapWall) {
        this.colorMapWall = colorMapWall;
    }

    public Color getColorCarpet() {
        return colorCarpet;
    }

    public void setColorCarpet(Color colorCarpet) {
        this.colorCarpet = colorCarpet;
    }

    public Color getColorGreyWall() {
        return colorGreyWall;
    }

    public void setColorGreyWall(Color colorGreyWall) {
        this.colorGreyWall = colorGreyWall;
    }

    public Color getColorPath() {
        return colorPath;
    }

    public void setColorPath(Color colorPath) {
        this.colorPath = colorPath;
    }

    public Color getColorZones() {
        return colorZones;
    }

    public void setColorZones(Color colorZones) {
        this.colorZones = colorZones;
    }

    public Color getColorNoGoZones() {
        return colorNoGoZones;
    }

    public void setColorNoGoZones(Color colorNoGoZones) {
        this.colorNoGoZones = colorNoGoZones;
    }

    public Color getColorChargerHalo() {
        return colorChargerHalo;
    }

    public void setColorChargerHalo(Color colorChargerHalo) {
        this.colorChargerHalo = colorChargerHalo;
    }

    public Color getColorRobo() {
        return colorRobo;
    }

    public void setColorRobo(Color colorRobo) {
        this.colorRobo = colorRobo;
    }

    public Color getColorScan() {
        return colorScan;
    }

    public void setColorScan(Color colorScan) {
        this.colorScan = colorScan;
    }

    public Color[] getRoomColors() {
        return roomColors;
    }

    public void setRoomColors(Color[] roomColors) {
        this.roomColors = roomColors;
    }

    public boolean isShowLogo() {
        return showLogo;
    }

    public void setShowLogo(boolean showLogo) {
        this.showLogo = showLogo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public final int getTextFontSize() {
        return textFontSize;
    }

    public final void setTextFontSize(int textFontSize) {
        this.textFontSize = textFontSize;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public final int getCropBorder() {
        return cropBorder;
    }

    public final void setCropBorder(int cropBorder) {
        this.cropBorder = cropBorder;
    }

    public static void writeOptionsToFile(RRMapDrawOptions options, String fileName, Logger logger) {
        String json = GSON.toJson(options, RRMapDrawOptions.class);
        try (PrintWriter pw = new PrintWriter(fileName)) {
            pw.println(json);
            logger.debug("Vacuum map draw options file created: {}", fileName);
        } catch (FileNotFoundException e) {
            logger.info("Error writing Vacuum map draw options file: {}", e.getMessage());
        }
    }

    public static RRMapDrawOptions getOptionsFromFile(String fileName, Logger logger) {
        try {
            RRMapDrawOptions options = GSON.fromJson(new FileReader(fileName), RRMapDrawOptions.class);
            return options;
        } catch (FileNotFoundException e) {
            logger.debug("Vacuum map draw options file {} not found. Using defaults", fileName);
            return new RRMapDrawOptions();
        } catch (JsonParseException e) {
            logger.info("Error reading vacuum map draw options file {}: {}", fileName, e.getMessage());
        }
        logger.info("Write default map draw options to {}", fileName);
        RRMapDrawOptions options = new RRMapDrawOptions();
        writeOptionsToFile(options, fileName, logger);
        return new RRMapDrawOptions();
    }
}
