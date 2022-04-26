package org.openhab.binding.mynice.internal.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Service")
public class Service {
    public int max_id;
    public String type;
    public String action;
}
