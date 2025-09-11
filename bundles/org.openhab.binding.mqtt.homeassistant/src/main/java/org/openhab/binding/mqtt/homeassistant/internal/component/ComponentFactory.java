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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantChannelLinkageChecker;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantPythonBridge;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.binding.mqtt.homeassistant.internal.exception.UnsupportedComponentException;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;

/**
 * A factory to create HomeAssistant MQTT components. Those components are specified at:
 * https://www.home-assistant.io/docs/mqtt/discovery/
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentFactory {
    /**
     * Create a HA MQTT component. The configuration JSon string is required.
     *
     * @param thingUID The Thing UID that this component will belong to.
     * @param haID The location of this component. The HomeAssistant ID contains the object-id, node-id and
     *            component-id.
     * @param channelConfigurationJSON Most components expect a "name", a "state_topic" and "command_topic" like with
     *            "{name:'Name',state_topic:'homeassistant/switch/0/object/state',command_topic:'homeassistant/switch/0/object/set'".
     * @param updateListener A channel state update listener
     * @return A HA MQTT Component
     */
    public static AbstractComponent<?> createComponent(ThingUID thingUID, HaID haID, String channelConfigurationJSON,
            ChannelStateUpdateListener updateListener, HomeAssistantChannelLinkageChecker linkageChecker,
            AvailabilityTracker tracker, ScheduledExecutorService scheduler, Gson gson,
            HomeAssistantPythonBridge python, UnitProvider unitProvider) throws ConfigurationException {
        ComponentContext componentContext = new ComponentContext(thingUID, haID, channelConfigurationJSON, gson, python,
                updateListener, linkageChecker, tracker, scheduler, unitProvider);
        switch (haID.component) {
            case "alarm_control_panel":
                return new AlarmControlPanel(componentContext);
            case "binary_sensor":
                return new BinarySensor(componentContext);
            case "button":
                return new Button(componentContext);
            case "camera":
                return new Camera(componentContext);
            case "climate":
                return new Climate(componentContext);
            case "cover":
                return new Cover(componentContext);
            case "device_automation":
                return new DeviceTrigger(componentContext);
            case "device_tracker":
                return new DeviceTracker(componentContext);
            case "event":
                return new Event(componentContext);
            case "fan":
                return new Fan(componentContext);
            case "humidifier":
                return new Humidifier(componentContext);
            case "light":
                return Light.create(componentContext);
            case "lock":
                return new Lock(componentContext);
            case "number":
                return new Number(componentContext);
            case "scene":
                return new Scene(componentContext);
            case "select":
                return new Select(componentContext);
            case "sensor":
                return new Sensor(componentContext);
            case "switch":
                return new Switch(componentContext);
            case "tag":
                return new Tag(componentContext);
            case "text":
                return new Text(componentContext);
            case "update":
                return new Update(componentContext);
            case "vacuum":
                return new Vacuum(componentContext);
            case "valve":
                return new Valve(componentContext);
            case "water_heater":
                return new WaterHeater(componentContext);
            default:
                throw new UnsupportedComponentException("Component '" + haID + "' is unsupported!");
        }
    }

    protected static class ComponentContext {
        private final ThingUID thingUID;
        private final HaID haID;
        private final String configJSON;
        private final ChannelStateUpdateListener updateListener;
        private final HomeAssistantChannelLinkageChecker linkageChecker;
        private final AvailabilityTracker tracker;
        private final Gson gson;
        private final HomeAssistantPythonBridge python;
        private final ScheduledExecutorService scheduler;
        private final UnitProvider unitProvider;

        /**
         * Provide a thingUID and HomeAssistant topic ID to determine the channel group UID and type.
         *
         * @param thingUID A ThingUID
         * @param haID A HomeAssistant topic ID
         * @param configJSON The configuration string
         */
        protected ComponentContext(ThingUID thingUID, HaID haID, String configJSON, Gson gson,
                HomeAssistantPythonBridge python, ChannelStateUpdateListener updateListener,
                HomeAssistantChannelLinkageChecker linkageChecker, AvailabilityTracker tracker,
                ScheduledExecutorService scheduler, UnitProvider unitProvider) {
            this.thingUID = thingUID;
            this.haID = haID;
            this.configJSON = configJSON;
            this.gson = gson;
            this.python = python;
            this.updateListener = updateListener;
            this.linkageChecker = linkageChecker;
            this.tracker = tracker;
            this.scheduler = scheduler;
            this.unitProvider = unitProvider;
        }

        public ThingUID getThingUID() {
            return thingUID;
        }

        public HaID getHaID() {
            return haID;
        }

        public String getConfigJSON() {
            return configJSON;
        }

        public ChannelStateUpdateListener getUpdateListener() {
            return updateListener;
        }

        public HomeAssistantChannelLinkageChecker getLinkageChecker() {
            return linkageChecker;
        }

        public Gson getGson() {
            return gson;
        }

        public HomeAssistantPythonBridge getPython() {
            return python;
        }

        public UnitProvider getUnitProvider() {
            return unitProvider;
        }

        public AvailabilityTracker getTracker() {
            return tracker;
        }

        public ScheduledExecutorService getScheduler() {
            return scheduler;
        }
    }
}
