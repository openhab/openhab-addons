package org.openhab.binding.mynice.internal.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Device")
public class Device {
    public int id;
    public String text;

    @XStreamAlias("Type")
    public String type;

    @XStreamAlias("Manuf")
    public String manuf;

    @XStreamAlias("Prod")
    public String prod;

    @XStreamAlias("Desc")
    public String desc;

    @XStreamAlias("VersionHW")
    public String versionHW;

    @XStreamAlias("VersionFW")
    public String versionFW;

    @XStreamAlias("SerialNr")
    public String serialNr;
}
