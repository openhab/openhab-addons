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
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.Response;
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

    private static final int BRIGHTNESS_CHANGE_DELAY_MSEC = 1500; // delay before sending another brightness status
                                                                  // request
    private static final int BRIGHTNESS_STATUS_REQUEST_DELAY_MSEC = 900; // we must wait some time to be sure dimmer has
                                                                         // reached final level before requesting its
                                                                         // status
    private static final int UNKNOWN_STATE = 1000;

    private long lastBrightnessChangeSentTS = 0; // timestamp when last brightness change was sent to the device

    private int brightness = UNKNOWN_STATE; // current brightness percent value for this device

    private int brightnessBeforeOff = UNKNOWN_STATE; // latest brightness before device was set to off

    private int sw[] = { UNKNOWN_STATE, UNKNOWN_STATE }; // current switch(es) state

    public OpenWebNetLightingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("requestChannelState() thingUID={} channel={}", thing.getUID(), channel.getId());
        requestStatus(channel.getId());
    }

    /** helper method to request light status based on channel */
    private void requestStatus(String channelId) {
        Where w = deviceWhere;
        if (w != null) {
            try {
                Response res = send(Lighting.requestStatus(toWhere(channelId)));
                if (res != null && res.isSuccess()) {
                    // set thing online if not already
                    ThingStatus ts = getThing().getStatus();
                    if (ThingStatus.ONLINE != ts && ThingStatus.REMOVING != ts && ThingStatus.REMOVED != ts) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                }
            } catch (OWNException e) {
                logger.warn("requestStatus() Exception while requesting light state: {}", e.getMessage());
            }
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
                logger.warn("Unsupported ChannelUID {}", channel);
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
                    send(Lighting.requestTurnOn(toWhere(channel.getId())));
                } else if (OnOffType.OFF.equals(command)) {
                    send(Lighting.requestTurnOff(toWhere(channel.getId())));
                }
            } catch (OWNException e) {
                logger.warn("Exception while processing command {}: {}", command, e.getMessage());
            }
        } else {
            logger.warn("Unsupported command: {}", command);
        }
    }

    /**
     * Handles Lighting brightness command (xx%, INCREASE, DECREASE, ON, OFF)
     *
     * @param command the command
     */
    private void handleBrightnessCommand(Command command) {
        logger.debug("handleBrightnessCommand() command={}", command);
        if (command instanceof PercentType) {
            dimLightTo(((PercentType) command).intValue(), command);
        } else if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                dimLightTo(brightness + 10, command);
            } else { // DECREASE
                dimLightTo(brightness - 10, command);
            }
        } else if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                dimLightTo(brightnessBeforeOff, command);
            } else { // OFF
                dimLightTo(0, command);
            }
        } else {
            logger.warn("Cannot handle command {} for thing {}", command, getThing().getUID());
        }
    }

    /**
     * Helper method to dim light to given percent
     */
    private void dimLightTo(int percent, Command command) {
        logger.debug("   DIM dimLightTo({}) bri={} briBeforeOff={}", percent, brightness, brightnessBeforeOff);
        int newBrightness = percent;
        if (newBrightness == UNKNOWN_STATE) {
            // we do not know last brightness -> set dimmer to 100%
            newBrightness = 100;
        } else if (newBrightness <= 0) {
            newBrightness = 0;
            brightnessBeforeOff = brightness;
            logger.debug("   DIM saved bri before sending bri=0 command to device");
        } else if (newBrightness > 100) {
            newBrightness = 100;
        }
        What newBrightnessWhat = Lighting.percentToWhat(newBrightness);
        logger.debug("   DIM newBrightness={} newBrightnessWhat={}", newBrightness, newBrightnessWhat);
        @Nullable
        What brightnessWhat = null;
        if (brightness != UNKNOWN_STATE) {
            brightnessWhat = Lighting.percentToWhat(brightness);
        }
        if (brightnessWhat == null || !newBrightnessWhat.value().equals(brightnessWhat.value())) {
            logger.debug("   DIM brightnessWhat {} --> {}  WHAT level change needed", brightnessWhat,
                    newBrightnessWhat);
            Where w = deviceWhere;
            if (w != null) {
                try {
                    lastBrightnessChangeSentTS = System.currentTimeMillis();
                    send(Lighting.requestDimTo(w.value(), newBrightnessWhat));
                } catch (OWNException e) {
                    logger.warn("Exception while sending dimTo request for command {}: {}", command, e.getMessage());
                }
            }
        } else {
            logger.debug("   DIM brightnessWhat {} --> {}  NO WHAT level change needed", brightnessWhat,
                    newBrightnessWhat);
        }
        brightness = newBrightness;
        updateState(CHANNEL_BRIGHTNESS, new PercentType(brightness));
        logger.debug("   DIM---END bri={} briBeforeOff={}", brightness, brightnessBeforeOff);
    }

    @Override
    protected String ownIdPrefix() {
        return Who.LIGHTING.value().toString();
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        logger.debug("handleMessage() for thing: {}", thing.getUID());
        ThingTypeUID thingType = thing.getThingTypeUID();
        if (THING_TYPE_ZB_DIMMER.equals(thingType) || THING_TYPE_BUS_DIMMER.equals(thingType)) {
            updateBrightness((Lighting) msg);
        } else {
            updateOnOffState((Lighting) msg);
        }
    }

    /**
     * Updates brightness based on OWN Lighting message received
     *
     * @param msg the Lighting message received
     */
    private synchronized void updateBrightness(Lighting msg) {
        long now = System.currentTimeMillis();
        logger.debug("  $BRI updateBrightness({})       || bri={} briBeforeOff={}", msg, brightness,
                brightnessBeforeOff);
        long delta = now - lastBrightnessChangeSentTS;
        boolean belowThresh = delta < BRIGHTNESS_CHANGE_DELAY_MSEC;
        logger.debug("  $BRI delta={}ms {}", delta, (belowThresh ? "< DELAY" : ""));
        if (belowThresh) {
            // we just sent a command from OH, so we can ignore this message from network
            logger.debug("  $BRI a request was sent {} < {} ms --> no action needed", delta,
                    BRIGHTNESS_CHANGE_DELAY_MSEC);
        } else {
            if (msg.isOn()) {
                logger.debug("  $BRI \"ON\" notification from network, scheduling requestStatus...");
                // we must wait BRIGHTNESS_STATUS_REQUEST_DELAY_MSEC to be sure dimmer has reached final level
                scheduler.schedule(() -> {
                    requestStatus(CHANNEL_BRIGHTNESS);
                }, BRIGHTNESS_STATUS_REQUEST_DELAY_MSEC, TimeUnit.MILLISECONDS);
            } else {
                logger.debug("  $BRI update from network");
                if (msg.getWhat() != null) {
                    updateBrightnessState(msg);
                } else { // dimension notification
                    if (msg.getDim() == Lighting.DIM.DIMMER_LEVEL_100) {
                        int newBrightness;
                        try {
                            newBrightness = msg.parseDimmerLevel100();
                        } catch (FrameException fe) {
                            logger.warn("updateBrightness() Wrong value for dimmerLevel100 in message: {}", msg);
                            return;
                        }
                        logger.debug("  $BRI DIMMER_LEVEL_100 newBrightness={}", newBrightness);
                        updateState(CHANNEL_BRIGHTNESS, new PercentType(newBrightness));
                        if (newBrightness == 0) {
                            brightnessBeforeOff = brightness;
                        }
                        brightness = newBrightness;
                    } else {
                        logger.warn("updateBrightness() Cannot handle message {} for thing {}", msg,
                                getThing().getUID());
                        return;
                    }
                }
            }
        }
        logger.debug("  $BRI---END updateBrightness({}) || bri={} briBeforeOff={}", msg, brightness,
                brightnessBeforeOff);
    }

    /**
     * Updates light brightness state based on a OWN Lighting message
     *
     * @param msg the Lighting message received
     */
    private void updateBrightnessState(Lighting msg) {
        if (msg.getWhat() != null) {
            int newBrightnessWhat = msg.getWhat().value();
            int brightnessWhat = UNKNOWN_STATE;
            if (brightness != UNKNOWN_STATE) {
                brightnessWhat = Lighting.percentToWhat(brightness).value();
            }
            logger.debug("  $BRI brightnessWhat {} --> {}", brightnessWhat, newBrightnessWhat);
            if (brightnessWhat != newBrightnessWhat) {
                int newBrightness = Lighting.levelToPercent(newBrightnessWhat);
                updateState(CHANNEL_BRIGHTNESS, new PercentType(newBrightness));
                if (msg.isOff()) {
                    brightnessBeforeOff = brightness;
                }
                brightness = newBrightness;
                logger.debug("  $BRI brightness CHANGED to {}", brightness);
            } else {
                logger.debug("  $BRI no change");
            }
        }
    }

    /**
     * Updates light on/off state based on a OWN Lighting event message received
     *
     * @param msg the Lighting message received
     */
    private void updateOnOffState(Lighting msg) {
        OpenWebNetBridgeHandler brH = bridgeHandler;
        if (brH != null) {
            if (msg.isOn() || msg.isOff()) {
                String channelId;
                int switchId = 0;
                if (brH.isBusGateway()) {
                    channelId = CHANNEL_SWITCH;
                } else {
                    WhereZigBee w = (WhereZigBee) (msg.getWhere());
                    if (WhereZigBee.UNIT_02.equals(w.getUnit())) {
                        channelId = CHANNEL_SWITCH_02;
                        switchId = 1;
                    } else {
                        channelId = CHANNEL_SWITCH_01;
                    }
                }
                int currentSt = sw[switchId];
                int newSt = (msg.isOn() ? 1 : 0);
                if (newSt != currentSt) {
                    updateState(channelId, (newSt == 1 ? OnOffType.ON : OnOffType.OFF));
                    sw[switchId] = newSt;
                    logger.debug("  {} ONOFF CHANGED to {}", ownId, newSt);
                } else {
                    logger.debug("  {} ONOFF no change", ownId);
                }
            } else {
                logger.debug("updateOnOffState() Ignoring unsupported WHAT for thing {}. Frame={}", getThing().getUID(),
                        msg.getFrameValue());
                return;
            }
        }
    }

    /**
     * Returns a WHERE address string based on channelId string
     *
     * @param channelId the channelId string
     **/
    @Nullable
    protected String toWhere(String channelId) {
        Where w = deviceWhere;
        if (w != null) {
            OpenWebNetBridgeHandler brH = bridgeHandler;
            if (brH != null) {
                if (brH.isBusGateway()) {
                    return w.value();
                } else if (channelId.equals(CHANNEL_SWITCH_02)) {
                    return ((WhereZigBee) w).valueWithUnit(WhereZigBee.UNIT_02);
                } else { // CHANNEL_SWITCH_01 or other channels
                    return ((WhereZigBee) w).valueWithUnit(WhereZigBee.UNIT_01);
                }
            }
        }
        return null;
    }
}
