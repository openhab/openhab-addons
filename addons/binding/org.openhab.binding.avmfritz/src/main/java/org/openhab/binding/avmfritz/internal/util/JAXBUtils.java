/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.util;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for a static use of JAXBContext as singleton instance.
 *
 * @author Christoph Weitkamp
 *
 */
public class JAXBUtils {

    private static final Logger logger = LoggerFactory.getLogger(JAXBUtils.class);

    public static final JAXBContext JAXBCONTEXT = initJAXBContext();

    public static Unmarshaller JAXBUnmarshaller;

    private static JAXBContext initJAXBContext() {
        try {
            return JAXBContext.newInstance(DevicelistModel.class);
        } catch (JAXBException e) {
            logger.error("Exception creating JAXBContext: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    public static DevicelistModel buildResult(final String xml) throws JAXBException {
        if (JAXBUnmarshaller == null) {
            JAXBUnmarshaller = JAXBCONTEXT.createUnmarshaller();
        }
        return (DevicelistModel) JAXBUnmarshaller.unmarshal(new StringReader(xml));
    }
}
