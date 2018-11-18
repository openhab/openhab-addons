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

/**
 * Listener for CanBusDevice.
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public interface CanBusDeviceListener {

    /**
     * Triggered when a CANMessage is received over CANBUS
     *
     * @param canMessage received CANMessage
     */
    default void onMessage(CanMessage canMessage) {
    }

    /**
     * Triggered when the underlying CanBusDevice is ready (initiated)
     */
    default void onDeviceReady() {
    }

    /**
     * Triggered when the underlying CanBusDevice reported an error
     *
     * @param desc description of the error
     */
    default void onDeviceError(String desc) {
    }

    /**
     * Triggered when the underlying CanBusDevice reported a fatal (no recoverable) error. The callback should assume
     * the device is no longer able to serve any requests and is disconnected
     *
     * @param desc description of the error
     */
    default void onDeviceFatalError(String desc) {
    }
}
