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
package org.openhab.binding.insteon.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.X10DeviceConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceType;
import org.openhab.binding.insteon.internal.device.DeviceTypeRegistry;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.X10Address;
import org.openhab.binding.insteon.internal.device.X10Device;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link X10DeviceHandler} represents an x10 device handler.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class X10DeviceHandler extends InsteonBaseThingHandler {
    private @Nullable X10Device device;

    public X10DeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public @Nullable X10Device getDevice() {
        return device;
    }

    @Override
    public void initialize() {
        X10DeviceConfiguration config = getConfigAs(X10DeviceConfiguration.class);

        scheduler.execute(() -> {
            if (getBridge() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
                return;
            }

            String houseCode = config.getHouseCode();
            if (!X10Address.isValidHouseCode(houseCode)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid X10 house code, it must be between A and P.");
                return;
            }

            int unitCode = config.getUnitCode();
            if (!X10Address.isValidUnitCode(unitCode)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid X10 unit code, it must be between 1 and 16.");
                return;
            }

            DeviceType deviceType = DeviceTypeRegistry.getInstance().getDeviceType(config.getDeviceType());
            if (deviceType == null || !deviceType.getName().startsWith("X10")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid device type.");
                return;
            }

            InsteonModem modem = getModem();
            X10Address address = new X10Address(houseCode, unitCode);
            if (modem != null && modem.hasDevice(address)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Duplicate device.");
                return;
            }

            ProductData productData = ProductData.makeX10Product(deviceType.getName());
            X10Device device = createDevice(address, modem, productData);
            this.device = device;

            if (modem != null) {
                modem.addDevice(device);
            }

            initializeChannels(device);
            updateProperties(device);
            refresh();
        });
    }

    private X10Device createDevice(X10Address address, @Nullable InsteonModem modem, ProductData productData) {
        X10Device device = X10Device.makeDevice(address, modem, productData);
        device.setHandler(this);
        return device;
    }

    @Override
    public void dispose() {
        X10Device device = getDevice();
        InsteonModem modem = getModem();
        if (device != null && modem != null) {
            modem.removeDevice(device);
        }
        this.device = null;

        super.dispose();
    }

    @Override
    public void bridgeThingDisposed() {
        X10Device device = getDevice();
        if (device != null) {
            device.setModem(null);
        }
    }

    @Override
    public void bridgeThingUpdated(InsteonBridgeConfiguration config, InsteonModem modem) {
        X10Device device = getDevice();
        if (device != null) {
            device.setModem(modem);

            modem.addDevice(device);
        }
    }

    @Override
    protected String getConfigInfo() {
        return getConfigAs(X10DeviceConfiguration.class).toString();
    }

    @Override
    public void updateStatus() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
            return;
        }

        if (bridge.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        InsteonModem modem = getModem();
        if (modem == null || !modem.getDB().isComplete()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Waiting for modem database.");
            return;
        }

        X10Device device = getDevice();
        if (device == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to determine device.");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
