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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiLightUtil.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLightChannel;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyLightHandler} handles light (Bulb, Duo and RGBW2) specific commands and status. All other commands
 * will be routet of the ShellyBaseHandler.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyLightHandler extends ShellyBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(ShellyLightHandler.class);
    private final Map<Integer, ShellyColorUtils> channelColors;

    public ShellyLightHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingRuntimeConfig bindingConfig, final ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient, WebSocketClient webSocketClient,
            final LocationProvider locationProvider) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient, webSocketClient,
                locationProvider);
        channelColors = new TreeMap<>();
    }

    @Override
    public void initialize() {
        logger.debug("Thing is using  {}", this.getClass());
        super.initialize();
    }

    @Override
    public boolean handleDeviceCommand(ChannelUID channelUID, Command command) throws IllegalArgumentException {
        String groupName = getString(channelUID.getGroupId());
        if (groupName.isEmpty()) {
            throw new IllegalArgumentException("Empty groupName");
        }

        int lightId = getLightIdFromGroup(groupName, profile);
        logger.trace("{}: Execute command {} on channel {}, lightId={}", thingName, command, channelUID.getAsString(),
                lightId);

        try {
            ShellyColorUtils oldCol = getCurrentColors(lightId);
            oldCol.mode = profile.device.mode;
            ShellyColorUtils col = new ShellyColorUtils(oldCol);

            boolean update = true;
            switch (channelUID.getIdWithoutGroup()) {
                default: // non-bulb commands will be handled by the generic handler
                    return false;

                case CHANNEL_LIGHT_POWER:
                    logger.debug("{}: Switch light {}", thingName, command);
                    api.setLightParm(lightId, SHELLY_LIGHT_TURN,
                            command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
                    col.power = (OnOffType) command;
                    requestUpdates(1, false);
                    update = false;
                    break;
                case CHANNEL_LIGHT_COLOR_MODE:
                    logger.debug("{}: Select color mode {}", thingName, command);
                    col.setMode((OnOffType) command == OnOffType.ON ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE);
                    break;
                case CHANNEL_COLOR_PICKER:
                    logger.debug("{}: Update colors from color picker", thingName);
                    update = handleColorPicker(profile, lightId, col, command);
                    break;
                case CHANNEL_COLOR_FULL:
                    logger.debug("{}: Set colors to {}", thingName, command);
                    handleFullColor(col, command);
                    break;
                case CHANNEL_COLOR_RED:
                    col.setRed(setColor(lightId, SHELLY_COLOR_RED, command, SHELLY_MAX_COLOR));
                    selectRgbcctMode(col, SHELLY_MODE_COLOR);
                    break;
                case CHANNEL_COLOR_GREEN:
                    col.setGreen(setColor(lightId, SHELLY_COLOR_GREEN, command, SHELLY_MAX_COLOR));
                    selectRgbcctMode(col, SHELLY_MODE_COLOR);
                    break;
                case CHANNEL_COLOR_BLUE:
                    col.setBlue(setColor(lightId, SHELLY_COLOR_BLUE, command, SHELLY_MAX_COLOR));
                    selectRgbcctMode(col, SHELLY_MODE_COLOR);
                    break;
                case CHANNEL_COLOR_WHITE:
                    col.setWhite(setColor(lightId, SHELLY_COLOR_WHITE, command, SHELLY_MAX_COLOR));
                    break;
                case CHANNEL_COLOR_GAIN:
                    col.setGain(setColor(lightId, SHELLY_COLOR_GAIN, command, SHELLY_MIN_GAIN, SHELLY_MAX_GAIN));
                    break;
                // brightness is a white-mode channel, except for Bulb/Duo where it's the only brightness channel
                case CHANNEL_BRIGHTNESS:
                    if (profile.hasColorTag(lightId) && !profile.isBulb && !(profile.isDuo && profile.isGen2)) {
                        logger.debug("{}: Not in white mode, brightness not available", thingName);
                        break;
                    }

                    int value = -1;
                    if (command instanceof OnOffType) { // Switch
                        logger.debug("{}: Switch light {}", thingName, command);
                        ShellyShortLightStatus light = api.setLightTurn(lightId,
                                command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
                        col.power = getOnOff(light.ison);
                        col.setBrightness(light.brightness);
                        String brightnessGroup = buildWhiteGroupName(profile, lightId);
                        updateChannel(brightnessGroup, CHANNEL_BRIGHTNESS + "$Value", toQuantityType(
                                (double) (col.power == OnOffType.ON ? col.brightness : 0), DIGITS_NONE, Units.PERCENT));
                        update = false;
                        break;
                    }

                    if (profile.isBulb) {
                        // setting the white-mode brightness implies white mode, switch if currently in color mode
                        col.setMode(SHELLY_MODE_WHITE);
                    }
                    if (command instanceof PercentType percentCommand) {
                        Float percent = percentCommand.floatValue();
                        value = percent.intValue(); // 0..100% = 0..100
                        logger.debug("{}: Set brightness to {}%/{}", thingName, percent, value);
                    } else if (command instanceof DecimalType decimalCommand) {
                        value = decimalCommand.intValue();
                        logger.debug("{}: Set brightness to {} (Integer)", thingName, value);
                    }
                    if (value == 0) {
                        logger.debug("{}: Brightness=0 -> switch light OFF", thingName);
                        api.setLightTurn(lightId, SHELLY_API_OFF);
                        update = false;
                    } else {
                        if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                            ShellyShortLightStatus light = api.getLightStatus(lightId);
                            if (increaseDecreaseCommand.equals(IncreaseDecreaseType.INCREASE)) {
                                value = Math.min(light.brightness + DIM_STEPSIZE, 100);
                            } else {
                                value = Math.max(light.brightness - DIM_STEPSIZE, 0);
                            }
                            logger.trace("{}: Change brightness from {} to {}", thingName, light.brightness, value);
                        }

                        validateRange("brightness", value, 0, 100);
                        logger.debug("{}: Changing brightness from {} to {}", thingName, oldCol.brightness, value);
                        col.setBrightness(value);
                    }
                    if (hasPowerChannel(profile)
                            && (!profile.isProRgbwwPm || CHANNEL_GROUP_LIGHT_CONTROL.equals(groupName))) {
                        updateChannel(CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_LIGHT_POWER, OnOffType.from(value > 0));
                    }
                    break;

                case CHANNEL_COLOR_TEMP:
                    Integer temp = -1;
                    if (command instanceof PercentType percentCommand) {
                        logger.debug("{}: Set color temp to {}%", thingName, percentCommand.floatValue());
                        Float percent = percentCommand.floatValue() / 100;
                        temp = new DecimalType(col.minTemp + ((col.maxTemp - col.minTemp)) * percent).intValue();
                        logger.debug("{}: Converted color-temp {}% to {}K (from Percent to Integer)", thingName,
                                percent, temp);
                    } else if (command instanceof DecimalType decimalCommand) {
                        temp = decimalCommand.intValue();
                        logger.debug("{}: Set color temp to {}K (Integer)", thingName, temp);
                    } else if (command instanceof QuantityType<?> genericQuantity) {
                        QuantityType<?> kelvinQuantity = genericQuantity.toInvertibleUnit(Units.KELVIN);
                        if (kelvinQuantity != null) {
                            temp = kelvinQuantity.intValue();
                            logger.debug("{}: Set color temp to {}K (Integer)", thingName, temp);
                        }
                    }
                    validateRange(CHANNEL_COLOR_TEMP, temp, col.minTemp, col.maxTemp);
                    col.setTemp(temp);
                    col.brightness = -1;
                    selectRgbcctMode(col, SHELLY_MODE_WHITE);
                    break;

                case CHANNEL_COLOR_EFFECT:
                    Integer effect = ((DecimalType) command).intValue();
                    logger.debug("{}: Set color effect to {}", thingName, effect);
                    validateRange("effect", effect, SHELLY_MIN_EFFECT, SHELLY_MAX_EFFECT);
                    col.setEffect(effect.intValue());
                    break;

                case CHANNEL_TIMER_AUTOON:
                    logger.debug("{}: Set Auto-ON timer to {}", thingName, command);
                    api.setAutoTimer(lightId, SHELLY_TIMER_AUTOON, getNumber(command).doubleValue());
                    update = false;
                    break;

                case CHANNEL_TIMER_AUTOOFF:
                    logger.debug("{}: Set Auto-OFF timer to {}", thingName, command);
                    api.setAutoTimer(lightId, SHELLY_TIMER_AUTOOFF, getNumber(command).doubleValue());
                    update = false;
                    break;
            }

            if (update) {
                // Gen1 Bulb switches color mode with a separate settings call; the Multicolor Bulb G3 gets the mode
                // combined into the RGBCCT.Set request built by sendColors()
                if (profile.isBulb && isModeSwitch(oldCol, col)) {
                    logger.debug("{}: Color mode changed from {} to {}, set new mode", thingName, oldCol.mode,
                            col.mode);
                    api.setLightMode(col.mode);
                    // make sure the UI promptly reflects the new mode rather than waiting for the next poll
                    requestUpdates(1, false);
                }

                // send changed colors to the device
                sendColors(profile, lightId, oldCol, col, config.getBrightnessAutoOn());
            }
            return true;
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to handle command: {}", thingName, e.toString());
            return false;
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Unable to handle command", thingName, e);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private boolean handleColorPicker(ShellyDeviceProfile profile, Integer lightId, ShellyColorUtils col,
            Command command) throws ShellyApiException {
        boolean updated = false;
        if (command instanceof HSBType hsb) {
            logger.debug("HSB-Info={}, Hue={}, getRGB={}, toRGB={}/{}/{}", hsb, hsb.getHue(),
                    String.format("0x%08X", hsb.getRGB()), hsb.toRGB()[0], hsb.toRGB()[1], hsb.toRGB()[2]);
            if (hsb.toString().contains("360,")) {
                logger.trace("{}: need to fix the Hue value (360->0)", thingName);
                HSBType fixHue = new HSBType(new DecimalType(0), hsb.getSaturation(), hsb.getBrightness());
                hsb = fixHue;
            }

            col.setRed(getColorFromHSB(hsb.getRed()));
            col.setBlue(getColorFromHSB(hsb.getBlue()));
            col.setGreen(getColorFromHSB(hsb.getGreen()));
            col.setBrightness(getColorFromHSB(hsb.getBrightness(), BRIGHTNESS_FACTOR));
            // white, gain and temp are not part of the HSB color scheme
            if (profile.isBulb) {
                // picking a color implies color mode, switch if the bulb is currently in white mode
                col.setMode(SHELLY_MODE_COLOR);
            }
            selectRgbcctMode(col, SHELLY_MODE_COLOR);
            updated = true;
        } else if (command instanceof PercentType percentCommand) {
            if (pickerControlsBrightness(profile, lightId)) {
                col.setBrightness(percentCommand.intValue());
                updated = true;
            }
        } else if (command instanceof OnOffType onOffCommand) {
            logger.debug("{}: Switch light {}", thingName, onOffCommand);
            api.setLightParm(lightId, SHELLY_LIGHT_TURN, onOffCommand == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
            col.power = onOffCommand;
        } else if (command instanceof IncreaseDecreaseType) {
            if (pickerControlsBrightness(profile, lightId)) {
                logger.debug("{}: {} brightness by {}", thingName, command, SHELLY_DIM_STEPSIZE);
                int currentBrightness = col.brightness;
                int newBrightness = currentBrightness;
                if (command == IncreaseDecreaseType.DECREASE) {
                    newBrightness = Math.max(currentBrightness - SHELLY_DIM_STEPSIZE, 0);
                } else {
                    newBrightness = Math.min(currentBrightness + SHELLY_DIM_STEPSIZE, SHELLY_MAX_BRIGHTNESS);
                }
                col.setBrightness(newBrightness);
                updated = currentBrightness != newBrightness;
            }
        }
        return updated;
    }

    private boolean handleFullColor(ShellyColorUtils col, Command command) throws IllegalArgumentException {
        String color = command.toString().toLowerCase(Locale.ROOT);
        if (color.contains(",")) {
            col.fromRGBW(color);
        } else if (color.equals(SHELLY_COLOR_RED)) {
            col.setRGBW(SHELLY_MAX_COLOR, 0, 0, 0);
        } else if (color.equals(SHELLY_COLOR_GREEN)) {
            col.setRGBW(0, SHELLY_MAX_COLOR, 0, 0);
        } else if (color.equals(SHELLY_COLOR_BLUE)) {
            col.setRGBW(0, 0, SHELLY_MAX_COLOR, 0);
        } else if (color.equals(SHELLY_COLOR_YELLOW)) {
            col.setRGBW(SHELLY_MAX_COLOR, SHELLY_MAX_COLOR, 0, 0);
        } else if (color.equals(SHELLY_COLOR_WHITE)) {
            if (profile.isProRgbwwPm) {
                // RGB component has no white output, mix full RGB instead
                col.setRGBW(SHELLY_MAX_COLOR, SHELLY_MAX_COLOR, SHELLY_MAX_COLOR, 0);
            } else if (!(profile.isDuo && profile.isRGBCCT)) {
                // the Multicolor Bulb G3 switches its shared LEDs to CCT mode instead, the RGB values stay untouched
                col.setRGBW(0, 0, 0, SHELLY_MAX_COLOR);
            }
            col.setMode(SHELLY_MODE_WHITE);
        } else {
            throw new IllegalArgumentException("Invalid full color selection: " + color);
        }
        col.setMode(color.equals(SHELLY_MODE_WHITE) ? SHELLY_MODE_WHITE : SHELLY_MODE_COLOR);
        return true;
    }

    private ShellyColorUtils getCurrentColors(int lightId) {
        ShellyColorUtils col = channelColors.get(lightId);
        if (col == null) {
            col = new ShellyColorUtils(); // create a new entry
            col.setMinMaxTemp(profile.getMinTemp(lightId), profile.getMaxTemp(lightId));
            channelColors.put(lightId, col);
            logger.trace("{}: Colors entry created for lightId {}", thingName, lightId);
        } else {
            logger.trace(
                    "{}: Colors loaded for lightId {}: power={}, RGBW={}/{}/{}/{}, gain={}, brightness={}, color temp={} (min={}, max={}",
                    thingName, lightId, col.power, col.red, col.green, col.blue, col.white, col.gain, col.brightness,
                    col.temp, col.minTemp, col.maxTemp);
        }
        return col;
    }

    @Override
    public boolean updateDeviceStatus(ShellySettingsStatus genericStatus) throws ShellyApiException {
        if (!profile.isInitialized()) {
            logger.debug("{}: Device not yet initialized!", thingName);
            return false;
        }
        if (!profile.isLight) {
            logger.debug("{}: ERROR: Device is not a light. but class ShellyHandlerLight is called!", thingName);
        }

        ShellyStatusLight status = api.getLightStatus();
        logger.trace("{}: Updating light status in {} mode, {} channel(s)", thingName, profile.device.mode,
                status.lights.size());

        // In white mode we have multiple channels
        int lightId = 0;
        boolean updated = false;
        for (ShellyStatusLightChannel light : status.lights) {
            String controlGroup = profile.getControlGroup(lightId);
            createLightChannels(light, lightId);
            // The bulb has a combined channel set for color or white mode
            // The RGBW2 uses 2 different thing types: color=1 channel, white=4 channel
            if (profile.isBulb) {
                updateChannel(CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_LIGHT_COLOR_MODE, getOnOff(profile.inColor));
            }

            ShellyColorUtils col = getCurrentColors(lightId);
            col.power = getOnOff(light.ison);

            List<ShellySettingsRgbwLight> lights = profile.settings.lights;
            if (lights != null) {
                // Channel control/timer
                ShellySettingsRgbwLight ls = lights.get(lightId);
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOON,
                        toQuantityType(getDouble(ls.autoOn), Units.SECOND));
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOOFF,
                        toQuantityType(getDouble(ls.autoOff), Units.SECOND));
                if (hasPowerChannel(profile)) {
                    updated |= updateChannel(controlGroup, CHANNEL_LIGHT_POWER, col.power);
                }
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_ACTIVE, getOnOff(light.hasTimer));
            }

            if (getBool(light.overpower)) {
                postEvent(ALARM_TYPE_OVERPOWER, false);
            }

            if (profile.hasColorTag(lightId)) {
                logger.trace("{}: update color settings", thingName);
                col.setRGBW(getInteger(light.red), getInteger(light.green), getInteger(light.blue),
                        getInteger(light.white));
                col.setGain(getInteger(light.gain));
                col.setEffect(getInteger(light.effect));

                String colorGroup = CHANNEL_GROUP_COLOR_CONTROL;
                logger.trace("{}: Update channels for group {}: RGBW={}/{}/{}, in %:{}%/{}%/{}%, white={}%, gain={}%",
                        thingName, colorGroup, col.red, col.green, col.blue, col.percentRed, col.percentGreen,
                        col.percentBlue, col.percentWhite, col.percentGain);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_RED, col.percentRed);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GREEN, col.percentGreen);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_BLUE, col.percentBlue);
                if (!profile.isProRgbwwPm) { // RGB component has no white output
                    updated |= updateChannel(colorGroup, CHANNEL_COLOR_WHITE, col.percentWhite);
                }
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GAIN, col.percentGain);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_EFFECT, getDecimal(col.effect));
                setFullColor(colorGroup, col);

                logger.trace("{}: update {}.color picker", thingName, colorGroup);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_PICKER, col.toHSB());
            }

            if (updatesWhiteChannels(profile, lightId)) {
                String whiteGroup = buildWhiteGroupName(profile, lightId);
                col.setBrightness(getInteger(light.brightness));
                updated |= updateChannel(whiteGroup, CHANNEL_BRIGHTNESS + "$Value",
                        toQuantityType(col.power == OnOffType.ON ? col.percentBrightness.doubleValue() : 0, DIGITS_NONE,
                                Units.PERCENT));

                boolean gen3Bulb = profile.isDuo && profile.isGen2;
                if ((profile.isBulb || profile.isDuo || profile.isCctComponent(lightId)) && (light.temp != null)) {
                    col.setTemp(getInteger(light.temp));
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_TEMP,
                            gen3Bulb ? toQuantityType(light.temp, Units.KELVIN) : col.percentTemp);
                    if ((profile.isBulb || profile.isDuo) && !gen3Bulb) {
                        logger.trace("{}: update {}.color picker", thingName, whiteGroup);
                        updated |= updateChannel(whiteGroup, CHANNEL_COLOR_PICKER, col.toHSB());
                    }
                } else if (gen3Bulb && profile.inColor) {
                    // the shared LEDs are in RGB mode, the last reported color temperature no longer applies
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_TEMP, UnDefType.UNDEF);
                }
            }

            // continue with next light
            lightId++;
        }
        return updated;
    }

    // Bulbs and Duo/Multicolor Bulb G3 always report white/temp alongside color; RGBW2/RGBW PM only for a
    // component that isn't the color one (this also covers a hybrid profile's secondary CCT/Light component,
    // whose color slot is a different index).
    private static boolean updatesWhiteChannels(ShellyDeviceProfile profile, int lightId) {
        return profile.isBulb || profile.isDuo
                || (!profile.hasColorTag(lightId) && (!profile.isGen2 || profile.isRGBW2));
    }

    // Duo/Multicolor Bulb G3 have no power channel, brightness 0 turns them off
    private static boolean hasPowerChannel(ShellyDeviceProfile profile) {
        return !(profile.isDuo && profile.isGen2);
    }

    // Bulbs expose the brightness through the color picker too, RGBW2 only while the picker's slot is in white mode
    private static boolean pickerControlsBrightness(ShellyDeviceProfile profile, int lightId) {
        return profile.isBulb || (profile.isDuo && profile.isGen2) || !profile.hasColorTag(lightId);
    }

    // The Multicolor Bulb G3 shares its LEDs between RGB and CCT mode: a color command must switch to RGB mode and a
    // color temperature back to CCT mode so the new value actually takes effect
    private void selectRgbcctMode(ShellyColorUtils col, String mode) {
        if (profile.isDuo && profile.isRGBCCT) {
            col.setMode(mode);
        }
    }

    private void createLightChannels(ShellyStatusLightChannel status, int idx) {
        if (!areChannelsCreated()) {
            updateChannelDefinitions(ShellyChannelDefinitions.createLightChannels(getThing(), profile, status, idx));
        }
    }

    private Integer setColor(Integer lightId, String colorName, Command command, Integer minValue, Integer maxValue)
            throws ShellyApiException, IllegalArgumentException {
        Integer value = -1;
        logger.debug("{}: Set {} to {} ({})", thingName, colorName, command, command.getClass());
        if (command instanceof PercentType percentCommand) {
            double v = (double) maxValue * percentCommand.doubleValue() / 100.0;
            value = (int) v;
            logger.debug("{}: Value for {} is in percent: {}%={}", thingName, colorName, percentCommand, value);
        } else if (command instanceof DecimalType decimalCommand) {
            value = decimalCommand.intValue();
            logger.debug("Value for {} is a number: {}", colorName, value);
        } else if (command instanceof OnOffType onOffCommand) {
            value = onOffCommand.equals(OnOffType.ON) ? SHELLY_MAX_COLOR : SHELLY_MIN_COLOR;
            logger.debug("{}: Value for {} of type OnOff was converted to {}", thingName, colorName, value);
        } else {
            throw new IllegalArgumentException(
                    "Invalid value type for " + colorName + ": " + value + " / type " + value.getClass());
        }
        validateRange(colorName, value, minValue, maxValue);
        return value.intValue();
    }

    private Integer setColor(Integer lightId, String colorName, Command command, Integer maxValue)
            throws ShellyApiException, IllegalArgumentException {
        return setColor(lightId, colorName, command, 0, maxValue);
    }

    private void setFullColor(String colorGroup, ShellyColorUtils col) {
        if ((col.red == SHELLY_MAX_COLOR) && (col.green == SHELLY_MAX_COLOR) && (col.blue == 0)) {
            updateChannel(colorGroup, CHANNEL_COLOR_FULL, new StringType(SHELLY_COLOR_YELLOW));
        } else if ((col.red == SHELLY_MAX_COLOR) && (col.green == 0) && (col.blue == 0)) {
            updateChannel(colorGroup, CHANNEL_COLOR_FULL, new StringType(SHELLY_COLOR_RED));
        } else if ((col.red == 0) && (col.green == SHELLY_MAX_COLOR) && (col.blue == 0)) {
            updateChannel(colorGroup, CHANNEL_COLOR_FULL, new StringType(SHELLY_COLOR_GREEN));
        } else if ((col.red == 0) && (col.green == 0) && (col.blue == SHELLY_MAX_COLOR)) {
            updateChannel(colorGroup, CHANNEL_COLOR_FULL, new StringType(SHELLY_COLOR_BLUE));
        } else if ((col.red == 0) && (col.green == 0) && (col.blue == 0) && (col.white == SHELLY_MAX_COLOR)) {
            updateChannel(colorGroup, CHANNEL_COLOR_FULL, new StringType(SHELLY_COLOR_WHITE));
        } else if ((col.red == SHELLY_MAX_COLOR) && (col.green == SHELLY_MAX_COLOR) && (col.blue == SHELLY_MAX_COLOR)) {
            updateChannel(colorGroup, CHANNEL_COLOR_FULL, new StringType(SHELLY_COLOR_WHITE));
        }
    }

    private void sendColors(ShellyDeviceProfile profile, Integer lightId, ShellyColorUtils oldCol,
            ShellyColorUtils newCol, boolean autoOn) throws ShellyApiException {
        // boolean updated = false;
        Integer channelId = lightId + 1;
        Map<String, String> parms = new TreeMap<>();

        logger.trace(
                "{}: New color settings for channel {}: RGB {}/{}/{}, white={}, gain={}, brightness={}, color-temp={}",
                thingName, channelId, newCol.red, newCol.green, newCol.blue, newCol.white, newCol.gain,
                newCol.brightness, newCol.temp);
        boolean switchRgbcctMode = profile.isDuo && profile.isRGBCCT && isModeSwitch(oldCol, newCol);
        boolean inColor = switchRgbcctMode ? SHELLY_MODE_COLOR.equals(newCol.mode) : profile.hasColorTag(lightId);
        if (switchRgbcctMode) {
            logger.debug("{}: Color mode changed from {} to {}", thingName, oldCol.mode, newCol.mode);
            parms.put(SHELLY_API_MODE, newCol.mode);
        }
        if (newCol.brightness == 0 && !hasPowerChannel(profile)) {
            // Gen3 bulbs have no separate power channel: brightness=0 always means OFF, regardless of autoOn setting
            parms.put(SHELLY_LIGHT_TURN, SHELLY_API_OFF);
        } else if (autoOn && (newCol.brightness >= 0)) {
            parms.put(SHELLY_LIGHT_TURN, inColor || newCol.brightness > 0 ? SHELLY_API_ON : SHELLY_API_OFF);
        }
        if (inColor) {
            if (oldCol.red != newCol.red || oldCol.green != newCol.green || oldCol.blue != newCol.blue
                    || oldCol.white != newCol.white) {
                logger.debug("{}: Setting RGBW to {}/{}/{}/{}", thingName, newCol.red, newCol.green, newCol.blue,
                        newCol.white);
                parms.put(SHELLY_COLOR_RED, String.valueOf(newCol.red));
                parms.put(SHELLY_COLOR_GREEN, String.valueOf(newCol.green));
                parms.put(SHELLY_COLOR_BLUE, String.valueOf(newCol.blue));
                parms.put(SHELLY_COLOR_WHITE, String.valueOf(newCol.white));
            }
        }
        // on a switch back to CCT mode the color temp must be repeated, the LEDs were driven by the RGB values
        if (!inColor && (switchRgbcctMode || oldCol.temp != newCol.temp)) {
            logger.debug("{}: Setting color temp to {}", thingName, newCol.temp);
            parms.put(SHELLY_COLOR_TEMP, String.valueOf(newCol.temp));
        }
        if (oldCol.gain != newCol.gain) {
            logger.debug("{}: Setting gain to {}", thingName, newCol.gain);
            parms.put(SHELLY_COLOR_GAIN, String.valueOf(newCol.gain));
        }
        if ((newCol.brightness >= 0) && (!inColor || profile.isBulb || (profile.isDuo && profile.isRGBCCT))
                && (oldCol.brightness != newCol.brightness)) {
            logger.debug("{}: Setting brightness to {}", thingName, newCol.brightness);
            parms.put(SHELLY_COLOR_BRIGHTNESS, String.valueOf(newCol.brightness));
        }
        if (!oldCol.effect.equals(newCol.effect)) {
            logger.debug("{}: Setting effect to {}", thingName, newCol.effect);
            parms.put(SHELLY_COLOR_EFFECT, newCol.effect.toString());
        }
        if (!parms.isEmpty()) {
            logger.debug("{}: Send light settings: {}", thingName, parms);
            api.setLightParms(lightId, parms);
            updateCurrentColors(lightId, newCol);
        }
    }

    private static boolean isModeSwitch(ShellyColorUtils oldCol, ShellyColorUtils newCol) {
        return !newCol.mode.isEmpty() && !newCol.mode.equals(oldCol.mode);
    }

    private void updateCurrentColors(int lightId, ShellyColorUtils col) {
        channelColors.replace(lightId, col);
        logger.debug("{}: Colors updated for lightId {}: RGBW={}/{}/{}/{}, Sat/Gain={}, Bright={}, Temp={} ", thingName,
                lightId, col.red, col.green, col.blue, col.white, col.gain, col.brightness, col.temp);
    }

    private int getColorFromHSB(PercentType colorPercent) {
        return getColorFromHSB(colorPercent, SATURATION_FACTOR);
    }

    private int getColorFromHSB(PercentType colorPercent, double factor) {
        double value = Math.round(colorPercent.doubleValue() * factor);
        logger.trace("{}: convert {}% into {}/{} (factor={})", thingName, colorPercent, value, (int) value, factor);
        return (int) value;
    }
}
