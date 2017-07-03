/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 *
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

// TODO: Auto-generated Javadoc
/**
 * The Class ActRegisterOptions.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ActRegisterOptions {

    /** The value. */
    private final String value;

    /** The function. */
    private final String function;

    /**
     * Instantiates a new act register options.
     */
    public ActRegisterOptions() {
        value = "yes";
        function = "WOL";
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the function.
     *
     * @return the function
     */
    public String getFunction() {
        return function;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ActRegisterOptions [value=" + value + ", function=" + function + "]";
    }
}
