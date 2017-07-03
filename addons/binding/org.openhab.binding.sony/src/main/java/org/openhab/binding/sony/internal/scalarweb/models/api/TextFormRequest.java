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
 * The Class TextFormRequest.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class TextFormRequest {

    /** The enc key. */
    private final String encKey;

    /** The text. */
    private final String text;

    /**
     * Instantiates a new text form request.
     *
     * @param encKey the enc key
     * @param text the text
     */
    public TextFormRequest(String encKey, String text) {
        super();
        this.encKey = encKey;
        this.text = text;
    }

    /**
     * Gets the enc key.
     *
     * @return the enc key
     */
    public String getEncKey() {
        return encKey;
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
        return "TextFormRequest [encKey=" + encKey + ", text=" + text + "]";
    }
}
