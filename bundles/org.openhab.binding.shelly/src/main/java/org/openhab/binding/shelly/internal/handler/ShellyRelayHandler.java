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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiJson;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyControlRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyInputState;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyShortStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.coap.ShellyCoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/***
 * The{@link ShellyRelayHandler} handles light (bulb+rgbw2) specific commands and status. All other commands will be
 * routet of the generic thing
 * handler.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyRelayHandler extends ShellyBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(ShellyRelayHandler.class);

    /**
     * @param thing The thing passed by the HandlerFactory
     * @param handlerFactory Handler Factory instance (will be used for event handler registration)
     * @param networkAddressService instance of NetworkAddressService to get access to the OH default ip settings
     */
    public ShellyRelayHandler(Thing thing, ShellyHandlerFactory handlerFactory,
            ShellyBindingConfiguration bindingConfig, @Nullable ShellyCoapServer coapServer) {
        super(thing, handlerFactory, bindingConfig, coapServer);
    }

    @Override
    public void initialize() {
        logger.debug("Thing is using class {}", this.getClass());
        super.initialize();
    }

    @SuppressWarnings("null")
    @Override
    public boolean handleDeviceCommand(ChannelUID channelUID, Command command) throws IOException {
        // Process command
        String groupName = channelUID.getGroupId();
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
                    logger.debug("{}: Set relay output to {}", thingName, command.toString());
                    api.setRelayTurn(rIndex, (OnOffType) command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
                } else {
                    logger.info("{}: Device is in roller mode, channel command {} ignored", thingName,
                            channelUID.toString());
                }
                break;
            case CHANNEL_BRIGHTNESS: // e.g.Dimmer
                handleBrightness(command, rIndex);
                break;

            case CHANNEL_ROL_CONTROL_POS:
            case CHANNEL_ROL_CONTROL_CONTROL:
                logger.debug("{}: Roller command/position {}", thingName, command.toString());
                handleRoller(command, groupName, rIndex,
                        channelUID.getIdWithoutGroup().equals(CHANNEL_ROL_CONTROL_CONTROL));

                // request updates the next 30sec to update roller position after it stopped
                requestUpdates(45 / UPDATE_STATUS_INTERVAL_SECONDS, false);
                break;

            case CHANNEL_TIMER_AUTOON:
                logger.debug("{}: Set Auto-ON timer to {}", thingName, command.toString());
                Validate.isTrue(command instanceof DecimalType,
                        "Timer AutoOn: Invalid value type: " + command.getClass());
                api.setTimer(rIndex, SHELLY_TIMER_AUTOON, ((DecimalType) command).doubleValue());
                break;
            case CHANNEL_TIMER_AUTOOFF:
                logger.debug("{}: Set Auto-OFF timer to {}", thingName, command.toString());
                Validate.isTrue(command instanceof DecimalType, "Invalid value type");
                api.setTimer(rIndex, SHELLY_TIMER_AUTOOFF, ((DecimalType) command).doubleValue());
                break;

            case CHANNEL_LED_STATUS_DISABLE:
                logger.debug("{}: Set STATUS LED disabled to {}", thingName, command.toString());
                Validate.isTrue(command instanceof OnOffType, "Invalid value type");
                api.setLedStatus(SHELLY_LED_STATUS_DISABLE, (OnOffType) command == OnOffType.ON);
                break;
            case CHANNEL_LED_POWER_DISABLE:
                logger.debug("{}: Set POWER LED disabled to {}", thingName, command.toString());
                Validate.isTrue(command instanceof OnOffType, "Invalid value type");
                api.setLedStatus(SHELLY_LED_POWER_DISABLE, (OnOffType) command == OnOffType.ON);
                break;
        }
        return true;
    }

    @SuppressWarnings("null")
    private void handleBrightness(Command command, Integer index) throws IOException {
        Integer value = -1;
        if (command instanceof PercentType) { // Dimmer
            value = ((PercentType) command).intValue();
        } else if (command instanceof DecimalType) { // Number
            value = ((DecimalType) command).intValue();
        } else if (command instanceof OnOffType) { // Switch
            logger.debug("Switch output {}", command.toString());
            api.setRelayTurn(index, (OnOffType) command == OnOffType.ON ? SHELLY_API_ON : SHELLY_API_OFF);
            requestUpdates(1, false);
            return;
        }

        // Switch light off on brightness = 0
        if (value == 0) {
            logger.debug("{}: Brightness=0 -> switch output OFF", thingName);
            api.setRelayTurn(index, SHELLY_API_OFF);
            requestUpdates(1, false);
            return;
        }

        ShellyShortLightStatus light = api.getLightStatus(index);
        Validate.notNull(light, "Unable to get Light status for brightness");
        if (command instanceof IncreaseDecreaseType) {
            if (((IncreaseDecreaseType) command).equals(IncreaseDecreaseType.INCREASE)) {
                value = Math.min(light.brightness + DIM_STEPSIZE, 100);
            } else {
                value = Math.max(light.brightness - DIM_STEPSIZE, 0);
            }
            logger.debug("{}: Change brightness from {} to {}", thingName, light.brightness, value);
        }

        validateRange("brightness", value, 0, 100);
        logger.debug("{}: Setting dimmer brightness to {}", thingName, value);
        api.setDimmerBrightness(index, value);

    }

    @Override
    public boolean updateDeviceStatus(ShellySettingsStatus status) throws IOException {
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
     * @throws IOException
     */
    @SuppressWarnings("null")
    private void handleRoller(Command command, String groupName, Integer index, boolean isControl) throws IOException {
        Integer position = -1;

        if ((command instanceof UpDownType) || (command instanceof OnOffType)) {
            ShellyControlRoller rstatus = api.getRollerStatus(index);

            if (!getString(rstatus.state).isEmpty() && !getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_STOP)) {
                boolean up = command instanceof UpDownType && (UpDownType) command == UpDownType.UP;
                boolean down = command instanceof UpDownType && (UpDownType) command == UpDownType.DOWN;
                if ((up && getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_OPEN))
                        || (down && getString(rstatus.state).equals(SHELLY_ALWD_ROLLER_TURN_CLOSE))) {
                    logger.info("{}: Roller is already moving ({}), ignore command {}", thingName,
                            getString(rstatus.state), command.toString());
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
            logger.debug("{}: Set roller to position {}", thingName, command.toString());
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
     * Update Relay/Roller channels
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     *
     * @throws IOException
     */
    @SuppressWarnings("null")
    public boolean updateRelays(ShellySettingsStatus status) throws IOException {
        Validate.notNull(status, "status must not be null!");
        ShellyDeviceProfile profile = getProfile();

        boolean updated = false;
        if (profile.hasRelays && !profile.isRoller && !profile.isDimmer) {
            logger.trace("{}: Updating {} relay(s)", thingName, profile.numRelays.toString());

            int i = 0;
            ShellyStatusRelay rstatus = api.getRelayStatus(i);
            if (rstatus != null) {
                for (ShellyShortStatusRelay relay : rstatus.relays) {
                    if ((relay.isValid == null) || relay.isValid) {
                        Integer r = i + 1;
                        String groupName = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                                : CHANNEL_GROUP_RELAY_CONTROL + r.toString();

                        if (getBool(relay.overpower)) {
                            sendAlarm(groupName + ": Over power detected!");
                        }

                        updated |= updateChannel(groupName, CHANNEL_OUTPUT, getOnOff(relay.ison));
                        updated |= updateChannel(groupName, CHANNEL_TIMER_ACTIVE, getOnOff(relay.hasTimer));
                        ShellySettingsRelay rsettings = profile.settings.relays.get(i);
                        if (rsettings != null) {
                            updated |= updateChannel(groupName, CHANNEL_TIMER_AUTOON,
                                    toQuantityType(getDouble(rsettings.autoOn), SmartHomeUnits.SECOND));
                            updated |= updateChannel(groupName, CHANNEL_TIMER_AUTOOFF,
                                    toQuantityType(getDouble(rsettings.autoOff), SmartHomeUnits.SECOND));
                        }
                        updated |= updateInputs(groupName, status);
                        i++;
                    }

                }
            }
        }
        if (profile.hasRelays && profile.isRoller && (status.rollers != null)) {
            logger.trace("{}: Updating {} rollers", thingName, profile.numRollers.toString());
            int i = 0;
            for (ShellySettingsRoller roller : status.rollers) {
                if (roller.isValid) {
                    ShellyControlRoller control = api.getRollerStatus(i);
                    Integer relayIndex = i + 1;
                    String groupName = profile.numRollers > 1 ? CHANNEL_GROUP_ROL_CONTROL + relayIndex.toString()
                            : CHANNEL_GROUP_ROL_CONTROL;

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
                    updated |= updateInputs(groupName, status);

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
     * @throws IOException
     */
    @SuppressWarnings("null")
    public boolean updateDimmers(ShellySettingsStatus orgStatus) throws IOException {
        ShellyDeviceProfile profile = getProfile();

        boolean updated = false;
        if (profile.isDimmer) {
            Validate.notNull(orgStatus, "orgStatus must not be null!");

            // We need to fixup the returned Json: The dimmer returns light[] element, which is ok, but it doesn't have
            // the same structure as lights[] from Bulb and RGBW2. The tag gets replaced by dimmers[] so that Gson maps
            // to a different structure (ShellyShortLight).
            Gson gson = new Gson();
            ShellySettingsStatus dstatus = gson.fromJson(ShellyApiJson.fixDimmerJson(orgStatus.json),
                    ShellySettingsStatus.class);
            Validate.notNull(dstatus.dimmers, "dstatus.dimmers must not be null!");
            Validate.notNull(dstatus.tmp, "dstatus.tmp must not be null!");

            logger.trace("{}: Updating {} dimmers(s)", thingName, dstatus.dimmers.size());

            int l = 0;
            logger.trace("{}: Updating dimmers {}", thingName, dstatus.dimmers.size());
            for (ShellyShortLightStatus dimmer : dstatus.dimmers) {
                Integer r = l + 1;
                String groupName = profile.numRelays <= 1 ? CHANNEL_GROUP_DIMMER_CONTROL
                        : CHANNEL_GROUP_DIMMER_CONTROL + r.toString();
                updated |= updateChannel(groupName, CHANNEL_OUTPUT, getOnOff(dimmer.ison));
                updated |= updateChannel(groupName, CHANNEL_BRIGHTNESS, getOnOff(dimmer.ison));
                updated |= updateChannel(groupName, CHANNEL_BRIGHTNESS,
                        toQuantityType(new Double(getInteger(dimmer.brightness)), SmartHomeUnits.PERCENT));

                ShellySettingsDimmer dsettings = profile.settings.dimmers.get(l);
                if (dsettings != null) {
                    updated |= updateChannel(groupName, CHANNEL_TIMER_AUTOON,
                            toQuantityType(getDouble(dsettings.autoOn), SmartHomeUnits.SECOND));
                    updated |= updateChannel(groupName, CHANNEL_TIMER_AUTOOFF,
                            toQuantityType(getDouble(dsettings.autoOff), SmartHomeUnits.SECOND));

                }

                updated |= updateInputs(groupName, orgStatus);
                l++;
            }
        }
        return updated;
    }

    /**
     * Map input states to channels
     *
     * @param groupName Channel Group (relay / relay1...)
     *
     * @param status Shelly device status
     * @return true: one or more inputs were updated
     */
    @SuppressWarnings("null")
    private boolean updateInputs(String groupName, ShellySettingsStatus status) {
        boolean updated = false;
        if (status.inputs != null && !getProfile().isRoller) {
            int count = status.inputs.size();
            logger.trace("{}: Updating {} input state(s)", thingName, count);
            for (int input = 0; input < count; input++) {
                ShellyInputState state = status.inputs.get(input);
                String channel = profile.isDimmer && count > 1 ? CHANNEL_INPUT + Integer.toString(input + 1)
                        : CHANNEL_INPUT;
                updated |= updateChannel(groupName, channel, state.input == 0 ? OnOffType.OFF : OnOffType.ON);
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
    @SuppressWarnings("null")
    public boolean updateLed(ShellySettingsStatus status) {
        boolean updated = false;
        if (profile.hasLed) {
            Validate.notNull(profile.settings.ledStatusDisable, "LED update: led_status_disable must not be null!");
            Validate.notNull(profile.settings.ledPowerDisable, "LED update: led_power_disable must not be null!");
            logger.debug("{}: LED disabled status: powerLed: {}, : statusLed{}", thingName,
                    getBool(profile.settings.ledPowerDisable), getBool(profile.settings.ledStatusDisable));
            updated |= updateChannel(CHANNEL_GROUP_LED_CONTROL, CHANNEL_LED_STATUS_DISABLE,
                    getOnOff(profile.settings.ledStatusDisable));
            updated |= updateChannel(CHANNEL_GROUP_LED_CONTROL, CHANNEL_LED_POWER_DISABLE,
                    getOnOff(profile.settings.ledPowerDisable));
        }
        return updated;
    }
}
