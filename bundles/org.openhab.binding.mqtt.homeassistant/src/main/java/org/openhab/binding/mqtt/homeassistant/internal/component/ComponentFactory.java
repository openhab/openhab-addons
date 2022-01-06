/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.binding.mqtt.homeassistant.internal.exception.UnsupportedComponentException;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentFactory.class);

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
            ChannelStateUpdateListener updateListener, AvailabilityTracker tracker, ScheduledExecutorService scheduler,
            Gson gson, TransformationServiceProvider transformationServiceProvider) throws ConfigurationException {
        ComponentConfiguration componentConfiguration = new ComponentConfiguration(thingUID, haID,
                channelConfigurationJSON, gson, updateListener, tracker, scheduler)
                        .transformationProvider(transformationServiceProvider);
        switch (haID.component) {
            case "alarm_control_panel":
                return new AlarmControlPanel(componentConfiguration);
            case "binary_sensor":
                return new BinarySensor(componentConfiguration);
            case "camera":
                return new Camera(componentConfiguration);
            case "cover":
                return new Cover(componentConfiguration);
            case "fan":
                return new Fan(componentConfiguration);
            case "climate":
                return new Climate(componentConfiguration);
            case "light":
                return new Light(componentConfiguration);
            case "lock":
                return new Lock(componentConfiguration);
            case "sensor":
                return new Sensor(componentConfiguration);
            case "switch":
                return new Switch(componentConfiguration);
            case "vacuum":
                return new Vacuum(componentConfiguration);
            default:
                throw new UnsupportedComponentException("Component '" + haID + "' is unsupported!");
        }
    }

    protected static class ComponentConfiguration {
        private final ThingUID thingUID;
        private final HaID haID;
        private final String configJSON;
        private final ChannelStateUpdateListener updateListener;
        private final AvailabilityTracker tracker;
        private final Gson gson;
        private final ScheduledExecutorService scheduler;
        private @Nullable TransformationServiceProvider transformationServiceProvider;

        /**
         * Provide a thingUID and HomeAssistant topic ID to determine the channel group UID and type.
         *
         * @param thingUID A ThingUID
         * @param haID A HomeAssistant topic ID
         * @param configJSON The configuration string
         * @param gson A Gson instance
         */
        protected ComponentConfiguration(ThingUID thingUID, HaID haID, String configJSON, Gson gson,
                ChannelStateUpdateListener updateListener, AvailabilityTracker tracker,
                ScheduledExecutorService scheduler) {
            this.thingUID = thingUID;
            this.haID = haID;
            this.configJSON = configJSON;
            this.gson = gson;
            this.updateListener = updateListener;
            this.tracker = tracker;
            this.scheduler = scheduler;
        }

        public ComponentConfiguration transformationProvider(
                TransformationServiceProvider transformationServiceProvider) {
            this.transformationServiceProvider = transformationServiceProvider;
            return this;
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

        @Nullable
        public TransformationServiceProvider getTransformationServiceProvider() {
            return transformationServiceProvider;
        }

        public Gson getGson() {
            return gson;
        }

        public AvailabilityTracker getTracker() {
            return tracker;
        }

        public ScheduledExecutorService getScheduler() {
            return scheduler;
        }

        public <C extends AbstractChannelConfiguration> C getConfig(Class<C> clazz) {
            return AbstractChannelConfiguration.fromString(configJSON, gson, clazz);
        }
    }
}
