/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

import java.util.Map;

import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

/**
 * Pojo for widget.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Widget {
    private String id;
    @SerializedName("package")
    private String packageName;
    private Integer index;
    private Map<String, JsonPrimitive> settings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Widget withId(String id) {
        setId(id);
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Widget withPackageName(String packageName) {
        setPackageName(packageName);
        return this;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Widget withIndex(Integer index) {
        setIndex(index);
        return this;
    }

    public Map<String, JsonPrimitive> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, JsonPrimitive> settings) {
        this.settings = settings;
    }

    public Widget withSettings(Map<String, JsonPrimitive> settings) {
        setSettings(settings);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        result = prime * result + ((settings == null) ? 0 : settings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Widget other = (Widget) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (index == null) {
            if (other.index != null) {
                return false;
            }
        } else if (!index.equals(other.index)) {
            return false;
        }
        if (packageName == null) {
            if (other.packageName != null) {
                return false;
            }
        } else if (!packageName.equals(other.packageName)) {
            return false;
        }
        if (settings == null) {
            if (other.settings != null) {
                return false;
            }
        } else if (!settings.equals(other.settings)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Widget [id=");
        builder.append(id);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append(", index=");
        builder.append(index);
        builder.append(", settings=");
        builder.append(settings);
        builder.append("]");
        return builder.toString();
    }
}
