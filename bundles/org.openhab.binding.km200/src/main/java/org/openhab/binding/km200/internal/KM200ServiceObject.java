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
package org.openhab.binding.km200.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;

/**
 * The KM200CommObject representing a service on a device with its all capabilities
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200ServiceObject {
    private int readable;
    private int writeable;
    private int recordable;
    private int virtual;
    private boolean updated;
    private @Nullable String parent;
    private String fullServiceName;
    private String serviceType;
    private @Nullable JsonObject jsonData;
    private @Nullable Object value;
    private @Nullable Object valueParameter;

    /* Device services */
    public Map<String, KM200ServiceObject> serviceTreeMap;

    public KM200ServiceObject(String fullServiceName, String serviceType, int readable, int writeable, int recordable,
            int virtual, @Nullable String parent) {
        serviceTreeMap = new HashMap<>();
        this.fullServiceName = fullServiceName;
        this.serviceType = serviceType;
        this.readable = readable;
        this.writeable = writeable;
        this.recordable = recordable;
        this.virtual = virtual;
        this.parent = parent;
        updated = false;
    }

    /* Sets */
    public void setValue(Object val) {
        value = val;
    }

    public void setUpdated(boolean updt) {
        updated = updt;
    }

    public void setValueParameter(Object val) {
        valueParameter = val;
    }

    public void setJSONData(JsonObject data) {
        jsonData = data;
    }

    /* gets */
    public int getReadable() {
        return readable;
    }

    public int getWriteable() {
        return writeable;
    }

    public int getRecordable() {
        return recordable;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getFullServiceName() {
        return fullServiceName;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public @Nullable Object getValueParameter() {
        return valueParameter;
    }

    public @Nullable String getParent() {
        return parent;
    }

    public int getVirtual() {
        return virtual;
    }

    public boolean getUpdated() {
        return updated;
    }

    public @Nullable JsonObject getJSONData() {
        return jsonData;
    }
}
