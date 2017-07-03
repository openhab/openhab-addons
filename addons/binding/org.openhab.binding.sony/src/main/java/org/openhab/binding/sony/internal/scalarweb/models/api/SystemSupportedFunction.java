/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

// TODO: Auto-generated Javadoc
/**
 * The Class SystemSupportedFunction.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class SystemSupportedFunction {

    /** The option. */
    public final String option;

    /** The value. */
    public final String value;

    /**
     * Instantiates a new system supported function.
     *
     * @param option the option
     * @param value the value
     */
    public SystemSupportedFunction(String option, String value) {
        super();
        this.option = option;
        this.value = value;
    }

    /**
     * Gets the option.
     *
     * @return the option
     */
    public String getOption() {
        return option;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SystemSupportedFunction [option=" + option + ", value=" + value + "]";
    }

}
