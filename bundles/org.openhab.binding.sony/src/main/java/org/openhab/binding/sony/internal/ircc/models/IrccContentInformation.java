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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the deserialized results of an IRCC content information command. The following is an example of
 * the results that will be deserialized. Please note that the 'field' is not unique and can be repeated (ex: each
 * director will have it's own infoItem line). The class will merge multiple lines together into a comma-delimited list
 * (making 'field' unique).
 *
 * <pre>
 * {@code
    <?xml version="1.0" encoding="UTF-8"?>
    <contentInformation>
      <infoItem field="class" value="video"/>
      <infoItem field="source" value="DVD"/>
      <infoItem field="mediaType" value="DVD"/>
      <infoItem field="mediaFormat" value="VIDEO"/>
    </contentInformation>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("contentInformation")
public class IrccContentInformation {
    /** The constant for a title (browswer title or video title) */
    public static final String TITLE = "title";

    /** The constant for a class (video, etc) */
    public static final String CLASS = "class";

    /** The constant for the source (DVD, USB, etc) */
    public static final String SOURCE = "source";

    /** The constant for the media type (DVD, etc) */
    public static final String MEDIATYPE = "mediaType";

    /** The constant for the media format (video, etc) */
    public static final String MEDIAFORMAT = "mediaFormat";

    /** The constant for the ID of the content (a long alphanumeric) */
    public static final String ID = "id"; // 3CD3N19Q253851813V98704329773844B92D3340A18D15901A2AP7 - matches status

    /** The constant for the content edition */
    public static final String EDITION = "edition"; // no example

    /** The constant for the content description (the dvd description) */
    public static final String DESCRIPTION = "description";

    /** The constant for the content genre (action, adventure, etc) */
    public static final String GENRE = "genre"; // Action/Adventure

    /** The constant for the content duration (in seconds) */
    public static final String DURATION = "duration";

    /** The constant for the content raging (G, PG, etc) */
    public static final String RATING = "rating"; // G

    /** The constant for when the content was released (2001-11-01, unknown if regional format) */
    public static final String DATERELEASE = "dateRelease";

    /** The constant for the list of directors */
    public static final String DIRECTOR = "director";

    /** The constant for the list of producers */
    public static final String PRODUCER = "producer";

    /** The constant for the list of screen writers */
    public static final String SCREENWRITER = "screenWriter";

    /** The constant for the icon data (base64 encoded) */
    public static final String ICONDATA = "iconData";

    /** The list of {@link IrccInfoItem} */
    @XStreamImplicit
    private @Nullable List<@Nullable IrccInfoItem> infoItems;

    /**
     * Constructs the {@link IrccContentInformation} from the given XML
     *
     * @param xml a non-null, non-empty XML
     * @return a {@link IrccContentInformation} or null if not valid
     */
    public static @Nullable IrccContentInformation get(final String xml) {
        Validate.notEmpty(xml, "xml cannot be null");
        return IrccXmlReader.CONTENTINFO.fromXML(xml);
    }

    /**
     * Gets the info item value or null if not found. If there are multiple items for the given name, they will be comma
     * separated.
     *
     * @param name the non-null, non-empty name to get
     * @return the value (possibly comma delimited) for the name or null if none found.
     */
    public @Nullable String getInfoItemValue(final String name) {
        Validate.notEmpty(name, "name cannot be empty");
        final List<@Nullable IrccInfoItem> ii = infoItems;
        if (ii == null) {
            return null;
        }

        final StringBuilder b = new StringBuilder();
        for (final IrccInfoItem i : ii) {
            if (i != null && StringUtils.equalsIgnoreCase(name, i.getName())) {
                b.append(i.getValue());
                b.append(", ");
            }
        }

        if (b.length() == 0) {
            return null;
        }

        final int len = b.length();
        if (len > 1) {
            b.delete(len - 2, len);
        }
        return b.toString();
    }
}
