package org.openhab.binding.gruenbecksoftener.data;

import javax.xml.bind.annotation.XmlAnyElement;

import org.w3c.dom.Element;

public class MapWrapper {

    @XmlAnyElement
    Element[] elements;

    public Element[] getElements() {
        return elements;
    }
}
