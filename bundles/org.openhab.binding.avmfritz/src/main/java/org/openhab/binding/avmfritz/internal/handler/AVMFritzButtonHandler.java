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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.BatteryModel;
import org.openhab.binding.avmfritz.internal.dto.ButtonModel;
import org.openhab.binding.avmfritz.internal.dto.DeviceModel;
import org.openhab.binding.avmfritz.internal.dto.HumidityModel;
import org.openhab.binding.avmfritz.internal.dto.TemperatureModel;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a FRITZ! buttons. Handles commands, which are sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class AVMFritzButtonHandler extends DeviceHandler {

    private static final String TOP_RIGHT_SUFFIX = "-1";
    private static final String BOTTOM_RIGHT_SUFFIX = "-3";
    private static final String BOTTOM_LEFT_SUFFIX = "-5";
    private static final String TOP_LEFT_SUFFIX = "-7";

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

            if (device instanceof DeviceModel deviceModel) {
                if (deviceModel.isHANFUNButton()) {
                    updateHANFUNButton(deviceModel.getButtons());
                }
                if (deviceModel.isButton()) {
                    if (DECT400_THING_TYPE.equals(thing.getThingTypeUID())) {
                        updateShortLongPressButton(deviceModel.getButtons());
                    } else if (DECT440_THING_TYPE.equals(thing.getThingTypeUID())) {
                        updateButtons(deviceModel.getButtons());
                    }
                    updateBattery(deviceModel);
                }
            }
        }
    }

    @Override
    protected void updateTemperatureSensor(@Nullable TemperatureModel temperatureModel) {
        if (temperatureModel != null) {
            String channelId = (DECT440_THING_TYPE.equals(thing.getThingTypeUID())
                    ? CHANNEL_GROUP_SENSORS + ChannelUID.CHANNEL_GROUP_SEPARATOR
                    : "") + CHANNEL_TEMPERATURE;
            updateThingChannelState(channelId, new QuantityType<>(temperatureModel.getCelsius(), SIUnits.CELSIUS));
            updateThingChannelConfiguration(channelId, CONFIG_CHANNEL_TEMP_OFFSET, temperatureModel.getOffset());
        }
    }

    @Override
    protected void updateHumiditySensor(@Nullable HumidityModel humidityModel) {
        if (humidityModel != null) {
            String channelId = (DECT440_THING_TYPE.equals(thing.getThingTypeUID())
                    ? CHANNEL_GROUP_SENSORS + ChannelUID.CHANNEL_GROUP_SEPARATOR
                    : "") + CHANNEL_HUMIDITY;
            updateThingChannelState(channelId, new QuantityType<>(humidityModel.getRelativeHumidity(), Units.PERCENT));
        }
    }

    @Override
    protected void updateBattery(BatteryModel batteryModel) {
        String batteryLevelChannelId = (DECT440_THING_TYPE.equals(thing.getThingTypeUID())
                ? CHANNEL_GROUP_DEVICE + ChannelUID.CHANNEL_GROUP_SEPARATOR
                : "") + CHANNEL_BATTERY;
        BigDecimal batteryLevel = batteryModel.getBattery();
        updateThingChannelState(batteryLevelChannelId,
                batteryLevel == null ? UnDefType.UNDEF : new DecimalType(batteryLevel));
        String lowBatteryChannelId = (DECT440_THING_TYPE.equals(thing.getThingTypeUID())
                ? CHANNEL_GROUP_DEVICE + ChannelUID.CHANNEL_GROUP_SEPARATOR
                : "") + CHANNEL_BATTERY_LOW;
        BigDecimal lowBattery = batteryModel.getBatterylow();
        if (lowBattery == null) {
            updateThingChannelState(lowBatteryChannelId, UnDefType.UNDEF);
        } else {
            updateThingChannelState(lowBatteryChannelId, OnOffType.from(BatteryModel.BATTERY_ON.equals(lowBattery)));
        }
    }

    private void updateShortLongPressButton(List<ButtonModel> buttons) {
        ButtonModel shortPressButton = !buttons.isEmpty() ? buttons.get(0) : null;
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

    private void updateButtons(List<ButtonModel> buttons) {
        Optional<ButtonModel> topLeft = buttons.stream().filter(b -> b.getIdentifier().endsWith(TOP_LEFT_SUFFIX))
                .findFirst();
        if (topLeft.isPresent()) {
            updateButton(topLeft.get(), CommonTriggerEvents.PRESSED, CHANNEL_GROUP_TOP_LEFT);
        }
        Optional<ButtonModel> bottomLeft = buttons.stream().filter(b -> b.getIdentifier().endsWith(BOTTOM_LEFT_SUFFIX))
                .findFirst();
        if (bottomLeft.isPresent()) {
            updateButton(bottomLeft.get(), CommonTriggerEvents.PRESSED, CHANNEL_GROUP_BOTTOM_LEFT);
        }
        Optional<ButtonModel> topRight = buttons.stream().filter(b -> b.getIdentifier().endsWith(TOP_RIGHT_SUFFIX))
                .findFirst();
        if (topRight.isPresent()) {
            updateButton(topRight.get(), CommonTriggerEvents.PRESSED, CHANNEL_GROUP_TOP_RIGHT);
        }
        Optional<ButtonModel> bottomRight = buttons.stream()
                .filter(b -> b.getIdentifier().endsWith(BOTTOM_RIGHT_SUFFIX)).findFirst();
        if (bottomRight.isPresent()) {
            updateButton(bottomRight.get(), CommonTriggerEvents.PRESSED, CHANNEL_GROUP_BOTTOM_RIGHT);
        }
    }

    private void updateHANFUNButton(List<ButtonModel> buttons) {
        if (!buttons.isEmpty()) {
            updateButton(buttons.get(0), CommonTriggerEvents.PRESSED);
        }
    }

    private void updateButton(ButtonModel buttonModel, String event) {
        updateButton(buttonModel, event, null);
    }

    private void updateButton(ButtonModel buttonModel, String event, @Nullable String channelGroupId) {
        int lastPressedTimestamp = buttonModel.getLastpressedtimestamp();
        if (lastPressedTimestamp == 0) {
            updateThingChannelState(
                    channelGroupId == null ? CHANNEL_LAST_CHANGE
                            : channelGroupId + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_LAST_CHANGE,
                    UnDefType.UNDEF);
        } else {
            ZonedDateTime timestamp = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastPressedTimestamp),
                    ZoneId.systemDefault());
            Instant then = timestamp.toInstant();
            // Avoid dispatching events if "lastpressedtimestamp" is older than now "lastTimestamp" (e.g. during
            // restart)
            if (then.isAfter(lastTimestamp)) {
                lastTimestamp = then;
                triggerThingChannel(channelGroupId == null ? CHANNEL_PRESS
                        : channelGroupId + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_PRESS, event);
            }
            updateThingChannelState(
                    channelGroupId == null ? CHANNEL_LAST_CHANGE
                            : channelGroupId + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_LAST_CHANGE,
                    new DateTimeType(timestamp));
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
