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
package org.openhab.binding.mqtt.awtrixlight.internal.app;

import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.MINUSONE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.ONEHUNDRED;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.awtrixlight.internal.Helper;

/**
 * The {@link AwtrixApp} is the representation of the current app configuration and provides a method to create a config
 * string for the clock.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AwtrixApp {

    public static final String DEFAULT_TEXT = "New Awtrix App";
    public static final BigDecimal DEFAULT_TEXTCASE = BigDecimal.ZERO;
    public static final boolean DEFAULT_TOPTEXT = false;
    public static final BigDecimal DEFAULT_TEXTOFFSET = BigDecimal.ZERO;
    public static final boolean DEFAULT_CENTER = true;
    public static final BigDecimal[] DEFAULT_COLOR = {};
    public static final BigDecimal[] DEFAULT_GRADIENT = {};
    public static final BigDecimal DEFAULT_BLINKTEXT = BigDecimal.ZERO;
    public static final BigDecimal DEFAULT_FADETEXT = BigDecimal.ZERO;
    public static final BigDecimal[] DEFAULT_BACKGROUND = {};
    public static final boolean DEFAULT_RAINBOW = false;
    public static final String DEFAULT_ICON = "None";
    public static final BigDecimal DEFAULT_PUSHICON = BigDecimal.ZERO;
    public static final BigDecimal DEFAULT_DURATION = new BigDecimal(7);
    public static final BigDecimal[] DEFAULT_LINE = {};
    public static final BigDecimal[] DEFAULT_BAR = {};
    public static final boolean DEFAULT_AUTOSCALE = true;
    public static final BigDecimal DEFAULT_PROGRESS = MINUSONE;
    public static final BigDecimal[] DEFAULT_PROGRESSC = {};
    public static final BigDecimal[] DEFAULT_PROGRESSBC = {};
    public static final BigDecimal DEFAULT_SCROLLSPEED = ONEHUNDRED;
    public static final String DEFAULT_EFFECT = "None";
    public static final BigDecimal DEFAULT_EFFECTSPEED = BigDecimal.ONE;
    public static final String DEFAULT_EFFECTPALETTE = "None";
    public static final boolean DEFAULT_EFFECTBLEND = true;

    private String text = DEFAULT_TEXT;
    private BigDecimal textCase = DEFAULT_TEXTCASE;
    private boolean topText = DEFAULT_TOPTEXT;
    private BigDecimal textOffset = DEFAULT_TEXTOFFSET;
    private boolean center = DEFAULT_CENTER;
    private BigDecimal[] color = DEFAULT_COLOR;
    private BigDecimal[] gradient = DEFAULT_GRADIENT;
    private BigDecimal blinkText = DEFAULT_BLINKTEXT;
    private BigDecimal fadeText = DEFAULT_FADETEXT;
    private BigDecimal[] background = DEFAULT_BACKGROUND;
    private boolean rainbow = DEFAULT_RAINBOW;
    private String icon = DEFAULT_ICON;
    private BigDecimal pushIcon = DEFAULT_PUSHICON;
    private BigDecimal duration = DEFAULT_DURATION;
    private BigDecimal[] line = DEFAULT_LINE;
    private BigDecimal[] bar = DEFAULT_BAR;
    private boolean autoscale = DEFAULT_AUTOSCALE;
    private BigDecimal progress = DEFAULT_PROGRESS;
    private BigDecimal[] progressC = DEFAULT_PROGRESSC;
    private BigDecimal[] progressBC = DEFAULT_PROGRESSBC;
    private BigDecimal scrollSpeed = DEFAULT_SCROLLSPEED;
    private String effect = DEFAULT_EFFECT;

    // effectSettings properties
    private BigDecimal effectSpeed = DEFAULT_EFFECTSPEED;
    private String effectPalette = DEFAULT_EFFECTPALETTE;
    private boolean effectBlend = DEFAULT_EFFECTBLEND;

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
        this.bar = getNumberArrayValue(params, "bar", DEFAULT_BAR);
        this.autoscale = getBoolValue(params, "autoscale", DEFAULT_AUTOSCALE);
        this.progress = getNumberValue(params, "progress", DEFAULT_PROGRESS);
        this.progressC = getNumberArrayValue(params, "progressC", DEFAULT_PROGRESSC);
        this.progressBC = getNumberArrayValue(params, "progressBC", DEFAULT_PROGRESSBC);
        this.scrollSpeed = getNumberValue(params, "scrollSpeed", DEFAULT_SCROLLSPEED);
        this.effect = getStringValue(params, "effect", DEFAULT_EFFECT);

        Map<String, Object> defaultEffectSettings = new HashMap<String, Object>();
        defaultEffectSettings.put("effectSpeed", DEFAULT_EFFECTSPEED);
        defaultEffectSettings.put("effectPalette", DEFAULT_EFFECTPALETTE);
        defaultEffectSettings.put("effectBlend", DEFAULT_EFFECTBLEND);
        Map<String, Object> effectSettingsValues = getEffectSettingsValues(params, defaultEffectSettings);
        this.effectSpeed = getNumberValue(effectSettingsValues, "effectSpeed", DEFAULT_EFFECTSPEED);
        this.effectPalette = getStringValue(effectSettingsValues, "effectPalette", DEFAULT_EFFECTPALETTE);
        this.effectBlend = getBoolValue(params, "effectBlend", DEFAULT_EFFECTBLEND);
    }

    public String getAppConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("text", this.text);
        fields.put("textCase", this.textCase);
        fields.put("topText", this.topText);
        fields.put("textOffset", this.textOffset);
        fields.put("center", this.center);
        fields.putAll(getColorConfig());
        fields.putAll(getTextEffectConfig());
        fields.putAll(getBackgroundConfig());
        fields.putAll(getIconConfig());
        fields.put("duration", this.duration);
        fields.putAll(getGraphConfig());
        fields.putAll(getProgressConfig());
        if (this.scrollSpeed.compareTo(BigDecimal.ZERO) == 0) {
            fields.put("noScroll", true);
        } else {
            fields.put("scrollSpeed", this.scrollSpeed);
        }
        fields.putAll(getEffectConfig());
        return Helper.encodeJson(fields);
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BigDecimal getTextCase() {
        return this.textCase;
    }

    public void setTextCase(BigDecimal textCase) {
        this.textCase = textCase;
    }

    public Boolean getTopText() {
        return this.topText;
    }

    public void setTopText(Boolean topText) {
        this.topText = topText;
    }

    public BigDecimal getTextOffset() {
        return this.textOffset;
    }

    public void setTextOffset(BigDecimal textOffset) {
        this.textOffset = textOffset;
    }

    public Boolean getCenter() {
        return this.center;
    }

    public void setCenter(Boolean center) {
        this.center = center;
    }

    public BigDecimal[] getColor() {
        return this.color;
    }

    public void setColor(BigDecimal[] color) {
        this.color = color;
    }

    public BigDecimal[] getGradient() {
        return this.gradient;
    }

    public void setGradient(BigDecimal[] gradient) {
        this.gradient = gradient;
    }

    public BigDecimal getBlinkText() {
        return this.blinkText;
    }

    public void setBlinkText(BigDecimal blinkText) {
        this.blinkText = blinkText;
    }

    public BigDecimal getFadeText() {
        return this.fadeText;
    }

    public void setFadeText(BigDecimal fadeText) {
        this.fadeText = fadeText;
    }

    public BigDecimal[] getBackground() {
        return this.background;
    }

    public void setBackground(BigDecimal[] background) {
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

    public BigDecimal getPushIcon() {
        return this.pushIcon;
    }

    public void setPushIcon(BigDecimal pushIcon) {
        this.pushIcon = pushIcon;
    }

    public BigDecimal getDuration() {
        return this.duration;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
    }

    public BigDecimal[] getLine() {
        return this.line;
    }

    public void setLine(BigDecimal[] line) {
        this.line = line;
    }

    public BigDecimal[] getBar() {
        return this.bar;
    }

    public void setBar(BigDecimal[] bar) {
        this.bar = bar;
    }

    public Boolean getAutoscale() {
        return this.autoscale;
    }

    public void setAutoscale(Boolean autoscale) {
        this.autoscale = autoscale;
    }

    public BigDecimal getProgress() {
        return this.progress;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    public BigDecimal[] getProgressC() {
        return this.progressC;
    }

    @Override
    public String toString() {
        return "AwtrixApp [text=" + text + ", textCase=" + textCase + ", topText=" + topText + ", textOffset="
                + textOffset + ", center=" + center + ", color=" + Arrays.toString(color) + ", gradient="
                + Arrays.toString(gradient) + ", blinkText=" + blinkText + ", fadeText=" + fadeText + ", background="
                + Arrays.toString(background) + ", rainbow=" + rainbow + ", icon=" + icon + ", pushIcon=" + pushIcon
                + ", duration=" + duration + ", line=" + Arrays.toString(line) + ", bar=" + Arrays.toString(bar)
                + ", autoscale=" + autoscale + ", progress=" + progress + ", progressC=" + Arrays.toString(progressC)
                + ", progressBC=" + Arrays.toString(progressBC) + ", scrollSpeed=" + scrollSpeed + ", effect=" + effect
                + ", effectSpeed=" + effectSpeed + ", effectPalette=" + effectPalette + ", effectBlend=" + effectBlend
                + "]";
    }

    public BigDecimal[] setProgressC() {
        return this.progressC;
    }

    public void setProgressC(BigDecimal[] progressC) {
        this.progressC = progressC;
    }

    public BigDecimal[] getProgressBC() {
        return this.progressBC;
    }

    public void setProgressBC(BigDecimal[] progressBC) {
        this.progressBC = progressBC;
    }

    public BigDecimal getScrollSpeed() {
        return this.scrollSpeed;
    }

    public void setScrollSpeed(BigDecimal scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public String getEffect() {
        return this.effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public BigDecimal getEffectSpeed() {
        return this.effectSpeed;
    }

    public void setEffectSpeed(BigDecimal effectSpeed) {
        this.effectSpeed = effectSpeed;
    }

    public String getEffectPalette() {
        return this.effectPalette;
    }

    public void setEffectPalette(String effectPalette) {
        this.effectPalette = effectPalette;
    }

    public Boolean getEffectBlend() {
        return this.effectBlend;
    }

    public void setEffectBlend(Boolean effectBlend) {
        this.effectBlend = effectBlend;
    }

    private boolean getBoolValue(Map<String, Object> params, String key, boolean defaultValue) {
        if (params.containsKey(key)) {
            Object value = params.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return defaultValue;
    }

    private BigDecimal getNumberValue(Map<String, Object> params, String key, BigDecimal defaultValue) {
        if (params.containsKey(key)) {
            Object value = params.get(key);
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }
        }
        return defaultValue;
    }

    private BigDecimal[] getNumberArrayValue(Map<String, Object> params, String key, BigDecimal[] defaultValue) {
        if (params.containsKey(key)) {
            Object value = params.get(key);
            if (value instanceof BigDecimal[]) {
                return (BigDecimal[]) value;
            }
        }
        return defaultValue;
    }

    private BigDecimal[] getGradientValue(Map<String, Object> params, BigDecimal[] defaultValue) {
        if (params.containsKey("gradient")) {
            Object value = params.get("gradient");
            if (value instanceof BigDecimal[][] && ((BigDecimal[][]) value).length == 2) {
                BigDecimal[] gradientColor = ((BigDecimal[][]) value)[1];
                if (gradientColor.length == 3) {
                    return gradientColor;
                }
            }
        }
        return defaultValue;
    }

    private Map<String, Object> getEffectSettingsValues(Map<String, Object> params, Map<String, Object> defaultValues) {
        if (params.containsKey("effectSettings")) {
            Object value = params.get("effectSettings");
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                if (map.containsKey("speed")) {
                    Object speed = map.get("speed");
                    if (speed != null) {
                        defaultValues.put("effectSpeed", speed);
                    }
                }
                if (map.containsKey("palette")) {
                    Object palette = map.get("palette");
                    if (palette != null) {
                        defaultValues.put("effectPalette", palette);
                    }
                }
                if (map.containsKey("blend")) {
                    Object blend = map.get("blend");
                    if (blend != null) {
                        defaultValues.put("effectBlend", blend);
                    }
                }
            }
        }
        return defaultValues;
    }

    private String getStringValue(Map<String, Object> params, String key, String defaultValue) {
        if (params.containsKey(key)) {
            Object value = params.get(key);
            if (value instanceof String) {
                return (String) value;
            }
        }
        return defaultValue;
    }

    private Map<String, Object> getColorConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        if (this.gradient.length != 3) {
            if (this.color.length == 3) {
                fields.put("color", this.color);
            }
        } else {
            if (this.color.length == 3) {
                BigDecimal[][] gradientColors = { this.color, this.gradient };
                fields.put("gradient", gradientColors);
            } else {
                fields.put("color", this.gradient);
            }
        }
        return fields;
    }

    private Map<String, Object> getTextEffectConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        if (this.color.length == 0 || this.gradient.length == 0) {
            if (this.blinkText.compareTo(BigDecimal.ZERO) > 0) {
                fields.put("blinkText", this.blinkText);
            } else if (this.fadeText.compareTo(BigDecimal.ZERO) > 0) {
                fields.put("fadeText", this.fadeText);
            } else if (this.rainbow) {
                fields.put("rainbow", this.rainbow);
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
        BigDecimal[] data = null;
        if (this.bar.length > 0) {
            graphType = "bar";
            if ("None".equals(this.icon)) {
                data = leftTrim(this.bar, 16);
            } else {
                data = leftTrim(this.bar, 11);
            }
        } else if (this.line.length > 0) {
            graphType = "line";
            if ("None".equals(this.icon)) {
                data = leftTrim(this.line, 16);
            } else {
                data = leftTrim(this.line, 11);
            }
        }
        if (graphType != null && data != null) {
            fields.put(graphType, data);
            fields.put("autoscale", this.autoscale);
        }
        return fields;
    }

    private BigDecimal[] leftTrim(BigDecimal[] data, int length) {
        if (length < data.length) {
            BigDecimal[] trimmed = new BigDecimal[length];
            for (int i = data.length - length; i <= data.length; i++) {
                trimmed[i - data.length - length] = data[i];
            }
            return trimmed;
        }
        return data;
    }

    private Map<String, Object> getProgressConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        if (progress.compareTo(MINUSONE) > 0 && progress.compareTo(ONEHUNDRED) <= 0) {
            fields.put("progress", this.progress);
            if (this.progressC.length == 3) {
                fields.put("progressC", this.progressC);
            }
            if (this.progressBC.length == 3) {
                fields.put("progressBC", this.progressBC);
            }
        }
        return fields;
    }

    private Map<String, Object> getEffectConfig() {
        Map<String, Object> fields = new HashMap<String, Object>();
        Map<String, Object> effectSettings = new HashMap<String, Object>();
        fields.put("effect", this.effect);
        if (!"None".equals(this.effect)) {
            if (this.effectSpeed.compareTo(MINUSONE) > 0) {
                effectSettings.put("speed", this.effectSpeed);
            }
            effectSettings.put("palette", this.effectPalette);
            effectSettings.put("blend", this.effectBlend);
            fields.put("effectSettings", effectSettings);
        }
        return fields;
    }
}
