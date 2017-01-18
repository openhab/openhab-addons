package org.openhab.binding.isy.internal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)

public class Program {

    @XmlAttribute(name = "id")
    protected String id;
    protected String name;
    @XmlAttribute(name = "running")
    protected String running;
    @XmlAttribute(name = "enabled")
    protected String enabled;
    @XmlAttribute(name = "folder")
    protected String folder;

    @XmlAttribute(name = "status")
    protected String status;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // protected String uri;
    @Override
    public String toString() {
        return new StringBuilder("Isy Program: name=").append(name).append(", id=").append(id).append(", running=")
                .append(running).toString();
    }

}
