/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler.sensor;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
 * The {@link MotionSensorHandler}
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MotionSensorHandler extends BaseHandler {

    private final Logger logger = LoggerFactory.getLogger(MotionSensorHandler.class);
    private final String timeFormat = "HH:mm";
    private String startTime = "20:00";
    private String endTime = "07:00";

    public MotionSensorHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        // links of types which can be established towards this device
        linkCandidateTypes = List.of(DEVICE_TYPE_LIGHT, DEVICE_TYPE_OUTLET);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);
            handleUpdate(values);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        String targetChannel = channelUID.getIdWithoutGroup();
        switch (targetChannel) {
            case CHANNEL_ACTIVE_DURATION:
                int seconds = -1;
                if (command instanceof DecimalType decimal) {
                    seconds = decimal.intValue();
                } else if (command instanceof QuantityType<?> quantity) {
                    QuantityType<?> secondsQunatity = quantity.toUnit(Units.SECOND);
                    if (secondsQunatity != null) {
                        seconds = secondsQunatity.intValue();
                    }
                }
                if (seconds > 0) {
                    String updateData = String
                            .format(gateway().model().getTemplate(Model.TEMPLATE_SENSOR_DURATION_UPDATE), seconds);
                    sendPatch(new JSONObject(updateData));
                }
                break;
            case CHANNEL_SCHEDULE:
                if (command instanceof DecimalType decimal) {
                    switch (decimal.intValue()) {
                        case 0:
                            gateway().api().sendPatch(config.id,
                                    new JSONObject(gateway().model().getTemplate(Model.TEMPLATE_SENSOR_ALWQAYS_ON)));
                            break;
                        case 1:
                            gateway().api().sendPatch(config.id,
                                    new JSONObject(gateway().model().getTemplate(Model.TEMPLATE_SENSOR_FOLLOW_SUN)));
                            break;
                        case 2:
                            String template = gateway().model().getTemplate(Model.TEMPLATE_SENSOR_SCHEDULE_ON);
                            gateway().api().sendPatch(config.id,
                                    new JSONObject(String.format(template, startTime, endTime)));
                            break;
                    }
                }
                break;
            case CHANNEL_SCHEDULE_START:
                String startSchedule = gateway().model().getTemplate(Model.TEMPLATE_SENSOR_SCHEDULE_ON);
                if (command instanceof StringType string) {
                    // take string as it is, no consistency check
                    startTime = string.toFullString();
                } else if (command instanceof DateTimeType dateTime) {
                    startTime = dateTime.format(timeFormat, ZoneId.systemDefault());
                }
                gateway().api().sendPatch(config.id, new JSONObject(String.format(startSchedule, startTime, endTime)));
                break;
            case CHANNEL_SCHEDULE_END:
                String endSchedule = gateway().model().getTemplate(Model.TEMPLATE_SENSOR_SCHEDULE_ON);
                if (command instanceof StringType string) {
                    endTime = string.toFullString();
                    // take string as it is, no consistency check
                } else if (command instanceof DateTimeType dateTime) {
                    endTime = dateTime.format(timeFormat, ZoneId.systemDefault());
                }
                gateway().api().sendPatch(config.id, new JSONObject(String.format(endSchedule, startTime, endTime)));
                break;
            case CHANNEL_LIGHT_PRESET:
                if (command instanceof StringType string) {
                    JSONArray presetValues = new JSONArray();
                    // handle the standard presets from IKEA app, custom otherwise without consistency check
                    switch (string.toFullString()) {
                        case "Off":
                            // fine - array stays empty
                            break;
                        case "Warm":
                            presetValues = new JSONArray(
                                    gateway().model().getTemplate(Model.TEMPLATE_LIGHT_PRESET_WARM));
                            break;
                        case "Slowdown":
                            presetValues = new JSONArray(
                                    gateway().model().getTemplate(Model.TEMPLATE_LIGHT_PRESET_SLOWDOWN));
                            break;
                        case "Smooth":
                            presetValues = new JSONArray(
                                    gateway().model().getTemplate(Model.TEMPLATE_LIGHT_PRESET_SMOOTH));
                            break;
                        case "Bright":
                            presetValues = new JSONArray(
                                    gateway().model().getTemplate(Model.TEMPLATE_LIGHT_PRESET_BRIGHT));
                            break;
                        default:
                            presetValues = new JSONArray(string.toFullString());
                    }
                    JSONObject preset = new JSONObject();
                    preset.put("circadianPresets", presetValues);
                    super.sendAttributes(preset);
                }
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    switch (targetChannel) {
                        case CHANNEL_MOTION_DETECTION:
                            updateState(new ChannelUID(thing.getUID(), targetChannel),
                                    OnOffType.from(attributes.getBoolean(key)));
                            break;
                        case CHANNEL_ACTIVE_DURATION:
                            if (attributes.has("sensorConfig")) {
                                JSONObject sensorConfig = attributes.getJSONObject("sensorConfig");
                                if (sensorConfig.has("onDuration")) {
                                    int duration = sensorConfig.getInt("onDuration");
                                    updateState(new ChannelUID(thing.getUID(), targetChannel),
                                            QuantityType.valueOf(duration, Units.SECOND));
                                }
                            }
                            break;
                    }
                }
                // no direct channel mapping - sensor mapping is deeply nested :(
                switch (key) {
                    case "circadianPresets":
                        if (attributes.has("circadianPresets")) {
                            JSONArray lightPresets = attributes.getJSONArray("circadianPresets");
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_PRESET),
                                    StringType.valueOf(lightPresets.toString()));
                        }
                        break;
                    case "sensorConfig":
                        if (attributes.has("sensorConfig")) {
                            JSONObject sensorConfig = attributes.getJSONObject("sensorConfig");
                            if (sensorConfig.has("scheduleOn")) {
                                boolean scheduled = sensorConfig.getBoolean("scheduleOn");
                                if (scheduled) {
                                    // examine schedule
                                    if (sensorConfig.has("schedule")) {
                                        JSONObject schedule = sensorConfig.getJSONObject("schedule");
                                        if (schedule.has("onCondition") && schedule.has("offCondition")) {
                                            JSONObject onCondition = schedule.getJSONObject("onCondition");
                                            JSONObject offCondition = schedule.getJSONObject("offCondition");
                                            if (onCondition.has("time")) {
                                                String onTime = onCondition.getString("time");
                                                String offTime = offCondition.getString("time");
                                                if ("sunset".equals(onTime)) {
                                                    // finally it's identified to follow the sun
                                                    updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE),
                                                            new DecimalType(1));
                                                    Instant sunsetDateTime = gateway().getSunsetDateTime();
                                                    if (sunsetDateTime != null) {
                                                        updateState(
                                                                new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START),
                                                                new DateTimeType(sunsetDateTime));
                                                    } else {
                                                        updateState(
                                                                new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START),
                                                                UnDefType.UNDEF);
                                                        logger.warn(
                                                                "MOTION_SENSOR Location not activated in IKEA App - cannot follow sun");
                                                    }
                                                    Instant sunriseDateTime = gateway().getSunriseDateTime();
                                                    if (sunriseDateTime != null) {
                                                        updateState(
                                                                new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_END),
                                                                new DateTimeType(sunriseDateTime));
                                                    } else {
                                                        updateState(
                                                                new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_END),
                                                                UnDefType.UNDEF);
                                                        logger.warn(
                                                                "MOTION_SENSOR Location not activated in IKEA App - cannot follow sun");
                                                    }
                                                } else {
                                                    // custom times - even worse parsing
                                                    String[] onHourMinute = onTime.split(":");
                                                    String[] offHourMinute = offTime.split(":");
                                                    if (onHourMinute.length == 2 && offHourMinute.length == 2) {
                                                        int onHour = Integer.parseInt(onHourMinute[0]);
                                                        int onMinute = Integer.parseInt(onHourMinute[1]);
                                                        int offHour = Integer.parseInt(offHourMinute[0]);
                                                        int offMinute = Integer.parseInt(offHourMinute[1]);
                                                        updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE),
                                                                new DecimalType(2));
                                                        ZonedDateTime on = ZonedDateTime.now().withHour(onHour)
                                                                .withMinute(onMinute);
                                                        ZonedDateTime off = ZonedDateTime.now().withHour(offHour)
                                                                .withMinute(offMinute);
                                                        updateState(
                                                                new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START),
                                                                new DateTimeType(on));
                                                        updateState(
                                                                new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_END),
                                                                new DateTimeType(off));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // always active
                                    updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE), new DecimalType(0));
                                    updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START),
                                            UnDefType.UNDEF);
                                    updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_END), UnDefType.UNDEF);
                                }
                            }
                        }
                        break;
                }
            }
        }
    }
}
