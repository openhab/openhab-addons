/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.discovery;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery for Enocean USB dongles, integrated in Eclipse SmartHome's USB-serial discovery by implementing
 * a component of type {@link UsbSerialDiscoveryParticipant}.
 * <p/>
 * Currently, this {@link UsbSerialDiscoveryParticipant} supports the Enocean USB300 dongles.
 *
 * @author Aitor Iturrioz - initial contribution
 */
@Component(service = UsbSerialDiscoveryParticipant.class)
public class EnOceanUsbSerialDiscoveryParticipant implements UsbSerialDiscoveryParticipant {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_BRIDGE));

    public static final int ENOCEAN_USB300_DONGLE_VENDOR_ID = 0x0403;
    public static final int ENOCEAN_USB300_DONGLE_PRODUCT_ID = 0x6001;
    public static final String ENOCEAN_USB300_DONGLE_MANUFACTURER = "EnOcean GmbH";
    public static final String ENOCEAN_USB300_DONGLE_PRODUCT = "usb 300";
    public static final String ENOCEAN_USB300_DONGLE_DEFAULT_LABEL = "Enocean USB300 Dongle";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable DiscoveryResult createResult(UsbSerialDeviceInformation deviceInformation) {
        if (isEnoceanUSB300Dongle(deviceInformation)) {
            return DiscoveryResultBuilder.create(createBridgeThingType(deviceInformation))
                    .withLabel(createEnoceanUSB300DongleLabel(deviceInformation)).withRepresentationProperty(PATH)
                    .withProperty(PATH, deviceInformation.getSerialPort()).build();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(UsbSerialDeviceInformation deviceInformation) {
        if (isEnoceanUSB300Dongle(deviceInformation)) {
            return createBridgeThingType(deviceInformation);
        } else {
            return null;
        }
    }

    private ThingUID createBridgeThingType(UsbSerialDeviceInformation deviceInformation) {
        if (deviceInformation.getSerialNumber() != null) {
            return new ThingUID(THING_TYPE_BRIDGE, deviceInformation.getSerialNumber());
        } else {
            return new ThingUID(THING_TYPE_BRIDGE, String.valueOf(deviceInformation.getProductId()));
        }
    }

    private boolean isEnoceanUSB300Dongle(UsbSerialDeviceInformation deviceInformation) {
        return deviceInformation.getVendorId() == ENOCEAN_USB300_DONGLE_VENDOR_ID
                && deviceInformation.getProductId() == ENOCEAN_USB300_DONGLE_PRODUCT_ID
                && deviceInformation.getManufacturer() != null
                && deviceInformation.getManufacturer().equalsIgnoreCase(ENOCEAN_USB300_DONGLE_MANUFACTURER)
                && deviceInformation.getProduct() != null
                && deviceInformation.getProduct().toLowerCase().contains(ENOCEAN_USB300_DONGLE_PRODUCT);
    }

    private @Nullable String createEnoceanUSB300DongleLabel(UsbSerialDeviceInformation deviceInformation) {
        if (deviceInformation.getSerialNumber() != null && !deviceInformation.getSerialNumber().isEmpty()) {
            return String.format("%s (%s)", ENOCEAN_USB300_DONGLE_DEFAULT_LABEL, deviceInformation.getSerialNumber());
        } else {
            return ENOCEAN_USB300_DONGLE_DEFAULT_LABEL;
        }
    }

}
