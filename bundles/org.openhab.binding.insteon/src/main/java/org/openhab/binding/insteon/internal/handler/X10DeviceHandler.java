/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.insteon.internal.config.X10DeviceConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceType;
import org.openhab.binding.insteon.internal.device.DeviceTypeLoader;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.X10;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link X10DeviceHandler} is the handler for an x10 device thing.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class X10DeviceHandler extends InsteonDeviceHandler {

    public X10DeviceHandler(Thing thing) {
        super(thing, null);
    }

    @Override
    public void initialize() {
        X10DeviceConfiguration config = getConfigAs(X10DeviceConfiguration.class);

        scheduler.execute(() -> {
            if (getBridge() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
                return;
            }

            if (!X10.isValidHouseCode(config.getHouseCode())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid X10 house code, it must be between " + X10.HOUSE_CODE_MIN + " and "
                                + X10.HOUSE_CODE_MAX + ".");
                return;
            }

            if (!X10.isValidUnitCode(config.getUnitCode())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid X10 unit code, it must be between " + X10.UNIT_CODE_MIN + " and " + X10.UNIT_CODE_MAX
                                + ".");
                return;
            }

            String deviceTypeName = config.getDeviceType();
            DeviceType deviceType = DeviceTypeLoader.instance().getDeviceType(deviceTypeName);
            if (!deviceTypeName.startsWith("X10") || deviceType == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid device type.");
                return;
            }

            InsteonAddress address = new InsteonAddress(config.getAddress());
            if (getInsteonBinding().getDevice(address) != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Duplicate device.");
                return;
            }

            ProductData productData = ProductData.makeX10Product(deviceType);
            InsteonDevice device = createDevice(address, productData);
            setDevice(device);
            initializeChannels(device);
            refresh();
        });
    }

    @Override
    protected String getConfigInfo() {
        return getConfigAs(X10DeviceConfiguration.class).toString();
    }
}
