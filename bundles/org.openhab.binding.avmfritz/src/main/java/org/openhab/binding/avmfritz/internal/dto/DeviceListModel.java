/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.avmfritz.internal.dto;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "devicelist")
public class DeviceListModel {

    @XmlAttribute(name = "version")
    private String apiVersion;

    //@formatter:off
    @XmlElements({
        @XmlElement(name = "device", type = DeviceModel.class),
        @XmlElement(name = "group", type = GroupModel.class)
    })
    //@formatter:on
    private List<AVMFritzBaseModel> devices;

    public List<AVMFritzBaseModel> getDevicelist() {
        if (devices == null) {
            devices = Collections.emptyList();
        }
        return devices;
    }

    public void setDevicelist(List<AVMFritzBaseModel> devices) {
        this.devices = devices;
    }

    public String getXmlApiVersion() {
        return apiVersion;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[devices=").append(devices).append(",version=").append(apiVersion)
                .append("]").toString();
    }
}
