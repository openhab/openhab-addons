/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import org.w3c.dom.Element;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccInfoItem.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccInfoItem {

    /** The name. */
    private final String name;

    /** The value. */
    private final String value;

    /**
     * Instantiates a new ircc info item.
     *
     * @param xml the xml
     */
    public IrccInfoItem(Element xml) {
        name = xml.getAttribute("field");
        value = xml.getAttribute("value");
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
