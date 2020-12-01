/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.ircc.models;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Class that represents the text in a text field. The XML that will be deserialized will
 * look like:
 *
 * <pre>
 * {@code
    <?xml version="1.0" encoding="UTF-8"?>
    <text>the text in the field</text>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias(IrccText.TEXTROOT)
public class IrccText {

    /** The dummy root name that will be wrapped around the text element to allow parsing */
    protected static final String TEXTROOT = "textroot";

    /** The text. */
    @XStreamAlias("text")
    private @Nullable String text;

    /**
     * Gets the text.
     *
     * @return the text
     */
    public @Nullable String getText() {
        return text;
    }

    /**
     * Get's the IRCC text from the specified XML
     *
     * @param xml the non-null, non-empty XML
     * @return the non-null IrccText
     */
    public @Nullable static IrccText get(final String xml) {
        Validate.notEmpty(xml, "xml cannot be empty");

        final StringBuilder sb = new StringBuilder(xml);
        final int idx = sb.indexOf("<text");
        if (idx >= 0) {
            sb.insert(idx, "<" + TEXTROOT + ">");
            sb.append("</" + TEXTROOT + ">");
        }

        return IrccXmlReader.TEXT.fromXML(sb.toString());
    }
}
