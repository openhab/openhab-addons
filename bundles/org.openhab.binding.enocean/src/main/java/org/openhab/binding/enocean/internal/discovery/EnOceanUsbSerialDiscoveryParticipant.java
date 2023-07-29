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
package org.openhab.binding.enocean.internal.discovery;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.openhab.core.config.discovery.usbserial.UsbSerialDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery for Enocean USB dongles, integrated in USB-serial discovery by implementing a component of type
 * {@link UsbSerialDiscoveryParticipant}.
 * <p/>
 * Currently, this {@link UsbSerialDiscoveryParticipant} supports the Enocean USB300 dongles.
 *
 * @author Aitor Iturrioz - initial contribution
 */
@NonNullByDefault
@Component(service = UsbSerialDiscoveryParticipant.class)
public class EnOceanUsbSerialDiscoveryParticipant implements UsbSerialDiscoveryParticipant {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_BRIDGE));

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
        String serialNumber = deviceInformation.getSerialNumber();
        if (serialNumber != null) {
            return new ThingUID(THING_TYPE_BRIDGE, serialNumber);
        } else {
            return new ThingUID(THING_TYPE_BRIDGE, String.valueOf(deviceInformation.getProductId()));
        }
    }

    private boolean isEnoceanUSB300Dongle(UsbSerialDeviceInformation deviceInformation) {
        String manufacturer = deviceInformation.getManufacturer();
        String product = deviceInformation.getProduct();

        return deviceInformation.getVendorId() == ENOCEAN_USB300_DONGLE_VENDOR_ID
                && deviceInformation.getProductId() == ENOCEAN_USB300_DONGLE_PRODUCT_ID && manufacturer != null
                && manufacturer.equalsIgnoreCase(ENOCEAN_USB300_DONGLE_MANUFACTURER) && product != null
                && product.toLowerCase().contains(ENOCEAN_USB300_DONGLE_PRODUCT);
    }

    private @Nullable String createEnoceanUSB300DongleLabel(UsbSerialDeviceInformation deviceInformation) {
        String serialNumber = deviceInformation.getSerialNumber();

        if (serialNumber != null && !serialNumber.isEmpty()) {
            return String.format("%s (%s)", ENOCEAN_USB300_DONGLE_DEFAULT_LABEL, serialNumber);
        } else {
            return ENOCEAN_USB300_DONGLE_DEFAULT_LABEL;
        }
    }
}
