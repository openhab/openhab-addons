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
package org.openhab.binding.plugwise.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * A filtered message listener listens to either all messages or only those of a device that has a certain MAC address.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PlugwiseFilteredMessageListener {

    private final PlugwiseMessageListener listener;
    private final @Nullable MACAddress macAddress;

    public PlugwiseFilteredMessageListener(PlugwiseMessageListener listener) {
        this(listener, null);
    }

    public PlugwiseFilteredMessageListener(PlugwiseMessageListener listener, @Nullable MACAddress macAddress) {
        this.listener = listener;
        this.macAddress = macAddress;
    }

    public PlugwiseMessageListener getListener() {
        return listener;
    }

    public @Nullable MACAddress getMACAddress() {
        return macAddress;
    }

    public boolean matches(Message message) {
        MACAddress localMACAddress = macAddress;
        return localMACAddress == null || localMACAddress.equals(message.getMACAddress());
    }
}
