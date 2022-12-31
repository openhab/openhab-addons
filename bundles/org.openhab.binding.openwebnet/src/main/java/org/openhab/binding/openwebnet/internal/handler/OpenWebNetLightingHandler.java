/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.What;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
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

    // interval to interpret ON as response to requestStatus
    private static final int BRIGHTNESS_STATUS_REQUEST_INTERVAL_MSEC = 250;

    // time to wait before sending a statusRequest, to avoid repeated requests and ensure dimmer has reached its final
    // level
    private static final int BRIGHTNESS_STATUS_REQUEST_DELAY_MSEC = 900;

    private static final int UNKNOWN_STATE = 1000;

    private long lastBrightnessChangeSentTS = 0; // timestamp when last brightness change was sent to the device

    private long lastStatusRequestSentTS = 0; // timestamp when last status request was sent to the device

    private static long lastAllDevicesRefreshTS = 0; // ts when last all device refresh was sent for this handler

    private int brightness = UNKNOWN_STATE; // current brightness percent value for this device

    private int brightnessBeforeOff = UNKNOWN_STATE; // latest brightness before device was set to off

    private int sw[] = { UNKNOWN_STATE, UNKNOWN_STATE }; // current switch(es) state

    public OpenWebNetLightingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        super.requestChannelState(channel);
        if (deviceWhere != null) {
            try {
                lastStatusRequestSentTS = System.currentTimeMillis();
                send(Lighting.requestStatus(toWhere(channel.getId())));
            } catch (OWNException e) {
                logger.debug("Exception while requesting state for channel {}: {} ", channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    protected long getRefreshAllLastTS() {
        return lastAllDevicesRefreshTS;
    };

    @Override
    protected void refreshDevice(boolean refreshAll) {
        if (refreshAll) {
            logger.debug("--- refreshDevice() : refreshing GENERAL... ({})", thing.getUID());
            try {
                send(Lighting.requestStatus(WhereLightAutom.GENERAL.value()));
                lastAllDevicesRefreshTS = System.currentTimeMillis();
            } catch (OWNException e) {
                logger.warn("Excpetion while requesting all devices refresh: {}", e.getMessage());
            }
        } else {
            logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());
            ThingTypeUID thingType = thing.getThingTypeUID();
            if (THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS.equals(thingType)) {
                // Unfortunately using USB Gateway OpenWebNet both switch endpoints cannot be requested at the same
                // time using UNIT 00 because USB stick returns NACK, so we need to send a request status for both
                // endpoints
                requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_SWITCH_02));
            }
            requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_SWITCH_01));
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
        logger.debug("handleMessage({}) for thing: {}", msg, thing.getUID());
        super.handleMessage(msg);
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
        logger.debug("  $BRI updateBrightness({})       || bri={} briBeforeOff={}", msg, brightness,
                brightnessBeforeOff);
        long now = System.currentTimeMillis();
        long delta = now - lastBrightnessChangeSentTS;
        boolean belowThresh = delta < BRIGHTNESS_STATUS_REQUEST_DELAY_MSEC;
        logger.debug("  $BRI delta={}ms {}", delta, (belowThresh ? "< DELAY" : ""));
        if (belowThresh) {
            // we just sent a command from OH, so we can ignore this message from network
            logger.debug("  $BRI a command was sent {} < {} ms --> no action needed", delta,
                    BRIGHTNESS_STATUS_REQUEST_DELAY_MSEC);
        } else {
            if (msg.isOn()) {
                // if we have not just sent a requestStatus, on ON event we send requestStatus to know current level
                long deltaStatusReq = now - lastStatusRequestSentTS;
                if (deltaStatusReq > BRIGHTNESS_STATUS_REQUEST_INTERVAL_MSEC) {
                    logger.debug("  $BRI 'ON' is new notification from network, scheduling requestStatus...");
                    // we must wait BRIGHTNESS_STATUS_REQUEST_DELAY_MSEC to be sure dimmer has reached its final level
                    scheduler.schedule(() -> {
                        requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_BRIGHTNESS));
                    }, BRIGHTNESS_STATUS_REQUEST_DELAY_MSEC, TimeUnit.MILLISECONDS);
                    return;
                } else {
                    // otherwise we interpret this ON event as the requestStatus response event with level=1
                    // so we proceed to call updateBrightnessState()
                    logger.debug("  $BRI 'ON' is the requestStatus response level");
                }
            }
            logger.debug("  $BRI update from network");
            if (msg.getWhat() != null) {
                updateBrightnessState(msg);
            } else { // dimension notification
                if (msg.getDim() == Lighting.DimLighting.DIMMER_LEVEL_100) {
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
                    logger.warn("updateBrightness() Cannot handle message {} for thing {}", msg, getThing().getUID());
                    return;
                }
            }
        }
        logger.debug("  $BRI---END updateBrightness({}) || bri={} briBeforeOff={}", msg, brightness,
                brightnessBeforeOff);
    }

    /**
     * Updates light brightness state based on an OWN Lighting message
     *
     * @param msg the Lighting message received
     */
    private void updateBrightnessState(Lighting msg) {
        What w = msg.getWhat();
        if (w != null) {
            if (Lighting.WhatLighting.ON.equals(w)) {
                w = Lighting.WhatLighting.DIMMER_LEVEL_2; // levels start at 2
            }
            int newBrightnessWhat = w.value();
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
     * Updates light on/off state based on an OWN Lighting event message received
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

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereLightAutom(wStr);
    }

    /**
     * Returns a WHERE address string based on channelId string
     *
     * @param channelId the channelId string
     **/
    @Nullable
    private String toWhere(String channelId) {
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
