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
package org.openhab.binding.sony.internal.dial.models;

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
public class DialXmlReader<T> {

    /** The XStream instance */
    private final XStream xstream = new XStream(new StaxDriver());

    /** The reader for the ROOT XML (see {@link DialRoot}) */
    public static final DialXmlReader<DialRoot> ROOT = new DialXmlReader<>(
            new Class[] { DialRoot.class, DialRoot.RootDevice.class, DialClient.class, DialDeviceInfo.class });

    /** The reader for the SERVICE XML (see {@link DialService}) */
    static final DialXmlReader<DialService> SERVICE = new DialXmlReader<>(
            new Class[] { DialService.class, DialApp.class, DialApp.SupportedAction.class });

    /** The reader for the APP STATE XML (see {@link DialAppState}) */
    static final DialXmlReader<DialAppState> APPSTATE = new DialXmlReader<>(new Class[] { DialAppState.class });

    /**
     * Constructs the reader using the specified classes to process annotations with
     *
     * @param classes a non-null, non-empty array of classes
     */
    private DialXmlReader(@SuppressWarnings("rawtypes") final Class[] classes) {
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
        Objects.requireNonNull(xml, "xml cannot be null");

        if (StringUtils.isNotEmpty(xml)) {
            return (T) this.xstream.fromXML(xml);
        }

        return null;
    }
}
