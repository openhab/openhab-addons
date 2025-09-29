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
package org.openhab.binding.unifiprotect.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.dto.Sensor;
import org.openhab.binding.unifiprotect.internal.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorAlarmEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorClosedEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorExtremeValueEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorMotionEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorOpenEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorTamperEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorWaterLeakEvent;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Child handler for a UniFi Protect Sensor.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectSensorHandler extends UnifiProtectAbstractDeviceHandler<Sensor> {

    public UnifiProtectSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (command instanceof RefreshType) {
            refreshState(id);
            return;
        }
    }

    @Override
    public void handleEvent(BaseEvent event, WSEventType eventType) {
        if (event.type == null) {
            return;
        }

        switch (event.type) {
            case SENSOR_OPENED:
                if (event instanceof SensorOpenEvent e) {
                    String payload = e.metadata != null && e.metadata.sensorMountType != null
                            && e.metadata.sensorMountType.text != null ? e.metadata.sensorMountType.text.getApiValue()
                                    : "none";
                    if (hasChannel(UnifiProtectBindingConstants.CHANNEL_OPENED)) {
                        triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_OPENED),
                                payload);
                    }
                    updateState(UnifiProtectBindingConstants.CHANNEL_CONTACT, OpenClosedType.OPEN);
                }
                break;
            case SENSOR_CLOSED:
                if (event instanceof SensorClosedEvent e) {
                    String payload = e.metadata != null && e.metadata.sensorMountType != null
                            && e.metadata.sensorMountType.text != null ? e.metadata.sensorMountType.text.getApiValue()
                                    : "none";
                    if (hasChannel(UnifiProtectBindingConstants.CHANNEL_CLOSED)) {
                        triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_CLOSED),
                                payload);
                    }
                    updateState(UnifiProtectBindingConstants.CHANNEL_CONTACT, OpenClosedType.CLOSED);
                }
                break;
            case SENSOR_MOTION:
                if (event instanceof SensorMotionEvent) {
                    if (hasChannel(UnifiProtectBindingConstants.CHANNEL_SENSOR_MOTION)) {
                        triggerChannel(
                                new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_SENSOR_MOTION));
                    }
                }
                break;
            case SENSOR_ALARM:
                if (event instanceof SensorAlarmEvent e) {
                    String payload = e.metadata != null && e.metadata.alarmType != null
                            && e.metadata.alarmType.text != null ? e.metadata.alarmType.text.getApiValue() : "";
                    if (hasChannel(UnifiProtectBindingConstants.CHANNEL_ALARM)) {
                        if (payload.isEmpty()) {
                            triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_ALARM));
                        } else {
                            triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_ALARM),
                                    payload);
                        }
                    }
                    updateState(UnifiProtectBindingConstants.CHANNEL_ALARM_CONTACT,
                            eventType == WSEventType.ADD ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
                break;
            case SENSOR_WATER_LEAK:
                if (event instanceof SensorWaterLeakEvent e) {
                    String payload = e.metadata != null && e.metadata.sensorMountType != null
                            && e.metadata.sensorMountType.text != null ? e.metadata.sensorMountType.text.getApiValue()
                                    : "none";
                    if (hasChannel(UnifiProtectBindingConstants.CHANNEL_WATER_LEAK)) {
                        triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_WATER_LEAK),
                                payload);
                    }
                    updateState(UnifiProtectBindingConstants.CHANNEL_WATER_LEAK_CONTACT,
                            eventType == WSEventType.ADD ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
                break;
            case SENSOR_TAMPER:
                if (event instanceof SensorTamperEvent) {
                    if (hasChannel(UnifiProtectBindingConstants.CHANNEL_TAMPER)) {
                        triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_TAMPER));
                    }
                    updateState(UnifiProtectBindingConstants.CHANNEL_TAMPER_CONTACT,
                            eventType == WSEventType.ADD ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
                break;
            case SENSOR_EXTREME_VALUES:
                if (event instanceof SensorExtremeValueEvent e && e.metadata != null && e.metadata.sensorType != null
                        && e.metadata.sensorValue != null) {
                    String type = e.metadata.sensorType.text;
                    Double value = e.metadata.sensorValue.text;
                    if (type != null && value != null) {
                        switch (type) {
                            case "temperature":
                                updateDecimalChannel(UnifiProtectBindingConstants.CHANNEL_TEMPERATURE, value);
                                break;
                            case "humidity":
                                updateDecimalChannel(UnifiProtectBindingConstants.CHANNEL_HUMIDITY, value);
                                break;
                            case "light":
                                updateDecimalChannel(UnifiProtectBindingConstants.CHANNEL_ILLUMINANCE, value);
                                break;
                            default:
                                break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void updateFromDevice(Sensor sensor) {
        super.updateFromDevice(sensor);
        if (sensor.batteryStatus != null && sensor.batteryStatus.percentage != null) {
            updateDecimalChannel(UnifiProtectBindingConstants.CHANNEL_BATTERY, sensor.batteryStatus.percentage);
        }
        updateContactChannel(UnifiProtectBindingConstants.CHANNEL_CONTACT, sensor.isOpened);
        if (sensor.stats != null) {
            if (sensor.stats.temperature != null) {
                updateDecimalChannel(UnifiProtectBindingConstants.CHANNEL_TEMPERATURE, sensor.stats.temperature.value);
            }
            if (sensor.stats.humidity != null) {
                updateDecimalChannel(UnifiProtectBindingConstants.CHANNEL_HUMIDITY, sensor.stats.humidity.value);
            }
            if (sensor.stats.light != null) {
                updateDecimalChannel(UnifiProtectBindingConstants.CHANNEL_ILLUMINANCE, sensor.stats.light.value);
            }
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
