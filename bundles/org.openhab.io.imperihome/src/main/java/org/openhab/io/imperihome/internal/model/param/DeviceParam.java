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
package org.openhab.io.imperihome.internal.model.param;

/**
 * Basic device key/value parameter.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DeviceParam {

    private ParamType key;
    private Object value;

    public DeviceParam(ParamType type) {
        this.key = type;
    }

    public DeviceParam(ParamType type, Object value) {
        this.key = type;
        this.value = value;
    }

    public ParamType getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceParam)) {
            return false;
        }

        DeviceParam that = (DeviceParam) o;

        if (key != that.key) {
            return false;
        }
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "DeviceParam{" + "key=" + key + ", value='" + value + '\'' + '}';
    }
}
