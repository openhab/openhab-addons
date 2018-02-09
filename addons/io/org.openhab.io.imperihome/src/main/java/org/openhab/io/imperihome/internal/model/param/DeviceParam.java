/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
