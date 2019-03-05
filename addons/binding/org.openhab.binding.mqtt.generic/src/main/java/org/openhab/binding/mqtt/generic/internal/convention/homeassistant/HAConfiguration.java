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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * Base class for home assistant configurations.
 *
 * @author Jochen Klein - Initial contribution
 */

@NonNullByDefault
public abstract class HAConfiguration {

    public static final Factory FACTORY = new Factory();

    public static class Factory {

        private final Gson gson;

        private Factory() {
            this.gson = new GsonBuilder().registerTypeAdapterFactory(new HAConfigTypeAdapterFactory()).create();
        }

        /**
         * This class is needed, to be able to parse only the common base attributes.
         * Without this, {@link HAConfiguration} cannot be instantiated, as it is abstract.
         * This is needed during the discovery.
         */
        private static class Config extends HAConfiguration {
            public Config() {
                super("private");
            }
        }

        /**
         * Parse the configJSON into a subclass of {@link HAConfiguration}
         *
         * @param configJSON
         * @param gson
         * @param clazz
         * @return configuration object
         */
        public <C extends HAConfiguration> C fromString(final String configJSON, final Class<C> clazz) {
            return gson.fromJson(configJSON, clazz);
        }

        /**
         * Parse the base properties of the configJSON into a {@link HAConfiguration}
         *
         * @param configJSON
         * @param gson
         * @return configuration object
         */
        public HAConfiguration fromString(final String configJSON) {
            return fromString(configJSON, Config.class);
        }

    }

    public String name;

    protected String icon = "";
    protected int qos = 0;
    protected boolean retain = false;
    protected @Nullable String value_template;
    protected @Nullable String unique_id;

    protected @Nullable String availability_topic;
    protected String payload_available = "online";
    protected String payload_not_available = "offline";

    @SerializedName(value = "~")
    protected String tilde = "";

    protected HAConfiguration(String defaultName) {
        this.name = defaultName;
    }

    public @Nullable String expand(@Nullable String value) {
        return value == null ? null : value.replaceAll("~", tilde);
    }

    protected @Nullable Device device;

    static class Device {
        protected @Nullable List<String> identifiers;
        protected @Nullable List<Connection> connections;
        protected @Nullable String manufacturer;
        protected @Nullable String model;
        protected @Nullable String name;
        protected @Nullable String sw_version;

        @Nullable
        public String getId() {
            return StringUtils.join(identifiers, "_");
        }

        public void addDeviceProperties(Map<String, Object> properties) {
            if (manufacturer != null) {
                properties.put(Thing.PROPERTY_VENDOR, manufacturer);
            }
            if (model != null) {
                properties.put(Thing.PROPERTY_MODEL_ID, model);
            }
            if (sw_version != null) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, sw_version);
            }
        }
    }

    static class Connection {
        protected @Nullable String type;
        protected @Nullable String identifier;
    }

    public void addDeviceProperties(Map<String, Object> properties) {
        if (device != null) {
            device.addDeviceProperties(properties);
        }
    }
}
