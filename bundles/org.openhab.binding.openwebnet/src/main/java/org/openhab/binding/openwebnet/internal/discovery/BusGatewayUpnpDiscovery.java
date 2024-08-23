/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.types.UDN;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BusGatewayUpnpDiscovery} is responsible for discovering supported BTicino BUS
 * gateways devices using UPnP. It implements {@link UpnpDiscoveryParticipant}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class)
public class BusGatewayUpnpDiscovery implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(BusGatewayUpnpDiscovery.class);

    public enum BusGatewayId {
        MH201("IPscenarioModule", "MH201"),
        MH202("scheduler", "MH202"),
        F454("webserver", "F454"),
        MY_HOME_SERVER1("myhomeserver1", "MYHOMESERVER1"),
        TOUCH_SCREEN_3_5("touchscreen", "TOUCHSCREEN3_5"),
        TOUCH_SCREEN_10("ts10", "TOUCHSCREEN10"),
        MH200N("lightingcontrolunit", "MH200N");

        private final String discoveryString, thingId;

        private BusGatewayId(String value, String thingId) {
            this.discoveryString = value;
            this.thingId = thingId;
        }

        public static @Nullable BusGatewayId fromValue(String s) {
            Optional<BusGatewayId> m = Arrays.stream(values()).filter(val -> s.equals(val.discoveryString)).findFirst();
            if (m.isPresent()) {
                return m.get();
            } else {
                return null;
            }
        }

        public String getThingId() {
            return thingId;
        }
    }

    /**
     * DeviceInfo bean to store device useful info (and log them)
     */
    public class DeviceInfo {
        @Nullable
        private String friendlyName;
        private String modelName = "<unknown>";
        private String modelDescription = "<unknown>";
        private String modelNumber = "<unknown>";
        private String serialNumber = "<unknown>";
        @Nullable
        private String host;
        private String manufacturer = "<unknown>";
        @Nullable
        private UDN udn;
        private boolean isBTicino = false;

        private DeviceInfo(RemoteDevice device) {
            String deviceLog = "Discovered device:\n+=== UPnP =========================================";
            RemoteDeviceIdentity identity = device.getIdentity();
            if (identity != null) {
                this.udn = identity.getUdn();
                deviceLog += "\n| ID.UDN       : " + udn;
                if (identity.getDescriptorURL() != null) {
                    deviceLog += "\n| ID.DESC URL  : " + identity.getDescriptorURL();
                    this.host = identity.getDescriptorURL().getHost();
                }
                deviceLog += "\n| ID.MAX AGE : " + identity.getMaxAgeSeconds();
            }
            deviceLog += "\n| --------------";
            DeviceDetails details = device.getDetails();
            if (details != null) {
                ManufacturerDetails manufacturerDetails = details.getManufacturerDetails();
                if (manufacturerDetails != null) {
                    this.manufacturer = manufacturerDetails.getManufacturer();
                    deviceLog += "\n| MANUFACTURER : " + manufacturer + " (" + manufacturerDetails.getManufacturerURI()
                            + ")";
                    if (manufacturer.toUpperCase().contains("BTICINO")) {
                        this.isBTicino = true;
                    }
                }
                ModelDetails modelDetails = details.getModelDetails();
                if (modelDetails != null) {
                    // Model Name | Desc | Number (Uri)
                    this.modelName = modelDetails.getModelName();
                    this.modelDescription = modelDetails.getModelDescription();
                    this.modelNumber = modelDetails.getModelNumber();
                    deviceLog += "\n| MODEL        : " + modelName + " | " + modelDescription + " | " + modelNumber
                            + " (" + modelDetails.getModelURI() + ")";
                }
                if (isBTicino) {
                    this.friendlyName = details.getFriendlyName();
                    deviceLog += "\n| FRIENDLY NAME: " + friendlyName;
                    this.serialNumber = details.getSerialNumber();
                    deviceLog += "\n| SERIAL #     : " + serialNumber;
                    deviceLog += "\n| BASE URL     : " + details.getBaseURL();
                    deviceLog += "\n| UPC          : " + details.getUpc();
                }
            }
            deviceLog += "\n+==================================================";
            logger.debug(deviceLog);
        }
    } /* DeviceInfo */

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(OpenWebNetBindingConstants.THING_TYPE_BUS_GATEWAY);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        logger.debug("Found device {}", device.getType());
        DeviceInfo devInfo = new DeviceInfo(device);
        if (!devInfo.manufacturer.matches("<unknown>")) {
            logger.debug("                              |- {} ({})", devInfo.modelName, devInfo.manufacturer);
        }
        ThingUID thingId = generateThingUID(devInfo);
        if (thingId != null) {
            String host = devInfo.host;
            if (host != null) {
                String label = "BUS Gateway";
                String fn = devInfo.friendlyName;
                if (fn != null) {
                    if (!fn.isEmpty()) {
                        label = fn;
                    }
                }
                label = label + " (" + devInfo.modelName + ", " + devInfo.modelNumber + ", " + devInfo.host + ")";
                Map<String, Object> properties = new HashMap<>(4);
                properties.put(OpenWebNetBindingConstants.CONFIG_PROPERTY_HOST, host);
                properties.put(OpenWebNetBindingConstants.PROPERTY_FIRMWARE_VERSION, devInfo.modelNumber);
                properties.put(OpenWebNetBindingConstants.PROPERTY_MODEL, devInfo.modelName);
                properties.put(OpenWebNetBindingConstants.PROPERTY_SERIAL_NO, devInfo.serialNumber);
                DiscoveryResult result = DiscoveryResultBuilder.create(thingId).withProperties(properties)
                        .withRepresentationProperty(OpenWebNetBindingConstants.PROPERTY_SERIAL_NO).withLabel(label)
                        .build();
                UDN udn = devInfo.udn;
                String udnStr;
                if (udn != null) {
                    udnStr = udn.getIdentifierString();
                } else {
                    udnStr = null;
                }
                logger.info("Created a DiscoveryResult for gateway '{}' (UDN={})", devInfo.friendlyName, udnStr);
                return result;
            } else {
                logger.warn("Could not get host for device (UDN={})", devInfo.udn);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        return generateThingUID(new DeviceInfo(device));
    }

    /**
     * Returns a ThingUID for supported devices from already extracted DeviceInfo
     *
     * @param devInfo the device info
     * @return a new ThingUID, or null if the device is not supported by the binding
     */
    private @Nullable ThingUID generateThingUID(DeviceInfo devInfo) {
        if (devInfo.isBTicino) {
            UDN udn = devInfo.udn;
            String idString = null;
            if (udn != null) {
                idString = udn.getIdentifierString();
                if (idString != null) {
                    String[] spl = idString.split("-");
                    if (spl.length > 3) {
                        BusGatewayId gwId = BusGatewayId.fromValue(spl[1]);
                        if (gwId != null) {
                            logger.debug("'{}' is a supported gateway", gwId);
                            String mac = spl[3]; // extract MAC address
                            String normalizedMac = mac.toLowerCase().replaceAll("[^a-f0-9]", "");
                            if (!normalizedMac.isEmpty()) {
                                return new ThingUID(OpenWebNetBindingConstants.THING_TYPE_BUS_GATEWAY,
                                        gwId.getThingId() + "_" + normalizedMac);
                            }
                        }
                    }
                }
            }
            logger.info("Found BTicino device: not an OpenWebNet gateway or not supported (UDN={})", idString);
        }
        return null;
    }
}
