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
package org.openhab.binding.sony.internal.upnp.models;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class represents creates the various XML readers (using XStream) to deserialize various calls.
 *
 * @author Tim Roberts - Initial contribution
 *
 * @param <T> the generic type to cast the XML to
 */
@NonNullByDefault
public class UpnpXmlReader<T> {

    /** The XStream instance */
    private final XStream xstream = new XStream(new StaxDriver());

    /** The XML reader for SCPD */
    public static final UpnpXmlReader<UpnpScpd> SCPD = new UpnpXmlReader<>(new Class[] { UpnpScpd.class,
            UpnpScpd.UpnpScpdActionList.class, UpnpScpd.UpnpScpdStateTable.class, UpnpScpdAction.class,
            UpnpScpdAction.UpnpScpdArgumentList.class, UpnpScpdArgument.class, UpnpScpdStateVariable.class });

    /**
     * Constructs the reader using the specified classes to process annotations with
     *
     * @param classes a non-null, non-empty array of classes
     */
    private UpnpXmlReader(@SuppressWarnings("rawtypes") final Class[] classes) {
        Objects.requireNonNull(classes, "classes cannot be null");

        xstream.setClassLoader(getClass().getClassLoader());
        xstream.ignoreUnknownElements();
        xstream.processAnnotations(classes);
    }

    /**
     * Will translate the XML and casts to the specified class
     *
     * @param xml the non-null, possibly empty XML to process
     * @return the possibly null translation
     */
    @SuppressWarnings("unchecked")
    public @Nullable T fromXML(final String xml) {
        if (StringUtils.isNotEmpty(xml)) {
            return (T) this.xstream.fromXML(xml);
        }

        return null;
    }
}
