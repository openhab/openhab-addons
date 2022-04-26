package org.openhab.binding.mynice.internal.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Property")
public class Property {
    @XStreamAlias("DoorStatus")
    public DoorStatus DoorStatus;
    @XStreamAlias("Obstruct")
    public Obstruct Obstruct;
    @XStreamAlias("T4_allowed")
    public T4Allowed t4Allowed;
}
