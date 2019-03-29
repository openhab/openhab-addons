/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceListModel;
import org.openhab.binding.avmfritz.internal.ahamodel.templates.TemplateListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for a static use of JAXBContext as singleton instance.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class JAXBUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JAXBUtils.class);

    public static final @Nullable JAXBContext JAXBCONTEXT_DEVICES = initJAXBContextDevices();
    public static final @Nullable JAXBContext JAXBCONTEXT_TEMPLATES = initJAXBContextTemplates();

    private static @Nullable JAXBContext initJAXBContextDevices() {
        try {
            return JAXBContext.newInstance(DeviceListModel.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for devices: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static @Nullable JAXBContext initJAXBContextTemplates() {
        try {
            return JAXBContext.newInstance(TemplateListModel.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for templates: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }
}
