/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler.matter;

import static org.openhab.binding.dirigera.internal.Constants.*;
import static org.openhab.binding.dirigera.internal.interfaces.Model.DEVICE_TYPE_OCCUPANCY_SENSOR;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.ResourceReader;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterSensor} for all sensor devices e.g. IKEA occupancy sensor.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MatterSensor extends BaseMatterHandler {
    private static final String TIME_FORMAT = "%02d:%02d";
    private final Logger logger = LoggerFactory.getLogger(MatterSensor.class);
    private String startTime = "20:00";
    private String endTime = "07:00";

    public MatterSensor(Thing thing) {
        super(thing);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.hasType(DEVICE_TYPE_OCCUPANCY_SENSOR)) {
            createSensorConfigChannels();
        }
    }

    /**
     * Updates handled by super class. Sensor configuration handled in code due to deep nested JSONObject evaluation is
     * necessary which cannot be reflected as simple configuration
     */
    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        if (!getProperty(Model.JSON_KEY_ATTRIBUTES + "/sensorConfig", update).isBlank()) {
            String scheduleOn = getProperty(Model.JSON_KEY_ATTRIBUTES + "/sensorConfig/scheduleOn", update);
            String duration = getProperty(Model.JSON_KEY_ATTRIBUTES + "/sensorConfig/onDuration", update);
            String onCondition = getProperty(Model.JSON_KEY_ATTRIBUTES + "/sensorConfig/schedule/onCondition/time",
                    update);
            String offCondition = getProperty(Model.JSON_KEY_ATTRIBUTES + "/sensorConfig/schedule/offCondition/time",
                    update);

            updateState(new ChannelUID(thing.getUID(), CHANNEL_ACTIVE_DURATION),
                    QuantityType.valueOf(Integer.parseInt(duration), Units.SECOND));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE),
                    getScheduleType(scheduleOn, onCondition, offCondition));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START), getDateTime(onCondition));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_END), getDateTime(offCondition));
        }
    }

    /**
     * Gets date time state from given time string with
     * - hout and minute - manual schedule
     * - "sunset"/"sunrise" - automatic schedule
     *
     * @param time with format "HH:mm" or "sunset"/"sunrise"
     * @return DateTimeType state or UNDEF if not resolvable
     */
    private State getDateTime(String time) {
        if ("sunset".equals(time)) {
            Instant sunset = gateway().getSunsetDateTime();
            if (sunset != null) {
                return new DateTimeType(sunset);
            }
            return new DateTimeType();
        } else if ("sunrise".equals(time)) {
            Instant sunrise = gateway().getSunriseDateTime();
            if (sunrise != null) {
                return new DateTimeType(sunrise);
            }
        } else {
            String[] timeSplit = time.split(":");
            if (timeSplit.length == 2) {
                int onHour = Integer.parseInt(timeSplit[0]);
                int onMinute = Integer.parseInt(timeSplit[1]);
                return new DateTimeType(Instant.now().truncatedTo(ChronoUnit.MINUTES)
                        .atZone(gateway().getTimeZoneProvider().getTimeZone()).withHour(onHour).withMinute(onMinute));
            }
        }
        return UnDefType.UNDEF;
    }

    /**
     * Gets schedule type from given parameters
     *
     * @param scheduleOn string with boolean value "true"/"false"
     * @param onCondition time string as "HH:mm" or "sunset"
     * @param offCondition time string as "HH:mm" or "sunrise"
     * @return DecimalType with 0=always off, 1=follow sun, 2=manual schedule
     */
    private DecimalType getScheduleType(String scheduleOn, String onCondition, String offCondition) {
        if (Boolean.TRUE.toString().equalsIgnoreCase(scheduleOn)) {
            if ("sunset".equals(onCondition) && "sunrise".equals(offCondition)) {
                return new DecimalType(1);
            } else {
                return new DecimalType(2);
            }
        } else {
            return new DecimalType(0);
        }
    }

    /**
     * Gets property from deep nested JSON object by given path with "/" as separator
     *
     * @param path path with "/" as separator
     * @param source JSON object
     * @return property value as string or empty string if not found
     */
    private String getProperty(String path, JSONObject source) {
        String[] keys = path.split("/");
        JSONObject iterator = source;
        for (int i = 0; i < keys.length - 1; i++) {
            iterator = iterator.optJSONObject(keys[i]);
            if (iterator == null) {
                return "";
            }
        }
        return iterator.optString(keys[keys.length - 1]).toString();
    }

    /**
     * Handles commands for sensor configuration channels
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        String targetChannel = channelUID.getIdWithoutGroup();
        switch (targetChannel) {
            case CHANNEL_ACTIVE_DURATION:
                int seconds = getDurationInSeconds(command);
                if (seconds >= 0) {
                    String patchString = String
                            .format(ResourceReader.getResource(Model.TEMPLATE_SENSOR_DURATION_UPDATE), seconds);
                    sendRequest(new JSONObject(patchString));
                }
                break;
            case CHANNEL_SCHEDULE:
                if (command instanceof DecimalType decimal) {
                    sendRequest(getSchedulePatch(decimal));
                }
                break;
            case CHANNEL_SCHEDULE_START:
                sendRequest(getScheduleTimePatch(command, true));
                break;
            case CHANNEL_SCHEDULE_END:
                sendRequest(getScheduleTimePatch(command, false));
                break;
            case CHANNEL_LIGHT_PRESET:
                if (command instanceof StringType string) {
                    var preset = switch (string.toFullString()) {
                        case "Off" -> new JSONArray();
                        case "Warm" -> new JSONArray(ResourceReader.getResource(Model.TEMPLATE_LIGHT_PRESET_WARM));
                        case "Slowdown" ->
                            new JSONArray(ResourceReader.getResource(Model.TEMPLATE_LIGHT_PRESET_SLOWDOWN));
                        case "Smooth" -> new JSONArray(ResourceReader.getResource(Model.TEMPLATE_LIGHT_PRESET_SMOOTH));
                        case "Bright" -> new JSONArray(ResourceReader.getResource(Model.TEMPLATE_LIGHT_PRESET_BRIGHT));
                        default -> {
                            try {
                                yield new JSONArray(string.toFullString());
                            } catch (JSONException e) {
                                logger.info("MATTER SENSOR cannot transform {} into preset JSONArray: {}", command,
                                        e.getMessage());
                                yield null;
                            }
                        }
                    };
                    if (preset instanceof JSONArray) {
                        JSONObject presetJson = (new JSONObject()).put("circadianPresets", preset);
                        sendRequest((new JSONObject()).put(Model.JSON_KEY_ATTRIBUTES, presetJson));
                    }
                }
        }
    }

    /**
     * Get request to API as JSONObject to set sensor configuration schedule
     *
     * @param decimal from options
     * @return
     */
    private JSONObject getSchedulePatch(DecimalType decimal) {
        return switch (decimal.intValue()) {
            case 0 -> new JSONObject(ResourceReader.getResource(Model.TEMPLATE_SENSOR_ALWQAYS_ON));
            case 1 -> new JSONObject(ResourceReader.getResource(Model.TEMPLATE_SENSOR_FOLLOW_SUN));
            case 2 -> new JSONObject(
                    String.format(ResourceReader.getResource(Model.TEMPLATE_SENSOR_SCHEDULE_ON), startTime, endTime));
            default -> new JSONObject();
        };
    }

    private JSONObject getScheduleTimePatch(Command command, boolean start) {
        String time;
        if (command instanceof StringType string) {
            time = string.toFullString();
        } else if (command instanceof DateTimeType dateTime) {
            time = getTimeString(dateTime);

        } else {
            logger.info("MATTER SENSOR cannot handle {} as DateTimeType", command);
            return new JSONObject();
        }
        if (start) {
            startTime = time;
        } else {
            endTime = time;
        }
        return new JSONObject(
                String.format(ResourceReader.getResource(Model.TEMPLATE_SENSOR_SCHEDULE_ON), startTime, endTime));
    }

    /**
     * Get duration in seconds from DecimalType or QuantityType
     *
     * @param command as OH type
     * @return seconds as integer
     */
    private int getDurationInSeconds(Command command) {
        int seconds = -1;
        if (command instanceof DecimalType decimal) {
            seconds = decimal.intValue();
        } else if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> secondsQunatity = quantity.toUnit(Units.SECOND);
            if (secondsQunatity != null) {
                seconds = secondsQunatity.intValue();
            }
        }
        return seconds;
    }

    /**
     * Send update to all related devices with the same device type
     *
     * @param patch as JSONObject
     */
    private void sendRequest(JSONObject patch) {
        if (!patch.isEmpty()) {
            super.getIdsFor(DEVICE_TYPE_OCCUPANCY_SENSOR).forEach(deviceId -> {
                super.sendPatch(deviceId, patch);
            });
        }
    }

    private String getTimeString(DateTimeType dateTime) {
        ZonedDateTime desired = dateTime.getZonedDateTime(gateway().getTimeZoneProvider().getTimeZone());
        logger.warn("Converted DateTimeType {} }", desired);
        return String.format(TIME_FORMAT, desired.getHour(), desired.getMinute());
    }

    /**
     * Creates the sensor configuration channels
     */
    private void createSensorConfigChannels() {
        createChannelIfNecessary(CHANNEL_SCHEDULE, "sensor-schedule", CoreItemFactory.NUMBER);
        createChannelIfNecessary(CHANNEL_ACTIVE_DURATION, "duration", "Number:Time");
        createChannelIfNecessary(CHANNEL_SCHEDULE_START, "schedule-start-time", CoreItemFactory.DATETIME);
        createChannelIfNecessary(CHANNEL_SCHEDULE_END, "schedule-end-time", CoreItemFactory.DATETIME);
    }
}
