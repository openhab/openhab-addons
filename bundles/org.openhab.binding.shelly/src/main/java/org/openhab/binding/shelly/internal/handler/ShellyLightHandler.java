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
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.RGBX.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.binding.shelly.internal.handler.ShellyLightModel.Mode;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
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
 * will be routed of the ShellyBaseHandler.
 *
 * @author Markus Michels - Initial contribution
 * @author Andrew Fiddian-Green - Migrate to LightModel
 */
@NonNullByDefault
public class ShellyLightHandler extends ShellyBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(ShellyLightHandler.class);
    private final Map<Integer, ShellyLightModel> lightModels; // TODO do we need multiple LightModel instances?

    public ShellyLightHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingRuntimeConfig bindingConfig, final ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient, WebSocketClient webSocketClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient, webSocketClient);
        lightModels = new TreeMap<>();
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

        ShellyLightModel col = getOrCreateLightModel(lightId); // TODO do we need multiple LightModel instances?
        try {
            switch (channelUID.getIdWithoutGroup()) {
                default: // non-bulb commands will be handled by the generic handler
                    return false;

                case CHANNEL_LIGHT_POWER:
                    // TODO what is the difference between CHANNEL_LIGHT_POWER and CHANNEL_BRIGHTNESS$Switch ??
                    // TODO what is the difference between api.setLightTurn and api.setLightParm ??
                    logger.debug("{}: Switch light {}", thingName, command);
                    api.setLightParm(lightId, SHELLY_LIGHT_TURN,
                            command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
                    col.handleCommand(command);
                    requestUpdates(1, false);
                    return true;

                case CHANNEL_LIGHT_COLOR_MODE:
                    logger.debug("{}: Select color mode {}", thingName, command);
                    col.setMode(OnOffType.ON == command ? Mode.COLOR : Mode.COLOR_TEMP);
                    break;

                case CHANNEL_COLOR_PICKER:
                    // TODO what is the difference between HSB B==0 and CHANNEL_BRIGHTNESS$Switch ??
                    logger.debug("{}: Update colors from color picker", thingName);
                    col.handleCommand(command);
                    break;

                case CHANNEL_COLOR_FULL:
                    logger.debug("{}: Set colors to {}", thingName, command);
                    col.setFullColorCommand(command);
                    break;

                case CHANNEL_COLOR_RED:
                    col.setColor(R, setColor(lightId, SHELLY_COLOR_RED, command, SHELLY_MAX_COLOR));
                    break;

                case CHANNEL_COLOR_GREEN:
                    col.setColor(G, setColor(lightId, SHELLY_COLOR_GREEN, command, SHELLY_MAX_COLOR));
                    break;

                case CHANNEL_COLOR_BLUE:
                    col.setColor(B, setColor(lightId, SHELLY_COLOR_BLUE, command, SHELLY_MAX_COLOR));
                    break;

                case CHANNEL_COLOR_WHITE:
                    col.setColor(WC, setColor(lightId, SHELLY_COLOR_WHITE, command, SHELLY_MAX_COLOR));
                    break;

                case CHANNEL_COLOR_GAIN:
                    // this is the brightness when the light is in color mode
                    col.setGain(setColor(lightId, SHELLY_COLOR_GAIN, command, SHELLY_MIN_GAIN, SHELLY_MAX_GAIN));
                    break;

                case CHANNEL_BRIGHTNESS:
                    // this is the brightness when the light is in color temperature mode
                    col.handleCommand(command);
                    break;

                case CHANNEL_COLOR_TEMP:
                    col.handleColorTemperatureCommand(command);
                    break;

                case CHANNEL_COLOR_EFFECT:
                    int effect = ((DecimalType) command).intValue();
                    logger.debug("{}: Set color effect to {}", thingName, command);
                    validateRange("effect", effect, SHELLY_MIN_EFFECT, SHELLY_MAX_EFFECT);
                    col.setEffect(effect);
                    break;
            }

            if (col.isOnOffDirty()) {
                // TODO what is the difference between api.setLightTurn and api.setLightParm ?
                ShellyShortLightStatus light = api.setLightTurn(lightId,
                        OnOffType.ON == col.getOnOff() ? SHELLY_API_ON : SHELLY_API_OFF);
                col.setOnOff(light.ison);
                col.setBrightness(light.brightness.intValue());
            }

            if (col.isModeDirty() && profile.isBulb) { // TODO check for RGBW2
                logger.debug("{}: Color mode changed mode to {}", thingName, col.getMode());
                api.setLightMode(Mode.COLOR == col.getMode() ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE);
            }

            if (col.isOnOffDirty()) {
                logger.debug("{}: Switch light {}", thingName, col.getOnOff());
                api.setLightTurn(lightId, col.getOnOff() == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
            }

            // send changed light parameters (if any) to the device
            sendParameters(profile, lightId, col, config.getBrightnessAutoOn());

            // TODO do we need to update cross dependent channels (or other channels)?
            // TODO do we need to set auto-update mode on channels?

            return true;
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to handle command: {}", thingName, e.toString());
            return false;
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Unable to handle command", thingName, e);
            return false;
        } finally {
            logger.debug("{}: Handled command for lightId {} -> {}", thingName, lightId, col);
            col.clearDirtyFlags();
        }
    }

    private ShellyLightModel getOrCreateLightModel(int lightId) {
        ShellyLightModel col = lightModels.get(lightId); // TODO do we need multiple LightModel instances?
        boolean isNew = false;
        if (col == null) {
            col = ShellyLightModel.create(thing.getThingTypeUID(), profile, DIM_STEPSIZE); // create a new entry
            lightModels.put(lightId, col);
            isNew = true;
        }
        logger.trace("{}: Light model {} for lightId {} -> {}", thingName, isNew ? "created" : "loaded", lightId, col);
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
        // TODO do we need multiple LightModel instances?
        int lightId = 0;
        boolean updated = false;
        for (ShellyStatusLightChannel light : status.lights) {
            Integer channelId = lightId + 1;
            String controlGroup = buildControlGroupName(profile, channelId);
            createLightChannels(light, lightId);

            ShellyLightModel col = getOrCreateLightModel(lightId);

            // The bulb has a combined channel set for color or white mode
            // The RGBW2 uses 2 different thing types: color=1 channel, white=4 channel
            // TODO do we need multiple LightModel instances?
            if (profile.isBulb) {
                Mode mode = SHELLY_MODE_COLOR.equals(profile.device.mode) ? Mode.COLOR : Mode.COLOR_TEMP;
                col.setMode(mode);
                updateChannel(CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_LIGHT_COLOR_MODE,
                        OnOffType.from(Mode.COLOR == mode));
            }

            col.setOnOff(light.ison);

            List<ShellySettingsRgbwLight> lights = profile.settings.lights;
            if (lights != null) {
                // Channel control/timer
                ShellySettingsRgbwLight ls = lights.get(lightId);
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOON,
                        toQuantityType(getDouble(ls.autoOn), Units.SECOND));
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOOFF,
                        toQuantityType(getDouble(ls.autoOff), Units.SECOND));
                updated |= updateChannel(controlGroup, CHANNEL_LIGHT_POWER, col.getOnOffState());
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_ACTIVE, getOnOff(light.hasTimer));
            }

            if (getBool(light.overpower)) {
                postEvent(ALARM_TYPE_OVERPOWER, false);
            }

            if (profile.inColor) { // TODO check logic for all light types
                logger.trace("{}: update color settings", thingName);
                col.setRGBX(getInteger(light.red), getInteger(light.green), getInteger(light.blue),
                        getInteger(light.white));
                col.setGain(getInteger(light.gain));
                col.setEffect(getInteger(light.effect));

                String colorGroup = CHANNEL_GROUP_COLOR_CONTROL;
                logger.trace("{}: Update channels for group {} => {}", thingName, colorGroup, col);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_RED, col.getColorState(R));
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GREEN, col.getColorState(G));
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_BLUE, col.getColorState(B));
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_WHITE, col.getColorState(WC));
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GAIN, col.getGainState());
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_EFFECT, col.getEffectState());
                setFullColor(colorGroup, col);

                logger.trace("{}: update {}.color picker", thingName, colorGroup);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_PICKER,
                        col.getColor() instanceof HSBType hsb ? hsb : UnDefType.NULL);
            }

            if ((!profile.inColor && (!profile.isGen2 || profile.isRGBW2)) || profile.isBulb) {
                // TODO check logic for all light types
                String whiteGroup = buildWhiteGroupName(profile, channelId);
                col.setBrightness(getInteger(light.brightness));
                updated |= updateChannel(whiteGroup, CHANNEL_BRIGHTNESS + "$Switch", col.getOnOffState());
                updated |= updateChannel(whiteGroup, CHANNEL_BRIGHTNESS + "$Value", col.getBrightnessState());

                if ((profile.isBulb || profile.isDuo) && (light.temp != null)) {
                    // TODO check logic for all light types
                    col.setColorTemp(getInteger(light.temp));
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_TEMP, col.getColorTemperaturePercentState());
                    logger.trace("{}: update {}.color picker", thingName, whiteGroup);
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_PICKER, col.getColorState());
                }
            }

            // continue with next light
            lightId++; // TODO do we need multiple LightModel instances?
        }
        return updated;
    }

    private void createLightChannels(ShellyStatusLightChannel status, int idx) {
        if (!areChannelsCreated()) {
            // TODO add absolute color temperature channel
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

    private void setFullColor(String colorGroup, ShellyLightModel col) {
        double[] rgbw = col.getRGBx();
        String color = null;
        if (rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == SHELLY_MAX_COLOR && rgbw[2] == 0) {
            color = SHELLY_COLOR_YELLOW;
        } else if (rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == 0 && rgbw[2] == 0) {
            color = SHELLY_COLOR_RED;
        } else if (rgbw[0] == 0 && rgbw[1] == SHELLY_MAX_COLOR && rgbw[2] == 0) {
            color = SHELLY_COLOR_GREEN;
        } else if (rgbw[0] == 0 && rgbw[1] == 0 && rgbw[2] == SHELLY_MAX_COLOR) {
            color = SHELLY_COLOR_BLUE;
        } else if (rgbw[0] == 0 && rgbw[1] == 0 && rgbw[2] == 0 && rgbw[2] == SHELLY_MAX_COLOR) {
            color = SHELLY_COLOR_WHITE;
        }
        updateChannel(colorGroup, CHANNEL_COLOR_FULL, color != null ? new StringType(color) : UnDefType.UNDEF);
    }

    private void sendParameters(ShellyDeviceProfile profile, Integer lightId, ShellyLightModel col, boolean autoOn)
            throws ShellyApiException {
        Integer channelId = lightId + 1;
        Map<String, String> parms = new TreeMap<>();

        logger.trace("{}: New color settings for channel {} -> {}", thingName, channelId, col);
        if (col.isOnOffDirty() && col.getOnOffState() instanceof OnOffType onOff && autoOn) { // TODO check autoOn logic
            logger.debug("{}: Setting OnOff to {}", thingName, onOff);
            // TODO what is the difference between api.setLightTurn and api.setLightParm and this call ?
            parms.put(SHELLY_LIGHT_TURN, OnOffType.ON == onOff ? SHELLY_API_ON : SHELLY_API_OFF);
        }
        if (col.isColorDirty() && profile.inColor) { // TODO check logic for all light types
            int[] rgbw = col.getRGBX();
            logger.debug("{}: Setting RGBW to {}", thingName, Arrays.toString(rgbw));
            parms.put(SHELLY_COLOR_RED, String.valueOf(rgbw[0]));
            parms.put(SHELLY_COLOR_GREEN, String.valueOf(rgbw[1]));
            parms.put(SHELLY_COLOR_BLUE, String.valueOf(rgbw[2]));
            parms.put(SHELLY_COLOR_WHITE, String.valueOf(rgbw[3]));
        }
        if (col.isColorTempDirty() && col.getColorTemperatureAbsoluteState() instanceof QuantityType<?> qty) {
            logger.debug("{}: Setting color temp to {}", thingName, qty);
            parms.put(SHELLY_COLOR_TEMP, String.valueOf(qty.intValue()));
        }
        if (col.isGainDirty() && col.getGainState() instanceof PercentType pct) {
            logger.debug("{}: Setting gain to {}", thingName, pct);
            parms.put(SHELLY_COLOR_GAIN, String.valueOf(pct.intValue()));
        }
        if (col.isBrightnessDirty() && col.getBrightnessState() instanceof PercentType pct) {
            logger.debug("{}: Setting brightness to {}", thingName, pct);
            parms.put(SHELLY_COLOR_BRIGHTNESS, String.valueOf(pct.intValue()));
        }
        if (col.isEffectDirty() && col.getEffectState() instanceof DecimalType dec) {
            logger.debug("{}: Setting effect to {}", thingName, dec);
            parms.put(SHELLY_COLOR_EFFECT, String.valueOf(dec.intValue()));
        }
        if (!parms.isEmpty()) {
            logger.debug("{}: Send light settings: {}", thingName, parms);
            api.setLightParms(lightId, parms);
            logger.debug("{}: Colors updated for lightId {} -> {}", thingName, lightId, parms);
        }
    }

    @Override
    public @Nullable ShellyLightModel getLightModel(int lightId) {
        return lightModels.get(lightId); // TODO do we need multiple LightModel instances?
    }
}
