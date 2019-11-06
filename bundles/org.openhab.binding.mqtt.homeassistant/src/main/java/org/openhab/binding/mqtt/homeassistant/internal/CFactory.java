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
package org.openhab.binding.mqtt.homeassistant.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
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
    public static @Nullable AbstractComponent<?> createComponent(ThingUID thingUID, HaID haID,
            String channelConfigurationJSON, ChannelStateUpdateListener updateListener, Gson gson,
            TransformationServiceProvider transformationServiceProvider) {
        ComponentConfiguration componentConfiguration = new ComponentConfiguration(thingUID, haID,
                channelConfigurationJSON, gson, updateListener).transformationProvider(transformationServiceProvider);
        try {
            switch (haID.component) {
                case "alarm_control_panel":
                    return new ComponentAlarmControlPanel(componentConfiguration);
                case "binary_sensor":
                    return new ComponentBinarySensor(componentConfiguration);
                case "camera":
                    return new ComponentCamera(componentConfiguration);
                case "cover":
                    return new ComponentCover(componentConfiguration);
                case "fan":
                    return new ComponentFan(componentConfiguration);
                case "climate":
                    return new ComponentClimate(componentConfiguration);
                case "light":
                    return new ComponentLight(componentConfiguration);
                case "lock":
                    return new ComponentLock(componentConfiguration);
                case "sensor":
                    return new ComponentSensor(componentConfiguration);
                case "switch":
                    return new ComponentSwitch(componentConfiguration);
            }
        } catch (UnsupportedOperationException e) {
            logger.warn("Not supported", e);
        }
        return null;
    }

    protected static class ComponentConfiguration {
        private ThingUID thingUID;
        private HaID haID;
        private String configJSON;
        private @Nullable TransformationServiceProvider transformationServiceProvider;
        private ChannelStateUpdateListener updateListener;
        private Gson gson;

        protected ComponentConfiguration(ThingUID thingUID, HaID haID, String configJSON, Gson gson,
                ChannelStateUpdateListener updateListener) {
            this.thingUID = thingUID;
            this.haID = haID;
            this.configJSON = configJSON;
            this.gson = gson;
            this.updateListener = updateListener;
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

        public <C extends BaseChannelConfiguration> C getConfig(Class<C> clazz) {
            return BaseChannelConfiguration.fromString(configJSON, gson, clazz);
        }
    }
}
