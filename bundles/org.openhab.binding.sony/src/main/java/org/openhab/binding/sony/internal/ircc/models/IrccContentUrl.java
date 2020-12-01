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
 * This class represents the deserialized results of an IRCC content url command. The following is an example of
 * the results that will be deserialized.
 *
 * <pre>
 * {@code
    <?xml version="1.0" encoding="UTF-8"?>
    <contenturl>
        <url>www.google.com</url>
        <contentInformation>
          <infoItem field="class" value="video"/>
          <infoItem field="source" value="DVD"/>
          <infoItem field="mediaType" value="DVD"/>
          <infoItem field="mediaFormat" value="VIDEO"/>
        </contentInformation>
    </contenturl>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("contenturl")
public class IrccContentUrl {
    /** The URL representing the content */
    @XStreamAlias("url")
    private @Nullable String url;

    /** The content information for the URL */
    @XStreamAlias("contentInformation")
    private @Nullable IrccContentInformation contentInformation;

    /**
     * Creates the {@link IrccContentUrl} from the given XML
     *
     * @param xml a non-null, non-empty XML to parse
     * @return the {@link IrccContentUrl} or null if not valid
     */
    public static @Nullable IrccContentUrl get(final String xml) {
        Validate.notEmpty(xml, "xml cannot be empty");
        return IrccXmlReader.CONTENTURL.fromXML(xml);
    }

    /**
     * Returns the content of the URL
     *
     * @return a possibly null, possibly empty URL
     */
    public @Nullable String getUrl() {
        return url;
    }

    /**
     * Gets the content information for the URL
     *
     * @return the possibly null content information
     */
    public @Nullable IrccContentInformation getContentInformation() {
        return contentInformation;
    }
}
