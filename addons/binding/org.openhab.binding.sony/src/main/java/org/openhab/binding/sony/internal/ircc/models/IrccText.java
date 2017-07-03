/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccText.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccText {

    /** The text. */
    private String text;

    /**
     * Instantiates a new ircc text.
     *
     * @param textXml the text xml
     */
    public IrccText(Document textXml) {
        final NodeList cmds = textXml.getElementsByTagName("text");
        if (cmds.getLength() > 0) {
            text = cmds.item(0).getTextContent();
        }
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }
}