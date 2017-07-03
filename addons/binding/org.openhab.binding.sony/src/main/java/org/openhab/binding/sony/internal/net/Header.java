/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.net;

// TODO: Auto-generated Javadoc
/**
 * The Class Header.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class Header {

    /** The name. */
    private final String _name;

    /** The value. */
    private final String _value;

    /**
     * Instantiates a new header.
     *
     * @param name the name
     * @param value the value
     */
    public Header(String name, String value) {
        _name = name;
        _value = value;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return _value;
    }
}