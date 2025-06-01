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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster.MatterEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ColorControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ColorControlCluster.EnhancedColorModeEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;

/**
 * A converter for translating {@link ColorControlCluster} events and attributes to openHAB channels and back again.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Chris Jackson - Original Zigbee binding color logic functions borrowed here
 *
 */
@NonNullByDefault
public class ColorControlConverter extends GenericConverter<ColorControlCluster> {

    // We will wait up for this time to receive multiple color updates before we update the color values
    private static final int UPDATE_DELAY = 500;
    // These are the default values, as well as the min and max limits for the color temperature mireds defined in the
    // Matter spec
    protected static final int MAX_MIREDS = 65279;
    protected static final int MIN_MIREDS = 1; // this was 0 until matter 1.4
    // These are sane defaults that should be used if the device does not provide a valid range (aka if using default
    // MIN / MAX)
    protected static final int MAX_DEFAULT_MIREDS = 667; // 1500K
    protected static final int MIN_DEFAULT_MIREDS = 153;
    protected boolean supportsHue = false;
    protected boolean supportsColorTemperature = false;
    protected int colorTempPhysicalMinMireds = 0;
    protected int colorTempPhysicalMaxMireds = 0;
    private LevelControlCluster.OptionsBitmap levelControlOptionsBitmap = new LevelControlCluster.OptionsBitmap(true,
            true);
    private boolean lastOnOff = true;
    private HSBType lastHSB = new HSBType("0,0,0");
    private @Nullable ScheduledFuture<?> colorUpdateTimer = null;
    private ScheduledExecutorService colorUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
    private EnhancedColorModeEnum lastColorMode = EnhancedColorModeEnum.CURRENT_X_AND_CURRENT_Y;
    private int lastHue = 0;
    private int lastSaturation = 0;
    private int lastX = 0;
    private int lastY = 0;
    private int lastColorTemperatureMireds;
    // These states are used to track the state of the color updates, once a state is ready we will set the color.
    private ColorUpdateState hueSaturationState = ColorUpdateState.READY;
    private ColorUpdateState xyState = ColorUpdateState.READY;
    private ColorUpdateState colorTemperatureState = ColorUpdateState.READY;

