/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import static org.openhab.binding.shelly.internal.ShellyUtils.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJson.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusLightChannel;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.coap.ShellyCoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyLightHandler} handles light (bulb+rgbw2) specific commands and status. All other commands will be
 * routet of the generic thing handler.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyLightHandler extends ShellyBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(ShellyLightHandler.class);
    private @Nullable Map<Integer, ShellyColorUtils> channelColors;

    /**
     * Constructor
     *
     * @param thing The thing passed by the HandlerFactory
     * @param bindingConfig configuration of the binding
     * @param coapServer coap server instance
     * @param localIP local IP of the openHAB host
     * @param httpPort port of the openHAB HTTP API
     */
    public ShellyLightHandler(Thing thing, ShellyBindingConfiguration bindingConfig,
            @Nullable ShellyCoapServer coapServer, String localIP, int httpPort) {
        super(thing, bindingConfig, coapServer, localIP, httpPort);
        channelColors = new HashMap<Integer, ShellyColorUtils>();
    }

    @Override
    public void initialize() {
        logger.debug("Thing is using class {}", this.getClass());
        super.initialize();
    }

    @SuppressWarnings("null")
    @Override
    public boolean handleDeviceCommand(ChannelUID channelUID, Command command) throws IOException {
        String groupName = channelUID.getGroupId();
        Integer lightId = getLightIdFromGroup(groupName);
        logger.trace("{}: Execute command {} on channel {}, lightId={}", thingName, command.toString(),
                channelUID.getAsString(), lightId);

        Validate.notNull(profile, "DeviceProfile must not be null, thing not initialized");
        Validate.notNull(api, "handleCommand(): api must not be null!");
        ShellyColorUtils oldCol = getCurrentColors(lightId);
        Validate.notNull(oldCol, "oldCol must not be null");
        oldCol.mode = profile.mode;
        ShellyColorUtils col = new ShellyColorUtils(oldCol);
        Validate.notNull(oldCol, "copy of oldCol must not be null");

        boolean update = true;
        switch (channelUID.getIdWithoutGroup()) {
            default: // non-bulb commands will be handled by the generic handler
                return false;

            case CHANNEL_LIGHT_POWER:
                logger.debug("{}: Switch light {}", thingName, command.toString());
                Validate.isTrue(command instanceof OnOffType, "Invalid value for power (ON or OFF): {}",
                        command.toString());
                api.setLightParm(lightId, SHELLY_LIGHT_TURN,
                        (OnOffType) command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
                col.power = (OnOffType) command;
                update = (OnOffType) command == OnOffType.ON;
                break;
            case CHANNEL_LIGHT_COLOR_MODE:
                logger.debug("{}: Select color mode {}", thingName, command.toString());
                Validate.isTrue(command instanceof OnOffType,
                        "Invalid value for color mode (ON or OFF): " + command.toString());
                col.setMode((OnOffType) command == OnOffType.ON ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE);
                break;
            case CHANNEL_COLOR_PICKER:
                logger.debug("{}: Update colors from color picker", thingName);
                update = handleColorPicker(profile, lightId, col, command);
                break;
            case CHANNEL_COLOR_FULL:
                logger.debug("{}: Set colors to {}", thingName, command.toString());
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
                Integer value = -1;
                if (command instanceof OnOffType) { // Switch
                    logger.debug("Switch light {}", command.toString());
                    api.setRelayTurn(lightId, (OnOffType) command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
                    requestUpdates(1, false);
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
                    api.setRelayTurn(lightId, SHELLY_API_OFF);
                    requestUpdates(1, false);
                    break;
                }

                if (command instanceof IncreaseDecreaseType) {
                    ShellyShortLightStatus light = api.getLightStatus(lightId);
                    Validate.notNull(light, "Unable to get Light status for brightness");

                    if (((IncreaseDecreaseType) command).equals(IncreaseDecreaseType.INCREASE)) {
                        value = Math.min(light.brightness + DIM_STEPSIZE, 100);
                    } else {
                        value = Math.max(light.brightness - DIM_STEPSIZE, 0);
                    }
                    logger.trace("{}: Change brightness from {} to {}", thingName, light.brightness, value);
                }

                validateRange("brightness", value, 0, 100);
                logger.debug("{}: Set brightness to", value);
                col.setBrightness(value);
                break;

            case CHANNEL_COLOR_TEMP:
                Integer temp = -1;
                if (command instanceof PercentType) {
                    logger.debug("{}: Set color temp to {}%", thingName, ((PercentType) command).floatValue());
                    Float percent = ((PercentType) command).floatValue() / 100;
                    temp = new DecimalType(
                            MIN_COLOR_TEMPERATURE + ((MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE)) * percent)
                                    .intValue();
                    logger.debug("{}: Converted color-temp {}% to {}K (from Percent to Integer)", thingName, percent,
                            temp);
                } else if (command instanceof DecimalType) {
                    temp = ((DecimalType) command).intValue();
                    logger.debug("{}: Set color temp to {}K (Integer)", thingName, temp);
                }
                validateRange(CHANNEL_COLOR_TEMP, temp, MIN_COLOR_TEMPERATURE, MAX_COLOR_TEMPERATURE);
                col.setTemp(temp);
                break;

            case CHANNEL_COLOR_EFFECT:
                Integer effect = ((DecimalType) command).intValue();
                logger.debug("{}: Set color effect to {}", thingName, effect);
                validateRange("effect", effect, SHELLY_MIN_EFFECT, SHELLY_MAX_EFFECT);
                col.setEffect(effect.intValue());
                break;
        }

        if (update) {
            // check for switching color mode
            if (profile.isBulb && !col.mode.isEmpty() && !col.mode.equals(oldCol.mode)) {
                logger.debug("{}: Color mode changed from {} to {}, set new mode", thingName, oldCol.mode, col.mode);
                api.setLightMode(col.mode);
            }

            // send changed colors to the device
            sendColors(profile, lightId, oldCol, col);
        }

        return true;

    }

    @SuppressWarnings("null")
    private boolean handleColorPicker(@Nullable ShellyDeviceProfile profile, Integer lightId, ShellyColorUtils col,
            Command command) throws IOException {
        Validate.notNull(profile);

        boolean updated = false;
        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;

            logger.debug("HSB-Info={}, Hue={}, getRGB={}, toRGB={}/{}/{}", hsb.toString(), hsb.getHue(),
                    String.format("0x%08X", hsb.getRGB()), hsb.toRGB()[0], hsb.toRGB()[1], hsb.toRGB()[2]);
            if (hsb.toString().contains("360,")) {
                logger.debug("need to fix the Hue value (360->0)");
                HSBType fixHue = new HSBType(new DecimalType(0), hsb.getSaturation(), hsb.getBrightness());
                hsb = fixHue;
            }

            col.setRed(getColorFromHSB(hsb.getRed())); // new Double((hsb.getRed().floatValue() *
                                                       // SATURATION_FACTOR)).intValue();
            col.setBlue(getColorFromHSB(hsb.getBlue())); // new Double((hsb.getBlue().floatValue() *
                                                         // SATURATION_FACTOR)).intValue();
            col.setGreen(getColorFromHSB(hsb.getGreen())); // new Double((hsb.getGreen().floatValue() *
                                                           // SATURATION_FACTOR)).intValue();
            col.setBrightness(getColorFromHSB(hsb.getBrightness(), BRIGHTNESS_FACTOR)); // new
                                                                                        // Double((hsb.getBrightness().floatValue()
                                                                                        // *
            // white, gain and temp are not part of the HSB color scheme
            updated = true;
        } else if (command instanceof PercentType) {
            if (!profile.inColor) {
                col.brightness = SHELLY_MAX_BRIGHTNESS * ((PercentType) command).intValue();
                updated = true;
            }
        } else if (command instanceof OnOffType) {
            logger.debug("{}: Switch light {}", thingName, command);
            api.setLightParm(lightId, SHELLY_LIGHT_TURN,
                    (OnOffType) command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
            col.power = (OnOffType) command;
        } else if (command instanceof IncreaseDecreaseType) {
            if (!profile.inColor) {
                logger.debug("{}: {} brightness by {}", thingName, command.toString(), SHELLY_DIM_STEPSIZE);
                Double percent = ((PercentType) getChannelValue(CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_BRIGHTNESS))
                        .doubleValue();
                Integer currentBrightness = percent.intValue() * SHELLY_MAX_BRIGHTNESS;
                Integer newBrightness;
                if (command == IncreaseDecreaseType.DECREASE) {
                    newBrightness = Math.max(currentBrightness - SHELLY_DIM_STEPSIZE, 0);
                } else {
                    newBrightness = Math.min(currentBrightness + SHELLY_DIM_STEPSIZE, SHELLY_MAX_BRIGHTNESS);
                }
                col.brightness = newBrightness;
                updated = true;
            }
        }
        return updated;
    }

    private boolean handleFullColor(ShellyColorUtils col, Command command)
            throws IOException, IllegalArgumentException {
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

    @SuppressWarnings({ "null", "unused" })
    private ShellyColorUtils getCurrentColors(Integer lightId) {
        Validate.notNull(channelColors);
        @Nullable
        ShellyColorUtils col = channelColors.get(lightId);
        if (col == null) {
            col = new ShellyColorUtils(); // create a new entry
            logger.trace("Colors entry created for lightId {}", lightId.toString());
        } else {
            logger.trace("Colors loaded for lightId {}: RGBW={}/{}/{}/{}, gain={}, brightness={}, color temp={} ",
                    lightId.toString(), col.red, col.green, col.blue, col.white, col.gain, col.brightness, col.temp);
        }
        return col;
    }

    @SuppressWarnings("null")
    @Override
    public boolean updateDeviceStatus(ShellySettingsStatus genericStatus) throws IOException {
        Validate.notNull(profile, "updateThingStatus(): profile must not be null!");
        Validate.isTrue(profile.isLight,
                "ERROR: Device " + profile.hostname + " is not a light. but class ShellyHandlerLight is called!");

        Validate.notNull(profile);
        ShellyStatusLight status = api.getLightStatus();
        Validate.notNull(status, "updateThingStatus(): status must not be null!");
        logger.trace("Updating bulb/rgw2 status for {}, in {} mode, {} channel(s)", profile.hostname, profile.mode,
                status.lights.size());

        // In white mode we have multiple channels
        int lightId = 0;
        boolean updated = false;
        for (ShellyStatusLightChannel light : status.lights) {
            Integer channelId = lightId + 1;
            logger.trace("Updating lightId {}/{}", lightId, channelId.toString());
            String controlGroup = buildControlGroupName(profile, channelId);

            logger.trace("Updating light channels {}.{} (mode={})", profile.hostname, controlGroup,
                    getString(profile.settings.mode));

            // The bulb has a combined channel set for color or white mode
            // The RGBW2 uses 2 different thing types: color=1 channel, white=4 channel
            if (profile.isBulb) {
                updateChannel(CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_LIGHT_COLOR_MODE, getOnOff(profile.inColor));
            }

            ShellyColorUtils col = getCurrentColors(lightId);
            Validate.notNull(col);
            col.power = getBool(light.ison) ? OnOffType.ON : OnOffType.OFF;

            // Channel control/timer
            // ShellyStatusLightChannel light = status.lights.get(i);
            updated |= updateChannel(controlGroup, CHANNEL_LIGHT_POWER, getOnOff(light.ison));
            updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOON, getDecimal(light.autoOn));
            updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOOFF, getDecimal(light.autoOff));
            if (getBool(light.overpower)) {
                postAlarm(ALARM_TYPE_OVERPOWER, false);
            }

            if (profile.inColor || profile.isBulb) {
                logger.trace("update color settings");
                col.setRGBW(getInteger(light.red), getInteger(light.green), getInteger(light.blue),
                        getInteger(light.white));
                col.setGain(getInteger(light.gain));
                col.setEffect(getInteger(light.effect));

                String colorGroup = CHANNEL_GROUP_COLOR_CONTROL;
                logger.trace("Update channels for {}: RGBW={}/{}/{}, in %:{}%/{}%/{}%, white={}%, gain={}%", colorGroup,
                        col.red, col.green, col.blue, col.percentRed, col.percentGreen, col.percentBlue,
                        col.percentWhite, col.percentGain);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_RED, col.percentRed);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GREEN, col.percentGreen);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_BLUE, col.percentBlue);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_WHITE, col.percentWhite);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GAIN, col.percentGain);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_EFFECT, new DecimalType(col.effect));
                setFullColor(colorGroup, col);

                logger.trace("update {}.color picker", colorGroup);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_PICKER, col.toHSB());
            }
            if (!profile.inColor || profile.isBulb) {
                String whiteGroup = buildWhiteGroupName(profile, channelId);
                logger.trace("update white settings for {}.{}", whiteGroup, channelId);
                col.setBrightness(getInteger(light.brightness));
                updated |= updateChannel(whiteGroup, CHANNEL_BRIGHTNESS, col.percentBrightness);
                if (profile.isBulb) {
                    col.setTemp(getInteger(light.temp));
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_TEMP, col.percentTemp);
                    logger.trace("update {}.color picker", whiteGroup);
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_PICKER, col.toHSB());
                }
            }

            // continue with next light
            lightId++;
        }
        return updated;
    }

    private Integer setColor(Integer lightId, String colorName, Command command, Integer minValue, Integer maxValue)
            throws IOException, IllegalArgumentException {
        Integer value = -1;
        logger.debug("Set {} to {} ({})", colorName, command, command.getClass());
        if (command instanceof PercentType) {
            PercentType percent = (PercentType) command;
            Double v = new Double(maxValue) * percent.doubleValue() / 100.0;
            value = v.intValue();
            logger.debug("Value for {} is in percent: {}%={}", colorName, percent, value);
        } else if (command instanceof DecimalType) {
            value = ((DecimalType) command).intValue();
            logger.debug("Value for {} is a number: {}", colorName, value);
        } else if (command instanceof OnOffType) {
            value = ((OnOffType) command).equals(OnOffType.ON) ? SHELLY_MAX_COLOR : SHELLY_MIN_COLOR;
            logger.debug("Value for {} of type OnOff was converted to {}", colorName, value);
        } else {
            throw new IllegalArgumentException(
                    "Invalid value type for " + colorName + ": " + value.toString() + " / type " + value.getClass());
        }
        validateRange(colorName, value, minValue, maxValue);
        return value.intValue();
    }

    private Integer setColor(Integer lightId, String colorName, Command command, Integer maxValue)
            throws IOException, IllegalArgumentException {
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

    private void sendColors(@Nullable ShellyDeviceProfile profile, Integer lightId, ShellyColorUtils oldCol,
            ShellyColorUtils newCol) throws IOException {
        Validate.notNull(profile);

        // boolean updated = false;
        Integer channelId = lightId + 1;
        Map<String, String> parms = new HashMap<String, String>();

        logger.trace(
                "{}: New color settings for channel {}: RGB {}/{}/{}, white={}, gain={}, brightness={}, color-temp={}",
                thingName, channelId, newCol.red, newCol.green, newCol.blue, newCol.white, newCol.gain,
                newCol.brightness, newCol.temp);
        parms.put(SHELLY_LIGHT_TURN, profile.inColor || newCol.brightness > 0 ? SHELLY_API_ON : SHELLY_API_OFF);
        if (profile.inColor) {
            if (!oldCol.red.equals(newCol.red) || !oldCol.green.equals(newCol.green) || !oldCol.blue.equals(newCol.blue)
                    || !oldCol.white.equals(newCol.white)) {
                logger.debug("{}: Setting RGBW to {}/{}/{}/{}", thingName, newCol.red, newCol.green, newCol.blue,
                        newCol.white);
                parms.put(SHELLY_COLOR_RED, newCol.red.toString());
                parms.put(SHELLY_COLOR_GREEN, newCol.green.toString());
                parms.put(SHELLY_COLOR_BLUE, newCol.blue.toString());
                parms.put(SHELLY_COLOR_WHITE, newCol.white.toString());
            }
        }
        if ((!profile.inColor) && !oldCol.temp.equals(newCol.temp)) {
            logger.debug("Setting color temp to {}", newCol.temp);
            parms.put(SHELLY_COLOR_TEMP, newCol.temp.toString());
        }
        if (!oldCol.gain.equals(newCol.gain)) {
            logger.debug("Setting gain to {}", newCol.gain);
            parms.put(SHELLY_COLOR_GAIN, newCol.gain.toString());
        }
        if ((profile.isBulb || !profile.inColor) && !oldCol.brightness.equals(newCol.brightness)) {
            logger.debug("Setting brightness to {}", newCol.brightness);
            parms.put(SHELLY_COLOR_BRIGHTNESS, newCol.brightness.toString());
        }
        if (!oldCol.effect.equals(newCol.effect)) {
            logger.debug("Setting effect to {}", newCol.effect);
            parms.put(SHELLY_COLOR_EFFECT, newCol.effect.toString());
        }
        if (parms.size() > 0) {
            logger.debug("Send collor settings: {}", parms.toString());
            Validate.notNull(api);
            api.setLightParms(lightId, parms);
            updateCurrentColors(lightId, newCol);
        }
    }

    @SuppressWarnings({ "null", "unused" })
    private void updateCurrentColors(Integer lightId, ShellyColorUtils col) {
        if (channelColors.get(lightId) == null) {
            channelColors.put(lightId, col);
        } else {
            channelColors.replace(lightId, col);
        }
        logger.debug("Colors updated for lightId {}: RGBW={}/{}/{}/{}, Sat/Gain={}, Bright={}, Temp={} ",
                lightId.toString(), col.red, col.green, col.blue, col.white, col.gain, col.brightness, col.temp);
    }

    private Integer getColorFromHSB(PercentType colorPercent) {
        return getColorFromHSB(colorPercent, new Double(SATURATION_FACTOR));
    }

    private Integer getColorFromHSB(PercentType colorPercent, Double factor) {
        Double value = new Double(Math.round(colorPercent.doubleValue() * factor));
        logger.trace("convert {}% into {}/{} (factor={})", colorPercent.toString(), value.toString(), value.intValue(),
                factor.toString());
        return value.intValue();
    }
}
