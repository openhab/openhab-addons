/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nuvo.internal.dto;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for a static use of JAXBContext as singleton instance.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class JAXBUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JAXBUtils.class);

    public static final @Nullable JAXBContext JAXBCONTEXT_NUVO_MENU = initJAXBContextNuvoMenu();
    public static final XMLInputFactory XMLINPUTFACTORY = initXMLInputFactory();

    private static @Nullable JAXBContext initJAXBContextNuvoMenu() {
        try {
            return JAXBContext.newInstance(NuvoMenu.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for nuvo menu: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static XMLInputFactory initXMLInputFactory() {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return xif;
    }
}
