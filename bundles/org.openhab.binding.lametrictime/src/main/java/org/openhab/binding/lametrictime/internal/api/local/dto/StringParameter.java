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
 * Pojo for stringparameter.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class StringParameter extends Parameter {
    private String format;
    private String value;

    @Override
    public StringParameter withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public StringParameter withRequired(Boolean required) {
        super.withRequired(required);
        return this;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public StringParameter withFormat(String format) {
        setFormat(format);
        return this;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public StringParameter withValue(String value) {
        setValue(value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StringParameter [format=");
        builder.append(format);
        builder.append(", value=");
        builder.append(value);
        builder.append(", getName()=");
        builder.append(getName());
        builder.append(", getRequired()=");
        builder.append(getRequired());
        builder.append("]");
        return builder.toString();
    }
}
