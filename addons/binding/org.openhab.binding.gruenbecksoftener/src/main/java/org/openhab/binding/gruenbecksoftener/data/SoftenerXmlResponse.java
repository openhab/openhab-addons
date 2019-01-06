/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gruenbecksoftener.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Element;

/**
 * The {@link SoftenerXmlResponse} is the Java class used to map the XML
 * response to the gruenbeck softener request.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@XmlRootElement(name = "data")
@NonNullByDefault
public class SoftenerXmlResponse {

    @XmlElement
    private @NonNullByDefault({}) String code;

    // @XmlJavaTypeAdapter(SoftenerDataXmlAdapter.class)
    @XmlAnyElement
    public @NonNullByDefault({}) Element[] data;

    public String getCode() {
        return code;
    }

    public Map<String, String> getData() {
        if (data != null) {
            return Arrays.stream(data).collect(Collectors.toMap(Element::getLocalName, Element::getTextContent));
        }
        return new HashMap<>();
    }

    @Override
    public String toString() {
        return "code=" + code + ", data=" + getData() + "";
    }

}
