/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.awtrixlight.internal.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.Helper;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TextSegment} is the representation of a text segment in an App.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
class TextSegment {
    @SerializedName("t")
    public final String text;

    @SerializedName("c")
    public final String color;

    public TextSegment(String text, String color) {
        this.text = text;
        this.color = color;
    }
}

/**
 * The {@link AwtrixApp} is the representation of the current app configuration and provides a method to create a config
 * string for the clock.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AwtrixApp {

    private static final int CLOSING_TAG_LENGTH = 7; // 7 = "</font>".length()

    public static final String DEFAULT_TEXT = "New Awtrix App";
    public static final int DEFAULT_TEXTCASE = 0;
    public static final boolean DEFAULT_TOPTEXT = false;
    public static final int DEFAULT_TEXTOFFSET = 0;
    public static final boolean DEFAULT_CENTER = true;
    public static final int[] DEFAULT_COLOR = { 255, 255, 255 };
    public static final int[][] DEFAULT_GRADIENT = {};
    public static final double DEFAULT_BLINKTEXT = 0;
    public static final double DEFAULT_FADETEXT = 0;
    public static final int[] DEFAULT_BACKGROUND = { 0, 0, 0 };
    public static final boolean DEFAULT_RAINBOW = false;
    public static final String DEFAULT_ICON = "None";
    public static final int DEFAULT_PUSHICON = 0;
    public static final int DEFAULT_DURATION = 7;
    public static final int[] DEFAULT_LINE = {};
    public static final int DEFAULT_LIFETIME = 0;
    public static final int DEFAULT_LIFETIME_MODE = 0;
    public static final int[] DEFAULT_BAR = {};
    public static final boolean DEFAULT_AUTOSCALE = true;
    public static final String DEFAULT_OVERLAY = "Clear";
    public static final int DEFAULT_PROGRESS = -1;
    public static final int[] DEFAULT_PROGRESSC = { 0, 255, 0 };
    public static final int[] DEFAULT_PROGRESSBC = { 255, 255, 255 };
    public static final int DEFAULT_SCROLLSPEED = 100;
    public static final String DEFAULT_EFFECT = "None";
    public static final int DEFAULT_EFFECTSPEED = 100;
    public static final String DEFAULT_EFFECTPALETTE = "None";
    public static final boolean DEFAULT_EFFECTBLEND = true;

    private String text = DEFAULT_TEXT;
    private int textCase = DEFAULT_TEXTCASE;
    private boolean topText = DEFAULT_TOPTEXT;
    private int textOffset = DEFAULT_TEXTOFFSET;
    private boolean center = DEFAULT_CENTER;
    private int[] color = DEFAULT_COLOR;
    private int[][] gradient = DEFAULT_GRADIENT;
    private double blinkText = DEFAULT_BLINKTEXT;
    private double fadeText = DEFAULT_FADETEXT;
    private int[] background = DEFAULT_BACKGROUND;
    private boolean rainbow = DEFAULT_RAINBOW;
    private String icon = DEFAULT_ICON;
    private int pushIcon = DEFAULT_PUSHICON;
    private int duration = DEFAULT_DURATION;
    private int[] line = DEFAULT_LINE;
    private int lifetime = DEFAULT_LIFETIME;
    private int lifetimeMode = DEFAULT_LIFETIME_MODE;
    private int[] bar = DEFAULT_BAR;
    private boolean autoscale = DEFAULT_AUTOSCALE;
    private String overlay = DEFAULT_OVERLAY;
    private int progress = DEFAULT_PROGRESS;
    private int[] progressC = DEFAULT_PROGRESSC;
    private int[] progressBC = DEFAULT_PROGRESSBC;
    private int scrollSpeed = DEFAULT_SCROLLSPEED;
    private String effect = DEFAULT_EFFECT;

    // effectSettings properties
    private Map<String, Object> effectSettings;

    private static final java.util.regex.Pattern TEXT_COLOR_PATTERN = java.util.regex.Pattern
            .compile("color=\"#([0-9A-Fa-f]{6})\"");

    public AwtrixApp() {
        this.effectSettings = new HashMap<String, Object>();
        this.effectSettings.put("speed", DEFAULT_EFFECTSPEED);
        this.effectSettings.put("palette", DEFAULT_EFFECTPALETTE);
        this.effectSettings.put("blend", DEFAULT_EFFECTBLEND);
    }

    public void updateFields(Map<String, Object> params) {
        this.text = getStringValue(params, "text", DEFAULT_TEXT);
        this.textCase = getNumberValue(params, "textCase", DEFAULT_TEXTCASE);
        this.topText = getBoolValue(params, "topText", DEFAULT_TOPTEXT);
        this.textOffset = getNumberValue(params, "textOffset", DEFAULT_TEXTOFFSET);
        this.center = getBoolValue(params, "center", DEFAULT_CENTER);
        this.color = getNumberArrayValue(params, "color", DEFAULT_COLOR);
        this.gradient = getGradientValue(params, DEFAULT_GRADIENT);
        this.blinkText = getNumberValue(params, "blinkText", DEFAULT_BLINKTEXT);
        this.fadeText = getNumberValue(params, "fadeText", DEFAULT_FADETEXT);
        this.background = getNumberArrayValue(params, "background", DEFAULT_BACKGROUND);
        this.rainbow = getBoolValue(params, "rainbow", DEFAULT_RAINBOW);
        this.icon = getStringValue(params, "icon", DEFAULT_ICON);
        this.pushIcon = getNumberValue(params, "pushIcon", DEFAULT_PUSHICON);
        this.duration = getNumberValue(params, "duration", DEFAULT_DURATION);
        this.line = getNumberArrayValue(params, "line", DEFAULT_LINE);
        this.lifetime = getNumberValue(params, "lifetime", DEFAULT_LIFETIME);
        this.lifetimeMode = getNumberValue(params, "lifetimeMode", DEFAULT_LIFETIME_MODE);
        this.bar = getNumberArrayValue(params, "bar", DEFAULT_BAR);
        this.autoscale = getBoolValue(params, "autoscale", DEFAULT_AUTOSCALE);
        this.overlay = getStringValue(params, "overlay", DEFAULT_OVERLAY);
        this.progress = getNumberValue(params, "progress", DEFAULT_PROGRESS);
        this.progressC = getNumberArrayValue(params, "progressC", DEFAULT_PROGRESSC);
        this.progressBC = getNumberArrayValue(params, "progressBC", DEFAULT_PROGRESSBC);
        this.scrollSpeed = getNumberValue(params, "scrollSpeed", DEFAULT_SCROLLSPEED);
        this.effect = getStringValue(params, "effect", DEFAULT_EFFECT);

        Map<String, Object> defaultEffectSettings = new HashMap<String, Object>();
        defaultEffectSettings.put("speed", DEFAULT_EFFECTSPEED);
        defaultEffectSettings.put("palette", DEFAULT_EFFECTPALETTE);
        defaultEffectSettings.put("blend", DEFAULT_EFFECTBLEND);
        this.effectSettings = getEffectSettingsValues(params, defaultEffectSettings);
    }

    public String getAppConfig() {
        Map<String, Object> fields = getAppParams();
        return Helper.encodeJson(fields);
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextCase() {
        return this.textCase;
    }

    public void setTextCase(int textCase) {
        this.textCase = textCase;
    }

    public boolean getTopText() {
        return this.topText;
    }

    public void setTopText(boolean topText) {
        this.topText = topText;
    }

    public int getTextOffset() {
        return this.textOffset;
    }

    public void setTextOffset(int textOffset) {
        this.textOffset = textOffset;
    }

    public Boolean getCenter() {
        return this.center;
    }

    public void setCenter(Boolean center) {
        this.center = center;
    }

    public int[] getColor() {
        return this.color;
    }

    public void setColor(int[] color) {
        this.color = color;
    }

    public int[][] getGradient() {
        return this.gradient;
    }

    public void setGradient(int[][] gradient) {
        this.gradient = gradient;
    }

    public double getBlinkText() {
        return this.blinkText;
    }

    public void setBlinkText(double blinkText) {
        this.blinkText = blinkText;
    }

    public double getFadeText() {
        return this.fadeText;
    }

    public void setFadeText(double fadeText) {
        this.fadeText = fadeText;
    }

    public int[] getBackground() {
        return this.background;
    }

    public void setBackground(int[] background) {
        this.background = background;
    }

    public Boolean getRainbow() {
        return this.rainbow;
    }

    public void setRainbow(Boolean rainbow) {
        this.rainbow = rainbow;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPushIcon() {
        return this.pushIcon;
    }

    public void setPushIcon(int pushIcon) {
        this.pushIcon = pushIcon;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int[] getLine() {
        return this.line;
    }

    public void setLine(int[] line) {
        this.line = line;
    }

    public int getLifetime() {
        return this.lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public int getLifetimeMode() {
        return this.lifetimeMode;
    }

    public void setLifetimeMode(int lifetimeMode) {
        this.lifetimeMode = lifetimeMode;
    }

    public int[] getBar() {
        return this.bar;
    }

    public void setBar(int[] bar) {
        this.bar = bar;
    }

    public Boolean getAutoscale() {
        return this.autoscale;
    }

    public void setAutoscale(Boolean autoscale) {
        this.autoscale = autoscale;
    }

    public String getOverlay() {
        return this.overlay;
    }

    public void setOverlay(String overlay) {
        this.overlay = overlay;
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int[] getProgressC() {
        return this.progressC;
    }

    public void setProgressC(int[] progressC) {
        this.progressC = progressC;
    }

    public int[] getProgressBC() {
        return this.progressBC;
    }

    public void setProgressBC(int[] progressBC) {
        this.progressBC = progressBC;
    }

    public int getScrollSpeed() {
        return this.scrollSpeed;
    }

    public void setScrollSpeed(int scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public String getEffect() {
        return this.effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public Map<String, Object> getEffectSettings() {
        return this.effectSettings;
    }

    public void setEffectSettings(Map<String, Object> effectSettings) {
        this.effectSettings = effectSettings;
    }

    protected String propertiesAsString() {
        return "text=" + text + ", textCase=" + textCase + ", topText=" + topText + ", textOffset=" + textOffset
                + ", center=" + center + ", color=" + Arrays.toString(color) + ", gradient=["
                + Arrays.stream(gradient).map(color -> Arrays.toString(color)).collect(Collectors.joining(", "))
                + "], blinkText=" + blinkText + ", fadeText=" + fadeText + ", background=" + Arrays.toString(background)
                + ", rainbow=" + rainbow + ", icon=" + icon + ", pushIcon=" + pushIcon + ", duration=" + duration
                + ", line=" + Arrays.toString(line) + ", lifetime=" + lifetime + ", lifetimeMode=" + lifetimeMode
                + ", bar=" + Arrays.toString(bar) + ", autoscale=" + autoscale + ", overlay=" + overlay + ", progress="
                + progress + ", progressC=" + Arrays.toString(progressC) + ", progressBC=" + Arrays.toString(progressBC)
                + ", scrollSpeed=" + scrollSpeed + ", effect=" + effect + ", effectSpeed=" + getEffectSpeed()
                + ", effectPalette=" + effectSettings.get("palette") + ", effectBlend=" + effectSettings.get("blend");
    }

    @Override
    public String toString() {
        return "AwtrixApp [" + propertiesAsString() + "]";
    }

    public Map<String, Object> getAppParams() {
        Map<String, Object> fields = new HashMap<String, Object>();
        if (textHasColorTags(this.text)) {
            fields.put("text", this.parseTextSegments());
        } else {
            fields.put("text", this.text);
        }
        fields.putAll(getTextEffectConfig());
        fields.put("textCase", this.textCase);
        fields.put("topText", this.topText);
        fields.put("textOffset", this.textOffset);
        fields.put("center", this.center);
        fields.put("lifetime", this.lifetime);
        fields.put("lifetimeMode", this.lifetimeMode);
        fields.put("overlay", this.overlay);
        fields.putAll(getColorConfig());
        fields.putAll(getBackgroundConfig());
        fields.putAll(getIconConfig());
        fields.put("duration", this.duration);
        fields.putAll(getGraphConfig());
        fields.putAll(getProgressConfig());
        if (this.scrollSpeed == 0) {
            fields.put("noScroll", true);
        } else {
            fields.put("scrollSpeed", this.scrollSpeed);
        }
        fields.putAll(getEffectConfig());
        return fields;
    }

    private boolean getBoolValue(Map<String, Object> params, String key, boolean defaultValue) {
        if (params.containsKey(key)) {
            @Nullable
            Object value = params.get(key);
            if (value instanceof Boolean boolValue) {
                return boolValue;
            }
        }
        return defaultValue;
    }

    private int getNumberValue(Map<String, Object> params, String key, int defaultValue) {
        if (params.containsKey(key)) {
            @Nullable
            Object value = params.get(key);
            if (value instanceof Number numberValue) {
                return numberValue.intValue();
            }
        }
        return defaultValue;
    }

    private double getNumberValue(Map<String, Object> params, String key, double defaultValue) {
        if (params.containsKey(key)) {
            @Nullable
            Object value = params.get(key);
            if (value instanceof Number numberValue) {
                return numberValue.doubleValue();
            }
        }
        return defaultValue;
    }

    private int[] getNumberArrayValue(Map<String, Object> params, String key, int[] defaultValue) {
        if (params.containsKey(key)) {
            @Nullable
            Object value = params.get(key);
            if (value instanceof int[] intArray) {
                return intArray;
            }
        }
        return defaultValue;
    }

    private int[][] getGradientValue(Map<String, Object> params, int[][] defaultValue) {
        if (params.containsKey("gradient")) {
            @Nullable
            Object gradientParam = params.get("gradient");
            // Check if we got a complete gradient with two colors
            if (gradientParam instanceof int[][] gradient) {
                return gradient;
            }
            // Check if we got a single color for the gradient
            if (gradientParam instanceof int[] gradient) {
                @Nullable
                Object colorParam = params.get("color");
                if (colorParam instanceof int[] color) {
                    return new int[][] { color, gradient };
                }
            }
        }
        return defaultValue;
    }

    private Map<String, Object> getEffectSettingsValues(Map<String, Object> params, Map<String, Object> defaultValues) {
        if (params.containsKey("effectSettings")) {
            @Nullable
            Object value = params.get("effectSettings");
            if (value instanceof Map map) {
                if (map.containsKey("speed")) {
                    Object speed = map.get("speed");
                    if (speed != null) {
                        defaultValues.put("speed", speed);
                    }
                }
                if (map.containsKey("palette")) {
                    Object palette = map.get("palette");
                    if (palette != null) {
                        defaultValues.put("palette", palette);
                    }
                }
                if (map.containsKey("blend")) {
                    Object blend = map.get("blend");
                    if (blend != null) {
                        defaultValues.put("blend", blend);
                    }
                }
            }
        }
        return defaultValues;
    }

    private String getStringValue(Map<String, Object> params, String key, String defaultValue) {
        if (params.containsKey(key)) {
            @Nullable
            Object value = params.get(key);
            if (value instanceof String stringValue) {
                return stringValue;
            }
        }
        return defaultValue;
    }

    private Map<String, Object> getColorConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        // When we don't have a valid gradient array, we just provide a color if available
        if (this.gradient.length != 2) {
            if (this.color.length == 3) {
                fields.put("color", this.color);
            }
        } else {
            // Here we have a gradient array. Use it unless it's not a valid gradient
            if (!this.rainbow && this.gradient[0] != null && this.gradient[0].length == 3 && this.gradient[1] != null
                    && this.gradient[1].length == 3) {
                fields.put("gradient", this.gradient);
            } else {
                // If we don't have a valid gradient, we try to provide any color we find
                if (this.color.length == 3) {
                    fields.put("color", this.color);
                } else if (this.gradient[0] != null && this.gradient[0].length == 3) {
                    fields.put("color", this.gradient[0]);
                } else if (this.gradient[1] != null && this.gradient[1].length == 3) {
                    fields.put("color", this.gradient[1]);
                }
            }
        }
        return fields;
    }

    private boolean textHasColorTags(String text) {
        if (text.isEmpty()) {
            return false;
        }
        // Check for the basic structure and use regex to validate color format
        // We need both opening and closing tags, and at least one valid color attribute
        return text.contains("<font") && text.contains("</font>") && TEXT_COLOR_PATTERN.matcher(text).find();
    }

    private static String rgbToHex(int[] rgb) {
        // Ensure values are in 0-255 range
        int r = Math.min(255, Math.max(0, rgb[0]));
        int g = Math.min(255, Math.max(0, rgb[1]));
        int b = Math.min(255, Math.max(0, rgb[2]));

        // Format as 6-digit hex string, padding with zeros if needed
        return String.format("%02x%02x%02x", r, g, b);
    }

    private List<TextSegment> parseTextSegments() {
        List<TextSegment> segments = new ArrayList<>();
        if (this.text.isEmpty()) {
            return segments;
        }

        String remaining = this.text;
        String defaultColor = rgbToHex(this.color);

        while (true) {
            int startTag = remaining.indexOf("<font");
            if (startTag < 0) {
                // No more tags, add remaining text
                if (!remaining.isEmpty()) {
                    segments.add(new TextSegment(remaining, defaultColor));
                }
                break;
            }

            // Add text before the tag
            if (startTag > 0) {
                segments.add(new TextSegment(remaining.substring(0, startTag), defaultColor));
            }

            // Find the end of the opening tag
            int endTag = remaining.indexOf(">", startTag);
            if (endTag < 0) {
                // Malformed tag, add everything and stop
                segments.add(new TextSegment(remaining, defaultColor));
                break;
            }

            // Extract color from tag (or use default)
            String tag = remaining.substring(startTag, endTag + 1);
            String color = extractColor(tag);
            if (color == null) {
                color = defaultColor;
            }

            // Find the closing tag
            int closeTag = remaining.indexOf("</font>", endTag);
            if (closeTag < 0) {
                // No closing tag, add rest with color and stop
                segments.add(new TextSegment(remaining.substring(endTag + 1), color));
                break;
            }

            // Add text between tags
            segments.add(new TextSegment(remaining.substring(endTag + 1, closeTag), color));

            // Move past the closing tag
            remaining = remaining.substring(closeTag + CLOSING_TAG_LENGTH);
        }

        return segments;
    }

    @Nullable
    private String extractColor(String tag) {
        java.util.regex.Matcher matcher = TEXT_COLOR_PATTERN.matcher(tag);
        if (matcher.find()) {
            // Group 1 contains the hex color value (without the #)
            return matcher.group(1).toLowerCase();
        }
        return null;
    }

    private Map<String, Object> getTextEffectConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        // Rainbow makes no sense when the text has color tags so ignore in this case
        if (textHasColorTags(this.text)) {
            if (this.blinkText > 0) {
                fields.put("blinkText", this.blinkText);
            } else if (this.fadeText > 0) {
                fields.put("fadeText", this.fadeText);
            }
        } else {
            // Rainbow overrides gradients, blink and fade because for a user a switch
            // is much easier to use to override these effects than the other way round.
            // This way you can easily switch rainbow on and off and fall back to the
            // gradient, blink or fade effects without setting these to the default
            // values.
            if (this.rainbow) {
                fields.put("rainbow", this.rainbow);
            } else if (Arrays.equals(this.gradient, DEFAULT_GRADIENT)) {
                // Gradient overrides the fade and blink effects. Blink overrides fade
                if (this.blinkText > 0) {
                    fields.put("blinkText", this.blinkText);
                } else if (this.fadeText > 0) {
                    fields.put("fadeText", this.fadeText);
                }
            }
        }
        return fields;
    }

    private Map<String, Object> getBackgroundConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        if (this.background.length == 3) {
            fields.put("background", this.background);
        }
        return fields;
    }

    private Map<String, Object> getIconConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        if (!"None".equals(this.icon)) {
            fields.put("icon", this.icon);
            fields.put("pushIcon", this.pushIcon);
        }
        return fields;
    }

    private Map<String, Object> getGraphConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        String graphType = null;
        int[] data = null;
        if (this.bar.length > 0) {
            graphType = "bar";
            if ("None".equals(this.icon)) {
                data = Helper.leftTrim(this.bar, 16);
            } else {
                data = Helper.leftTrim(this.bar, 11);
            }
        } else if (this.line.length > 0) {
            graphType = "line";
            if ("None".equals(this.icon)) {
                data = Helper.leftTrim(this.line, 16);
            } else {
                data = Helper.leftTrim(this.line, 11);
            }
        }
        if (graphType != null && data != null) {
            fields.put(graphType, data);
            fields.put("autoscale", this.autoscale);
        }
        return fields;
    }

    private Map<String, Object> getProgressConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        if (progress > -1 && progress <= 100) {
            fields.put("progress", this.progress);
            fields.put("progressC", this.progressC);
            fields.put("progressBC", this.progressBC);
        }
        return fields;
    }

    private Map<String, Object> getEffectConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        Map<String, Object> effectSettings = new HashMap<String, Object>();
        fields.put("effect", this.effect);
        if (!"None".equals(this.effect)) {
            if (getEffectSpeed() > -1) {
                effectSettings.put("speed", getEffectSpeed());
            }
            effectSettings.put("palette", getEffectPalette());
            effectSettings.put("blend", getEffectBlend());
            fields.put("effectSettings", effectSettings);
        }
        return fields;
    }

    public int getEffectSpeed() {
        @Nullable
        Object effectSpeed = this.effectSettings.get("speed");
        if (effectSpeed instanceof Number numberValue) {
            return numberValue.intValue();
        } else {
            return DEFAULT_EFFECTSPEED;
        }
    }

    public void setEffectSpeed(int effectSpeed) {
        this.effectSettings.put("speed", effectSpeed);
    }

    public String getEffectPalette() {
        @Nullable
        Object effectPalette = this.effectSettings.get("palette");
        if (effectPalette instanceof String stringValue) {
            return stringValue;
        } else {
            return DEFAULT_EFFECTPALETTE;
        }
    }

    public void setEffectPalette(String effectPalette) {
        this.effectSettings.put("palette", effectPalette);
    }

    public Boolean getEffectBlend() {
        @Nullable
        Object effectBlend = this.effectSettings.get("blend");
        if (effectBlend instanceof Boolean boolValue) {
            return boolValue;
        } else {
            return DEFAULT_EFFECTBLEND;
        }
    }

    public void setEffectBlend(Boolean effectBlend) {
        this.effectSettings.put("blend", effectBlend);
    }
}
