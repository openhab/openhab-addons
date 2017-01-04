package org.openhab.binding.isy.internal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)

public class Node {

    @XmlAttribute(name = "flag")
    protected String flag;
    protected String name;
    protected String address;
    protected String type;

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    // protected String uri;
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return new StringBuilder("Isy Node: name=").append(name).append(", flag=").append(flag).append(", address=")
                .append(address).append(", type=").append(getTypeReadable()).toString();
    }

    public String getTypeReadable() {
        String[] typeElements = type.split("\\.");
        return String.format("%02X", Integer.parseInt(typeElements[0])) + "."
                + String.format("%02X", Integer.parseInt(typeElements[1]));
    }
}
