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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for boolean parameter.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class BooleanParameter extends Parameter {
    private Boolean value;

    @Override
    public BooleanParameter withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public BooleanParameter withRequired(Boolean required) {
        super.withRequired(required);
        return this;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public BooleanParameter withValue(Boolean value) {
        setValue(value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BooleanParameter [value=");
        builder.append(value);
        builder.append(", getName()=");
        builder.append(getName());
        builder.append(", getRequired()=");
        builder.append(getRequired());
        builder.append("]");
        return builder.toString();
    }
}
