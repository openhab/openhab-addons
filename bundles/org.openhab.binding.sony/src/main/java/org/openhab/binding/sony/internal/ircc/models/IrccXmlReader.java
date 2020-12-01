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

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.openhab.binding.sony.internal.upnp.models.UpnpServiceList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class represents creates the various XML readers (using XStream) to deserialize various calls.
 *
 * @author Tim Roberts - Initial contribution
 *
 * @param <T> the generic type to cast the XML to
 */
@NonNullByDefault
public class IrccXmlReader<T> {
    /** The XStream instance */
    private final XStream xstream = new XStream(new StaxDriver());

    /** The various reader functions */
    public static final IrccXmlReader<IrccRoot> ROOT = new IrccXmlReader<>(
            new Class[] { IrccRoot.class, IrccDevice.class, IrccUnrDeviceInfo.class, IrccCodeList.class, IrccCode.class,
                    UpnpServiceList.class, UpnpService.class },
            new IrccCode.IrccCodeConverter());

    public static final IrccXmlReader<IrccActionList> ACTIONS = new IrccXmlReader<>(
            new Class[] { IrccActionList.class, IrccActionList.IrccAction.class });

    public static final IrccXmlReader<IrccSystemInformation> SYSINFO = new IrccXmlReader<>(
            new Class[] { IrccSystemInformation.class, IrccSystemInformation.SupportedFunction.class,
                    IrccSystemInformation.Function.class, IrccSystemInformation.FunctionItem.class });

    public static final IrccXmlReader<IrccRemoteCommands> REMOTECOMMANDS = new IrccXmlReader<>(
            new Class[] { IrccRemoteCommands.class, IrccRemoteCommand.class, },
            new IrccRemoteCommand.IrccRemoteCommandConverter(), new IrccRemoteCommands.IrccRemoteCommandsConverter());

    static final IrccXmlReader<IrccContentInformation> CONTENTINFO = new IrccXmlReader<>(
            new Class[] { IrccContentInformation.class, IrccInfoItem.class, });

    static final IrccXmlReader<IrccStatusList> STATUS = new IrccXmlReader<>(
            new Class[] { IrccStatusList.class, IrccStatus.class, IrccStatusItem.class });

    static final IrccXmlReader<IrccText> TEXT = new IrccXmlReader<>(new Class[] { IrccText.class });
    static final IrccXmlReader<IrccContentUrl> CONTENTURL = new IrccXmlReader<>(new Class[] { IrccContentUrl.class });

    /**
     * Constructs the reader using the specified classes to process annotations with
     *
     * @param classes a non-null, non-empty array of classes
     * @param converters a possibly empty list of converters
     */
    private IrccXmlReader(@SuppressWarnings("rawtypes") final Class[] classes, final Converter... converters) {
        Objects.requireNonNull(classes, "classes cannot be null");

        xstream.setClassLoader(getClass().getClassLoader());
        xstream.ignoreUnknownElements();
        xstream.processAnnotations(classes);
        for (final Converter conv : converters) {
            xstream.registerConverter(conv);
        }
    }

    /**
     * Will translate the XML and casts to the specified class
     *
     * @param xml the non-null, possibly empty XML to process
     * @return the possibly null translation
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public T fromXML(final String xml) {
        Objects.requireNonNull(xml, "xml cannot be null");

        if (StringUtils.isNotEmpty(xml)) {
            return (T) this.xstream.fromXML(xml);
        }

        return null;
    }
}
