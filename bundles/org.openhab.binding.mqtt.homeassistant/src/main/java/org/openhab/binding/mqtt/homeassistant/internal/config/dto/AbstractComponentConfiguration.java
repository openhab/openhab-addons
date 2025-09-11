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
package org.openhab.binding.mqtt.homeassistant.internal.config.dto;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantPythonBridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.util.UIDUtils;

/**
 * Base class for home assistant configurations.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class AbstractComponentConfiguration extends AbstractConfiguration {
    protected final int qos;
    private final @Nullable Device device;
    private final String name;
    private final boolean enabledByDefault;
    private final @Nullable List<Availability> availability;

    /**
     * Parse the base properties of the configJSON into an {@link AbstractComponentConfiguration}
     *
     * @param configJSON channels configuration in JSON
     * @param gson parser
     * @return configuration object
     */
    public static <C extends AbstractComponentConfiguration> C create(HomeAssistantPythonBridge python,
            String component, String configJSON, Class<C> clazz) {
        try {
            return clazz.getDeclaredConstructor(Map.class)
                    .newInstance(python.processDiscoveryConfig(component, configJSON));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new RuntimeException("Failed to create component configuration: " + cause.getClass().getName() + ": "
                    + cause.getMessage(), cause);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            // None of these exceptions should be possible
            throw new RuntimeException(
                    "Failed to create component configuration: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    public static AbstractComponentConfiguration create(HomeAssistantPythonBridge python, String component,
            String configJSON) {
        return create(python, component, configJSON, AbstractComponentConfiguration.class);
    }

    protected AbstractComponentConfiguration(Map<String, @Nullable Object> config) {
        this(config, "MQTT Component");
    }

    @SuppressWarnings("unchecked")
    protected AbstractComponentConfiguration(Map<String, @Nullable Object> config, String defaultName) {
        super(config);
        this.qos = getInt("qos");
        Map<String, @Nullable Object> deviceConfig = (Map<String, @Nullable Object>) config.get("device");
        if (deviceConfig != null) {
            this.device = new Device(deviceConfig);
        } else {
            this.device = null;
        }
        String name = getOptionalString("name");
        if (name == null || name.isBlank()) {
            this.name = defaultName;
        } else {
            this.name = name;
        }
        Boolean enabledByDefault = getOptionalBoolean("enabled_by_default");
        if (enabledByDefault == null) {
            // Non-entity components don't have this field
            this.enabledByDefault = true;
        } else {
            this.enabledByDefault = enabledByDefault;
        }
        List<Map<String, @Nullable Object>> availability = (List<Map<String, @Nullable Object>>) config
                .get("availability");
        if (availability != null) {
            this.availability = availability.stream().map(c -> new Availability(c)).toList();
        } else {
            this.availability = null;
        }
    }

    public String getThingName() {
        String result = null;

        Device device = this.device;
        if (device != null) {
            result = device.getName();
        }
        if (result == null) {
            result = getName();
        }
        return result;
    }

    public String getThingId(String defaultId) {
        String result = null;
        Device device = this.device;
        if (device != null) {
            result = device.getId();
        }
        if (result == null) {
            result = getUniqueId();
        }
        return UIDUtils.encode(result != null ? result : defaultId);
    }

    public Map<String, Object> appendToProperties(Map<String, Object> properties) {
        final Device d = device;
        if (d == null) {
            return properties;
        }
        final String manufacturer = d.getManufacturer();
        if (manufacturer != null) {
            properties.put(Thing.PROPERTY_VENDOR, manufacturer);
        }
        String model = d.getModelId();
        if (model == null) {
            model = d.getModel();
        }
        if (model != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, model);
        }
        final String swVersion = d.getSwVersion();
        if (swVersion != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, swVersion);
        }
        return properties;
    }

    public String getName() {
        return name;
    }

    public int getQos() {
        return getInt("qos");
    }

    public @Nullable String getUniqueId() {
        return getOptionalString("unique_id");
    }

    public @Nullable String getAvailabilityTopic() {
        return getOptionalString("availability_topic");
    }

    public String getPayloadAvailable() {
        String payloadAvailable = getOptionalString("payload_available");
        if (payloadAvailable == null) {
            // Non-entity components don't have this field
            return "online";
        }
        return payloadAvailable;
    }

    public String getPayloadNotAvailable() {
        String payloadNotAvailable = getOptionalString("payload_not_available");
        if (payloadNotAvailable == null) {
            // Non-entity components don't have this field
            return "offline";
        }
        return payloadNotAvailable;
    }

    public @Nullable Value getAvailabilityTemplate() {
        return getOptionalValue("availability_template");
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public @Nullable Device getDevice() {
        return device;
    }

    public @Nullable List<Availability> getAvailability() {
        return availability;
    }

    public String getAvailabilityMode() {
        String availabilityMode = getOptionalString("availability_mode");
        if (availabilityMode == null) {
            // Non-entity components don't have this field
            return AvailabilityMode.LATEST;
        }

        return availabilityMode;
    }
}
