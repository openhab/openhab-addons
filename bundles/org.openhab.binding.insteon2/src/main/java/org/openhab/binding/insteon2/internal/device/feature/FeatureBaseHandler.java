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
package org.openhab.binding.insteon2.internal.device.feature;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon2.internal.device.Device;
import org.openhab.binding.insteon2.internal.device.DeviceFeature;
import org.openhab.binding.insteon2.internal.device.InsteonDevice;
import org.openhab.binding.insteon2.internal.device.InsteonModem;
import org.openhab.binding.insteon2.internal.device.X10Device;
import org.openhab.binding.insteon2.internal.utils.ParameterParser;

/**
 * Feature base handler abstract class
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class FeatureBaseHandler {

    protected DeviceFeature feature;
    protected Map<String, String> parameters = Map.of();

    public FeatureBaseHandler(DeviceFeature feature) {
        this.feature = feature;
    }

    protected Device getDevice() {
        return feature.getDevice();
    }

    protected InsteonDevice getInsteonDevice() {
        try {
            return (InsteonDevice) feature.getDevice();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Not Insteon device");
        }
    }

    protected InsteonModem getInsteonModem() {
        try {
            return (InsteonModem) feature.getDevice();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Not Insteon modem");
        }
    }

    protected X10Device getX10Device() {
        try {
            return (X10Device) feature.getDevice();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Not X10 device");
        }
    }

    private @Nullable String getParameter(String key) {
        return feature.hasParameter(key) ? feature.getParameter(key) : parameters.get(key);
    }

    protected boolean getParameterAsBoolean(String key, boolean defaultValue) {
        return ParameterParser.getParameterAsOrDefault(getParameter(key), Boolean.class, defaultValue);
    }

    protected double getParameterAsDouble(String key, double defaultValue) {
        return ParameterParser.getParameterAsOrDefault(getParameter(key), Double.class, defaultValue);
    }

    protected int getParameterAsInteger(String key, int defaultValue) {
        return ParameterParser.getParameterAsOrDefault(getParameter(key), Integer.class, defaultValue);
    }

    protected long getParameterAsLong(String key, long defaultValue) {
        return ParameterParser.getParameterAsOrDefault(getParameter(key), Long.class, defaultValue);
    }

    protected String getParameterAsString(String key, String defaultValue) {
        return ParameterParser.getParameterAsOrDefault(getParameter(key), String.class, defaultValue);
    }

    protected void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns shorthand class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        String s = nm();
        if (!parameters.isEmpty()) {
            s += parameters;
        }
        return s;
    }
}
