/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.thing.Thing;
import org.openhab.core.util.UIDUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * Base class for home assistant configurations.
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractChannelConfiguration {
    public static final char PARENT_TOPIC_PLACEHOLDER = '~';
    private static final String DEFAULT_THING_NAME = "Home Assistant Device";

    protected @Nullable String name;

    protected String icon = "";
    protected int qos; // defaults to 0 according to HA specification
    protected boolean retain; // defaults to false according to HA specification
    @SerializedName("value_template")
    protected @Nullable String valueTemplate;
    @SerializedName("unique_id")
    protected @Nullable String uniqueId;

    @SerializedName("availability_mode")
    protected AvailabilityMode availabilityMode = AvailabilityMode.LATEST;
    @SerializedName("availability_topic")
    protected @Nullable String availabilityTopic;
    @SerializedName("payload_available")
    protected String payloadAvailable = "online";
    @SerializedName("payload_not_available")
    protected String payloadNotAvailable = "offline";
    @SerializedName("availability_template")
    protected @Nullable String availabilityTemplate;

    @SerializedName("enabled_by_default")
    protected boolean enabledByDefault = true;

    /**
     * A list of MQTT topics subscribed to receive availability (online/offline) updates. Must not be used together with
     * availability_topic
     */
    protected @Nullable List<Availability> availability;

    @SerializedName("json_attributes_template")
    protected @Nullable String jsonAttributesTemplate;
    @SerializedName("json_attributes_topic")
    protected @Nullable String jsonAttributesTopic;

    @SerializedName(value = "~")
    protected String parentTopic = "";

    protected @Nullable Device device;

    /**
     * Parse the base properties of the configJSON into an {@link AbstractChannelConfiguration}
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

    public String getThingName() {
        String result = null;

        if (this.device != null) {
            result = this.device.name;
        }
        if (result == null) {
            result = name;
        }
        if (result == null) {
            result = DEFAULT_THING_NAME;
        }
        return result;
    }

    public String getThingId(String defaultId) {
        String result = null;
        if (this.device != null) {
            result = this.device.getId();
        }
        if (result == null) {
            result = uniqueId;
        }
        return UIDUtils.encode(result != null ? result : defaultId);
    }

    public Map<String, Object> appendToProperties(Map<String, Object> properties) {
        final Device d = device;
        if (d == null) {
            return properties;
        }
        final String manufacturer = d.manufacturer;
        if (manufacturer != null) {
            properties.put(Thing.PROPERTY_VENDOR, manufacturer);
        }
        final String model = d.model;
        if (model != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, model);
        }
        final String swVersion = d.swVersion;
        if (swVersion != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, swVersion);
        }
        return properties;
    }

    public @Nullable String getName() {
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
        return valueTemplate;
    }

    @Nullable
    public String getUniqueId() {
        return uniqueId;
    }

    @Nullable
    public String getAvailabilityTopic() {
        return availabilityTopic;
    }

    public String getPayloadAvailable() {
        return payloadAvailable;
    }

    public String getPayloadNotAvailable() {
        return payloadNotAvailable;
    }

    @Nullable
    public String getAvailabilityTemplate() {
        return availabilityTemplate;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    @Nullable
    public Device getDevice() {
        return device;
    }

    @Nullable
    public List<Availability> getAvailability() {
        return availability;
    }

    public String getParentTopic() {
        return parentTopic;
    }

    public AvailabilityMode getAvailabilityMode() {
        return availabilityMode;
    }

    @Nullable
    public String getJsonAttributesTemplate() {
        return jsonAttributesTemplate;
    }

    @Nullable
    public String getJsonAttributesTopic() {
        return jsonAttributesTopic;
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
        try {
            @Nullable
            final C config = gson.fromJson(configJSON, clazz);
            if (config == null) {
                throw new ConfigurationException("Channel configuration is empty");
            }
            return config;
        } catch (JsonSyntaxException e) {
            throw new ConfigurationException("Cannot parse channel configuration JSON: " + e.getMessage(), e);
        }
    }
}
