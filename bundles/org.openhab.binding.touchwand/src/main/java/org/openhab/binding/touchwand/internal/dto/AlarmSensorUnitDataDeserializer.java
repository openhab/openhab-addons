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

package org.openhab.binding.touchwand.internal.dto;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.touchwand.internal.dto.TouchWandAlarmSensorCurrentStatus.Alarm;
import org.openhab.binding.touchwand.internal.dto.TouchWandAlarmSensorCurrentStatus.bSensor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link AlarmSensorUnitDataDeserializer} implements AlarmSensorUnitData unit
 * Json De-serializer.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class AlarmSensorUnitDataDeserializer implements JsonDeserializer<TouchWandUnitDataAlarmSensor> {

    @Override
    public TouchWandUnitDataAlarmSensor deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {

        TouchWandUnitDataAlarmSensor touchWandUnitDataAlarmSensor = new TouchWandUnitDataAlarmSensor();

        JsonElement myJson = json;
        if (myJson != null) {
            JsonObject jsonObject = myJson.getAsJsonObject();
            touchWandUnitDataAlarmSensor.setId(jsonObject.get("id").getAsInt());
            touchWandUnitDataAlarmSensor.setName(jsonObject.get("name").getAsString());
            touchWandUnitDataAlarmSensor.setConnectivity(jsonObject.get("connectivity").getAsString());
            touchWandUnitDataAlarmSensor.setType(jsonObject.get("type").getAsString());
            touchWandUnitDataAlarmSensor.setHasBattery(jsonObject.get("hasBattery").getAsBoolean());
            JsonElement powerMeterElement = jsonObject.get("hasPowerMeter");
            if (!powerMeterElement.isJsonNull()) {
                touchWandUnitDataAlarmSensor.setHasPowerMeter(powerMeterElement.getAsBoolean());
            } else {
                touchWandUnitDataAlarmSensor.setHasPowerMeter(false);
            }

            if (!jsonObject.get("status").isJsonNull()) { // Sometimes status in null
                touchWandUnitDataAlarmSensor.setStatus(jsonObject.get("status").getAsString());
            }

            GsonBuilder builder = new GsonBuilder();
            JsonObject currentStatusObj = builder.create().fromJson(jsonObject.get("currStatus").getAsJsonObject(),
                    JsonObject.class);

            TouchWandAlarmSensorCurrentStatus touchWandUnitDataAlarmSensorCurrentStatus = touchWandUnitDataAlarmSensor
                    .getCurrStatus();

            for (Entry<String, JsonElement> entry : currentStatusObj.entrySet()) {
                String key = entry.getKey();
                String splits[] = key.split("_"); // the key is xxxx_n where xxx is sensor type and n is
                String keyName = splits[0];
                int index = 0;

                if (splits.length > 1 && !splits[1].isEmpty()) {
                    try {
                        index = Integer.parseInt(splits[1]);
                    } catch (final NumberFormatException e) {
                        index = 0;
                    }
                }

                final Gson gson = new Gson();
                switch (keyName) {
                    case "batt":
                        touchWandUnitDataAlarmSensorCurrentStatus.setBatt(entry.getValue().getAsInt());
                        break;
                    case "alarm":
                        Alarm alarm = gson.fromJson(entry.getValue().getAsJsonObject(), Alarm.class);
                        TouchWandAlarmSensorCurrentStatus.AlarmEvent alarmEvent = touchWandUnitDataAlarmSensorCurrentStatus.new AlarmEvent();
                        alarmEvent.alarm = alarm;
                        alarmEvent.alarmType = index;
                        touchWandUnitDataAlarmSensor.getCurrStatus().getAlarmsStatus().add(alarmEvent);
                        break;
                    case "sensor":
                        TouchWandAlarmSensorCurrentStatus.Sensor sensor = touchWandUnitDataAlarmSensorCurrentStatus.new Sensor();
                        sensor.value = entry.getValue().getAsFloat();
                        sensor.type = index;
                        touchWandUnitDataAlarmSensor.getCurrStatus().getSensorsStatus().add(sensor);
                        break;
                    case "bsensor":
                        bSensor bsensor = gson.fromJson(entry.getValue().getAsJsonObject(), bSensor.class);
                        TouchWandAlarmSensorCurrentStatus.bSensorEvent bsensorevent = touchWandUnitDataAlarmSensorCurrentStatus.new bSensorEvent();
                        bsensorevent.sensor = bsensor;
                        bsensorevent.sensorType = index;
                        touchWandUnitDataAlarmSensor.getCurrStatus().getbSensorsStatus().add(bsensorevent);
                        break;
                    default:
                        break;
                }
            }
        }
        return touchWandUnitDataAlarmSensor;
    }
}
