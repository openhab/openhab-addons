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
package org.openhab.binding.insteon.internal.handler.feature;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.utils.ParameterParser;

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

    protected InsteonDevice getDevice() {
        return feature.getDevice();
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
}
