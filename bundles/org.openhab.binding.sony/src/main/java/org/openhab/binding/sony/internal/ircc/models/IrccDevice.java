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

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.upnp.models.UpnpService;
import org.openhab.binding.sony.internal.upnp.models.UpnpServiceList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the deserialized results of an IRCC device. The following is an example of
 * the results that will be deserialized.
 *
 * The following is an example of a Scalar/IRCC device
 * 
 * <pre>
 * {@code
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
 * or The following is an example of an IRCC (non-scalar)
 * 
 * <pre>
 * {@code
<?xml version="1.0" encoding="UTF-8"?>
<root xmlns="urn:schemas-upnp-org:device-1-0">
   <specVersion>
      <major>1</major>
      <minor>0</minor>
   </specVersion>
   <device>
      <deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>
      <friendlyName>UBP-X800</friendlyName>
      <manufacturer>Sony Corporation</manufacturer>
      <manufacturerURL>http://www.sony.net/</manufacturerURL>
      <modelDescription />
      <modelName>Blu-ray Disc Player</modelName>
      <modelURL />
      <UDN>uuid:00000003-0000-1010-8000-045d4b24d9ff</UDN>
      <iconList>
         <icon>
            <mimetype>image/jpeg</mimetype>
            <width>120</width>
            <height>120</height>
            <depth>24</depth>
            <url>/bdp_cx_device_icon_large.jpg</url>
         </icon>
         <icon>
            <mimetype>image/png</mimetype>
            <width>120</width>
            <height>120</height>
            <depth>24</depth>
            <url>/bdp_cx_device_icon_large.png</url>
         </icon>
         <icon>
            <mimetype>image/jpeg</mimetype>
            <width>48</width>
            <height>48</height>
            <depth>24</depth>
            <url>/bdp_cx_device_icon_small.jpg</url>
         </icon>
         <icon>
            <mimetype>image/png</mimetype>
            <width>48</width>
            <height>48</height>
            <depth>24</depth>
            <url>/bdp_cx_device_icon_small.png</url>
         </icon>
      </iconList>
      <serviceList>
         <service>
            <serviceType>urn:schemas-sony-com:service:IRCC:1</serviceType>
            <serviceId>urn:schemas-sony-com:serviceId:IRCC</serviceId>
            <SCPDURL>/IRCCSCPD.xml</SCPDURL>
            <controlURL>/upnp/control/IRCC</controlURL>
            <eventSubURL />
         </service>
      </serviceList>
      <presentationURL />
      <av:X_IRCC_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
         <av:X_IRCC_Version>1.0</av:X_IRCC_Version>
         <av:X_IRCC_CategoryList>
            <av:X_IRCC_Category>
               <av:X_CategoryInfo>AAMAABxa</av:X_CategoryInfo>
            </av:X_IRCC_Category>
         </av:X_IRCC_CategoryList>
      </av:X_IRCC_DeviceInfo>
      <av:X_UNR_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
         <av:X_UNR_Version>1.3</av:X_UNR_Version>
         <av:X_CERS_ActionList_URL>http://192.168.1.123:50002/actionList</av:X_CERS_ActionList_URL>
      </av:X_UNR_DeviceInfo>
      <av:X_RDIS_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
         <av:X_RDIS_Version>1.0</av:X_RDIS_Version>
         <av:X_RDIS_SESSION_CONTROL>false</av:X_RDIS_SESSION_CONTROL>
         <av:X_RDIS_ENTRY_PORT>50004</av:X_RDIS_ENTRY_PORT>
      </av:X_RDIS_DeviceInfo>
   </device>
</root>
 * }
 * </pre>
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("device")
public class IrccDevice {
    /** Represents the device type (URN) */
    @XStreamAlias("deviceType")
    private @Nullable String deviceType;

    /** Represents the friendly name (usually set on the device) */
    @XStreamAlias("friendlyName")
    private @Nullable String friendlyName;

    /** Represents the manufacturer (should be Sony Corporation) */
    @XStreamAlias("manufacturer")
    private @Nullable String manufacturer;

    /** Represents the manufacturer URL (should be www.sony.com) */
    @XStreamAlias("manufacturerURL")
    private @Nullable String manufacturerURL;

    /** Represents the model # of the device */
    @XStreamAlias("modelName")
    private @Nullable String modelName;

    /** Represents the unique ID (UUID) of the device - generally GEN 3 UUID */
    @XStreamAlias("UDN")
    private @Nullable String udn;

    /** Represents the services that the device support */
    @XStreamAlias("serviceList")
    private @Nullable UpnpServiceList services;

    /** Represents sony extended device information */
    @XStreamAlias("X_UNR_DeviceInfo")
    private @Nullable IrccUnrDeviceInfo unrDeviceInfo;

    /** Represents any additional codes supported by the device */
    @XStreamAlias("X_IRCCCodeList")
    private @Nullable IrccCodeList codeList;

    /**
     * Returns the device type
     *
     * @return the possibly null, possibly empty device type
     */
    public @Nullable String getDeviceType() {
        return deviceType;
    }

    /**
     * Returns the friendly name
     *
     * @return the possibly null, possibly empty friendly name
     */
    public @Nullable String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Returns the manufacturer
     *
     * @return the possibly null, possibly empty manufacturer
     */
    public @Nullable String getManufacturer() {
        return manufacturer;
    }

    /**
     * Returns the manufacturer URL
     *
     * @return the possibly null, possibly empty manufacturer URL
     */
    public @Nullable String getManufacturerURL() {
        return manufacturerURL;
    }

    /**
     * Returns the model name
     *
     * @return the possibly null, possibly empty model name
     */
    public @Nullable String getModelName() {
        return modelName;
    }

    /**
     * Returns the unique ID (UUID)
     *
     * @return the possibly null, possibly empty unique ID (UUID)
     */
    public @Nullable String getUdn() {
        return udn;
    }

    /**
     * Returns the list of supported services
     *
     * @return the non-null, possibly empty list of services
     */
    public List<UpnpService> getServices() {
        final UpnpServiceList srvc = services;
        return srvc == null ? Collections.emptyList() : srvc.getServices();
    }

    /**
     * Returns the {@link IrccUnrDeviceInfo}
     *
     * @return the possibly null {@link IrccUnrDeviceInfo}
     */
    public @Nullable IrccUnrDeviceInfo getUnrDeviceInfo() {
        return unrDeviceInfo;
    }

    /**
     * Returns the {@link IrccCodeList}
     *
     * @return the possibly null {@link IrccCodeList}
     */
    public @Nullable IrccCodeList getCodeList() {
        return codeList;
    }
}
