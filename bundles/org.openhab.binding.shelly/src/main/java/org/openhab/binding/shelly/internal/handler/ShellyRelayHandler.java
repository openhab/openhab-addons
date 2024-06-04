/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param translationProvider
     * @param bindingConfig configuration of the binding
     * @param thingTable
     * @param coapServer coap server instance
     * @param httpClient to connect to the openHAB HTTP API
     */
    public ShellyRelayHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient);
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
            rIndex = Integer.parseInt(substringAfter(channelUID.getGroupId(), CHANNEL_GROUP_RELAY_CONTROL)) - 1;
        } else if (groupName.startsWith(CHANNEL_GROUP_ROL_CONTROL)
                && groupName.length() > CHANNEL_GROUP_ROL_CONTROL.length()) {
            rIndex = Integer.parseInt(substringAfter(channelUID.getGroupId(), CHANNEL_GROUP_ROL_CONTROL)) - 1;
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
                if (!autoCoIoT && !profile.isGen2) {
                    requestUpdates(45 / UPDATE_STATUS_INTERVAL_SECONDS, false);
                }
                break;

            case CHANNEL_ROL_CONTROL_FAV:
                if (command instanceof Number numberCommand) {
                    int id = numberCommand.intValue() - 1;
                    int pos = profile.getRollerFav(id);
                    if (pos > 0) {
                        logger.debug("{}: Selecting favorite {}, position = {}", thingName, id, pos);
                        api.setRollerPos(rIndex, pos);
                        break;
                    }
                }
                logger.debug("{}: Invalid favorite index: {}", thingName, command);
                break;

            case CHANNEL_TIMER_AUTOON:
                logger.debug("{}: Set Auto-ON timer to {}", thingName, command);
                api.setAutoTimer(rIndex, SHELLY_TIMER_AUTOON, getNumber(command).doubleValue());
                break;
            case CHANNEL_TIMER_AUTOOFF:
                logger.debug("{}: Set Auto-OFF timer to {}", thingName, command);
                api.setAutoTimer(rIndex, SHELLY_TIMER_AUTOOFF, getNumber(command).doubleValue());
                break;
            case CHANNEL_EMETER_RESETTOTAL:
                String id = substringAfter(groupName, CHANNEL_GROUP_METER);
                int mIdx = id.isEmpty() ? 0 : Integer.parseInt(id) - 1;
                logger.debug("{}: Reset Meter Totals for meter {}", thingName, mIdx + 1);
                api.resetMeterTotal(mIdx); // currently there is only 1 emdata component
                updateChannel(groupName, CHANNEL_EMETER_RESETTOTAL, OnOffType.OFF);
                break;
        }
        return true;
    }

    /**
     * Brightness channel has 2 functions: Switch On/Off (OnOnType) and setting brightness (PercentType)
     * There is some more logic in the control. When brightness is set to 0 the control sends also an OFF command
     * When current brightness is 0 and slider will be moved the new brightness will be set, but also an ON command is
     * sent.
     *
     * @param command
     * @param index
     * @throws ShellyApiException
     */
    private void handleBrightness(Command command, Integer index) throws ShellyApiException {
        Integer value = -1;
        if (command instanceof PercentType percentCommand) { // Dimmer
            value = percentCommand.intValue();
        } else if (command instanceof DecimalType decimalCommand) { // Number
            value = decimalCommand.intValue();
        } else if (command instanceof OnOffType onOffCommand) { // Switch
            logger.debug("{}: Switch output {}", thingName, command);
            updateBrightnessChannel(index, onOffCommand, value);
            return;
        } else if (command instanceof IncreaseDecreaseType) {
            ShellyShortLightStatus light = api.getLightStatus(index);
            if (command == IncreaseDecreaseType.INCREASE) {
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
        updateChannel(CHANNEL_COLOR_WHITE, CHANNEL_BRIGHTNESS + "$Switch", power);
        if (brightness > 0) {
            api.setBrightness(lightId, brightness, config.brightnessAutoOn);
        } else {
            api.setLightTurn(lightId, power == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
            if (brightness >= 0) { // ignore -1
                updateChannel(CHANNEL_COLOR_WHITE, CHANNEL_BRIGHTNESS + "$Value",
                        toQuantityType((double) (power == OnOffType.ON ? brightness : 0), DIGITS_NONE, Units.PERCENT));
            }
        }
    }

    @Override
    public boolean updateDeviceStatus(ShellySettingsStatus status) throws ShellyApiException {
        // map status to channels
        boolean updated = false;
        updated |= updateRelays(status);
        updated |= ShellyComponents.updateDimmers(this, status);
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
        int position = -1;

        if ((command instanceof UpDownType) || (command instanceof OnOffType)) {
            ShellyRollerStatus rstatus = api.getRollerStatus(index);

            if (!getString(rstatus.state).isEmpty() && !getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_STOP)) {
                if ((command == UpDownType.UP && getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_OPEN))
                        || (command == UpDownType.DOWN
                                && getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_CLOSE))) {
                    logger.debug("{}: Roller is already in requested position ({}), ignore command {}", thingName,
                            getString(rstatus.state), command);
                    requestUpdates(1, false);
                    return;
                }
            }

            if (command == UpDownType.UP || command == OnOffType.ON
                    || ((command instanceof DecimalType decimalCommand) && (decimalCommand.intValue() == 100))) {
                logger.debug("{}: Open roller", thingName);
                int shpos = profile.getRollerFav(config.favoriteUP - 1);
                if (shpos > 0) {
                    logger.debug("{}: Use favoriteUP id {} for positioning roller({}%)", thingName, config.favoriteUP,
                            shpos);
                    api.setRollerPos(index, shpos);
                    position = shpos;
                } else {
                    api.setRollerTurn(index, SHELLY_ALWD_ROLLER_TURN_OPEN);
                }
            } else if (command == UpDownType.DOWN || command == OnOffType.OFF
                    || ((command instanceof DecimalType decimalCommand) && (decimalCommand.intValue() == 0))) {
                logger.debug("{}: Closing roller", thingName);
                int shpos = profile.getRollerFav(config.favoriteDOWN - 1);
                if (shpos > 0) {
                    // use favorite position
                    logger.debug("{}: Use favoriteDOWN id {} for positioning roller ({}%)", thingName,
                            config.favoriteDOWN, shpos);
                    api.setRollerPos(index, shpos);
                    position = shpos;
                } else {
                    api.setRollerTurn(index, SHELLY_ALWD_ROLLER_TURN_CLOSE);
                }
            }
        } else if (command == StopMoveType.STOP) {
            logger.debug("{}: Stop roller", thingName);
            api.setRollerTurn(index, SHELLY_ALWD_ROLLER_TURN_STOP);
        } else {
            logger.debug("{}: Set roller to position {}", thingName, command);
            if (command instanceof PercentType percentCommand) {
                position = percentCommand.intValue();
            } else if (command instanceof DecimalType decimalCommand) {
                position = decimalCommand.intValue();
            } else {
                throw new IllegalArgumentException(
                        "Invalid value type for roller control/position" + command.getClass().toString());
            }

            // take position from RollerShutter control and map to Shelly positon
            // OH: 0=closed, 100=open; Shelly 0=open, 100=closed)
            // take position 1:1 from position channel
            position = isControl ? SHELLY_MAX_ROLLER_POS - position : position;
            validateRange("roller position", position, SHELLY_MIN_ROLLER_POS, SHELLY_MAX_ROLLER_POS);

            logger.debug("{}: Changing roller position to {}", thingName, position);
            api.setRollerPos(index, position);
        }
    }

    /**
     * Auto-create relay channels depending on relay type/mode
     */
    private void createRelayChannels(ShellySettingsRelay relay, int idx) {
        if (!areChannelsCreated()) {
            updateChannelDefinitions(ShellyChannelDefinitions.createRelayChannels(getThing(), profile, relay, idx));
        }
    }

    private void createRollerChannels(ShellyRollerStatus roller) {
        if (!areChannelsCreated()) {
            updateChannelDefinitions(ShellyChannelDefinitions.createRollerChannels(getThing(), profile, roller));
        }
    }

    /**
     * Update Relay/Roller channels
     *
     * @param status Last ShellySettingsStatus
     *
     * @throws ShellyApiException
     */
    public boolean updateRelays(ShellySettingsStatus status) throws ShellyApiException {
        boolean updated = false;
        if (profile.hasRelays && !profile.isDimmer) {
            double voltage = -1;
            if (status.voltage == null && profile.settings.supplyVoltage != null) {
                // Shelly 1PM/1L (fix)
                voltage = profile.settings.supplyVoltage == 0 ? 110.0 : 220.0;
            } else {
                // Shelly 2.5 (measured)
                voltage = getDouble(status.voltage);
            }
            if (voltage > 0) {
                updated |= updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_VOLTAGE,
                        toQuantityType(voltage, DIGITS_VOLT, Units.VOLT));
            }

            if (!profile.isRoller) {
                logger.trace("{}: Updating {} relay(s)", thingName, profile.numRelays);
                for (int i = 0; i < status.relays.size(); i++) {
                    createRelayChannels(status.relays.get(i), i);
                    updated |= ShellyComponents.updateRelay(this, status, i);
                }
            } else {
                // Check for Relay in Roller Mode
                logger.trace("{}: Updating {} rollers", thingName, profile.numRollers);
                for (int i = 0; i < profile.numRollers; i++) {
                    ShellyRollerStatus roller = status.rollers.get(i);
                    createRollerChannels(roller);
                    updated |= ShellyComponents.updateRoller(this, roller, i);
                }
            }
        }
        return updated;
    }

    /**
     * Update LED channels
     *
     * @param status Last ShellySettingsStatus
     */
    public boolean updateLed(ShellySettingsStatus status) {
        boolean updated = false;
        updated |= updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_LED_STATUS_DISABLE,
                getOnOff(profile.settings.ledStatusDisable));
        updated |= updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_LED_POWER_DISABLE,
                getOnOff(profile.settings.ledPowerDisable));
        return updated;
    }
}
