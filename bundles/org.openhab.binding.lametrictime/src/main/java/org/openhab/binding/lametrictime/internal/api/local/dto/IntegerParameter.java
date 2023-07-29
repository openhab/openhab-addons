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

/**
 * Pojo for integer parameter.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class IntegerParameter extends Parameter {
    private Integer value;

    @Override
    public IntegerParameter withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public IntegerParameter withRequired(Boolean required) {
        super.withRequired(required);
        return this;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public IntegerParameter withValue(Integer value) {
        setValue(value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IntegerParameter [value=");
        builder.append(value);
        builder.append(", getName()=");
        builder.append(getName());
        builder.append(", getRequired()=");
        builder.append(getRequired());
        builder.append("]");
        return builder.toString();
    }
}
