/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * A factory to create HomeAssistant MQTT components. Those components are specified at:
 * https://www.home-assistant.io/docs/mqtt/discovery/
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class CFactory {
    private static final Logger logger = LoggerFactory.getLogger(CFactory.class);

    /**
     * Create a HA MQTT component. The configuration JSon string is required.
     *
     * @param thingUID The Thing UID that this component will belong to.
     * @param haID The location of this component. The HomeAssistant ID contains the object-id, node-id and
     *            component-id.
     * @param configJSON Most components expect a "name", a "state_topic" and "command_topic" like with
     *            "{name:'Name',state_topic:'homeassistant/switch/0/object/state',command_topic:'homeassistant/switch/0/object/set'".
     * @param updateListener A channel state update listener
     * @return A HA MQTT Component
     */
    public static @Nullable AbstractComponent createComponent(ThingUID thingUID, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener updateListener, Gson gson) {
        try {
            switch (haID.component) {
                case "alarm_control_panel":
                    return new ComponentAlarmControlPanel(thingUID, haID, configJSON, updateListener, gson);
                case "binary_sensor":
                    return new ComponentBinarySensor(thingUID, haID, configJSON, updateListener, gson);
                case "camera":
                    return new ComponentCamera(thingUID, haID, configJSON, updateListener, gson);
                case "cover":
                    return new ComponentCover(thingUID, haID, configJSON, updateListener, gson);
                case "fan":
                    return new ComponentFan(thingUID, haID, configJSON, updateListener, gson);
                case "climate":
                    return new ComponentClimate(thingUID, haID, configJSON, updateListener, gson);
                case "light":
                    return new ComponentLight(thingUID, haID, configJSON, updateListener, gson);
                case "lock":
                    return new ComponentLock(thingUID, haID, configJSON, updateListener, gson);
                case "sensor":
                    return new ComponentSensor(thingUID, haID, configJSON, updateListener, gson);
                case "switch":
                    return new ComponentSwitch(thingUID, haID, configJSON, updateListener, gson);
            }
        } catch (UnsupportedOperationException e) {
            logger.warn("Not supported", e);
        }
        return null;
    }

    /**
     * Create a HA MQTT component by a given channel configuration.
     *
     * @param basetopic The MQTT base topic, usually "homeassistant"
     * @param channel A channel with the JSON configuration embedded as configuration (key: 'config')
     * @param updateListener A channel state update listener
     * @return A HA MQTT Component
     */
    public static @Nullable AbstractComponent createComponent(String basetopic, Channel channel,
            @Nullable ChannelStateUpdateListener updateListener, Gson gson) {
        HaID haID = new HaID(basetopic, channel.getUID());
        ThingUID thingUID = channel.getUID().getThingUID();
        String configJSON = (String) channel.getConfiguration().get("config");
        if (configJSON == null) {
            logger.warn("Provided channel does not have a 'config' configuration key!");
            return null;
        }
        try {
            switch (haID.component) {
                case "alarm_control_panel":
                    return new ComponentAlarmControlPanel(thingUID, haID, configJSON, updateListener, gson);
                case "binary_sensor":
                    return new ComponentBinarySensor(thingUID, haID, configJSON, updateListener, gson);
                case "camera":
                    return new ComponentCamera(thingUID, haID, configJSON, updateListener, gson);
                case "cover":
                    return new ComponentCover(thingUID, haID, configJSON, updateListener, gson);
                case "fan":
                    return new ComponentFan(thingUID, haID, configJSON, updateListener, gson);
                case "climate":
                    return new ComponentClimate(thingUID, haID, configJSON, updateListener, gson);
                case "light":
                    return new ComponentLight(thingUID, haID, configJSON, updateListener, gson);
                case "lock":
                    return new ComponentLock(thingUID, haID, configJSON, updateListener, gson);
                case "sensor":
                    return new ComponentSensor(thingUID, haID, configJSON, updateListener, gson);
                case "switch":
                    return new ComponentSwitch(thingUID, haID, configJSON, updateListener, gson);
            }
        } catch (UnsupportedOperationException e) {
            logger.warn("Not supported", e);
        }
        return null;
    }
}
