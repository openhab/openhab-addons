/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class SEMPConstants {
    public static final String SEMP_BASE_URL_PATH_DEFAULT = "/semp";
    public static final int SEMP_BASE_URL_PORT_DEFAULT = 80;
    public static final String SEMP_DOMAIN_NAME = "schemas-simple-energy-management-protocol";
    public static final String SEMP_DEVICE_TYPE = "Gateway";
    public static final int SEMP_DEVICE_TYPE_VER = 1;
    public static final String SEMP_SERVICE_TYPE = "SimpleEnergyManagementProtocol";
    public static final int SEMP_SERVICE_TYPE_VER = 1;

    public static final String SEMP_URN_DEVICE = SEMP_DOMAIN_NAME + ":device:" + SEMP_DEVICE_TYPE + ":"
            + SEMP_DEVICE_TYPE_VER;
    public static final String SEMP_URN_SERVICE = SEMP_DOMAIN_NAME + ":service:" + SEMP_SERVICE_TYPE + ":"
            + SEMP_SERVICE_TYPE_VER;

    public static final String SSDP_MCAST_IP = "239.255.255.250";
    public static final int SSDP_MCAST_PORT = 1900;
    public static final String SSDP_SERVER_TYPE = "Linux/2.6.32 UPnP/1.0 SMA SSDP Server/1.0.0";
    public static final int SSDP_VALIDITY_PERIOD = 120;

    public static final String SEMP_XSD_VERSION = "1.1.5";
    public static final String GATEWAY_SERIAL = "43-4D-43-4D-11-FF";

    public static final int DEFAULT_GATEWAY_HTTP_PORT = 8080;

    public static final List<SSDPServiceConfig> SSDP_SERVICE_CONFIG_LIST = new ArrayList<SSDPServiceConfig>();

    public static final SEMPDeviceConfig SEMP_DEVICE_CONFIG = new SEMPDeviceConfig();

    public static enum SSDPDiscoveryType {
        SSDP_DT_ROOTDEVICE,
        SSDP_DT_DEVICE_TYPE,
        SSDP_DT_DEVICEID_TYPE,
        SSDP_DT_SERVICE_TYPE,
    };

    enum SEMPMessageType {
        MSG_SEMP_INFO,
        MSG_DEVICE_INFO,
        MSG_DEVICE_STATUS,
        MSG_TIMEFRAME
    };

    public static final class SEMPDeviceConfig {
        public String urn; // device-type URN. Format: "[schemas-upnp-org|<domain-name>]:device:<deviceType>:<v>" (no
                           // "urn:"-prefix)
        public String friendlyName;
        public String manufacturer;
        public String manufacturerURL;
        public String modelDescription;
        public String modelName;
        public String modelNumber;
        public String modelURL;
        public String serialNumber;
        public String presentationURL;
        public List<String> additionalElements = new ArrayList<String>();

        public SEMPDeviceConfig() {
            this.urn = SEMP_URN_DEVICE;
            this.friendlyName = "SimpleEnergyManagementProtocol Gateway";
            this.manufacturer = "openHAB";
            this.manufacturerURL = "http://www.openHAB.org";
            this.modelDescription = "openHAB SimpleEnergyManagementProtocol Gateway";
            this.modelName = "org.binding.io.semp";
            this.modelNumber = "1.0.0";
            this.modelURL = "http://www.openHAB.org";
            this.serialNumber = GATEWAY_SERIAL;
            this.presentationURL = "presentation.html";
            additionalElements.add("<semp:X_SEMPSERVICE xmlns:semp=\"urn:" + SEMP_DOMAIN_NAME + ":service-1-0\">\n");
            additionalElements
                    .add("\t<semp:server>http://@IFACE_ADDR@:" + DEFAULT_GATEWAY_HTTP_PORT + "</semp:server>\n");
            additionalElements.add("\t<semp:basePath>" + SEMP_BASE_URL_PATH_DEFAULT + "</semp:basePath>\n");
            additionalElements.add("\t<semp:transport>HTTP/Pull</semp:transport>\n");
            additionalElements.add("\t<semp:exchangeFormat>XML</semp:exchangeFormat>\n");
            additionalElements.add("\t<semp:wsVersion>" + SEMP_XSD_VERSION + "</semp:wsVersion>\n");
            additionalElements.add("</semp:X_SEMPSERVICE>\n");
            SSDP_SERVICE_CONFIG_LIST.add(new SSDPServiceConfig(SEMP_DOMAIN_NAME + ":service:NULL:1",
                    SEMP_DOMAIN_NAME + ":serviceId:NULL", "/XD/NULL.xml", "/UD/?0", ""));
        }
    }

    public static final class SSDPServiceConfig {
        public String typeUrn; // service-type URN. Format:
                               // "[schemas-upnp-org|<domain-name>]:service:<serviceType>:<v>"(no "urn:"-prefix)
        public String idUrn; // service-Id URN. Format: "[upnp-org|<domain-name>]:serviceId:<serviceID>" (no
                             // "urn:"-prefix)
        public String serviceDescriptorURL; // URL to service descriptor
        public String controlURL; // URL for control
        public String eventSubURL; // URL for eventing

        public SSDPServiceConfig(String typeUrn, String idUrn, String SCPDURL, String controlURL, String eventSubURL) {
            this.typeUrn = typeUrn;
            this.idUrn = idUrn;
            this.serviceDescriptorURL = SCPDURL;
            this.controlURL = controlURL;
            this.eventSubURL = eventSubURL;
        }
    }
}
