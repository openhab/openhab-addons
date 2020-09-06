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
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.What;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.LIGHTING_SUPPORTED_THING_TYPES;

    private static final int BRIGHTNESS_CHANGE_DELAY_MSEC = 1500; // delay to wait before sending another brightness
    // status request

    private long lastBrightnessChangeSentTS = 0; // timestamp when last brightness change was sent to the device
    private boolean brightnessLevelRequested = false; // was the brightness level requested ?
    private int latestBrightnessWhat = -1; // latest brightness WHAT value (-1 = unknown)
    private int latestBrightnessWhatBeforeOff = -1; // latest brightness WHAT value before device was set to off

    public OpenWebNetLightingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("requestChannelState() thingUID={} channel={}", thing.getUID(), channel.getId());
        try {
            send(Lighting.requestStatus(toWhere(channel)));
        } catch (OWNException e) {
            logger.warn("Exception while requesting channel {} state: {}", channel, e.getMessage());
        }
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            case CHANNEL_BRIGHTNESS:
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
            try {
                if (OnOffType.ON.equals(command)) {
                    send(Lighting.requestTurnOn(toWhere(channel)));
                } else if (OnOffType.OFF.equals(command)) {
                    send(Lighting.requestTurnOff(toWhere(channel)));
                }
            } catch (OWNException e) {
                logger.warn("Exception while processing command {}: {}", command, e.getMessage());
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
            dimLightTo(Lighting.percentToWhat(percent).value(), command);
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
        } else {
            logger.warn("Cannot handle command {} for thing {}", command, getThing().getUID());
        }
    }

    /**
     * Helper method to dim light to a valid OWN value
     */
    private void dimLightTo(int whatInt, Command command) {
        int newWhatInt = whatInt;
        logger.debug("-DIM- dimLightTo() latestBriWhat={} latestBriBeforeOff={} briLevelRequested={}",
                latestBrightnessWhat, latestBrightnessWhatBeforeOff, brightnessLevelRequested);
        What newWhat;
        if (OnOffType.ON.equals(command) && latestBrightnessWhat <= 0) {
            // ON after OFF/Unknown -> we reset channel to last value before OFF (if exists)
            if (latestBrightnessWhatBeforeOff > 0) { // we know last brightness -> set dimmer to it
                newWhatInt = latestBrightnessWhatBeforeOff;
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
                Where w = deviceWhere;
                if (w != null) {
                    try {
                        lastBrightnessChangeSentTS = System.currentTimeMillis();
                        send(Lighting.requestDimTo(w.value(), newWhat));
                    } catch (OWNException e) {
                        logger.warn("Exception while sending dimLightTo for command {}: {}", command, e.getMessage());
                    }
                }
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
        return Who.LIGHTING.value().toString();
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        updateLightState((Lighting) msg);
    }

    /**
     * Updates light state based on a OWN Lighting event message received
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
     * Updates on/off state based on a OWN Lighting event message received
     *
     * @param msg the Lighting message received
     */
    private void updateLightOnOffState(Lighting msg) {
        String channelID;
        OpenWebNetBridgeHandler brH = bridgeHandler;
        if (brH != null) {
            if (brH.isBusGateway()) {
                channelID = CHANNEL_SWITCH;
            } else {
                WhereZigBee w = (WhereZigBee) (msg.getWhere());
                if (WhereZigBee.UNIT_02.equals(w.getUnit())) {
                    channelID = CHANNEL_SWITCH_02;
                } else {
                    channelID = CHANNEL_SWITCH_01;
                }
            }
            if (msg.isOn()) {
                updateState(channelID, OnOffType.ON);
            } else if (msg.isOff()) {
                updateState(channelID, OnOffType.OFF);
            } else {
                logger.debug("updateLightOnOffState() Ignoring unsupported WHAT for thing {}. Frame={}",
                        getThing().getUID(), msg.getFrameValue());
            }
        }
    }

    /**
     * Updates brightness level based on a OWN Lighting event message received
     *
     * @param msg the Lighting message received
     */
    private synchronized void updateLightBrightnessState(Lighting msg) {
        final String channel = CHANNEL_BRIGHTNESS;
        logger.debug("  $BRI updateLightBrightnessState() msg={}", msg);
        logger.debug("  $BRI updateLightBr() latestBriWhat={} latestBriBeforeOff={} brightnessLevelRequested={}",
                latestBrightnessWhat, latestBrightnessWhatBeforeOff, brightnessLevelRequested);
        long now = System.currentTimeMillis();
        long delta = now - lastBrightnessChangeSentTS;
        logger.debug("  $BRI now={} -> delta={}", now, delta);
        if (msg.isOn() && !brightnessLevelRequested) {
            if (delta >= BRIGHTNESS_CHANGE_DELAY_MSEC) {
                // we send a light brightness status request ONLY if last brightness change
                // was not just sent (>=BRIGHTNESS_CHANGE_DELAY_MSEC ago)
                logger.debug("  $BRI change sent >={}ms ago, sending requestStatus...", BRIGHTNESS_CHANGE_DELAY_MSEC);
                Where w = deviceWhere;
                if (w != null) {
                    try {
                        send(Lighting.requestStatus(w.value()));
                        brightnessLevelRequested = true;
                    } catch (OWNException e) {
                        logger.warn("  $BRI exception while requesting light state: {}", e.getMessage());
                    }
                }
            } else {
                logger.debug("  $BRI change sent {}<{}ms, NO requestStatus needed", delta,
                        BRIGHTNESS_CHANGE_DELAY_MSEC);
            }
        } else {
            logger.debug("  $BRI update from network -> level should be present in WHAT part of the message");
            if (msg.getWhat() != null) {
                int newLevel = msg.getWhat().value();
                logger.debug("  $BRI current level={} ----> new level={}", latestBrightnessWhat, newLevel);
                if (latestBrightnessWhat != newLevel) {
                    updateState(channel, new PercentType(Lighting.levelToPercent(newLevel)));
                    if (msg.isOff()) {
                        latestBrightnessWhatBeforeOff = latestBrightnessWhat;
                    }
                    latestBrightnessWhat = newLevel;
                } else {
                    logger.debug("  $BRI no change");
                }
                brightnessLevelRequested = false;
            } else { // dimension notification
                if (msg.getDim() == Lighting.DIM.DIMMER_LEVEL_100) {
                    int newPercent;
                    try {
                        newPercent = msg.parseDimmerLevel100();
                    } catch (FrameException fe) {
                        logger.warn("updateLightBrightnessState() Wrong value for dimmerLevel100 in message: {}", msg);
                        return;
                    }
                    int newLevel = Lighting.percentToWhat(newPercent).value();
                    logger.debug("  $BRI latest level={} ----> new percent={} ----> new level={}", latestBrightnessWhat,
                            newPercent, newLevel);
                    updateState(channel, new PercentType(newPercent));
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
        logger.debug("  $BRI latestBriWhat={} latestBriBeforeOff={} brightnessLevelRequested={}", latestBrightnessWhat,
                latestBrightnessWhatBeforeOff, brightnessLevelRequested);
    }

    /**
     * Returns a WHERE address string based on bridge type and unit (optional)
     *
     * @param unit the device unit
     **/
    @Nullable
    protected String toWhere(String unit) {
        Where w = deviceWhere;
        if (w != null) {
            OpenWebNetBridgeHandler brH = bridgeHandler;
            if (brH != null && brH.isBusGateway()) {
                return w.value();
            } else {
                return w + unit;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns a WHERE address string based on channel
     *
     * @param channel the channel
     **/
    @Nullable
    protected String toWhere(ChannelUID channel) {
        Where w = deviceWhere;
        if (w != null) {
            OpenWebNetBridgeHandler brH = bridgeHandler;
            if (brH != null) {
                if (brH.isBusGateway()) {
                    return w.value();
                } else if (channel.getId().equals(CHANNEL_SWITCH_02)) {
                    return ((WhereZigBee) w).valueWithUnit(WhereZigBee.UNIT_02);
                } else { // CHANNEL_SWITCH_01 or other channels
                    return ((WhereZigBee) w).valueWithUnit(WhereZigBee.UNIT_01);
                }
            }
        }
        return null;
    }
}
