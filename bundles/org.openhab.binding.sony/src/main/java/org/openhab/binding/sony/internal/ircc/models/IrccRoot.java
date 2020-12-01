/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.ircc.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the root element in the XML for a IRCC device. The XML that will be deserialized will look like
 *
 * <pre>
 * {@code
 <?xml version="1.0"?>
  <root xmlns="urn:schemas-upnp-org:device-1-0">
    <specVersion>
      <major>1</major>
      <minor>0</minor>
    </specVersion>
    <device>
      <deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>
      <friendlyName>XBR-55X900E</friendlyName>
      <manufacturer>Sony Corporation</manufacturer>
      <manufacturerURL>http://www.sony.net/</manufacturerURL>
      <modelName>XBR-55X900E</modelName>
      <UDN>uuid:25257d9e-41b1-43df-9332-ffe401305cf4</UDN>

      <iconList>
        <icon>
          <mimetype>image/jpeg</mimetype>
          <width>120</width>
          <height>120</height>
          <depth>24</depth>
          <url>/sony/webapi/ssdp/icon/dlna_tv_120.jpg</url>
        </icon>
        <icon>
          <mimetype>image/png</mimetype>
          <width>120</width>
          <height>120</height>
          <depth>24</depth>
          <url>/sony/webapi/ssdp/icon/dlna_tv_120.png</url>
        </icon>
        <icon>
          <mimetype>image/jpeg</mimetype>
          <width>32</width>
          <height>32</height>
          <depth>24</depth>
          <url>/sony/webapi/ssdp/icon/dlna_tv_32.jpg</url>
        </icon>
        <icon>
          <mimetype>image/png</mimetype>
          <width>32</width>
          <height>32</height>
          <depth>24</depth>
          <url>/sony/webapi/ssdp/icon/dlna_tv_32.png</url>
        </icon>
        <icon>
          <mimetype>image/jpeg</mimetype>
          <width>48</width>
          <height>48</height>
          <depth>24</depth>
          <url>/sony/webapi/ssdp/icon/dlna_tv_48.jpg</url>
        </icon>
        <icon>
          <mimetype>image/png</mimetype>
          <width>48</width>
          <height>48</height>
          <depth>24</depth>
          <url>/sony/webapi/ssdp/icon/dlna_tv_48.png</url>
        </icon>
        <icon>
          <mimetype>image/jpeg</mimetype>
          <width>60</width>
          <height>60</height>
          <depth>24</depth>
          <url>/sony/webapi/ssdp/icon/dlna_tv_60.jpg</url>
        </icon>
        <icon>
          <mimetype>image/png</mimetype>
          <width>60</width>
          <height>60</height>
          <depth>24</depth>
          <url>/sony/webapi/ssdp/icon/dlna_tv_60.png</url>
        </icon>
      </iconList>
      <serviceList>
        <service>
          <serviceType>urn:schemas-sony-com:service:ScalarWebAPI:1</serviceType>
          <serviceId>urn:schemas-sony-com:serviceId:ScalarWebAPI</serviceId>
          <SCPDURL>/sony/webapi/ssdp/scpd/WebApiSCPD.xml</SCPDURL>
          <controlURL>http://192.168.1.190/sony</controlURL>
          <eventSubURL></eventSubURL>
        </service>
        <service>
          <serviceType>urn:schemas-sony-com:service:IRCC:1</serviceType>
          <serviceId>urn:schemas-sony-com:serviceId:IRCC</serviceId>
          <SCPDURL>http://192.168.1.190/sony/ircc/IRCCSCPD.xml</SCPDURL>
          <controlURL>http://192.168.1.190/sony/ircc</controlURL>
          <eventSubURL/>
        </service>
      </serviceList>
      <av:X_ScalarWebAPI_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
        <av:X_ScalarWebAPI_Version>1.0</av:X_ScalarWebAPI_Version>
        <av:X_ScalarWebAPI_BaseURL>http://192.168.1.190/sony</av:X_ScalarWebAPI_BaseURL>
        <av:X_ScalarWebAPI_ServiceList>
          <av:X_ScalarWebAPI_ServiceType>guide</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>avContent</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>cec</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>audio</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>accessControl</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>system</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>appControl</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>videoScreen</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>encryption</av:X_ScalarWebAPI_ServiceType>        <av:X_ScalarWebAPI_ServiceType>contentshare</av:X_ScalarWebAPI_ServiceType>
        </av:X_ScalarWebAPI_ServiceList>
      </av:X_ScalarWebAPI_DeviceInfo>
      <av:X_IRCC_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
        <av:X_IRCC_Version>1.0</av:X_IRCC_Version>
        <av:X_IRCC_CategoryList>
          <av:X_IRCC_Category>
            <av:X_CategoryInfo>AAEAAAAB</av:X_CategoryInfo>
          </av:X_IRCC_Category>
          <av:X_IRCC_Category>
            <av:X_CategoryInfo>AAIAAACk</av:X_CategoryInfo>
          </av:X_IRCC_Category>
          <av:X_IRCC_Category>
            <av:X_CategoryInfo>AAIAAACX</av:X_CategoryInfo>
          </av:X_IRCC_Category>
          <av:X_IRCC_Category>
            <av:X_CategoryInfo>AAIAAAB3</av:X_CategoryInfo>
          </av:X_IRCC_Category>
          <av:X_IRCC_Category>
            <av:X_CategoryInfo>AAIAAAAa</av:X_CategoryInfo>
          </av:X_IRCC_Category>
        </av:X_IRCC_CategoryList>
      </av:X_IRCC_DeviceInfo>
      <av:X_IRCCCodeList xmlns:av="urn:schemas-sony-com:av">
        <av:X_IRCCCode command="Power">AAAAAQAAAAEAAAAVAw==</av:X_IRCCCode>
      </av:X_IRCCCodeList>
      <av:X_RDIS_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
        <av:X_RDIS_Version>1.0</av:X_RDIS_Version>
        <av:X_RDIS_SESSION_CONTROL>true</av:X_RDIS_SESSION_CONTROL>
        <av:X_RDIS_KEEP_ALIVE>false</av:X_RDIS_KEEP_ALIVE>
        <av:X_RDIS_ENTRY_PORT>59095</av:X_RDIS_ENTRY_PORT>
      </av:X_RDIS_DeviceInfo>
    </device>
  </root>
 * }
 * </pre>
 *
 * Please note this class is used strictly in the deserialization process and retrieval of the {@link IrccDevice}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("root")
public class IrccRoot {
    /** The IRCC device that is part of the root */
    @XStreamAlias("device")
    private @Nullable IrccDevice device;

    /**
     * Returns the IRCC device or null if none found
     *
     * @return a possibly null {@link IrccDevice}
     */
    public @Nullable IrccDevice getDevice() {
        return device;
    }
}
