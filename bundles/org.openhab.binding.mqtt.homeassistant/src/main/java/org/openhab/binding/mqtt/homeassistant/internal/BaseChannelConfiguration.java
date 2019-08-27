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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.util.UIDUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Base class for home assistant configurations.
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public abstract class BaseChannelConfiguration {

    /**
     * This class is needed, to be able to parse only the common base attributes.
     * Without this, {@link BaseChannelConfiguration} cannot be instantiated, as it is abstract.
     * This is needed during the discovery.
     */
    private static class Config extends BaseChannelConfiguration {
        public Config() {
            super("private");
        }
    }

    /**
     * Parse the configJSON into a subclass of {@link BaseChannelConfiguration}
     *
     * @param configJSON
     * @param gson
     * @param clazz
     * @return configuration object
     */
    public static <C extends BaseChannelConfiguration> C fromString(final String configJSON, final Gson gson,
            final Class<C> clazz) {
        return gson.fromJson(configJSON, clazz);
    }

    /**
     * Parse the base properties of the configJSON into a {@link BaseChannelConfiguration}
     *
     * @param configJSON
     * @param gson
     * @return configuration object
     */
    public static BaseChannelConfiguration fromString(final String configJSON, final Gson gson) {
        return fromString(configJSON, gson, Config.class);
    }

    public String name;

    protected String icon = "";
    protected int qos; // defaults to 0 according to HA specification
    protected boolean retain; // defaults to false according to HA specification
    protected @Nullable String value_template;
    protected @Nullable String unique_id;

    protected @Nullable String availability_topic;
    protected String payload_available = "online";
    protected String payload_not_available = "offline";

    @SerializedName(value = "~")
    protected String tilde = "";

    protected BaseChannelConfiguration(String defaultName) {
        this.name = defaultName;
    }

    public @Nullable String expand(@Nullable String value) {
        return value == null ? null : value.replaceAll("~", tilde);
    }

    protected @Nullable Device device;

    static class Device {
        @JsonAdapter(ListOrStringDeserializer.class)
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
    }

    static class Connection {
        protected @Nullable String type;
        protected @Nullable String identifier;
    }

    public String getThingName() {
        @Nullable
        String result = null;

        if (this.device != null) {
            result = this.device.name;
        }
        if (result == null) {
            result = name;
        }
        return result;
    }

    public String getThingId(String defaultId) {
        @Nullable
        String result = null;
        if (this.device != null) {
            result = this.device.getId();
        }
        if (result == null) {
            result = unique_id;
        }
        return UIDUtils.encode(result != null ? result : defaultId);
    }

    public Map<String, Object> appendToProperties(Map<String, Object> properties) {
        final Device device_ = device;
        if (device_ == null) {
            return properties;
        }
        final String manufacturer = device_.manufacturer;
        if (manufacturer != null) {
            properties.put(Thing.PROPERTY_VENDOR, manufacturer);
        }
        final String model = device_.model;
        if (model != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, model);
        }
        final String sw_version = device_.sw_version;
        if (sw_version != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, sw_version);
        }
        return properties;
    }
}
