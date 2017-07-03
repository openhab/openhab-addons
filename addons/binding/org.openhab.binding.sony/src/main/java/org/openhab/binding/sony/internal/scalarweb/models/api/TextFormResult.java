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
 * The Class TextFormResult.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class TextFormResult {

    /** The text. */
    private final String text;

    /**
     * Instantiates a new text form result.
     *
     * @param text the text
     */
    public TextFormResult(String text) {
        super();
        this.text = text;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TextForm [text=" + text + "]";
    }
}
