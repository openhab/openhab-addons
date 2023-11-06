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
package org.openhab.binding.mikrotik.internal.model;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RouterosBaseData} is a base class for other data models having internal hashmap access methods and
 * values convertors.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public abstract class RouterosBaseData {
    private final Map<String, String> propMap;

    public RouterosBaseData(Map<String, String> props) {
        this.propMap = props;
    }

    public void mergeProps(Map<String, String> otherProps) {
        this.propMap.putAll(otherProps);
    }

    protected boolean hasProp(String key) {
        return propMap.containsKey(key);
    }

    protected String getProp(String key, String defaultValue) {
        return propMap.getOrDefault(key, defaultValue);
    }

    protected void setProp(String key, String value) {
        propMap.put(key, value);
    }

    protected @Nullable String getProp(String key) {
        return propMap.get(key);
    }

    protected @Nullable Integer getIntProp(String key) {
        String val = propMap.get(key);
        return val == null ? null : Integer.valueOf(val);
    }

    protected @Nullable BigInteger getBigIntProp(String key) {
        String val = propMap.get(key);
        return val == null ? null : new BigInteger(propMap.getOrDefault(key, "0"));
    }

    protected @Nullable Float getFloatProp(String key) {
        String val = propMap.get(key);
        return val == null ? null : Float.valueOf(val);
    }
}
