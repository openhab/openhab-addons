/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.Element;

/**
 * The {@link StatusEventDTO} is responsible for
 *
 * @author Jan N. Klug - Initial contribution
 */
@XmlRootElement(name = "Event")
public class StatusEventDTO {

    @XmlElement(name = "InstanceID")
    @XmlJavaTypeAdapter(InstanceIdAdapter.class)
    public HashMap<String, String> values;

    @Override
    public String toString() {
        return "StatusEventDTO{" + "values=" + values + '}';
    }

    public static class InstanceIdAdapter extends XmlAdapter<InstanceIdAdapter.ElementList, Map<String, String>> {
        public static class ElementList {
            @XmlAnyElement
            public List<Element> elements = new ArrayList<>();
        }

        @Override
        public ElementList marshal(Map<String, String> map) {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public Map<String, String> unmarshal(ElementList elementList) {
            return elementList.elements.stream().collect(
                    Collectors.toMap(e -> e.getLocalName().toLowerCase(), e -> e.getAttributeNode("val").getValue()));
        }
    }
}
