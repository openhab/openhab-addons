package org.openhab.binding.mynice.internal.dto;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("Response")
public class Response {
    @XStreamAsAttribute
    private String gw;
    @XStreamAsAttribute
    private String protocolVersion;
    @XStreamAsAttribute
    private String id;
    @XStreamAsAttribute
    private String source;
    @XStreamAsAttribute
    private String protocolType;
    @XStreamAsAttribute
    public CommandType type;
    @XStreamAsAttribute
    private String target;

    @XStreamAlias("Error")
    private Error error;

    @XStreamAlias("Authentication")
    public Authentication authentication;

    @XStreamAlias("Interface")
    public Interface intf;

    @XStreamAlias("Devices")
    public List<Device> devices;

    @Override
    public String toString() {
        return "Response [gw = " + gw + ", Authentication = " + authentication + ", protocolVersion = "
                + protocolVersion + ", id = " + id + ", source = " + source + ", protocolType = " + protocolType
                + ", type = " + type + ", target = " + target + "]";
    }
}
