/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.config.dto;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.util.UIDUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Base class for home assistant configurations.
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractChannelConfiguration {
    protected String name;

    protected String icon = "";
    protected int qos; // defaults to 0 according to HA specification
    protected boolean retain; // defaults to false according to HA specification
    protected @Nullable String value_template;
    protected @Nullable String unique_id;

    protected AvailabilityMode availability_mode = AvailabilityMode.LATEST;
    protected @Nullable String availability_topic;
    protected String payload_available = "online";
    protected String payload_not_available = "offline";

    /**
     * A list of MQTT topics subscribed to receive availability (online/offline) updates. Must not be used together with
     * availability_topic
     */
    protected @Nullable List<Availability> availability;

    @SerializedName(value = "~")
    protected String tilde = "";

    protected @Nullable Device device;

    /**
     * Parse the base properties of the configJSON into a {@link AbstractChannelConfiguration}
     *
     * @param configJSON channels configuration in JSON
     * @param gson parser
     * @return configuration object
     */
    public static AbstractChannelConfiguration fromString(final String configJSON, final Gson gson) {
        return fromString(configJSON, gson, Config.class);
    }

    protected AbstractChannelConfiguration(String defaultName) {
        this.name = defaultName;
    }

    public @Nullable String expand(@Nullable String value) {
        return value == null ? null : value.replaceAll("~", tilde);
    }

    public String getThingName() {
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
        final String sw_version = device_.swVersion;
        if (sw_version != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, sw_version);
        }
        return properties;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public int getQos() {
        return qos;
    }

    public boolean isRetain() {
        return retain;
    }

    @Nullable
    public String getValueTemplate() {
        return value_template;
    }

    @Nullable
    public String getUniqueId() {
        return unique_id;
    }

    @Nullable
    public String getAvailabilityTopic() {
        return availability_topic;
    }

    public String getPayloadAvailable() {
        return payload_available;
    }

    public String getPayloadNotAvailable() {
        return payload_not_available;
    }

    @Nullable
    public Device getDevice() {
        return device;
    }

    @Nullable
    public List<Availability> getAvailability() {
        return availability;
    }

    public String getTilde() {
        return tilde;
    }

    public AvailabilityMode getAvailabilityMode() {
        return availability_mode;
    }

    /**
     * This class is needed, to be able to parse only the common base attributes.
     * Without this, {@link AbstractChannelConfiguration} cannot be instantiated, as it is abstract.
     * This is needed during the discovery.
     */
    private static class Config extends AbstractChannelConfiguration {
        public Config() {
            super("private");
        }
    }

    /**
     * Parse the configJSON into a subclass of {@link AbstractChannelConfiguration}
     *
     * @param configJSON channels configuration in JSON
     * @param gson parser
     * @param clazz target configuration class
     * @return configuration object
     */
    public static <C extends AbstractChannelConfiguration> C fromString(final String configJSON, final Gson gson,
            final Class<C> clazz) {
        return Objects.requireNonNull(gson.fromJson(configJSON, clazz));
    }
}
