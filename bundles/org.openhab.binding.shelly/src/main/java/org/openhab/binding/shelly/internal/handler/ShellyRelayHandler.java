/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import static org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyControlRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyShortStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.coap.ShellyCoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.util.ShellyTranslationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/***
 * The{@link ShellyRelayHandler} handles light (bulb+rgbw2) specific commands and status. All other commands will be
 * handled by the generic thing handler.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyRelayHandler extends ShellyBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(ShellyRelayHandler.class);

    /**
     * Constructor
     *
     * @param thing The thing passed by the HandlerFactory
     * @param bindingConfig configuration of the binding
     * @param coapServer coap server instance
     * @param localIP local IP of the openHAB host
     * @param httpPort port of the openHAB HTTP API
     */
    public ShellyRelayHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, final ShellyCoapServer coapServer, final String localIP,
            int httpPort, final HttpClient httpClient) {
        super(thing, translationProvider, bindingConfig, coapServer, localIP, httpPort, httpClient);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public boolean handleDeviceCommand(ChannelUID channelUID, Command command) throws ShellyApiException {
        // Process command
        String groupName = getString(channelUID.getGroupId());
        Integer rIndex = 0;
        if (groupName.startsWith(CHANNEL_GROUP_RELAY_CONTROL)
                && groupName.length() > CHANNEL_GROUP_RELAY_CONTROL.length()) {
            rIndex = Integer.parseInt(StringUtils.substringAfter(channelUID.getGroupId(), CHANNEL_GROUP_RELAY_CONTROL))
                    - 1;
        } else if (groupName.startsWith(CHANNEL_GROUP_ROL_CONTROL)
                && groupName.length() > CHANNEL_GROUP_ROL_CONTROL.length()) {
            rIndex = Integer.parseInt(StringUtils.substringAfter(channelUID.getGroupId(), CHANNEL_GROUP_ROL_CONTROL))
                    - 1;
        }

        switch (channelUID.getIdWithoutGroup()) {
            default:
                return false;

            case CHANNEL_OUTPUT:
                if (!profile.isRoller) {
                    // extract relay number of group name (relay0->0, relay1->1...)
                    logger.debug("{}: Set relay output to {}", thingName, command);
                    api.setRelayTurn(rIndex, command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
                } else {
                    logger.debug("{}: Device is in roller mode, channel command {} ignored", thingName, channelUID);
                }
                break;
            case CHANNEL_BRIGHTNESS: // e.g.Dimmer, Duo
                handleBrightness(command, rIndex);
                break;

            case CHANNEL_ROL_CONTROL_POS:
            case CHANNEL_ROL_CONTROL_CONTROL:
                logger.debug("{}: Roller command/position {}", thingName, command);
                handleRoller(command, groupName, rIndex,
                        channelUID.getIdWithoutGroup().equals(CHANNEL_ROL_CONTROL_CONTROL));

                // request updates the next 45sec to update roller position after it stopped
                requestUpdates(autoCoIoT ? 1 : 45 / UPDATE_STATUS_INTERVAL_SECONDS, false);
                break;

            case CHANNEL_TIMER_AUTOON:
                logger.debug("{}: Set Auto-ON timer to {}", thingName, command);
                api.setTimer(rIndex, SHELLY_TIMER_AUTOON, ((DecimalType) command).doubleValue());
                break;
            case CHANNEL_TIMER_AUTOOFF:
                logger.debug("{}: Set Auto-OFF timer to {}", thingName, command);
                api.setTimer(rIndex, SHELLY_TIMER_AUTOOFF, ((DecimalType) command).doubleValue());
                break;

            case CHANNEL_LED_STATUS_DISABLE:
                logger.debug("{}: Set STATUS LED disabled to {}", thingName, command);
                api.setLedStatus(SHELLY_LED_STATUS_DISABLE, command == OnOffType.ON);
                break;
            case CHANNEL_LED_POWER_DISABLE:
                logger.debug("{}: Set POWER LED disabled to {}", thingName, command);
                api.setLedStatus(SHELLY_LED_POWER_DISABLE, command == OnOffType.ON);
                break;
        }
        return true;
    }

    /**
     * PaperUI Control has a combined Slider for Brightness combined with On/Off
     * Brightness channel has 2 functions: Switch On/Off (OnOnType) and setting brightness (PercentType)
     * There is some more logic in the control. When brightness is set to 0 the control sends also an OFF command
     * When current brightness is 0 and slider will be moved the new brightness will be set, but also a ON command is
     * send.
     *
     * @param command
     * @param index
     * @throws ShellyApiException
     */
    private void handleBrightness(Command command, Integer index) throws ShellyApiException {
        Integer value = -1;
        if (command instanceof PercentType) { // Dimmer
            value = ((PercentType) command).intValue();
        } else if (command instanceof DecimalType) { // Number
            value = ((DecimalType) command).intValue();
        } else if (command instanceof OnOffType) { // Switch
            logger.debug("{}: Switch output {}", thingName, command);
            updateBrightnessChannel(index, (OnOffType) command, value);
            return;
        } else if (command instanceof IncreaseDecreaseType) {
            ShellyShortLightStatus light = api.getLightStatus(index);
            if (((IncreaseDecreaseType) command).equals(IncreaseDecreaseType.INCREASE)) {
                value = Math.min(light.brightness + DIM_STEPSIZE, 100);
            } else {
                value = Math.max(light.brightness - DIM_STEPSIZE, 0);
            }
            logger.debug("{}: Increase/Decrease brightness from {} to {}", thingName, light.brightness, value);
        }
        validateRange("brightness", value, 0, 100);

        // Switch light off on brightness = 0
        if (value == 0) {
            logger.debug("{}: Brightness=0 -> switch output OFF", thingName);
            updateBrightnessChannel(index, OnOffType.OFF, 0);
        } else {
            logger.debug("{}: Setting dimmer brightness to {}", thingName, value);
            updateBrightnessChannel(index, OnOffType.ON, value);
        }
    }

    private void updateBrightnessChannel(int lightId, OnOffType power, int brightness) throws ShellyApiException {
        if (brightness > 0) {
            api.setBrightness(lightId, brightness, config.brightnessAutoOn);
        } else {
            api.setRelayTurn(lightId, power == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
        }
        updateChannel(CHANNEL_COLOR_WHITE, CHANNEL_BRIGHTNESS + "$Switch", power);
        updateChannel(CHANNEL_COLOR_WHITE, CHANNEL_BRIGHTNESS + "$Value", toQuantityType(
                new Double(power == OnOffType.ON ? brightness : 0), DIGITS_NONE, SmartHomeUnits.PERCENT));
    }

    @Override
    public boolean updateDeviceStatus(ShellySettingsStatus status) throws ShellyApiException {
        // map status to channels
        boolean updated = false;
        updated |= updateRelays(status);
        updated |= updateDimmers(status);
        updated |= updateLed(status);
        return updated;
    }

    /**
     * Handle Roller Commands
     *
     * @param command from handleCommand()
     * @param groupName relay, roller...
     * @param index relay number
     * @param isControl true: is the Rollershutter channel, false: rollerpos channel
     * @throws ShellyApiException
     */
    private void handleRoller(Command command, String groupName, Integer index, boolean isControl)
            throws ShellyApiException {
        Integer position = -1;

        if ((command instanceof UpDownType) || (command instanceof OnOffType)) {
            ShellyControlRoller rstatus = api.getRollerStatus(index);

            if (!getString(rstatus.state).isEmpty() && !getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_STOP)) {
                boolean up = command instanceof UpDownType && (UpDownType) command == UpDownType.UP;
                boolean down = command instanceof UpDownType && (UpDownType) command == UpDownType.DOWN;
                if ((up && getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_OPEN))
                        || (down && getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_CLOSE))) {
                    logger.debug("{}: Roller is already moving ({}), ignore command {}", thingName,
                            getString(rstatus.state), command);
                    requestUpdates(1, false);
                    return;
                }
            }

            if (((command instanceof UpDownType) && UpDownType.UP.equals(command))
                    || ((command instanceof OnOffType) && OnOffType.ON.equals(command))) {
                logger.debug("{}: Open roller", thingName);
                api.setRollerTurn(index, SHELLY_ALWD_ROLLER_TURN_OPEN);
                position = SHELLY_MAX_ROLLER_POS;

            }
            if (((command instanceof UpDownType) && UpDownType.DOWN.equals(command))
                    || ((command instanceof OnOffType) && OnOffType.OFF.equals(command))) {
                logger.debug("{}: Closing roller", thingName);
                api.setRollerTurn(index, SHELLY_ALWD_ROLLER_TURN_CLOSE);
                position = SHELLY_MIN_ROLLER_POS;
            }
        } else if ((command instanceof StopMoveType) && StopMoveType.STOP.equals(command)) {
            logger.debug("{}: Stop roller", thingName);
            api.setRollerTurn(index, SHELLY_ALWD_ROLLER_TURN_STOP);
        } else {
            logger.debug("{}: Set roller to position {}", thingName, command);
            if (command instanceof PercentType) {
                PercentType p = (PercentType) command;
                position = p.intValue();
            } else if (command instanceof DecimalType) {
                DecimalType d = (DecimalType) command;
                position = d.intValue();
            } else {
                throw new IllegalArgumentException(
                        "Invalid value type for roller control/posiution" + command.getClass().toString());
            }

            // take position from RollerShutter control and map to Shelly positon (OH:
            // 0=closed, 100=open; Shelly 0=open, 100=closed)
            // take position 1:1 from position channel
            position = isControl ? SHELLY_MAX_ROLLER_POS - position : position;
            validateRange("roller position", position, SHELLY_MIN_ROLLER_POS, SHELLY_MAX_ROLLER_POS);

            logger.debug("{}: Changing roller position to {}", thingName, position);
            api.setRollerPos(index, position);
        }
        if (position != -1) {
            // make sure both are in sync
            if (isControl) {
                int pos = SHELLY_MAX_ROLLER_POS - Math.max(0, Math.min(position, SHELLY_MAX_ROLLER_POS));
                updateChannel(groupName, CHANNEL_ROL_CONTROL_CONTROL, new PercentType(pos));
            } else {
                updateChannel(groupName, CHANNEL_ROL_CONTROL_POS, new PercentType(position));
            }
        }
    }

    /**
     * Auto-create relay channels depending on relay type/mode
     */
    private void createRelayChannels(ShellyStatusRelay relays) {
        if (!areChannelsCreated()) {
            updateChannelDefinitions(ShellyChannelDefinitionsDTO.createRelayChannels(getThing(), relays));
        }
    }

    private void createRollerChannels(ShellyControlRoller roller) {
        if (!areChannelsCreated()) {
            updateChannelDefinitions(ShellyChannelDefinitionsDTO.createRollerChannels(getThing(), roller));
        }
    }

    /**
     * Update Relay/Roller channels
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     *
     * @throws ShellyApiException
     */
    public boolean updateRelays(ShellySettingsStatus status) throws ShellyApiException {
        boolean updated = false;
        // Check for Relay in Standard Mode
        if (profile.hasRelays && !profile.isRoller && !profile.isDimmer) {
            logger.trace("{}: Updating {} relay(s)", thingName, profile.numRelays);

            int i = 0;
            ShellyStatusRelay rstatus = api.getRelayStatus(i);
            createRelayChannels(rstatus);
            for (ShellyShortStatusRelay relay : rstatus.relays) {
                if ((relay.isValid == null) || relay.isValid) {
                    Integer r = i + 1;
                    String groupName = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                            : CHANNEL_GROUP_RELAY_CONTROL + r.toString();

                    if (getBool(relay.overpower)) {
                        postEvent(ALARM_TYPE_OVERPOWER, false);
                    }

                    updated |= updateChannel(groupName, CHANNEL_OUTPUT, getOnOff(relay.ison));
                    updated |= updateChannel(groupName, CHANNEL_TIMER_ACTIVE, getOnOff(relay.hasTimer));
                    if (rstatus.extTemperature != null) {
                        // Shelly 1/1PM support up to 3 external sensors
                        // for whatever reason those are not represented as an array, but 3 elements
                        if (rstatus.extTemperature.sensor1 != null) {
                            updated |= updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENDOR_TEMP1, toQuantityType(
                                    getDouble(rstatus.extTemperature.sensor1.tC), DIGITS_TEMP, SIUnits.CELSIUS));
                        }
                        if (rstatus.extTemperature.sensor2 != null) {
                            updated |= updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENDOR_TEMP2, toQuantityType(
                                    getDouble(rstatus.extTemperature.sensor2.tC), DIGITS_TEMP, SIUnits.CELSIUS));
                        }
                        if (rstatus.extTemperature.sensor3 != null) {
                            updated |= updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENDOR_TEMP3, toQuantityType(
                                    getDouble(rstatus.extTemperature.sensor3.tC), DIGITS_TEMP, SIUnits.CELSIUS));
                        }
                    }
                    if ((rstatus.extHumidity != null) && (rstatus.extHumidity.sensor1 != null)) {
                        updated |= updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_HUM, toQuantityType(
                                getDouble(rstatus.extHumidity.sensor1.hum), DIGITS_PERCENT, SmartHomeUnits.PERCENT));
                    }

                    // Update Auto-ON/OFF timer
                    ShellySettingsRelay rsettings = profile.settings.relays.get(i);
                    if (rsettings != null) {
                        updated |= updateChannel(groupName, CHANNEL_TIMER_AUTOON,
                                toQuantityType(getDouble(rsettings.autoOn), SmartHomeUnits.SECOND));
                        updated |= updateChannel(groupName, CHANNEL_TIMER_AUTOOFF,
                                toQuantityType(getDouble(rsettings.autoOff), SmartHomeUnits.SECOND));
                    }

                    // Update input(s) state
                    updated |= updateInputs(groupName, status, i);
                    i++;
                }

            }
        }

        // Check for Relay in Roller Mode
        if (profile.hasRelays && profile.isRoller && (status.rollers != null)) {
            logger.trace("{}: Updating {} rollers", thingName, profile.numRollers);
            int i = 0;

            for (ShellySettingsRoller roller : status.rollers) {
                if (roller.isValid) {
                    ShellyControlRoller control = api.getRollerStatus(i);
                    Integer relayIndex = i + 1;
                    String groupName = profile.numRollers > 1 ? CHANNEL_GROUP_ROL_CONTROL + relayIndex.toString()
                            : CHANNEL_GROUP_ROL_CONTROL;

                    createRollerChannels(control);
                    if (getString(control.state).equals(SHELLY_ALWD_ROLLER_TURN_STOP)) { // only valid in stop state
                        Integer pos = Math.max(SHELLY_MIN_ROLLER_POS,
                                Math.min(control.currentPos, SHELLY_MAX_ROLLER_POS));
                        updated |= updateChannel(groupName, CHANNEL_ROL_CONTROL_CONTROL,
                                toQuantityType(new Double(SHELLY_MAX_ROLLER_POS - pos), SmartHomeUnits.PERCENT));
                        updated |= updateChannel(groupName, CHANNEL_ROL_CONTROL_POS,
                                toQuantityType(new Double(pos), SmartHomeUnits.PERCENT));
                        scheduledUpdates = 1; // one more poll and then stop
                    }

                    updated |= updateChannel(groupName, CHANNEL_ROL_CONTROL_DIR, getStringType(control.lastDirection));
                    updated |= updateChannel(groupName, CHANNEL_ROL_CONTROL_STOPR, getStringType(control.stopReason));
                    updated |= updateInputs(groupName, status, i);

                    i++;
                }
            }
        }
        return updated;
    }

    /**
     * Update Relay/Roller channels
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     *
     * @throws ShellyApiException
     */
    public boolean updateDimmers(ShellySettingsStatus orgStatus) throws ShellyApiException {
        boolean updated = false;
        if (profile.isDimmer) {
            // We need to fixup the returned Json: The dimmer returns light[] element, which is ok, but it doesn't have
            // the same structure as lights[] from Bulb,RGBW2 and Duo. The tag gets replaced by dimmers[] so that Gson
            // maps to a different structure (ShellyShortLight).
            Gson gson = new Gson();
            ShellySettingsStatus dstatus = gson.fromJson(ShellyApiJsonDTO.fixDimmerJson(orgStatus.json),
                    ShellySettingsStatus.class);

            logger.trace("{}: Updating {} dimmers(s)", thingName, dstatus.dimmers.size());
            int l = 0;
            for (ShellyShortLightStatus dimmer : dstatus.dimmers) {
                Integer r = l + 1;
                String groupName = profile.numRelays <= 1 ? CHANNEL_GROUP_DIMMER_CONTROL
                        : CHANNEL_GROUP_DIMMER_CONTROL + r.toString();

                // On a status update we map a dimmer.ison = false to brightness 0 rather than the device's brightness
                // and send a OFF status to the same channel.
                // When the device's brightness is > 0 we send the new value to the channel and a ON command
                if (dimmer.ison) {
                    updated |= updateChannel(groupName, CHANNEL_BRIGHTNESS + "$Switch", OnOffType.ON);
                    updated |= updateChannel(groupName, CHANNEL_BRIGHTNESS + "$Value", toQuantityType(
                            new Double(getInteger(dimmer.brightness)), DIGITS_NONE, SmartHomeUnits.PERCENT));
                } else {
                    updated |= updateChannel(groupName, CHANNEL_BRIGHTNESS + "$Switch", OnOffType.OFF);
                    updated |= updateChannel(groupName, CHANNEL_BRIGHTNESS + "$Value",
                            toQuantityType(new Double(0), DIGITS_NONE, SmartHomeUnits.PERCENT));
                }

                ShellySettingsDimmer dsettings = profile.settings.dimmers.get(l);
                if (dsettings != null) {
                    updated |= updateChannel(groupName, CHANNEL_TIMER_AUTOON,
                            toQuantityType(getDouble(dsettings.autoOn), SmartHomeUnits.SECOND));
                    updated |= updateChannel(groupName, CHANNEL_TIMER_AUTOOFF,
                            toQuantityType(getDouble(dsettings.autoOff), SmartHomeUnits.SECOND));
                }

                updated |= updateInputs(groupName, orgStatus, l);
                l++;
            }
        }
        return updated;
    }

    /**
     * Update LED channels
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     */
    public boolean updateLed(ShellySettingsStatus status) {
        boolean updated = false;
        if (profile.hasLed) {
            updated |= updateChannel(CHANNEL_GROUP_LED_CONTROL, CHANNEL_LED_STATUS_DISABLE,
                    getOnOff(profile.settings.ledStatusDisable));
            updated |= updateChannel(CHANNEL_GROUP_LED_CONTROL, CHANNEL_LED_POWER_DISABLE,
                    getOnOff(profile.settings.ledPowerDisable));
        }
        return updated;
    }
}
