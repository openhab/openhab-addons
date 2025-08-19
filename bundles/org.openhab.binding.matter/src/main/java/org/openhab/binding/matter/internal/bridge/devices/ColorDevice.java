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
    // the onFuture is used to wait for the device to turn on before updating the HSB values
    private CompletableFuture<Void> onFuture = CompletableFuture.completedFuture(null);
    // the lastH, lastS are used to store the last HSB values as they come in from the device
    private @Nullable DecimalType lastH;
    private @Nullable PercentType lastS;
    private @Nullable PercentType lastB;

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
            Integer currentHue = toHue(hsbType.getHue());
            Integer currentSaturation = toSaturation(hsbType.getSaturation());
            Integer currentLevel = toBrightness(hsbType.getBrightness());
            attributeMap.put(LevelControlCluster.CLUSTER_PREFIX + "." + LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL,
                    Math.max(currentLevel, 1));
            attributeMap.put(ColorControlCluster.CLUSTER_PREFIX + "." + ColorControlCluster.ATTRIBUTE_CURRENT_HUE,
                    currentHue);
            attributeMap.put(
                    ColorControlCluster.CLUSTER_PREFIX + "." + ColorControlCluster.ATTRIBUTE_CURRENT_SATURATION,
                    currentSaturation);
            attributeMap.put(OnOffCluster.CLUSTER_PREFIX + "." + OnOffCluster.ATTRIBUTE_ON_OFF, currentLevel > 0);
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
                updateBrightness(ValueUtils.levelToPercent(value.intValue()));
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
            List<AttributeState> states = new ArrayList<>();
            if (hsb.getBrightness().intValue() == 0) {
                states.add(new AttributeState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF, false));
            } else {
                // since we are on, complete the future
                completeOnFuture();
                lastB = null; // reset the cached brightness
                states.add(new AttributeState(OnOffCluster.CLUSTER_PREFIX, OnOffCluster.ATTRIBUTE_ON_OFF, true));
                states.add(new AttributeState(LevelControlCluster.CLUSTER_PREFIX,
                        LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL, toBrightness(hsb.getBrightness())));
            }
            states.add(new AttributeState(ColorControlCluster.CLUSTER_PREFIX, ColorControlCluster.ATTRIBUTE_CURRENT_HUE,
                    toHue(hsb.getHue())));
            states.add(new AttributeState(ColorControlCluster.CLUSTER_PREFIX,
                    ColorControlCluster.ATTRIBUTE_CURRENT_SATURATION, toSaturation(hsb.getSaturation())));
            setEndpointStates(states);
        }
    }

    private void updateBrightness(PercentType brightness) {
        if (primaryItem instanceof ColorItem colorItem) {
            lastB = brightness;
            colorItem.send(brightness);
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
            colorItem.send(OnOffType.from(onOff));
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
            colorItem.send(new HSBType(h, s, b));
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
}
