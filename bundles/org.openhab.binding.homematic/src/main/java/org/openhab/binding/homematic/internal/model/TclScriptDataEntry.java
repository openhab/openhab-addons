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
package org.openhab.binding.homematic.internal.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Simple class with the XStream mapping for a data entry returned from a TclRega script.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@XStreamAlias("entry")
public class TclScriptDataEntry {

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
