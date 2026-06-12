/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Simple class with the XStream mapping for a data entry returned from a TclRega script.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("entry")
public class TclScriptDataEntry {
    public TclScriptDataEntry(String name, String description, String value, String valueType, boolean readOnly,
            String options, String minValue, String maxValue, String unit, String operations) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.valueType = valueType;
        this.readOnly = readOnly;
        this.options = options;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
        this.operations = operations;
    }

    @XStreamAsAttribute
    public String name;

    @XStreamAsAttribute
    public String description;

    @XStreamAsAttribute
    public String value;

    @XStreamAsAttribute
    public String valueType;

    @XStreamAsAttribute
    public boolean readOnly;

    @XStreamAsAttribute
    public String options;

    @XStreamAsAttribute
    @XStreamAlias("min")
    public String minValue;

    @XStreamAsAttribute
    @XStreamAlias("max")
    public String maxValue;

    @XStreamAsAttribute
    public String unit;

    @XStreamAsAttribute
    public String operations;
}
