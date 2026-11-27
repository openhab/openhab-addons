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
package org.openhab.binding.matter.internal.bridge.devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.bridge.AttributeState;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ColorControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.util.ColorUtil;

/**
 * The {@link ColorDevice} is a device that represents a Color Light.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ColorDevice extends BaseDevice {
    // how long to wait (max) for the device to turn on before updating the HSB values
    private static final int ONOFF_DELAY_MILLIS = 500;
    // Level 0 is not permitted with the Lighting feature, so an off light reports the minimum instead
    private static final int MIN_LEVEL = 1;
    private static final double XY_SCALE = 65536.0;
    private static final int MAX_XY = 0xFEFF;
    // degrees of hue and percent of saturation
    private static final double COLOR_TOLERANCE = 2;
    // the onFuture is used to wait for the device to turn on before updating the HSB values
    private CompletableFuture<Void> onFuture = CompletableFuture.completedFuture(null);
    // the lastH, lastS are used to store the last HSB values as they come in from the device
    private @Nullable DecimalType lastH;
    private @Nullable PercentType lastS;
    private @Nullable PercentType lastB;
    private @Nullable Double lastX;
    private @Nullable Double lastY;
    // the color the item last reported, to tell color changes from brightness changes
    private volatile @Nullable HSBType reportedColor;
    // the color the last Matter command sent to the item, so its echo keeps the color mode that command set
    private volatile @Nullable HSBType matterColor;

    public ColorDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "ColorLight";
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        if (primaryItem instanceof ColorItem colorItem) {
            HSBType hsbType = colorItem.getStateAs(HSBType.class);
            if (hsbType == null) {
                hsbType = new HSBType();
            }
            reportedColor = hsbType;
            Integer currentHue = toHue(hsbType.getHue());
            Integer currentSaturation = toSaturation(hsbType.getSaturation());
            attributeMap.put(LevelControlCluster.CLUSTER_PREFIX + "." + LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL,
                    toBrightness(hsbType.getBrightness()));
            attributeMap.put(ColorControlCluster.CLUSTER_PREFIX + "." + ColorControlCluster.ATTRIBUTE_CURRENT_HUE,
                    currentHue);
            attributeMap.put(
                    ColorControlCluster.CLUSTER_PREFIX + "." + ColorControlCluster.ATTRIBUTE_CURRENT_SATURATION,
                    currentSaturation);
            attributeMap.put(OnOffCluster.CLUSTER_PREFIX + "." + OnOffCluster.ATTRIBUTE_ON_OFF,
                    hsbType.getBrightness().intValue() > 0);
            int[] xy = toXy(hsbType);
            attributeMap.put(ColorControlCluster.CLUSTER_PREFIX + "." + ColorControlCluster.ATTRIBUTE_CURRENT_X, xy[0]);
            attributeMap.put(ColorControlCluster.CLUSTER_PREFIX + "." + ColorControlCluster.ATTRIBUTE_CURRENT_Y, xy[1]);
        }

        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        Double value = Double.valueOf(0);
        if (data instanceof Double d) {
            value = d;
        }
        switch (attributeName) {
            case OnOffCluster.ATTRIBUTE_ON_OFF:
                updateOnOff(Boolean.valueOf(data.toString()));
                break;
            case LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL:
                updateBrightness(ValueUtils.levelToPercentWhenOn(value.intValue()));
                break;
            // currentHue and currentSaturation will always be updated together sequentially in the matter.js bridge
            // code
            case ColorControlCluster.ATTRIBUTE_CURRENT_HUE:
                float hueValue = value == 0 ? 0.0f : value.floatValue() * 360.0f / 254.0f;
                lastH = new DecimalType(Float.valueOf(hueValue).toString());
                updateHueSaturation();
                break;
            case ColorControlCluster.ATTRIBUTE_CURRENT_SATURATION:
                float saturationValue = value == 0 ? 0.0f : value.floatValue() / 254.0f * 100.0f;
                lastS = new PercentType(Float.valueOf(saturationValue).toString());
                updateHueSaturation();
                break;
            // currentX and currentY are also sent together, X first
            case ColorControlCluster.ATTRIBUTE_CURRENT_X:
                lastX = value / XY_SCALE;
                break;
            case ColorControlCluster.ATTRIBUTE_CURRENT_Y:
                Double x = lastX;
                lastX = null;
                if (x != null) {
                    HSBType xyHSB = ColorUtil.xyToHsb(new double[] { x, value / XY_SCALE });
                    lastH = xyHSB.getHue();
                    lastS = xyHSB.getSaturation();
                    updateHueSaturation();
                }
                break;
            case ColorControlCluster.ATTRIBUTE_COLOR_TEMPERATURE_MIREDS:
                Double kelvin = 1e6 / (Double) data;
                HSBType ctHSB = ColorUtil.xyToHsb(ColorUtil.kelvinToXY(Math.max(1000, Math.min(kelvin, 10000))));
                lastH = ctHSB.getHue();
                lastS = ctHSB.getSaturation();
                updateHueSaturation();
                break;
            default:
                break;
        }
    }

    @Override
    public void updateState(Item item, State state) {
        if (state instanceof HSBType hsb) {
            boolean on = hsb.getBrightness().intValue() > 0;
            if (on) {
                // since we are on, complete the future
                completeOnFuture();
                lastB = null; // reset the cached brightness
            }
            List<AttributeState> states = new ArrayList<>();
            states.add(new AttributeState(LevelControlCluster.CLUSTER_PREFIX,
                    LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL, on ? toBrightness(hsb.getBrightness()) : MIN_LEVEL));
            states.add(new AttributeState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF, on));
            states.add(new AttributeState(ColorControlCluster.CLUSTER_PREFIX, ColorControlCluster.ATTRIBUTE_CURRENT_HUE,
                    toHue(hsb.getHue())));
            states.add(new AttributeState(ColorControlCluster.CLUSTER_PREFIX,
                    ColorControlCluster.ATTRIBUTE_CURRENT_SATURATION, toSaturation(hsb.getSaturation())));
            int[] xy = toXy(hsb);
            states.add(new AttributeState(ColorControlCluster.CLUSTER_PREFIX, ColorControlCluster.ATTRIBUTE_CURRENT_X,
                    xy[0]));
            states.add(new AttributeState(ColorControlCluster.CLUSTER_PREFIX, ColorControlCluster.ATTRIBUTE_CURRENT_Y,
                    xy[1]));
            HSBType reported = reportedColor;
            reportedColor = hsb;
            // A color set in openHAB is a hue and saturation, so apps should show it that way. The echo of a color
            // an app set keeps the mode the app chose, such as a color temperature.
            if ((reported == null || !sameColor(hsb, reported)) && !isMatterColor(hsb)) {
                states.add(
                        new AttributeState(ColorControlCluster.CLUSTER_PREFIX, ColorControlCluster.ATTRIBUTE_COLOR_MODE,
                                ColorControlCluster.ColorModeEnum.CURRENT_HUE_AND_CURRENT_SATURATION.getValue()));
                states.add(new AttributeState(ColorControlCluster.CLUSTER_PREFIX,
                        ColorControlCluster.ATTRIBUTE_ENHANCED_COLOR_MODE,
                        ColorControlCluster.EnhancedColorModeEnum.CURRENT_HUE_AND_CURRENT_SATURATION.getValue()));
            }
            setEndpointStates(states);
        }
    }

    private void updateBrightness(PercentType brightness) {
        if (primaryItem instanceof ColorItem colorItem) {
            lastB = brightness.intValue() > 0 ? brightness : null;
            colorItem.send(brightness, MATTER_SOURCE);
        }
    }

    private synchronized void updateOnOff(boolean onOff) {
        if (primaryItem instanceof ColorItem colorItem) {
            if (!onFuture.isDone()) {
                onFuture.cancel(true);
            }
            // if we are turning on, we need to wait for the device to turn on before updating the HSB due to brightness
            // being 0 until the device has turned on (and we need to query this state)
            if (onOff) {
                onFuture = new CompletableFuture<>();
                onFuture.orTimeout(ONOFF_DELAY_MILLIS, TimeUnit.MILLISECONDS);
                onFuture.whenComplete((v, ex) -> {
                    if (lastH != null && lastS != null) {
                        // if these are not null, we need to update the HSB now
                        updateHSB();
                    }
                });
            }
            colorItem.send(OnOffType.from(onOff), MATTER_SOURCE);
        }
    }

    private synchronized void updateHSB() {
        if (primaryItem instanceof ColorItem colorItem) {
            HSBType hsb = colorItem.getStateAs(HSBType.class);
            if (hsb == null) {
                return;
            }

            DecimalType lastH = this.lastH;
            PercentType lastS = this.lastS;
            PercentType lastB = this.lastB;

            if (lastH == null && lastS == null) {
                return;
            }

            DecimalType h = hsb.getHue();
            PercentType s = hsb.getSaturation();
            PercentType b = hsb.getBrightness();

            if (lastH != null) {
                h = lastH;
            }
            if (lastS != null) {
                s = lastS;
            }
            if (lastB != null) {
                b = lastB;
            }
            // the device is still off but should not be, just set the brightness to 100%
            if (b.intValue() == 0) {
                b = new PercentType(100);
            }
            HSBType color = new HSBType(h, s, b);
            matterColor = color;
            colorItem.send(color, MATTER_SOURCE);
        }
        this.lastH = null;
        this.lastS = null;
    }

    private void updateHueSaturation() {
        if (onFuture.isDone() && lastH != null && lastS != null) {
            // we have OnOff and both Hue and Saturation so update
            updateHSB();
        }
    }

    private synchronized void completeOnFuture() {
        if (!onFuture.isDone()) {
            onFuture.complete(Void.TYPE.cast(null));
        }
    }

    private Integer toHue(DecimalType h) {
        return Math.round(h.floatValue() * 254.0f / 360.0f);
    }

    private Integer toSaturation(PercentType s) {
        return Math.round(s.floatValue() * 254.0f / 100.0f);
    }

    private Integer toBrightness(PercentType b) {
        return ValueUtils.percentToLevel(b);
    }

    private boolean isMatterColor(HSBType hsb) {
        HSBType color = matterColor;
        return color != null && sameColor(hsb, color);
    }

    /**
     * Whether two colors have the same hue and saturation, allowing for the rounding a device applies
     */
    private static boolean sameColor(HSBType a, HSBType b) {
        double hueDifference = Math.abs(a.getHue().doubleValue() - b.getHue().doubleValue());
        return Math.min(hueDifference, 360 - hueDifference) <= COLOR_TOLERANCE
                && Math.abs(a.getSaturation().doubleValue() - b.getSaturation().doubleValue()) <= COLOR_TOLERANCE;
    }

    /**
     * The color's CIE x and y in Matter's units of 1/65536, at most 0xFEFF. Brightness does not change the color, and
     * an off light would have no color at all, so it is converted at full brightness.
     */
    private int[] toXy(HSBType hsb) {
        double[] xy = ColorUtil.hsbToXY(new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED));
        return new int[] { toXyValue(xy[0]), toXyValue(xy[1]) };
    }

    private int toXyValue(double value) {
        return (int) Math.max(0, Math.min(MAX_XY, Math.round(value * XY_SCALE)));
    }
}
