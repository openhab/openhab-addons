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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLightChannel;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
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

    /**
     * Constructor
     *
     * @param thing The thing passed by the HandlerFactory
     * @param bindingConfig configuration of the binding
     * @param coapServer coap server instance
     * @param localIP local IP of the openHAB host
     * @param httpPort port of the openHAB HTTP API
     */
    public ShellyLightHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, final ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient);
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

        int lightId = getLightIdFromGroup(groupName);
        logger.trace("{}: Execute command {} on channel {}, lightId={}", thingName, command, channelUID.getAsString(),
                lightId);

        try {
            ShellyColorUtils oldCol = getCurrentColors(lightId);
            oldCol.mode = profile.mode;
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
                    break;
                case CHANNEL_COLOR_GREEN:
                    col.setGreen(setColor(lightId, SHELLY_COLOR_GREEN, command, SHELLY_MAX_COLOR));
                    break;
                case CHANNEL_COLOR_BLUE:
                    col.setBlue(setColor(lightId, SHELLY_COLOR_BLUE, command, SHELLY_MAX_COLOR));
                    break;
                case CHANNEL_COLOR_WHITE:
                    col.setWhite(setColor(lightId, SHELLY_COLOR_WHITE, command, SHELLY_MAX_COLOR));
                    break;
                case CHANNEL_COLOR_GAIN:
                    col.setGain(setColor(lightId, SHELLY_COLOR_GAIN, command, SHELLY_MIN_GAIN, SHELLY_MAX_GAIN));
                    break;
                case CHANNEL_BRIGHTNESS: // only in white mode
                    if (profile.inColor && !profile.isBulb) {
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
                        updateChannel(CHANNEL_COLOR_WHITE, CHANNEL_BRIGHTNESS + "$Switch", col.power);
                        updateChannel(CHANNEL_COLOR_WHITE, CHANNEL_BRIGHTNESS + "$Value", toQuantityType(
                                (double) (col.power == OnOffType.ON ? col.brightness : 0), DIGITS_NONE, Units.PERCENT));
                        update = false;
                        break;
                    }

                    if (command instanceof PercentType) {
                        Float percent = ((PercentType) command).floatValue();
                        value = percent.intValue(); // 0..100% = 0..100
                        logger.debug("{}: Set brightness to {}%/{}", thingName, percent, value);
                    } else if (command instanceof DecimalType) {
                        value = ((DecimalType) command).intValue();
                        logger.debug("{}: Set brightness to {} (Integer)", thingName, value);
                    }
                    if (value == 0) {
                        logger.debug("{}: Brightness=0 -> switch light OFF", thingName);
                        api.setLightTurn(lightId, SHELLY_API_OFF);
                        update = false;
                    } else {
                        if (command instanceof IncreaseDecreaseType) {
                            ShellyShortLightStatus light = api.getLightStatus(lightId);
                            if (((IncreaseDecreaseType) command).equals(IncreaseDecreaseType.INCREASE)) {
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
                    updateChannel(CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_LIGHT_POWER,
                            value > 0 ? OnOffType.ON : OnOffType.OFF);
                    break;

                case CHANNEL_COLOR_TEMP:
                    Integer temp = -1;
                    if (command instanceof PercentType) {
                        logger.debug("{}: Set color temp to {}%", thingName, ((PercentType) command).floatValue());
                        Float percent = ((PercentType) command).floatValue() / 100;
                        temp = new DecimalType(col.minTemp + ((col.maxTemp - col.minTemp)) * percent).intValue();
                        logger.debug("{}: Converted color-temp {}% to {}K (from Percent to Integer)", thingName,
                                percent, temp);
                    } else if (command instanceof DecimalType) {
                        temp = ((DecimalType) command).intValue();
                        logger.debug("{}: Set color temp to {}K (Integer)", thingName, temp);
                    }
                    validateRange(CHANNEL_COLOR_TEMP, temp, col.minTemp, col.maxTemp);
                    col.setTemp(temp);
                    col.brightness = -1;
                    break;

                case CHANNEL_COLOR_EFFECT:
                    Integer effect = ((DecimalType) command).intValue();
                    logger.debug("{}: Set color effect to {}", thingName, effect);
                    validateRange("effect", effect, SHELLY_MIN_EFFECT, SHELLY_MAX_EFFECT);
                    col.setEffect(effect.intValue());
            }

            if (update) {
                // check for switching color mode
                if (profile.isBulb && !col.mode.isEmpty() && !col.mode.equals(oldCol.mode)) {
                    logger.debug("{}: Color mode changed from {} to {}, set new mode", thingName, oldCol.mode,
                            col.mode);
                    api.setLightMode(col.mode);
                }

                // send changed colors to the device
                sendColors(profile, lightId, oldCol, col, config.brightnessAutoOn);
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

    private boolean handleColorPicker(ShellyDeviceProfile profile, Integer lightId, ShellyColorUtils col,
            Command command) throws ShellyApiException {
        boolean updated = false;
        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;

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
            updated = true;
        } else if (command instanceof PercentType) {
            if (!profile.inColor || profile.isBulb) {
                col.brightness = SHELLY_MAX_BRIGHTNESS * ((PercentType) command).intValue();
                updated = true;
            }
        } else if (command instanceof OnOffType) {
            logger.debug("{}: Switch light {}", thingName, command);
            api.setLightParm(lightId, SHELLY_LIGHT_TURN,
                    (OnOffType) command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
            col.power = (OnOffType) command;
        } else if (command instanceof IncreaseDecreaseType) {
            if (!profile.inColor || profile.isBulb) {
                logger.debug("{}: {} brightness by {}", thingName, command, SHELLY_DIM_STEPSIZE);
                PercentType percent = (PercentType) super.getChannelValue(CHANNEL_GROUP_COLOR_CONTROL,
                        CHANNEL_BRIGHTNESS);
                int currentBrightness = percent.intValue() * SHELLY_MAX_BRIGHTNESS;
                int newBrightness = currentBrightness;
                if (command == IncreaseDecreaseType.DECREASE) {
                    newBrightness = Math.max(currentBrightness - SHELLY_DIM_STEPSIZE, 0);
                } else {
                    newBrightness = Math.min(currentBrightness + SHELLY_DIM_STEPSIZE, SHELLY_MAX_BRIGHTNESS);
                }
                col.brightness = newBrightness;
                updated = currentBrightness != newBrightness;
            }
        }
        return updated;
    }

    private boolean handleFullColor(ShellyColorUtils col, Command command) throws IllegalArgumentException {
        String color = command.toString().toLowerCase();
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
            col.setRGBW(0, 0, 0, SHELLY_MAX_COLOR);
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
            col.setMinMaxTemp(profile.minTemp, profile.maxTemp);
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
        logger.trace("{}: Updating light status in {} mode, {} channel(s)", thingName, profile.mode,
                status.lights.size());

        // In white mode we have multiple channels
        int lightId = 0;
        boolean updated = false;
        for (ShellyStatusLightChannel light : status.lights) {
            Integer channelId = lightId + 1;
            String controlGroup = buildControlGroupName(profile, channelId);
            createLightChannels(light, lightId);
            // The bulb has a combined channel set for color or white mode
            // The RGBW2 uses 2 different thing types: color=1 channel, white=4 channel
            if (profile.isBulb) {
                updateChannel(CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_LIGHT_COLOR_MODE, getOnOff(profile.inColor));
            }

            ShellyColorUtils col = getCurrentColors(lightId);
            col.power = getOnOff(light.ison);

            if (profile.settings.lights != null) {
                // Channel control/timer
                ShellySettingsRgbwLight ls = profile.settings.lights.get(lightId);
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOON,
                        toQuantityType(getDouble(ls.autoOn), Units.SECOND));
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOOFF,
                        toQuantityType(getDouble(ls.autoOff), Units.SECOND));
                updated |= updateChannel(controlGroup, CHANNEL_LIGHT_POWER, col.power);
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_ACTIVE, getOnOff(light.hasTimer));
                updated |= updateChannel(controlGroup, CHANNEL_LIGHT_POWER, col.power);
            }

            if (getBool(light.overpower)) {
                postEvent(ALARM_TYPE_OVERPOWER, false);
            }

            if (profile.inColor) {
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
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_WHITE, col.percentWhite);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GAIN, col.percentGain);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_EFFECT, getDecimal(col.effect));
                setFullColor(colorGroup, col);

                logger.trace("{}: update {}.color picker", thingName, colorGroup);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_PICKER, col.toHSB());
            }

            if (!profile.inColor || profile.isBulb) {
                String whiteGroup = buildWhiteGroupName(profile, channelId);
                col.setBrightness(getInteger(light.brightness));
                updated |= updateChannel(whiteGroup, CHANNEL_BRIGHTNESS + "$Switch", col.power);
                updated |= updateChannel(whiteGroup, CHANNEL_BRIGHTNESS + "$Value",
                        toQuantityType(col.power == OnOffType.ON ? col.percentBrightness.doubleValue() : 0, DIGITS_NONE,
                                Units.PERCENT));

                if ((profile.isBulb || profile.isDuo) && (light.temp != null)) {
                    col.setTemp(getInteger(light.temp));
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_TEMP, col.percentTemp);
                    logger.trace("{}: update {}.color picker", thingName, whiteGroup);
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_PICKER, col.toHSB());
                }
            }

            // continue with next light
            lightId++;
        }
        return updated;
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
        if (command instanceof PercentType) {
            PercentType percent = (PercentType) command;
            double v = (double) maxValue * percent.doubleValue() / 100.0;
            value = (int) v;
            logger.debug("{}: Value for {} is in percent: {}%={}", thingName, colorName, percent, value);
        } else if (command instanceof DecimalType) {
            value = ((DecimalType) command).intValue();
            logger.debug("Value for {} is a number: {}", colorName, value);
        } else if (command instanceof OnOffType) {
            value = ((OnOffType) command).equals(OnOffType.ON) ? SHELLY_MAX_COLOR : SHELLY_MIN_COLOR;
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
        if (autoOn && (newCol.brightness >= 0)) {
            parms.put(SHELLY_LIGHT_TURN, profile.inColor || newCol.brightness > 0 ? SHELLY_API_ON : SHELLY_API_OFF);
        }
        if (profile.inColor) {
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
        if ((!profile.inColor) && (oldCol.temp != newCol.temp)) {
            logger.debug("{}: Setting color temp to {}", thingName, newCol.temp);
            parms.put(SHELLY_COLOR_TEMP, String.valueOf(newCol.temp));
        }
        if (oldCol.gain != newCol.gain) {
            logger.debug("{}: Setting gain to {}", thingName, newCol.gain);
            parms.put(SHELLY_COLOR_GAIN, String.valueOf(newCol.gain));
        }
        if ((newCol.brightness >= 0) && (!profile.inColor || profile.isBulb)
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
