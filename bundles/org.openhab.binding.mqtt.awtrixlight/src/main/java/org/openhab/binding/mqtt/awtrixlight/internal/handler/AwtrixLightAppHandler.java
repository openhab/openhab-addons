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

package org.openhab.binding.mqtt.awtrixlight.internal.handler;

import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_ACTIVE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_AUTOSCALE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_BACKGROUND;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_BAR;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_BLINK_TEXT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_BUTLEFT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_BUTRIGHT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_BUTSELECT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_CENTER;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_COLOR;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_DURATION;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_EFFECT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_EFFECT_BLEND;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_EFFECT_PALETTE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_EFFECT_SPEED;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_FADE_TEXT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_GRADIENT_COLOR;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_ICON;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_LINE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_PROGRESS;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_PROGRESSBC;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_PROGRESSC;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_PUSH_ICON;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_RAINBOW;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_REPEAT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_RESET;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_SCROLLSPEED;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_TEXT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_TEXTCASE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_TEXT_OFFSET;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_TOP_TEXT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.PROP_APPID;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.PROP_UNIQUEID;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.THING_TYPE_APP;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.THOUSAND;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.AppConfigOptions;
import org.openhab.binding.mqtt.awtrixlight.internal.Helper;
import org.openhab.binding.mqtt.awtrixlight.internal.app.AwtrixApp;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwtrixLightAppHandler} is responsible for handling commands of the globes, which are then
 * sent to one of the bridges to be sent out by MQTT.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AwtrixLightAppHandler extends BaseThingHandler implements MqttMessageSubscriber {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_APP);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String channelPrefix = "";
    private String appName = "";

    private Boolean synchronizationRequired = true;
    private Boolean buttonControlled = false;
    private Boolean active = true;

    private AwtrixApp app = new AwtrixApp();

    private @Nullable ScheduledFuture<?> finishInitJob;

    public AwtrixLightAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Received command {} of type {} on channel {}", command.toString(), command.getClass(),
                channelUID.getAsString());
        if (command instanceof RefreshType) {
            updateApp();
            return;
        }

        // WARNING: Inactive Apps wont survive an OH reboot
        if (channelUID.getId().equals(CHANNEL_ACTIVE)) {
            if (command instanceof OnOffType) {
                if (OnOffType.OFF.equals(command)) {
                    this.active = false;
                    deleteApp();
                } else if (OnOffType.ON.equals(command)) {
                    this.active = true;
                }
            }
        } else if (channelUID.getId().equals(CHANNEL_RESET)) {
            if (command instanceof OnOffType) {
                if (OnOffType.ON.equals((OnOffType) command)) {
                    deleteApp();
                    this.app = new AwtrixApp();
                    updateApp();
                    initStates();
                    return;
                }
            }
        } else if (channelUID.getId().equals(CHANNEL_COLOR)) {
            if (command instanceof HSBType) {
                int[] hsbToRgb = ColorUtil.hsbToRgb((HSBType) command);
                this.app.setColor(convertRgbArray(hsbToRgb));
            }
        } else if (channelUID.getId().equals(CHANNEL_GRADIENT_COLOR)) {
            if (command instanceof HSBType) {
                int[] hsbToRgb = ColorUtil.hsbToRgb((HSBType) command);
                this.app.setGradient(convertRgbArray(hsbToRgb));
            }
        } else if (channelUID.getId().equals(CHANNEL_SCROLLSPEED)) {
            if (command instanceof QuantityType) {
                this.app.setScrollSpeed(((QuantityType<?>) command).toBigDecimal());
            }
        } else if (channelUID.getId().equals(CHANNEL_REPEAT)) {
            if (command instanceof QuantityType) {
                this.app.setRepeat(((QuantityType<?>) command).toBigDecimal());
            }
        } else if (channelUID.getId().equals(CHANNEL_DURATION)) {
            if (command instanceof QuantityType) {
                this.app.setDuration(((QuantityType<?>) command).toBigDecimal());
            }
        } else if (channelUID.getId().equals(CHANNEL_EFFECT)) {
            if (command instanceof StringType) {
                this.app.setEffect(((StringType) command).toString());
            }
        } else if (channelUID.getId().equals(CHANNEL_EFFECT_SPEED)) {
            if (command instanceof QuantityType) {
                this.app.setEffectSpeed(((QuantityType<?>) command).toBigDecimal());
            }
        } else if (channelUID.getId().equals(CHANNEL_EFFECT_PALETTE)) {
            if (command instanceof StringType) {
                this.app.setEffectPalette(((StringType) command).toString());
            }
        } else if (channelUID.getId().equals(CHANNEL_EFFECT_BLEND)) {
            if (command instanceof OnOffType) {
                this.app.setEffectBlend(command.equals(OnOffType.ON));
            }
        } else if (channelUID.getId().equals(CHANNEL_TEXT)) {
            if (command instanceof StringType) {
                this.app.setText(((StringType) command).toString());
            }
        } else if (channelUID.getId().equals(CHANNEL_TEXT_OFFSET)) {
            if (command instanceof QuantityType) {
                this.app.setTextOffset(((QuantityType<?>) command).toBigDecimal());
            }
        } else if (channelUID.getId().equals(CHANNEL_TOP_TEXT)) {
            if (command instanceof OnOffType) {
                this.app.setTopText(command.equals(OnOffType.ON));
            }
        } else if (channelUID.getId().equals(CHANNEL_TEXTCASE)) {
            if (command instanceof QuantityType) {
                this.app.setTextCase(((QuantityType<?>) command).toBigDecimal());
            }
        } else if (channelUID.getId().equals(CHANNEL_CENTER)) {
            if (command instanceof OnOffType) {
                this.app.setCenter(command.equals(OnOffType.ON));
            }
        } else if (channelUID.getId().equals(CHANNEL_BLINK_TEXT)) {
            if (command instanceof QuantityType) {
                QuantityType<?> blinkInS = ((QuantityType<?>) command).toUnit(Units.SECOND);
                if (blinkInS != null) {
                    BigDecimal blinkInMs = blinkInS.toBigDecimal().multiply(THOUSAND);
                    if (blinkInMs != null) {
                        this.app.setBlinkText(blinkInMs);
                    }
                }
            }
        } else if (channelUID.getId().equals(CHANNEL_FADE_TEXT)) {
            if (command instanceof QuantityType) {
                QuantityType<?> fadeInS = ((QuantityType<?>) command).toUnit(Units.SECOND);
                if (fadeInS != null) {
                    BigDecimal fadeInMs = fadeInS.toBigDecimal().multiply(THOUSAND);
                    if (fadeInMs != null) {
                        this.app.setFadeText(fadeInMs);
                    }
                }
            }
        } else if (channelUID.getId().equals(CHANNEL_RAINBOW)) {
            if (command instanceof OnOffType) {
                this.app.setRainbow(command.equals(OnOffType.ON));
            }
        } else if (channelUID.getId().equals(CHANNEL_ICON)) {
            if (command instanceof StringType) {
                this.app.setIcon(((StringType) command).toString());
            }
        } else if (channelUID.getId().equals(CHANNEL_PUSH_ICON)) {
            if (command instanceof OnOffType) {
                this.app.setPushIcon(command.equals(OnOffType.ON));
            }
        } else if (channelUID.getId().equals(CHANNEL_BACKGROUND)) {
            if (command instanceof HSBType) {
                int[] hsbToRgb = ColorUtil.hsbToRgb((HSBType) command);
                this.app.setBackground(convertRgbArray(hsbToRgb));
            }
        } else if (channelUID.getId().equals(CHANNEL_LINE)) {
            if (command instanceof StringType) {
                try {
                    String[] points = command.toString().split(",");
                    BigDecimal[] pointsAsNumber = new BigDecimal[points.length];
                    for (int i = 0; i < points.length; i++) {
                        pointsAsNumber[i] = new BigDecimal(points[i]);
                    }
                    this.app.setLine(pointsAsNumber);
                } catch (Exception e) {
                    logger.warn("Command {} cannot be parsed as line graph. Format should be: 1,2,3,4,5",
                            command.toString());
                }
            }
        } else if (channelUID.getId().equals(CHANNEL_BAR)) {
            if (command instanceof StringType) {
                try {
                    String[] points = command.toString().split(",");
                    BigDecimal[] pointsAsNumber = new BigDecimal[points.length];
                    for (int i = 0; i < points.length; i++) {
                        pointsAsNumber[i] = new BigDecimal(points[i]);
                    }
                    this.app.setBar(pointsAsNumber);
                } catch (Exception e) {
                    logger.warn("Command {} cannot be parsed as bar graph. Format should be: 1,2,3,4,5",
                            command.toString());
                }
            }
        } else if (channelUID.getId().equals(CHANNEL_AUTOSCALE)) {
            if (command instanceof OnOffType) {
                this.app.setAutoscale(command.equals(OnOffType.ON));
            }
        } else if (channelUID.getId().equals(CHANNEL_PROGRESS)) {
            if (command instanceof QuantityType) {
                this.app.setProgress(((QuantityType<?>) command).toBigDecimal());
            }
        } else if (channelUID.getId().equals(CHANNEL_PROGRESSC)) {
            logger.warn("RECEIVED 1 {}", command.toFullString());
            if (command instanceof HSBType) {
                int[] hsbToRgb = ColorUtil.hsbToRgb((HSBType) command);
                logger.warn("RECEIVED 2 {}", hsbToRgb);
                this.app.setProgressC(convertRgbArray(hsbToRgb));
            }
        } else if (channelUID.getId().equals(CHANNEL_PROGRESSBC)) {
            if (command instanceof HSBType) {
                int[] hsbToRgb = ColorUtil.hsbToRgb((HSBType) command);
                this.app.setProgressBC(convertRgbArray(hsbToRgb));
            }
        }
        logger.debug("Current app configuration: {}", this.app.toString());
        if (this.active) {
            updateApp();
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_COLOR)) {
            this.app.setColor(AwtrixApp.DEFAULT_COLOR);
        } else if (channelUID.getId().equals(CHANNEL_GRADIENT_COLOR)) {
            this.app.setGradient(AwtrixApp.DEFAULT_GRADIENT);
        } else if (channelUID.getId().equals(CHANNEL_SCROLLSPEED)) {
            this.app.setScrollSpeed(AwtrixApp.DEFAULT_SCROLLSPEED);
        } else if (channelUID.getId().equals(CHANNEL_REPEAT)) {
            this.app.setRepeat(AwtrixApp.DEFAULT_REPEAT);
        } else if (channelUID.getId().equals(CHANNEL_DURATION)) {
            this.app.setDuration(AwtrixApp.DEFAULT_DURATION);
        } else if (channelUID.getId().equals(CHANNEL_EFFECT)) {
            this.app.setEffect(AwtrixApp.DEFAULT_EFFECT);
        } else if (channelUID.getId().equals(CHANNEL_EFFECT_SPEED)) {
            this.app.setEffectSpeed(AwtrixApp.DEFAULT_EFFECTSPEED);
        } else if (channelUID.getId().equals(CHANNEL_EFFECT_PALETTE)) {
            this.app.setEffectPalette(AwtrixApp.DEFAULT_EFFECTPALETTE);
        } else if (channelUID.getId().equals(CHANNEL_EFFECT_BLEND)) {
            this.app.setEffectBlend(AwtrixApp.DEFAULT_EFFECTBLEND);
        } else if (channelUID.getId().equals(CHANNEL_TEXT)) {
            this.app.setText(AwtrixApp.DEFAULT_TEXT);
        } else if (channelUID.getId().equals(CHANNEL_TEXT_OFFSET)) {
            this.app.setTextOffset(AwtrixApp.DEFAULT_TEXTOFFSET);
        } else if (channelUID.getId().equals(CHANNEL_TOP_TEXT)) {
            this.app.setTopText(AwtrixApp.DEFAULT_TOPTEXT);
        } else if (channelUID.getId().equals(CHANNEL_TEXTCASE)) {
            this.app.setTextCase(AwtrixApp.DEFAULT_TEXTCASE);
        } else if (channelUID.getId().equals(CHANNEL_CENTER)) {
            this.app.setCenter(AwtrixApp.DEFAULT_CENTER);
        } else if (channelUID.getId().equals(CHANNEL_BLINK_TEXT)) {
            this.app.setBlinkText(AwtrixApp.DEFAULT_BLINKTEXT);
        } else if (channelUID.getId().equals(CHANNEL_FADE_TEXT)) {
            this.app.setFadeText(AwtrixApp.DEFAULT_FADETEXT);
        } else if (channelUID.getId().equals(CHANNEL_RAINBOW)) {
            this.app.setRainbow(AwtrixApp.DEFAULT_RAINBOW);
        } else if (channelUID.getId().equals(CHANNEL_ICON)) {
            this.app.setIcon(AwtrixApp.DEFAULT_ICON);
        } else if (channelUID.getId().equals(CHANNEL_PUSH_ICON)) {
            this.app.setPushIcon(AwtrixApp.DEFAULT_PUSHICON);
        } else if (channelUID.getId().equals(CHANNEL_BACKGROUND)) {
            this.app.setBackground(AwtrixApp.DEFAULT_BACKGROUND);
        } else if (channelUID.getId().equals(CHANNEL_LINE)) {
            this.app.setLine(AwtrixApp.DEFAULT_LINE);
        } else if (channelUID.getId().equals(CHANNEL_BAR)) {
            this.app.setBar(AwtrixApp.DEFAULT_BAR);
        } else if (channelUID.getId().equals(CHANNEL_AUTOSCALE)) {
            this.app.setAutoscale(AwtrixApp.DEFAULT_AUTOSCALE);
        } else if (channelUID.getId().equals(CHANNEL_PROGRESS)) {
            this.app.setProgress(AwtrixApp.DEFAULT_PROGRESS);
        } else if (channelUID.getId().equals(CHANNEL_PROGRESSC)) {
            this.app.setProgressC(AwtrixApp.DEFAULT_PROGRESSC);
        } else if (channelUID.getId().equals(CHANNEL_PROGRESSBC)) {
            this.app.setProgressBC(AwtrixApp.DEFAULT_PROGRESSBC);
        }
        logger.debug("Current app configuration: {}", this.app.toString());
        updateApp();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        initStates();
    }

    private BigDecimal[] convertRgbArray(int[] rgbIn) {
        if (rgbIn.length == 3) {
            BigDecimal[] rgb = new BigDecimal[3];
            rgb[0] = new BigDecimal(rgbIn[0]);
            rgb[1] = new BigDecimal(rgbIn[1]);
            rgb[2] = new BigDecimal(rgbIn[2]);
            return rgb;
        } else {
            return new BigDecimal[0];
        }
    }

    private void deleteApp() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof AwtrixLightBridgeHandler) {
                AwtrixLightBridgeHandler albh = (AwtrixLightBridgeHandler) bridgeHandler;
                albh.deleteApp(this.appName);
            }
        }
    }

    private void updateApp() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof AwtrixLightBridgeHandler) {
                AwtrixLightBridgeHandler albh = (AwtrixLightBridgeHandler) bridgeHandler;
                albh.updateApp(this.appName, this.app.getAppConfig());
            }
        }
    }

    @Override
    public void initialize() {
        AppConfigOptions config = getConfigAs(AppConfigOptions.class);
        this.appName = config.appname;
        this.buttonControlled = config.useButtons;
        this.channelPrefix = getThing().getUID() + ":";
        thing.setProperty(PROP_APPID, this.appName);
        logger.trace("Configured handler for app {} with channelPrefix {}", this.appName, this.channelPrefix);
        bridgeStatusChanged(getBridgeStatus());
    }

    private void initStates() {
        updateState(new ChannelUID(channelPrefix + CHANNEL_ACTIVE), this.active ? OnOffType.ON : OnOffType.OFF);
        if (this.app.getColor().length == 3) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_COLOR),
                    HSBType.fromRGB(this.app.getColor()[0].intValue(), this.app.getColor()[1].intValue(),
                            this.app.getColor()[2].intValue()));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_COLOR), UnDefType.UNDEF);
        }
        if (this.app.getGradient().length == 3) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_GRADIENT_COLOR),
                    HSBType.fromRGB(this.app.getGradient()[0].intValue(), this.app.getGradient()[1].intValue(),
                            this.app.getGradient()[2].intValue()));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_COLOR), UnDefType.UNDEF);
        }
        updateState(new ChannelUID(channelPrefix + CHANNEL_SCROLLSPEED),
                new QuantityType<>(this.app.getScrollSpeed(), Units.PERCENT));
        updateState(new ChannelUID(channelPrefix + CHANNEL_REPEAT),
                new QuantityType<>(this.app.getRepeat(), Units.ONE));
        updateState(new ChannelUID(channelPrefix + CHANNEL_DURATION),
                new QuantityType<>(this.app.getDuration(), Units.SECOND));
        updateState(new ChannelUID(channelPrefix + CHANNEL_EFFECT), new StringType(this.app.getEffect()));
        if (this.app.getEffectSpeed().compareTo(BigDecimal.ZERO) > 0) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_EFFECT_SPEED),
                    new QuantityType<>(this.app.getEffectSpeed(), Units.ONE));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_EFFECT_SPEED), UnDefType.UNDEF);
        }
        updateState(new ChannelUID(channelPrefix + CHANNEL_EFFECT_PALETTE),
                new StringType(this.app.getEffectPalette()));
        updateState(new ChannelUID(channelPrefix + CHANNEL_EFFECT_BLEND),
                this.app.getEffectBlend() ? OnOffType.ON : OnOffType.OFF);
        updateState(new ChannelUID(channelPrefix + CHANNEL_TEXT), new StringType(this.app.getText()));
        updateState(new ChannelUID(channelPrefix + CHANNEL_TEXT_OFFSET),
                new QuantityType<>(this.app.getTextOffset(), Units.ONE));
        updateState(new ChannelUID(channelPrefix + CHANNEL_TOP_TEXT),
                this.app.getTopText() ? OnOffType.ON : OnOffType.OFF);
        updateState(new ChannelUID(channelPrefix + CHANNEL_CENTER),
                this.app.getCenter() ? OnOffType.ON : OnOffType.OFF);
        BigDecimal blinkTextInSeconds = this.app.getBlinkText().divide(THOUSAND);
        if (blinkTextInSeconds != null) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_BLINK_TEXT),
                    new QuantityType<>(blinkTextInSeconds, Units.SECOND));
        }
        BigDecimal fadeTextInSeconds = this.app.getFadeText().divide(THOUSAND);
        if (fadeTextInSeconds != null) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_FADE_TEXT),
                    new QuantityType<>(fadeTextInSeconds, Units.SECOND));
        }
        updateState(new ChannelUID(channelPrefix + CHANNEL_RAINBOW),
                this.app.getRainbow() ? OnOffType.ON : OnOffType.OFF);
        updateState(new ChannelUID(channelPrefix + CHANNEL_ICON), new StringType(this.app.getIcon()));
        updateState(new ChannelUID(channelPrefix + CHANNEL_PUSH_ICON),
                this.app.getPushIcon() ? OnOffType.ON : OnOffType.OFF);
        updateState(new ChannelUID(channelPrefix + CHANNEL_TEXTCASE),
                new QuantityType<>(this.app.getBlinkText(), Units.ONE));
        if (this.app.getBackground().length == 3) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_BACKGROUND),
                    HSBType.fromRGB(this.app.getBackground()[0].intValue(), this.app.getBackground()[1].intValue(),
                            this.app.getBackground()[2].intValue()));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_BACKGROUND), UnDefType.UNDEF);
        }
        if (this.app.getLine().length > 0) {
            String line = Arrays.stream(this.app.getLine()).map(BigDecimal::toString).collect(Collectors.joining(","));
            updateState(new ChannelUID(channelPrefix + CHANNEL_LINE), new StringType(line));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_LINE), new StringType(""));
        }
        if (this.app.getBar().length > 0) {
            String bar = Arrays.stream(this.app.getBar()).map(BigDecimal::toString).collect(Collectors.joining(","));
            updateState(new ChannelUID(channelPrefix + CHANNEL_BAR), new StringType(bar));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_LINE), new StringType(""));
        }
        updateState(new ChannelUID(channelPrefix + CHANNEL_AUTOSCALE),
                this.app.getAutoscale() ? OnOffType.ON : OnOffType.OFF);
        if (this.app.getProgress().compareTo(BigDecimal.ZERO) > 0) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_PROGRESS),
                    new QuantityType<>(this.app.getBlinkText(), Units.PERCENT));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_PROGRESS), UnDefType.UNDEF);
        }

        if (this.app.getProgressC().length == 3) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_PROGRESSC),
                    HSBType.fromRGB(this.app.getProgressC()[0].intValue(), this.app.getProgressC()[1].intValue(),
                            this.app.getProgressC()[2].intValue()));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_PROGRESSC), UnDefType.UNDEF);
        }

        if (this.app.getProgressBC().length == 3) {
            updateState(new ChannelUID(channelPrefix + CHANNEL_PROGRESSBC),
                    HSBType.fromRGB(this.app.getProgressBC()[0].intValue(), this.app.getProgressBC()[1].intValue(),
                            this.app.getProgressBC()[2].intValue()));
        } else {
            updateState(new ChannelUID(channelPrefix + CHANNEL_PROGRESSBC), UnDefType.UNDEF);
        }
    }

    public ThingStatusInfo getBridgeStatus() {
        Bridge b = getBridge();
        if (b != null) {
            return b.getStatusInfo();
        } else {
            return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        Bridge localBridge = this.getBridge();
        if (localBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge is missing or offline.");
            return;
        }
        ThingHandler handler = localBridge.getHandler();
        if (handler instanceof AwtrixLightBridgeHandler) {
            AwtrixLightBridgeHandler albh = (AwtrixLightBridgeHandler) handler;
            Map<String, String> bridgeProperties = albh.getThing().getProperties();
            String bridgeHardwareId = bridgeProperties.get(PROP_UNIQUEID);
            if (bridgeHardwareId != null) {
                thing.setProperty(PROP_APPID, bridgeHardwareId + "-" + this.appName);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NOT_YET_READY, "Synchronizing...");
            this.finishInitJob = scheduler.schedule(this::finishInit, 5, TimeUnit.SECONDS);
        }
        return;
    }

    void handleLeftButton(String event) {
        triggerChannel(new ChannelUID(channelPrefix + CHANNEL_BUTLEFT), event);
    }

    void handleRightButton(String event) {
        triggerChannel(new ChannelUID(channelPrefix + CHANNEL_BUTRIGHT), event);
    }

    void handleSelectButton(String event) {
        triggerChannel(new ChannelUID(channelPrefix + CHANNEL_BUTSELECT), event);
    }

    private void finishInit() {
        synchronized (this.synchronizationRequired) {
            if (this.synchronizationRequired) {
                initStates();
                updateApp();
                this.synchronizationRequired = false;
            }
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            this.finishInitJob = null;
        }
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        synchronized (this.synchronizationRequired) {
            if (this.synchronizationRequired) {
                String payloadString = new String(payload, StandardCharsets.UTF_8);
                HashMap<String, Object> decodedJson = Helper.decodeJson(payloadString);
                this.app.updateFields(decodedJson);
                initStates();
                this.synchronizationRequired = false;
            }
        }
    }

    public String getAppName() {
        return this.appName;
    }

    public boolean isButtonControlled() {
        return this.buttonControlled;
    }
}
