/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.canbus;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;

/**
 * CanBusDevice API. Used to communicate to the device connected to this machine to acquire access to the CANBUS.
 * Mind that the device needs to be thread safe since this API is directly tight to the openHAB framework, so multiple
 * requests can be sent at the same time
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public interface CanBusDevice {

    /**
     * Connect to the device using the in-passed device path (port, e.g. /dev/ttyACM0). This method never throws any
     * exception
     *
     * @param device   device to connect to
     * @param baudrate of the device to use
     * @return status of the connection
     */
    CanBusDeviceStatus connect(String device, int baudrate);

    /**
     * Disconnect the device
     */
    void disconnect();

    /**
     * Detects status of the CanBusDevice.
     */
    CanBusDeviceStatus getStatus();

    /**
     * Sends a CANmessage over CANBUS. Calling this on a canBusDevice that is not in OK status would immediatelly fail,
     * so you need to call connect first
     *
     * @param message message to send over CANBUS
     * @throws CanBusCommunicationException if an error occured during transmission or this device was not properly
     *                                          setup prior this call. The caller can react to this (e.g. revert last
     *                                          command done in UI or perhaps register a service to refresh the device
     *                                          later
     */
    void send(CanMessage message) throws CanBusCommunicationException;

    /**
     * Register new listener for this CanBusDevice. Listeners would get notified for example on new CanMessages received
     * or when the device is ready to send/receive traffic. Mind that no filtering is supported at the moment, so the
     * listener would get all traffic
     *
     * @param listener listener to be registered for this device
     */
    void registerCanBusDeviceListener(CanBusDeviceListener listener);

    /**
     * Set serial port manager for this device to communicate through
     *
     * @param serialPortManager serial port manager for accessing the serial port
     */
    void setSerialPortManager(@Nullable SerialPortManager serialPortManager);
}
