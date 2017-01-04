package org.openhab.binding.isy.internal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "var")
@XmlAccessorType(XmlAccessType.FIELD)

public class Variable {

    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "id")
    protected String id;

    protected Integer init;
    protected Integer val;
    // protected String ts;

    @Override
    public String toString() {
        return new StringBuilder("Isy Variable: ts=").append(id).append(", id=").append(id).append(", enabled=")
                .append(id).toString();
    }

}
