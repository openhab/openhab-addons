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
package org.openhab.binding.sony.internal.dial.models;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the root element in the XML for a DIAL device. The XML that will be deserialized will look like
 *
 * <pre>
 * {@code
   <?xml version="1.0"?>
   <root xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="urn:schemas-upnp-org:device-1-0 Device.xsd"
    xmlns="urn:schemas-upnp-org:device-1-0">
     <specVersion>
       <major>1</major>
       <minor>0</minor>
     </specVersion>
     <device>
       <deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>
       <friendlyName>Blu-ray Disc Player</friendlyName>
       <manufacturer>Sony Corporation</manufacturer>
       <manufacturerURL>http://www.sony.net/</manufacturerURL>
       <modelDescription></modelDescription>
       <modelName>Blu-ray Disc Player</modelName>
       <modelURL></modelURL>
       <UDN>uuid:00000004-0000-1010-8000-ac9b0ac65609</UDN>
       <serviceList>
         <service>
           <serviceType>urn:dial-multiscreen-org:service:dial:1</serviceType>
           <serviceId>urn:dial-multiscreen-org:serviceId:dial</serviceId>
           <SCPDURL>/dialSCPD.xml</SCPDURL>
           <controlURL>/dial/control</controlURL>
           <eventSubURL>/dial/event</eventSubURL>
         </service>
       </serviceList>
       <presentationURL></presentationURL>
       <av:X_DIALEX_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
         <av:X_DIALEX_AppsListURL>http://192.168.1.12:50202/appslist</av:X_DIALEX_AppsListURL>
         <av:X_DIALEX_DeviceID>B0:00:04:07:DD:7E</av:X_DIALEX_DeviceID>
         <av:X_DIALEX_DeviceType>BDP_DIAL</av:X_DIALEX_DeviceType>
       </av:X_DIALEX_DeviceInfo>
     </device>
   </root>
 * }
 * </pre>
 *
 * Please note this class is used strictly in the deserialization process and retrieval of the {@link DialClient}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("root")
public class DialRoot {

    /** The deserialized {@link DialClient} */
    @XStreamAlias("device")
    private @Nullable RootDevice device;

    /**
     * Get's the list of dial device infos
     *
     * @return a non-null list of dial clients
     */
    public List<DialDeviceInfo> getDevices() {
        final RootDevice dev = device;
        return dev == null || dev.deviceInfos == null ? Collections.emptyList()
                : Collections.unmodifiableList(dev.deviceInfos);
    }

    /**
     * Internal class used simply for deserializing the device infos. Note: this class is not private since
     * DialXmlReader needs access to the class (to process the annotations)
     *
     * @author Tim Roberts - Initial contribution
     */
    @NonNullByDefault
    class RootDevice {
        @XStreamImplicit
        @Nullable
        private List<DialDeviceInfo> deviceInfos;
    }
}
