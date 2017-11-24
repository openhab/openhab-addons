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
package org.openhab.binding.osramlightify.internal.effects;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.messages.LightifySetEffectMessage;

/**
 * Set an effect on a device.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public abstract class LightifyEffect {

    protected static final int NSTEPS = 15;
    protected static final int STEP_FIELD_HUE = 0;
    protected static final int STEP_FIELD_SATURATION = 1;
    protected static final int STEP_FIELD_LUMINANCE = 2;
    protected static final int STEP_FIELD_DURATION = 3;

    protected final Logger logger = LoggerFactory.getLogger(LightifyEffect.class);

    protected boolean debug = false;
    protected boolean debugOnce = false;

    protected LightifyBridgeHandler bridgeHandler;
    protected LightifyDeviceHandler deviceHandler;
    protected String name;
    protected String params = "";
    protected boolean color = false;
    protected byte[] data = new byte[0];
    private boolean dataWritable = false;

    public LightifyEffect(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name) {
        this.bridgeHandler = bridgeHandler;
        this.deviceHandler = deviceHandler;
        this.name = name;
    }

    public LightifyEffect(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name, boolean color) {
        this(bridgeHandler, deviceHandler, name);
        this.color = color;
    }

    public LightifyEffect(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name, boolean color, byte[] data) {
        this(bridgeHandler, deviceHandler, name, color);
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void parseParams(String params) {
        this.params = params;

        logger.debug("params: {}", params);

        for (String field : params.split("\\s*,\\s*")) {
            String[] pair = field.split("\\s*=\\s*", 2);
            if (pair.length >= 1 && pair[0] != null && !pair[0].isEmpty()) {
                param(pair[0], (pair.length == 2 ? pair[1] : ""));
            }
        }
    }

    protected void param(String key, String value) {
        if (key.equals("debug")) {
            if (value == null || value.isEmpty()) {
                debug = true;
            } else if (value.equals("once")) {
                debug = true;
                debugOnce = true;
            } else {
                debug = parseBoolean(value);
            }
        } else {
            logger.warn("{}: unknown parameter {}", name, key);
        }
    }

    protected boolean parseBoolean(String value) {
        return (value.equals("true") || !value.equals("0"));
    }

    protected double parseHue(String value) {
        return Double.parseDouble(value) % 360.0;
    }

    protected double parseAbsoluteOrPercentage(String value) {
        if (value.endsWith("%")) {
            return Double.parseDouble(value.substring(0, value.length() - 1)) * 2.55;
        } else {
            return Double.parseDouble(value);
        }
    }

    protected void setWritableData(byte[] data) {
        this.data = data;
        dataWritable = true;
    }

    protected void writeEnableData() {
        if (!dataWritable) {
            writeEnableData(data);
        }
    }

    protected void writeEnableData(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
        dataWritable = true;
    }

    protected int getHue(int step) {
        return getData(step, STEP_FIELD_HUE);
    }

    protected int getSaturation(int step) {
        return getData(step, STEP_FIELD_SATURATION);
    }

    protected int getLuminance(int step) {
        return getData(step, STEP_FIELD_LUMINANCE);
    }

    protected int getDuration(int step) {
        return getData(step, STEP_FIELD_DURATION);
    }

    protected int getData(int step, int index) {
        return ((int) data[9 + step * 4 + index] ) & 0xff;
    }

    protected void setHue(int step, double value) {
        value %= 360;
        if (value < 0.0) {
            value += 360.0;
        }
        value = value * 256.0 / 360.0;
        setData(step, STEP_FIELD_HUE, (byte) value);
    }

    protected void setSaturation(int step, double value) {
        setField(step, STEP_FIELD_SATURATION, value);
    }

    protected void setLuminance(int step, double value) {
        setField(step, STEP_FIELD_LUMINANCE, value);
    }

    protected void setDuration(int step, int value) {
        setData(step, STEP_FIELD_DURATION, (byte) value);
    }

    protected void setField(int step, int field, double value) {
        if (value < 0.0) {
            setData(step, field, (byte) 0.0);
        } else if (value > 255.0) {
            setData(step, field, (byte) 255);
        } else {
            setData(step, field, (byte) value);
        }
    }

    protected void setData(int step, int index, byte value) {
        setData(9 + step * 4 + index, value);
    }

    protected void setData(int index, byte value) {
        if (index != data.length - 1) {
            if (index >= 9) {
                data[data.length-1] -= value - data[index];
            }

            data[index] = value;
        }
    }

    public void start() {
        bridgeHandler.sendMessage(new LightifySetEffectMessage(deviceHandler, name, params, color, data));

        if (debugOnce) {
            debugOnce = false;
            debug = false;
        }
    }

    public void stop() {
    }
}
