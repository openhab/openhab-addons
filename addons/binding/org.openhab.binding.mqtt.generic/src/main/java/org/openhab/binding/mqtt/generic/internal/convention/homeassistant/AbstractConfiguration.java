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

import com.google.gson.annotations.SerializedName;

@NonNullByDefault
public class AbstractConfiguration {

    AbstractConfiguration() {
        this("no name");
    }

    protected AbstractConfiguration(String defaultName) {
        this.name = defaultName;
    }

    public String name;

    @SerializedName(value = "icon", alternate = "ic")
    protected String icon = "";
    protected int qos = 0;
    @SerializedName(value = "retain", alternate = "ret")
    protected boolean retain = false;
    @SerializedName(value = "value_template", alternate = "val_tpl")
    protected @Nullable String value_template;
    @SerializedName(value = "unique_id", alternate = "uniq_id")
    protected @Nullable String unique_id;

    @SerializedName(value = "availability_topic", alternate = "avty_t")
    protected @Nullable String availability_topic;
    @SerializedName(value = "payload_available", alternate = "pl_avail")
    protected String payload_available = "online";
    @SerializedName(value = "payload_not_available", alternate = "pl_not_avail")
    protected String payload_not_available = "offline";

    @SerializedName(value = "~")
    protected String tilde = "";

    public @Nullable String expand(@Nullable String value) {
        return value == null ? null : value.replaceAll("~", tilde);
    }

    protected @Nullable Device device;

    public String getThingName() {
        @Nullable
        String result = null;

        if (this.device != null) {
            result = this.device.name;
        }
        if (result == null && this.device != null) {
            result = this.device.getId();
        }
        if (result == null) {
            result = name;
        }
        return result;
    }

    public String getId(String defaultId) {
        @Nullable
        String result = null;
        if (this.device != null) {
            result = this.device.getId();
        }
        if (result == null) {
            result = unique_id;
        }
        return result != null ? result : defaultId;
    }

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
