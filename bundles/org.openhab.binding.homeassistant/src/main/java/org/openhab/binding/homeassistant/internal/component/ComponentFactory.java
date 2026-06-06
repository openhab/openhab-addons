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
package org.openhab.binding.homeassistant.internal.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeassistant.internal.HaID;
import org.openhab.binding.homeassistant.internal.HomeAssistantBindingConstants;
import org.openhab.binding.homeassistant.internal.HomeAssistantChannelLinkageChecker;
import org.openhab.binding.homeassistant.internal.HomeAssistantPythonBridge;
import org.openhab.binding.homeassistant.internal.config.dto.MqttComponentConfig;
import org.openhab.binding.homeassistant.internal.exception.ConfigurationException;
import org.openhab.binding.homeassistant.internal.exception.UnsupportedComponentException;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.thing.ThingUID;
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
public class ComponentFactory {
    private static final Logger logger = LoggerFactory.getLogger(ComponentFactory.class);

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
    public static List<AbstractComponent<?>> createComponent(ThingUID thingUID, HaID haID,
            String channelConfigurationJSON, ChannelStateUpdateListener updateListener,
            HomeAssistantChannelLinkageChecker linkageChecker, AvailabilityTracker tracker,
            ScheduledExecutorService scheduler, Gson gson, HomeAssistantPythonBridge python, UnitProvider unitProvider)
            throws ConfigurationException {
        List<MqttComponentConfig> mqttComponentConfigs = python.processDiscoveryConfig(haID.toShortTopic(),
                channelConfigurationJSON);
        return createComponent(thingUID, haID, channelConfigurationJSON, mqttComponentConfigs, updateListener,
                linkageChecker, tracker, scheduler, gson, python, unitProvider);
    }

    public static List<AbstractComponent<?>> createComponent(ThingUID thingUID, HaID haID,
            String channelConfigurationJSON, List<MqttComponentConfig> mqttComponentConfigs,
            ChannelStateUpdateListener updateListener, HomeAssistantChannelLinkageChecker linkageChecker,
            AvailabilityTracker tracker, ScheduledExecutorService scheduler, Gson gson,
            HomeAssistantPythonBridge python, UnitProvider unitProvider) throws ConfigurationException {
        if (HomeAssistantBindingConstants.DEVICE_COMPONENT.equals(haID.component)) {
            List<AbstractComponent<?>> components = new ArrayList<>();
            for (MqttComponentConfig config : mqttComponentConfigs) {
                String nodeId = config.getNodeId();
                if (nodeId == null) {
                    // Use the device component's object id as the node id for the sub components if
                    // not explicitly specified
                    nodeId = haID.objectID;
                }
                HaID componentHaID = new HaID(haID.baseTopic, config.getObjectId(), nodeId, config.getComponent());
                ComponentContext componentContext = new ComponentContext(thingUID, componentHaID,
                        channelConfigurationJSON, gson, python, updateListener, linkageChecker, tracker, scheduler,
                        unitProvider, config.getDiscoveryPayload(), false);
                try {
                    components.add(createComponent(componentContext));
                } catch (UnsupportedComponentException e) {
                    logger.warn("Home Assistant discovery error: component {} is unsupported", haID.toShortTopic());
                }
            }

            return Collections.unmodifiableList(components);
        }

        MqttComponentConfig mqttComponentConfig = mqttComponentConfigs.getFirst();
        ComponentContext componentContext = new ComponentContext(thingUID, haID, channelConfigurationJSON, gson, python,
                updateListener, linkageChecker, tracker, scheduler, unitProvider,
                mqttComponentConfig.getDiscoveryPayload(), true);
        return List.of(createComponent(componentContext));
    }

    private static AbstractComponent<?> createComponent(ComponentContext componentContext)
            throws ConfigurationException {
        switch (componentContext.getHaID().component) {
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
                throw new UnsupportedComponentException(
                        "Component '" + componentContext.getHaID().toShortTopic() + "' is unsupported!");
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
        private final Map<String, @Nullable Object> discoveryPayload;
        private final boolean persistChannelConfiguration;

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
                ScheduledExecutorService scheduler, UnitProvider unitProvider,
                Map<String, @Nullable Object> discoveryPayload, boolean persistChannelConfiguration) {
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
            this.discoveryPayload = discoveryPayload;
            this.persistChannelConfiguration = persistChannelConfiguration;
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

        public Map<String, @Nullable Object> getDiscoveryPayload() {
            return discoveryPayload;
        }

        public boolean shouldPersistChannelConfiguration() {
            return persistChannelConfiguration;
        }
    }
}
