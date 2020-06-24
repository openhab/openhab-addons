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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.ButtonModel;
import org.openhab.binding.avmfritz.internal.dto.DeviceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a FRITZ! buttons. Handles commands, which are sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class AVMFritzButtonHandler extends DeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzButtonHandler.class);
    /**
     * keeps track of the last timestamp for handling trigger events
     */
    private Instant lastTimestamp;

    /**
     * Constructor
     *
     * @param thing Thing object representing a FRITZ! button
     */
    public AVMFritzButtonHandler(Thing thing) {
        super(thing);
        lastTimestamp = Instant.now();
    }

    @Override
    public void onDeviceUpdated(ThingUID thingUID, AVMFritzBaseModel device) {
        if (thing.getUID().equals(thingUID)) {
            super.onDeviceUpdated(thingUID, device);

            if (device instanceof DeviceModel) {
                DeviceModel deviceModel = (DeviceModel) device;
                if (deviceModel.isHANFUNButton()) {
                    updateHANFUNButton(deviceModel.getButtons());
                }
                if (deviceModel.isButton()) {
                    updateShortLongPressButton(deviceModel.getButtons());
                    updateBattery(deviceModel);
                }
            }
        }
    }

    private void updateShortLongPressButton(List<ButtonModel> buttons) {
        ButtonModel shortPressButton = buttons.size() > 0 ? buttons.get(0) : null;
        ButtonModel longPressButton = buttons.size() > 1 ? buttons.get(1) : null;
        ButtonModel lastPressedButton = shortPressButton != null && (longPressButton == null
                || shortPressButton.getLastpressedtimestamp() > longPressButton.getLastpressedtimestamp())
                        ? shortPressButton
                        : longPressButton;
        if (lastPressedButton != null) {
            updateButton(lastPressedButton,
                    lastPressedButton.equals(shortPressButton) ? CommonTriggerEvents.SHORT_PRESSED
                            : CommonTriggerEvents.LONG_PRESSED);
        }
    }

    private void updateHANFUNButton(List<ButtonModel> buttons) {
        if (!buttons.isEmpty()) {
            updateButton(buttons.get(0), CommonTriggerEvents.PRESSED);
        }
    }

    private void updateButton(ButtonModel buttonModel, String event) {
        int lastPressedTimestamp = buttonModel.getLastpressedtimestamp();
        if (lastPressedTimestamp == 0) {
            updateThingChannelState(CHANNEL_LAST_CHANGE, UnDefType.UNDEF);
        } else {
            ZonedDateTime timestamp = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastPressedTimestamp),
                    ZoneId.systemDefault());
            Instant then = timestamp.toInstant();
            // Avoid dispatching events if "lastpressedtimestamp" is older than now "lastTimestamp" (e.g. during
            // restart)
            if (then.isAfter(lastTimestamp)) {
                lastTimestamp = then;
                triggerThingChannel(CHANNEL_PRESS, event);
            }
            updateThingChannelState(CHANNEL_LAST_CHANGE, new DateTimeType(timestamp));
        }
    }

    /**
     * Triggers thing channels.
     *
     * @param channelId ID of the channel to be triggered.
     * @param event Event to emit
     */
    private void triggerThingChannel(String channelId, String event) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            triggerChannel(channel.getUID(), event);
        } else {
            logger.debug("Channel '{}' in thing '{}' does not exist.", channelId, thing.getUID());
        }
    }
}
