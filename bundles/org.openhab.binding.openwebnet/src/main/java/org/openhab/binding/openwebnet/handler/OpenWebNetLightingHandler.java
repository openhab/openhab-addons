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
package org.openhab.binding.openwebnet.handler;

import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openwebnet.message.BaseOpenMessage;
import org.openwebnet.message.Lighting;
import org.openwebnet.message.OpenMessageFactory;
import org.openwebnet.message.What;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetLightingHandler} is responsible for handling commands/messages for a Lighting OpenWebNet device.
 * It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetLightingHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetLightingHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.LIGHTING_SUPPORTED_THING_TYPES;

    protected Lighting.Type lightingType = Lighting.Type.ZIGBEE;

    private double lastBrightnessChangeSentTS = 0; // timestamp when last brightness change was sent to the device
    private static final int BRIGHTNESS_CHANGE_DELAY = 1500; // ms delay to wait before sending another brightness
                                                             // status request

    private boolean brightnessLevelRequested = false; // was the brightness level requested ?
    private int latestBrightnessWhat = -1; // latest brightness WHAT value (-1 = unknown)
    private int latestBrightnessWhatBeforeOff = -1; // latest brightness WHAT value before device was set to off

    public OpenWebNetLightingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("initialize() thing={}", thing.getUID());
        if (bridgeHandler != null && bridgeHandler.isBusGateway()) {
            lightingType = Lighting.Type.POINT_TO_POINT;
        }
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("requestChannelState() thingUID={} channel={}", thing.getUID(), channel.getId());
        bridgeHandler.gateway.send(Lighting.requestStatus(toWhere(channel), lightingType));
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            case CHANNEL_BRIGHTNESS:
            case "dimmerLevel":
                handleBrightnessCommand(command);
                break;
            case CHANNEL_SWITCH:
            case CHANNEL_SWITCH_01:
            case CHANNEL_SWITCH_02:
                handleSwitchCommand(channel, command);
                break;
            default: {
                logger.warn("Unsupported channel UID {}", channel);
            }
        }
        // TODO if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    /**
     * Handles Lighting switch command for a channel
     *
     * @param channel the channel
     * @param command the command
     */
    private void handleSwitchCommand(ChannelUID channel, Command command) {
        logger.debug("handleSwitchCommand() (command={} - channel={})", command, channel);
        if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                bridgeHandler.gateway.send(Lighting.requestTurnOn(toWhere(channel), lightingType));
            } else if (OnOffType.OFF.equals(command)) {
                bridgeHandler.gateway.send(Lighting.requestTurnOff(toWhere(channel), lightingType));
            }
        } else {
            logger.warn("Unsupported command: {}", command);
        }

    }

    /**
     * Handles Lighting brightness command (ON, OFF, xx%, INCREASE, DECREASE)
     *
     * @param command the command
     */
    private void handleBrightnessCommand(Command command) {
        logger.debug("handleBrightnessCommand() command={}", command);
        if (command instanceof PercentType) {
            int percent = ((PercentType) command).intValue();
            dimLightTo(percentToLevel(percent), command);
        } else if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                dimLightTo(latestBrightnessWhat + 1, command);
            } else { // DECREASE
                dimLightTo(latestBrightnessWhat - 1, command);
            }
        } else if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                dimLightTo(latestBrightnessWhat, command);
            } else { // OFF
                dimLightTo(0, command);
            }
        }
        // DEBUG MODE: this is the other channel (level)
        /*
         * else if (command instanceof DecimalType) {
         * dimLightTo(((DecimalType) command).intValue(), command);
         * }
         */
        else {
            logger.warn("Cannot handle command {} for thing {}", command, getThing().getUID());
            return;
        }
    }

    /**
     * Helper method to dim light to a valid OWN value
     */
    private void dimLightTo(int whatInt, Command command) {
        final String channel = CHANNEL_BRIGHTNESS;
        final String where = toWhere(BaseOpenMessage.UNIT_01);
        int newWhatInt = whatInt;
        logger.debug("-DIM- dimLightTo() latestBriWhat={} latestBriBeforeOff={} briLevelRequested={}",
                latestBrightnessWhat, latestBrightnessWhatBeforeOff, brightnessLevelRequested);
        What newWhat;
        if (OnOffType.ON.equals(command) && latestBrightnessWhat <= 0) {
            // ON after OFF/Unknown -> we reset channel to last value before OFF (if exists)
            if (latestBrightnessWhatBeforeOff > 0) {
                newWhatInt = latestBrightnessWhatBeforeOff;
                updateState(channel, new PercentType(levelToPercent(newWhatInt)));
            } else { // we do not know last brightness -> set dimmer to 100%
                newWhatInt = 10;
            }
        }
        logger.debug("-DIM- requested level={}", newWhatInt);
        if (newWhatInt != latestBrightnessWhat) {
            if (newWhatInt >= 0 && newWhatInt <= 10) {
                newWhat = Lighting.WHAT.fromValue(newWhatInt);
                if (newWhat.equals(Lighting.WHAT.ON)) {
                    // change it to WHAT.DIMMER_20 (dimming to 10% is not allowed in OWN)
                    newWhat = Lighting.WHAT.DIMMER_20;
                }
                // save current brightness level before sending bri=0 command to device
                if (newWhatInt == 0) {
                    latestBrightnessWhatBeforeOff = latestBrightnessWhat;
                }
                lastBrightnessChangeSentTS = System.currentTimeMillis();
                bridgeHandler.gateway.send(Lighting.requestDimTo(where, newWhat, lightingType));
                if (!(command instanceof PercentType)) {
                    updateState(channel, new PercentType(levelToPercent(newWhatInt)));
                }
                updateState("dimmerLevel", new DecimalType(newWhatInt));
                latestBrightnessWhat = newWhatInt;
            } else {
                logger.debug("-DIM- do nothing");
            }
        } else {
            logger.debug("-DIM- do nothing");
        }
        logger.debug("-DIM- latestBriWhat={} latestBriBeforeOff={} briLevelRequested={}", latestBrightnessWhat,
                latestBrightnessWhatBeforeOff, brightnessLevelRequested);
    }

    @Override
    protected String ownIdPrefix() {
        return org.openwebnet.message.Who.LIGHTING.value().toString();
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        updateLightState((Lighting) msg);
    }

    /**
     * Updates light state based on a OWN Lighting message received
     *
     * @param msg the Lighting message received
     */
    private void updateLightState(Lighting msg) {
        logger.debug("updateLightState() for thing: {}", thing.getUID());
        ThingTypeUID thingType = thing.getThingTypeUID();
        if (THING_TYPE_ZB_DIMMER.equals(thingType) || THING_TYPE_BUS_DIMMER.equals(thingType)) {
            updateLightBrightnessState(msg);
        } else {
            updateLightOnOffState(msg);
        }
    }

    /**
     * Updates on/off state based on a OWN Lighting message received
     *
     * @param msg the Lighting message received
     */
    private void updateLightOnOffState(Lighting msg) {
        String channelID;
        if (bridgeHandler.isBusGateway()) {
            channelID = CHANNEL_SWITCH;
        } else {
            if (BaseOpenMessage.UNIT_02.equals(OpenMessageFactory.getUnit(msg.getWhere()))) {
                channelID = CHANNEL_SWITCH_02;
            } else {
                channelID = CHANNEL_SWITCH_01;
            }
        }
        if (msg.isOn() || msg.isMovement()) {
            updateState(channelID, OnOffType.ON);
        } else if (msg.isOff() || msg.isEndMovement()) {
            updateState(channelID, OnOffType.OFF);
        } else {
            logger.info("updateLightOnOffState() Ignoring unsupported WHAT for thing {}. Frame={}", getThing().getUID(),
                    msg);
        }
    }

    /**
     * Updates brightness level based on a OWN Lighting message received
     *
     * @param msg the Lighting message received
     */
    private synchronized void updateLightBrightnessState(Lighting msg) {
        final String channel = CHANNEL_BRIGHTNESS;
        String where = toWhere(BaseOpenMessage.UNIT_01);
        logger.debug("updateLightBrightnessState() msg={}", msg);
        logger.debug("-BRI- updateLightBr() latestBriWhat={} latestBriBeforeOff={} brightnessLevelRequested={}",
                latestBrightnessWhat, latestBrightnessWhatBeforeOff, brightnessLevelRequested);
        double now = System.currentTimeMillis();
        double delta = now - lastBrightnessChangeSentTS;
        logger.debug("$bri now={} delta={}", now, delta);
        if (msg.isOn() && !brightnessLevelRequested) {
            if (delta >= BRIGHTNESS_CHANGE_DELAY) {
                // we send a light brightness status request ONLY if last brightness change
                // was sent >BRIGHTNESS_CHANGE_DELAY ago
                logger.debug("-BRI- change sent >={}ms ago, sending requestStatus...", BRIGHTNESS_CHANGE_DELAY);
                brightnessLevelRequested = true;
                Lighting li = Lighting.requestStatus(where, lightingType);
                bridgeHandler.gateway.send(li);
            } else {
                logger.debug("-BRI- change sent {}<{}ms, NO requestStatus needed", delta, BRIGHTNESS_CHANGE_DELAY);
            }
        } else {
            logger.debug("-BRI- update from network -> level should be present in WHAT part of the message");
            if (msg.getWhat() != null) {
                int newLevel = msg.getWhat().value();
                logger.debug("-BRI- current level={} ----> new level={}", latestBrightnessWhat, newLevel);
                if (latestBrightnessWhat != newLevel) {
                    if (delta >= BRIGHTNESS_CHANGE_DELAY) {
                        logger.debug("-BRI- change sent >={}ms ago, updating state...", BRIGHTNESS_CHANGE_DELAY);
                        updateState(channel, new PercentType(levelToPercent(newLevel)));
                    } else if (msg.isOff()) {
                        logger.debug("-BRI- change just sent, but OFF from network received, updating state...");
                        updateState(channel, new PercentType(levelToPercent(newLevel)));
                    } else {
                        logger.debug("-BRI- change just sent, NO update needed.");
                    }
                    updateState("dimmerLevel", new DecimalType(newLevel));
                    if (msg.isOff()) {
                        latestBrightnessWhatBeforeOff = latestBrightnessWhat;
                    }
                    latestBrightnessWhat = newLevel;
                } else {
                    logger.debug("-BRI- no change");
                }
                brightnessLevelRequested = false;
            } else { // dimension notification
                if (msg.getDim() == Lighting.DIM_DIMMER_LEVEL_100) {
                    int newPercent;
                    try {
                        newPercent = Lighting.parseDimmerLevel100(msg);
                    } catch (NumberFormatException nfe) {
                        logger.warn("updateLightBrightnessState() Wrong value for dimmerLevel100 in message: {}", msg);
                        return;
                    }
                    int newLevel = Lighting.percentToWhat(newPercent).value();
                    logger.debug("-BRI- latest level={} ----> new percent={} ----> new level={}", latestBrightnessWhat,
                            newPercent, newLevel);
                    updateState(channel, new PercentType(newPercent));
                    updateState("dimmerLevel", new DecimalType(newLevel));
                    if (newPercent == 0) {
                        latestBrightnessWhatBeforeOff = latestBrightnessWhat;
                    }
                    latestBrightnessWhat = newLevel;
                    brightnessLevelRequested = false;
                } else {
                    logger.warn("updateLightBrightnessState() Cannot handle message {} for thing {}", msg,
                            getThing().getUID());
                    return;
                }
            }
        }
        logger.debug("-BRI- latestBriWhat={} latestBriBeforeOff={} brightnessLevelRequested={}", latestBrightnessWhat,
                latestBrightnessWhatBeforeOff, brightnessLevelRequested);

    }

    /**
     * Transforms a 0,1,2-10 level (int) to a percent (0-100%) int
     */
    // TODO push down in the OWN lib
    private int levelToPercent(int level) {
        // TODO for now, we use a linear mapping
        return level * 10;
    }

    /**
     * Transforms a percent int (0-100%) into a 0,2-10 level (int)
     */
    private int percentToLevel(int percent) {
        // TODO push down in the OWN lib
        // TODO for now, we use a linear mapping
        if (percent > 0 && percent < 10) {
            return 1;
        } else {
            return (int) Math.floor(percent / 10.0);
        }

    }

    /**
     * Returns a WHERE address based on bridge type and unit (optional)
     *
     * @param unit the device unit
     **/
    @Nullable
    protected String toWhere(String unit) {
        logger.debug("toWhere(unit) ownId={}", ownId);
        if (bridgeHandler.isBusGateway()) {
            return deviceWhere;
        } else {
            return deviceWhere + unit;
        }
    }

    /**
     * Returns a WHERE address based on channel
     *
     * @param channel the channel
     **/
    @Nullable
    protected String toWhere(ChannelUID channel) {
        logger.debug("toWhere(ChannelUID) ownId={}", ownId);
        if (bridgeHandler.isBusGateway()) {
            return deviceWhere;
        } else if (channel.getId().equals(CHANNEL_SWITCH_02)) {
            return deviceWhere + BaseOpenMessage.UNIT_02;
        } else { // CHANNEL_SWITCH_01 or other channels
            return deviceWhere + BaseOpenMessage.UNIT_01;
        }
    }

} // class