    public ColorControlConverter(ColorControlCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
        supportsHue = initializingCluster.featureMap.hueSaturation;
        supportsColorTemperature = initializingCluster.featureMap.colorTemperature;
        // The inovelli device for example sends a max mireds of 65279 (matter spec max value), which means they did not
        // actually set the max mireds, but this should never really exceed 667 (1500K)
        Integer maxMireds = initializingCluster.colorTempPhysicalMaxMireds;
        colorTempPhysicalMaxMireds = (maxMireds == null || maxMireds >= MAX_MIREDS) ? MAX_DEFAULT_MIREDS : maxMireds;
        // the inovelli device for example sends a min mireds of 0 (matter 1.3 default) which means they did not
        // actually set the min mireds, but this should never really be less than 153 (6500K) in real life
        Integer minMireds = initializingCluster.colorTempPhysicalMinMireds;
        colorTempPhysicalMinMireds = (minMireds == null || minMireds <= MIN_MIREDS) ? MIN_DEFAULT_MIREDS : minMireds;
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> map = new HashMap<>();

        map.put(ChannelBuilder.create(new ChannelUID(channelGroupUID, CHANNEL_ID_COLOR_COLOR), CoreItemFactory.COLOR)
                .withType(CHANNEL_COLOR_COLOR).build(), null);

        // see Matter spec 3.2.6.1 For more information on color temperature
        if (initializingCluster.featureMap.colorTemperature) {
            map.put(ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_COLOR_TEMPERATURE), CoreItemFactory.DIMMER)
                    .withType(CHANNEL_COLOR_TEMPERATURE).build(), null);
            StateDescription stateDescription = null;
            if (colorTempPhysicalMinMireds < colorTempPhysicalMaxMireds) {
                stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%.0f mirek")
                        .withMinimum(BigDecimal.valueOf(colorTempPhysicalMinMireds))
                        .withMaximum(BigDecimal.valueOf(colorTempPhysicalMaxMireds)).build().toStateDescription();
            }
            map.put(ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_COLOR_TEMPERATURE_ABS), "Number:Temperature")
                    .withType(CHANNEL_COLOR_TEMPERATURE_ABS).build(), stateDescription);
        }
        return map;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof HSBType color) {
            PercentType brightness = color.getBrightness();
            if (brightness.equals(PercentType.ZERO)) {
                handler.sendClusterCommand(endpointNumber, OnOffCluster.CLUSTER_NAME, OnOffCluster.off());
            } else {
                ClusterCommand levelCommand = LevelControlCluster.moveToLevelWithOnOff(
                        ValueUtils.percentToLevel(brightness), 0, levelControlOptionsBitmap, levelControlOptionsBitmap);
                handler.sendClusterCommand(endpointNumber, LevelControlCluster.CLUSTER_NAME, levelCommand);
                if (supportsHue) {
                    changeColorHueSaturation(color);
                } else {
                    changeColorXY(color);
                }
            }
        } else if (command instanceof OnOffType onOffType) {
            ClusterCommand onOffCommand = onOffType == OnOffType.ON ? OnOffCluster.on() : OnOffCluster.off();
            handler.sendClusterCommand(endpointNumber, OnOffCluster.CLUSTER_NAME, onOffCommand);
        } else if (command instanceof PercentType percentType) {
            if (channelUID.getIdWithoutGroup().equals(CHANNEL_ID_COLOR_TEMPERATURE)) {
                if (!lastOnOff) {
                    handler.sendClusterCommand(endpointNumber, OnOffCluster.CLUSTER_NAME, OnOffCluster.on());
                }
                ClusterCommand tempCommand = ColorControlCluster.moveToColorTemperature(
                        percentTypeToMireds(percentType), 0, initializingCluster.options, initializingCluster.options);
                handler.sendClusterCommand(endpointNumber, ColorControlCluster.CLUSTER_NAME, tempCommand);
            } else {
                if (percentType.equals(PercentType.ZERO)) {
                    handler.sendClusterCommand(endpointNumber, OnOffCluster.CLUSTER_NAME, OnOffCluster.off());
                } else {
                    ClusterCommand levelCommand = LevelControlCluster.moveToLevelWithOnOff(
                            ValueUtils.percentToLevel(percentType), 0, levelControlOptionsBitmap,
                            levelControlOptionsBitmap);
                    handler.sendClusterCommand(endpointNumber, LevelControlCluster.CLUSTER_NAME, levelCommand);
                }
            }
        } else if (channelUID.getIdWithoutGroup().equals(CHANNEL_ID_COLOR_TEMPERATURE_ABS)
                && command instanceof DecimalType decimal) {
            ClusterCommand tempCommand = ColorControlCluster.moveToColorTemperature(decimal.intValue(), 0,
                    initializingCluster.options, initializingCluster.options);
            handler.sendClusterCommand(endpointNumber, ColorControlCluster.CLUSTER_NAME, tempCommand);
        } else if (channelUID.getIdWithoutGroup().equals(CHANNEL_ID_COLOR_TEMPERATURE_ABS)
                && command instanceof QuantityType<?> quantity) {
            quantity = quantity.toInvertibleUnit(Units.MIRED);
            if (quantity != null) {
                ClusterCommand tempCommand = ColorControlCluster.moveToColorTemperature(quantity.intValue(), 0,
                        initializingCluster.options, initializingCluster.options);
                handler.sendClusterCommand(endpointNumber, ColorControlCluster.CLUSTER_NAME, tempCommand);
            }
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        logger.debug("OnEvent: {} with value {}", message.path.attributeName, message.value);
        Integer numberValue = message.value instanceof Number number ? number.intValue() : 0;
        boolean colorChanged = false;
        switch (message.path.attributeName) {
            case ColorControlCluster.ATTRIBUTE_CURRENT_X:
                lastX = numberValue;
                xyState = xyState.next();
                colorChanged = true;
                break;
            case ColorControlCluster.ATTRIBUTE_CURRENT_Y:
                lastY = numberValue;
                xyState = xyState.next();
                colorChanged = true;
                break;
            case ColorControlCluster.ATTRIBUTE_CURRENT_HUE:
                lastHue = numberValue;
                hueSaturationState = hueSaturationState.next();
                colorChanged = true;
                break;
            case ColorControlCluster.ATTRIBUTE_CURRENT_SATURATION:
                lastSaturation = numberValue;
                hueSaturationState = hueSaturationState.next();
                colorChanged = true;
                break;
            case ColorControlCluster.ATTRIBUTE_COLOR_TEMPERATURE_MIREDS:
                if (numberValue >= colorTempPhysicalMinMireds && numberValue <= colorTempPhysicalMaxMireds) {
                    lastColorTemperatureMireds = numberValue;
                    colorTemperatureState = ColorUpdateState.READY;
                    colorChanged = true;
                } else {
                    throw new IllegalArgumentException("Invalid color temperature mireds: " + numberValue
                            + " must be between " + colorTempPhysicalMinMireds + " and " + colorTempPhysicalMaxMireds);
                }
                break;
            case ColorControlCluster.ATTRIBUTE_COLOR_MODE:
            case ColorControlCluster.ATTRIBUTE_ENHANCED_COLOR_MODE:
                EnhancedColorModeEnum newColorMode = lastColorMode;
                if (message.value instanceof ColorControlCluster.ColorModeEnum colorMode) {
                    try {
                        newColorMode = MatterEnum.fromValue(EnhancedColorModeEnum.class, colorMode.value);
                    } catch (IllegalArgumentException e) {
                        logger.debug("Unknown color mode: {}", numberValue);
                    }
                }
                if (message.value instanceof ColorControlCluster.EnhancedColorModeEnum enhancedColorMode) {
                    newColorMode = enhancedColorMode;
                }
                if (newColorMode != lastColorMode) {
                    lastColorMode = newColorMode;
                    switch (lastColorMode) {
                        case CURRENT_HUE_AND_CURRENT_SATURATION:
                            hueSaturationState = ColorUpdateState.WAITING1;
                            break;
                        case CURRENT_X_AND_CURRENT_Y:
                            xyState = ColorUpdateState.WAITING1;
                            break;
                        case COLOR_TEMPERATURE_MIREDS:
                            colorTemperatureState = ColorUpdateState.WAITING1;
                            break;
                        default:
                            break;
                    }
                    colorChanged = true;
                }
                break;
            case ColorControlCluster.ATTRIBUTE_ENHANCED_CURRENT_HUE:
                logger.debug("enhancedCurrentHue not supported yet");
                break;
            case LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL:
                updateBrightness(ValueUtils.levelToPercent(numberValue));
                break;
            case OnOffCluster.ATTRIBUTE_ON_OFF:
                updateOnOff((Boolean) message.value);
                break;
            default:
                logger.debug("Unknown attribute {}", message.path.attributeName);
        }
        if (colorChanged) {
            updateColor();
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        initState(true, null);
    }

    public void initState(boolean onOff, @Nullable LevelControlCluster levelControlCluster) {
        Integer brightness = 100;
        if (levelControlCluster != null) {
            if (levelControlCluster.currentLevel != null) {
                brightness = levelControlCluster.currentLevel;
            }
            if (levelControlCluster.options != null) {
                levelControlOptionsBitmap = levelControlCluster.options;
            }
        }
        lastHSB = new HSBType(lastHSB.getHue(), lastHSB.getSaturation(), ValueUtils.levelToPercent(brightness));
        lastColorMode = Optional.ofNullable(initializingCluster.enhancedColorMode).orElseGet(
                () -> MatterEnum.fromValue(EnhancedColorModeEnum.class, initializingCluster.colorMode.value));
        lastOnOff = onOff;
        lastX = initializingCluster.currentX != null ? initializingCluster.currentX : 0;
        lastY = initializingCluster.currentY != null ? initializingCluster.currentY : 0;
        lastHue = initializingCluster.currentHue != null ? initializingCluster.currentHue : 0;
        lastSaturation = initializingCluster.currentSaturation != null ? initializingCluster.currentSaturation : 0;
        lastColorTemperatureMireds = initializingCluster.colorTemperatureMireds != null
                ? initializingCluster.colorTemperatureMireds
                : 154;

        updateColor();
    }

    /**
     * A color device will send a colorMode attribute when changing from temperature mode or to color mode and vice
     * versa. We will update the color values after we wait for the device to send multiple update within the timer
     * duration or if we get all states for a required color mode
     * 
     * Calling this will cancel any existing timer and start a new one to wait for the next update
     */
    private void updateColor() {
        logger.debug("updateColor: lastColorMode {}", lastColorMode);
        cancelUpdateTimer();
        switch (lastColorMode) {
            case CURRENT_HUE_AND_CURRENT_SATURATION:
                if (hueSaturationState.isReady()) {
                    updateColorHSB();
                } else {
                    startUpdateTimer(this::updateColorHSB);
                }
                temperatureModeToColor();
                break;
            case CURRENT_X_AND_CURRENT_Y:
                if (xyState.isReady()) {
                    updateColorXY();
                } else {
                    startUpdateTimer(this::updateColorXY);
                }
                temperatureModeToColor();
                break;
            case COLOR_TEMPERATURE_MIREDS:
                if (colorTemperatureState.isReady()) {
                    updateColorTemperature();
                } else {
                    startUpdateTimer(this::updateColorTemperature);
                }
                break;
            default:
                logger.debug("Unknown color mode: {}", lastColorMode);
        }
    }

    private synchronized void startUpdateTimer(Runnable updateFunction) {
        cancelUpdateTimer();
        logger.debug("starting color timer");
        colorUpdateTimer = colorUpdateScheduler.schedule(updateFunction, UPDATE_DELAY, TimeUnit.MILLISECONDS);
    }

    private void cancelUpdateTimer() {
        @Nullable
        ScheduledFuture<?> colorUpdateTimer = this.colorUpdateTimer;
        if (colorUpdateTimer != null) {
            colorUpdateTimer.cancel(true);
        }
    }

    private void updateOnOff(boolean onOff) {
        lastOnOff = onOff;
        HSBType hsb = new HSBType(lastHSB.getHue(), lastHSB.getSaturation(),
                lastOnOff ? lastHSB.getBrightness() : new PercentType(0));
        updateState(CHANNEL_ID_COLOR_COLOR, hsb);
    }

    private void updateBrightness(PercentType brightness) {
        // Extra temp variable to avoid thread sync concurrency issues on lastHSB
        HSBType oldHSB = lastHSB;
        HSBType newHSB = new HSBType(oldHSB.getHue(), oldHSB.getSaturation(), brightness);
        lastHSB = newHSB;
        if (!lastOnOff) {
            updateState(CHANNEL_ID_COLOR_COLOR,
                    new HSBType(newHSB.getHue(), newHSB.getSaturation(), new PercentType(0)));
        } else {
            updateState(CHANNEL_ID_COLOR_COLOR, newHSB);
        }
    }

    // These color functions are borrowed from the Zigbee openHAB binding and modified for Matter

    private void updateColorHSB() {
        float hueValue = lastHue * 360.0f / 254.0f;
        float saturationValue = lastSaturation * 100.0f / 254.0f;
        DecimalType hue = new DecimalType(Float.valueOf(hueValue).toString());
        PercentType saturation = new PercentType(Float.valueOf(saturationValue).toString());
        updateColorHSB(hue, saturation);
        hueSaturationState = ColorUpdateState.READY;
    }

    private void updateColorHSB(DecimalType hue, PercentType saturation) {
        // Extra temp variable to avoid thread sync concurrency issues on lastHSB
        HSBType oldHSB = lastHSB;
        HSBType newHSB = new HSBType(hue, saturation, oldHSB.getBrightness());
        lastHSB = newHSB;
        if (!lastOnOff) {
            updateState(CHANNEL_ID_COLOR_COLOR,
                    new HSBType(newHSB.getHue(), newHSB.getSaturation(), new PercentType(0)));
        } else {
            updateState(CHANNEL_ID_COLOR_COLOR, newHSB);
        }
    }

    private void updateColorXY() {
        float xValue = lastX / 65536.0f;
        float yValue = lastY / 65536.0f;
        PercentType x = new PercentType(Float.valueOf(xValue * 100.0f).toString());
        PercentType y = new PercentType(Float.valueOf(yValue * 100.0f).toString());
        updateColorXY(x, y);
        xyState = ColorUpdateState.READY;
    }

    private void updateColorXY(PercentType x, PercentType y) {
        try {
            HSBType color = ColorUtil.xyToHsb(new double[] { x.floatValue() / 100.0f, y.floatValue() / 100.0f });
            updateColorHSB(color.getHue(), color.getSaturation());
        } catch (IllegalArgumentException e) {
            updateState(CHANNEL_ID_COLOR_COLOR, UnDefType.UNDEF);
        }
    }

    private void updateColorTemperature() {
        if (lastOnOff) {
            updateState(CHANNEL_ID_COLOR_TEMPERATURE, miredsToPercentType(lastColorTemperatureMireds));
            updateState(CHANNEL_ID_COLOR_TEMPERATURE_ABS,
                    QuantityType.valueOf(Double.valueOf(lastColorTemperatureMireds), Units.MIRED));
        }
        colorTemperatureState = ColorUpdateState.READY;
        colorModeToTemperature();
    }

    private void changeColorHueSaturation(HSBType color) {
        int hue = (int) (color.getHue().floatValue() * 254.0f / 360.0f + 0.5f);
        int saturation = ValueUtils.percentToLevel(color.getSaturation());
        handler.sendClusterCommand(endpointNumber, ColorControlCluster.CLUSTER_NAME, ColorControlCluster
                .moveToHueAndSaturation(hue, saturation, 0, initializingCluster.options, initializingCluster.options));
    }

    private void changeColorXY(HSBType color) {
        PercentType xy[] = color.toXY();
        int x = (int) (xy[0].floatValue() / 100.0f * 65536.0f + 0.5f); // up to 65279
        int y = (int) (xy[1].floatValue() / 100.0f * 65536.0f + 0.5f); // up to 65279
        handler.sendClusterCommand(endpointNumber, ColorControlCluster.CLUSTER_NAME,
                ColorControlCluster.moveToColor(x, y, 0, initializingCluster.options, initializingCluster.options));
    }

    private PercentType miredsToPercentType(Integer mireds) {
        int mired = Math.max(colorTempPhysicalMinMireds, Math.min(colorTempPhysicalMaxMireds, mireds));
        return new PercentType((int) (((double) (mired - colorTempPhysicalMinMireds)
                / (colorTempPhysicalMaxMireds - colorTempPhysicalMinMireds) * 100)));
    }

    private Integer percentTypeToMireds(PercentType percent) {
        return (int) ((percent.doubleValue() / 100) * (colorTempPhysicalMaxMireds - colorTempPhysicalMinMireds))
                + colorTempPhysicalMinMireds;
    }

    private void colorModeToTemperature() {
        try {
            HSBType color = ColorUtil.xyToHsb(ColorUtil.kelvinToXY(1000000.0 / lastColorTemperatureMireds));
            updateColorHSB(color.getHue(), color.getSaturation());
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            updateState(CHANNEL_ID_COLOR_COLOR, UnDefType.UNDEF);
        }
    }

    private void temperatureModeToColor() {
        updateState(CHANNEL_ID_COLOR_TEMPERATURE, UnDefType.UNDEF);
        updateState(CHANNEL_ID_COLOR_TEMPERATURE_ABS, UnDefType.UNDEF);
    }

    /**
     * Tracks the state of the color updates, once a state is ready we will set the color.
     * X/Y and Hue/Saturation potentially waits for 2 updates before setting the color
     */
    enum ColorUpdateState {
        READY,
        WAITING1,
        WAITING2;

        // move to the next waiting state
        public ColorUpdateState next() {
            return switch (this) {
                case READY -> WAITING2;
                case WAITING1 -> WAITING2;
                case WAITING2 -> READY;
            };
        }

        public boolean isReady() {
            return this == READY;
        }
    }
}
