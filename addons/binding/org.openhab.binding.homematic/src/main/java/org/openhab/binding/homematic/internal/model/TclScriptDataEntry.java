/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
