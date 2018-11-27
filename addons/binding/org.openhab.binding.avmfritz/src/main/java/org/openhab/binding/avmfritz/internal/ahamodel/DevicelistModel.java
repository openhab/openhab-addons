/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This JAXB model class maps the XML response to an <b>getdevicelistinfos</b>
 * command on a FRITZ!Box device. As of today, this class is able to to bind the
 * devicelist version 1 (currently used by AVM) response:
 *
 * <pre>
 * <devicelist version="1">
 * <device identifier="##############" id="##" functionbitmask="2944" fwversion="03.83" manufacturer="AVM" productname=
 * "FRITZ!DECT 200">
 * <present>1</present>
 * <name>FRITZ!DECT 200 #1</name>
 * <switch>
 * <state>0</state>
 * <mode>manuell</mode>
 * <lock>0</lock>
 * <devicelock>1</devicelock>
 * </switch>
 * <powermeter>
 * <power>0</power>
 * <energy>166</energy>
 * </powermeter>
 * <temperature>
 * <celsius>255</celsius>
 * <offset>0</offset>
 * </temperature>
 * </device>
 * <device identifier="##############" id="xx" functionbitmask="320" fwversion="03.50" manufacturer="AVM" productname=
 * "Comet DECT">
 * <present>1</present>
 * <name>Comet DECT #1</name>
 * <temperature>
 * <celsius>220</celsius>
 * <offset>-10</offset>
 * </temperature>
 * <hkr>
 * <tist>44</tist>
 * <tsoll>42</tsoll>
 * <absenk>28</absenk>
 * <komfort>42</komfort>
 * <lock>0</lock>
 * <devicelock>0</devicelock>
 * <errorcode>0</errorcode>
 * <batterylow>0</batterylow>
 * <nextchange>
 * <endperiod>1484341200</endperiod>
 * <tchange>28</tchange>
 * </nextchange>
 * </hkr>
 * </device>
 * </devicelist>
 *
 * <pre>
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@XmlRootElement(name = "devicelist")
public class DevicelistModel {

    @XmlAttribute(name = "version")
    private String apiVersion;

    @XmlElements({ @XmlElement(name = "device", type = DeviceModel.class),
            @XmlElement(name = "group", type = GroupModel.class) })
    private ArrayList<AVMFritzBaseModel> devices;

    public ArrayList<AVMFritzBaseModel> getDevicelist() {
        if (this.devices == null) {
            this.devices = new ArrayList<>();
        }
        return devices;
    }

    public void setDevicelist(ArrayList<AVMFritzBaseModel> devicelist) {
        this.devices = devicelist;
    }

    public String getXmlApiVersion() {
        return this.apiVersion;
    }
}
