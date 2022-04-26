package org.openhab.binding.mynice.internal.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("DoorStatus")
public class DoorStatus {
    public String type;
    public String values;
    public String perm;
}
