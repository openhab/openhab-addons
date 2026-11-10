/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal.discovery;

import static org.openhab.binding.tailwind.internal.TailwindBindingConstants.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tailwind.internal.TailwindModel;
import org.openhab.binding.tailwind.internal.utils.Utilities;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TailwindDiscoveryParticipant} is responsible for looking for TailWind
 * controllers and adding them to the InBox. Uses mDNS to look for http servers
 * with vendor name of "tailwind". Uses the MAC address to ensure the controller
 * is not rediscovered it is already configured.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class TailwindDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(TailwindDiscoveryParticipant.class);

    // Service type for HTTP enabled TailWind garage controllers
    private static final String HTTP_SERVICE_TYPE = "_http._tcp.local.";

    private Utilities utilities = new Utilities();
    private boolean isAutoDiscoveryEnabled;
    private Set<ThingTypeUID> supportedThingTypes;

    public TailwindDiscoveryParticipant() {
        this.isAutoDiscoveryEnabled = true;
        this.supportedThingTypes = SUPPORTED_THING_TYPE_UIDS;
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        if (componentContext.getProperties() != null) {
            String autoDiscoveryPropertyValue = (String) componentContext.getProperties().get("enableAutoDiscovery");
            if (autoDiscoveryPropertyValue != null && !autoDiscoveryPropertyValue.isEmpty()) {
                isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
            }
        }
        supportedThingTypes = isAutoDiscoveryEnabled ? SUPPORTED_THING_TYPE_UIDS : new HashSet<>();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return supportedThingTypes;
    }

    @Override
    public String getServiceType() {
        return HTTP_SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        /**
         * The TailWind controller will broadcast its vendor name in the ServiceInfo PropertyString
         * with the keyword "vendor" and the value of "tailwind".
         */
        if (!serviceInfo.hasData()) {
            if (logger.isDebugEnabled()) {
                logger.debug("ServiceInfo does not have data");
            }
            return null;
        } else {
            String vendor = serviceInfo.getPropertyString("vendor");
            if (vendor != null) {
                if (vendor.equals(TAILWIND_VENDOR_NAME)) {
                    // Gather device details to build discovered thing
                    String qualifiedName = serviceInfo.getQualifiedName(); // qualified name is in format like
                                                                           // "???._http._tcp.local."
                    String server = serviceInfo.getServer(); // Controller server URL
                    String serverURL = utilities.getServerURL(server);
                    int port = serviceInfo.getPort(); // HTTP server listening port
                    String modelNumber = serviceInfo.getPropertyString("product"); // TailWind Model (iQ3,..)
                    String deviceId = serviceInfo.getPropertyString("device_id"); // Device ID (MAC)
                    String softwareVersion = serviceInfo.getPropertyString("SW ver"); // SW version
                    String hardwareVersion = serviceInfo.getPropertyString("HW ver"); // HW version
                    String secureCode = serviceInfo.getPropertyString("secure_code"); // Security code
                    String homeKit = serviceInfo.getPropertyString("Homekit"); // HomeKit enabled 1 or 0
                    // Internet Protocol address for discovered device
                    InetAddress[] ipAddresses = serviceInfo.getInetAddresses(); // IP Addresses for discovered device
                    if (logger.isDebugEnabled()) {
                        logger.debug("A TailWind device server was found: {}", qualifiedName);
                        logger.debug("Tailwind mDNS service: server: {}, port: {}, ipAddresses: {} ({})", server, port,
                                ipAddresses[0], ipAddresses.length);
                        logger.debug(
                                "Tailwind mDNS properties: vendor: {}, model: {}, device_id: {}, SW ver: {}, HW ver: {}, secure_code: {}, Homekit: {}",
                                vendor, modelNumber, deviceId, softwareVersion, hardwareVersion, secureCode, homeKit);
                    } // If debug logging is enabled
                    ThingUID thingUID = getThingUID(serviceInfo);
                    if (thingUID != null) {
                        Map<String, Object> properties = new HashMap<>(2);
                        // Store the properties of the mDNS query in the thing
                        properties.put(Thing.PROPERTY_MAC_ADDRESS, getDeviceMAC(deviceId));
                        properties.put(Thing.PROPERTY_MODEL_ID, modelNumber);
                        properties.put(Thing.PROPERTY_VENDOR, vendor);
                        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwareVersion);
                        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, "");
                        properties.put(TAILWIND_PROPERTY_MAX_DOORS, utilities.getMaxDoors(modelNumber));
                        properties.put(TAILWIND_HTTP_SERVER_URL, serverURL);

                        // Suggested name of discovered device (.e.g. "TailWind iQ3")
                        String label = utilities.makeFirstLetterUpperCase(vendor) + " " + modelNumber;
                        // Add discovered TailWind device to the In-box
                        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                                .withLabel(label).withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
                        return result;

                    } // If thingUID is not null
                } // If this vendor is TailWind
            } // If vendor is not null
        } // If ServiceInfo does not, else, does have data
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (isAutoDiscoveryEnabled) {
            String vendor = service.getPropertyString("vendor");
            if (vendor != null && "tailwind".equals(vendor)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("This seems like a supported Taiwind Controller!");
                }
                String deviceId = getDeviceMAC(service.getPropertyString("device_id"));
                ThingTypeUID thingTypeUID = findThingType(service.getPropertyString("product"));
                // Add the MAC address to the UID to make it unique and identifiable
                return new ThingUID(thingTypeUID, deviceId);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("This discovered device is not supported by the Tailwind binding: {}",
                            service.getQualifiedName());
                } // Debug message for Unsupported device
            }
        }
        return null;
    }

    private String getDeviceMAC(String deviceId) {
        String delimiter = "_";
        String id = "";
        String[] array = deviceId.split(delimiter);
        for (String value : array) {
            if (value.length() == 1) {
                id += "0" + value;
            } else {
                id += value;
            } // Concatenate array elements to build MAC address
        } // Loop through device Id elements

        return id;
    }

    /**
     * Method to set the thing UID for the found device
     *
     * @param deviceModel - Device model obtained from the mDNS service info
     * @return - A supported device UID or Unsupported if not found
     */
    private ThingTypeUID findThingType(@Nullable String deviceModel) {
        ThingTypeUID thingTypeUID = THING_TYPE_TAILWIND_UNSUPPORTED;

        for (ThingTypeUID thingType : SUPPORTED_THING_TYPE_UIDS) {
            logger.debug("thingType.getId(): {}", thingType.getId());
            if (thingType.getId().equalsIgnoreCase(deviceModel)) {
                return thingType;
            }
        }
        if (isSupportedDeviceModel(deviceModel)) {
            thingTypeUID = THING_TYPE_TAILWIND;
        }

        return thingTypeUID;
    }

    /**
     * Method to determine if the discovered device is supported by the binding
     *
     * @param deviceModel - Device mode obtained from the mDNS service info
     * @return True of found in list of supported models and false if not
     */
    private boolean isSupportedDeviceModel(final @Nullable String deviceModel) {
        boolean isSupported = false;
        if (deviceModel != null && !deviceModel.isBlank()) {
            List<TailwindModel> models = new ArrayList<TailwindModel>(EnumSet.allOf(TailwindModel.class));
            ListIterator<TailwindModel> modelIterator = models.listIterator();
            while (modelIterator.hasNext() && !isSupported) {
                isSupported = deviceModel.equalsIgnoreCase(modelIterator.toString());
            }
        }
        return isSupported;
    }
}
