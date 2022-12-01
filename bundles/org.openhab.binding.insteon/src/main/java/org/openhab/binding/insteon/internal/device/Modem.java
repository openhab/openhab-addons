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
package org.openhab.binding.insteon.internal.device;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.database.ModemDB;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.driver.PortListener;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that represents modem
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class Modem implements PortListener {
    private final Logger logger = LoggerFactory.getLogger(Modem.class);

    private Driver driver;
    private @Nullable InsteonDevice device;
    private ModemDB db = new ModemDB();

    public Modem(Driver driver) {
        this.driver = driver;
    }

    public InsteonAddress getAddress() {
        InsteonDevice device = this.device;
        return device == null ? new InsteonAddress() : device.getAddress();
    }

    public @Nullable InsteonDevice getDevice() {
        return device;
    }

    public ModemDB getDB() {
        return db;
    }

    public void startPolling() {
        InsteonDevice device = this.device;
        if (device != null) {
            device.startPolling();
        }
    }

    public void stopPolling() {
        InsteonDevice device = this.device;
        if (device != null) {
            device.stopPolling();
        }
    }

    public void initialize() {
        logger.debug("initializing modem");
        InsteonDevice device = this.device;
        if (device != null) {
            logger.debug("modem {} already initialized", device.getAddress());
            return;
        }

        driver.addPortListener(this);
        getModemInfo();
    }

    private void getModemInfo() {
        try {
            Msg msg = Msg.makeMessage("GetIMInfo");
            driver.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending modem info query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    @Override
    public void disconnected() {
        // do nothing
    }

    @Override
    public void messageReceived(Msg msg) {
        try {
            if (msg.isPureNack()) {
                return;
            }
            if (msg.getByte("Cmd") == 0x60) {
                InsteonAddress address = msg.getAddress("IMAddress");
                String deviceCategory = msg.getHexString("DeviceCategory");
                String subCategory = msg.getHexString("DeviceSubCategory");
                ProductData productData = ProductDataLoader.instance().getProductData(deviceCategory, subCategory);
                if (productData.getDeviceType() == null) {
                    logger.warn("unsupported product data for devCat:{} subCat:{}", deviceCategory, subCategory);
                } else {
                    InsteonDevice device = InsteonDevice.makeDevice(driver, address, productData);
                    device.setIsModem(true);
                    productData.setFirmwareVersion(msg.getInt("FirmwareVersion"));
                    driver.getListener().modemFound(device);
                    this.device = device;
                    // build the modem db if not complete
                    if (!db.isComplete()) {
                        driver.buildModemDB();
                    }
                }
                // remove listener
                driver.removePortListener(this);
            }
        } catch (FieldException e) {
            logger.warn("error parsing im info reply field: ", e);
        }
    }

    @Override
    public void messageSent(Msg msg) {
        // ignore outbound message
    }
}
