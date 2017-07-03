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
 * The Class Source.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class Source {

    /** The tv analog. */
    public static String TV_ANALOG = "tv:analog";

    /** The tv digital. */
    public static String TV_DIGITAL = "tv:atsct";

    /** The source. */
    private final String source;

    /**
     * Instantiates a new source.
     *
     * @param source the source
     */
    public Source(String source) {
        super();
        this.source = source;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Source [source=" + source + "]";
    }
}
