package org.openhab.binding.mynice.internal.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Interface")
public class Interface {
    @XStreamAlias("Zone")
    public String zone;
    @XStreamAlias("DST")
    public String dst;
    @XStreamAlias("VersionHW")
    public String versionHW;
    @XStreamAlias("VersionFW")
    public String versionFW;
    @XStreamAlias("Manuf")
    public String manuf;
    @XStreamAlias("Prod")
    public String prod;
    @XStreamAlias("SerialNr")
    public String serialNr;
}
