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
package org.openhab.binding.dirigera.internal.handler.sensor;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.model.Model;
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

/**
 * The {@link MotionSensorHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MotionSensorHandler extends BaseHandler {

    protected static final String DURATION_UPDATE = "{\"attributes\":{\"sensorConfig\":{\"onDuration\":%s}}}";

    protected static final String SCHEDULE_ALWAYS_ON = "{\"attributes\":{\"sensorConfig\":{\"scheduleOn\":false}}}";
    protected static final String SCHEDULE_FOLLOW_SUN = "{\"attributes\":{\"sensorConfig\":{\"scheduleOn\": true,\"schedule\": {\"onCondition\": {\"time\": \"sunset\"},\"offCondition\": {\"time\": \"sunrise\"}}}}}";
    protected static final String SCHEDULE_SCHEDULE_ON = "{\"attributes\":{\"sensorConfig\":{\"scheduleOn\":true}}}";
    protected static final String SCHEDULE_START_TIME = "{\"attributes\":{\"sensorConfig\":{\"schedule\": {\" onCondition\": {\"time\": %s}}}}}";
    protected static final String SCHEDULE_END_TIME = "{\"attributes\":{\"sensorConfig\":{\"schedule\": {,\"offCondition\": {\"time\": %s}}}}}";

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
                    String updateData = String.format(DURATION_UPDATE, seconds);
                    gateway().api().sendPatch(config.id, new JSONObject(updateData));
                }
                break;
            case CHANNEL_SCHEDULE:
                if (command instanceof DecimalType decimal) {
                    switch (decimal.intValue()) {
                        case 0:
                            gateway().api().sendPatch(config.id, new JSONObject(SCHEDULE_ALWAYS_ON));
                        case 1:
                            gateway().api().sendPatch(config.id, new JSONObject(SCHEDULE_FOLLOW_SUN));
                        case 2:
                            gateway().api().sendPatch(config.id, new JSONObject(SCHEDULE_SCHEDULE_ON));

                    }
                }
            case CHANNEL_SCHEDULE_START:
                if (command instanceof StringType string) {
                    // take string as it is, no consistency check
                    String scheduleStart = String.format(SCHEDULE_START_TIME, string.toFullString());
                    gateway().api().sendPatch(config.id, new JSONObject(scheduleStart));
                } else if (command instanceof DateTimeType dateTime) {
                    String startTime = dateTime.getZonedDateTime().getHour() + ":"
                            + dateTime.getZonedDateTime().getMinute();
                    String scheduleStart = String.format(SCHEDULE_START_TIME, startTime);
                    gateway().api().sendPatch(config.id, new JSONObject(scheduleStart));
                }
            case CHANNEL_SCHEDULE_END:
                if (command instanceof StringType string) {
                    // take string as it is, no consistency check
                    String scheduleStart = String.format(SCHEDULE_START_TIME, string.toFullString());
                    gateway().api().sendPatch(config.id, new JSONObject(scheduleStart));
                } else if (command instanceof DateTimeType dateTime) {
                    String endTime = dateTime.getZonedDateTime().getHour() + ":"
                            + dateTime.getZonedDateTime().getMinute();
                    String scheduleEnd = String.format(SCHEDULE_END_TIME, endTime);
                    gateway().api().sendPatch(config.id, new JSONObject(scheduleEnd));
                }
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        // handle reachable flag
        super.handleUpdate(update);
        // now device specific
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    switch (targetChannel) {
                        case CHANNEL_DETECTION:
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
                    case "schedule":
                    case "schedule-start":
                    case "schedule-end":
                        if (attributes.has("sensorConfig")) {
                            JSONObject sensorConfig = attributes.getJSONObject("sensorConfig");
                            if (sensorConfig.has("onDuration")) {
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
                                                    updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START),
                                                            new DateTimeType(gateway().getSunsetDateTime()));
                                                    updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_END),
                                                            new DateTimeType(gateway().getSunriseDateTime()));
                                                } else {
                                                    // custom times - even worse parsing
                                                    String[] onHourMinute = onTime.split(":");
                                                    String[] offHourMinute = offTime.split(":");
                                                    if (onHourMinute.length == 2 && offHourMinute.length == 2) {
                                                        int onHour = Integer.parseInt(onHourMinute[0]);
                                                        int onMinute = Integer.parseInt(onHourMinute[1]);
                                                        int offHour = Integer.parseInt(onHourMinute[0]);
                                                        int offMinute = Integer.parseInt(onHourMinute[1]);
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
                }
            }
        }
    }
}
