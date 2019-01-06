/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gruenbecksoftener.handler;

import java.io.StringReader;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gruenbecksoftener.data.SoftenerXmlResponse;

/**
 * Converts a {@link String} to an {@link SoftenerXmlResponse}.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
class XmlResponseParser implements Function<String, SoftenerXmlResponse> {

    @Override
    public SoftenerXmlResponse apply(String responseBody) {
        if ("".equals(responseBody)) {
            return new SoftenerXmlResponse();
        }
        try {
            JAXBContext context = JAXBContext.newInstance(SoftenerXmlResponse.class);
            return (SoftenerXmlResponse) context.createUnmarshaller().unmarshal(new StringReader(responseBody));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Failed to parse the input XML", e);
        }
    }

}