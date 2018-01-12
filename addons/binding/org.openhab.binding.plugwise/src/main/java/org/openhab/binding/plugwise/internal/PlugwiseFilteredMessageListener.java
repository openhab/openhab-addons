/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * A filtered message listener listens to either all messages or only those of a device that has a certain MAC address.
 *
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseFilteredMessageListener {

    private final PlugwiseMessageListener listener;
    private final MACAddress macAddress;

    public PlugwiseFilteredMessageListener(PlugwiseMessageListener listener) {
        this(listener, null);
    }

    public PlugwiseFilteredMessageListener(PlugwiseMessageListener listener, MACAddress macAddress) {
        this.listener = listener;
        this.macAddress = macAddress;
    }

    public PlugwiseMessageListener getListener() {
        return listener;
    }

    public MACAddress getMACAddress() {
        return macAddress;
    }

    public boolean matches(Message message) {
        return macAddress == null || macAddress.equals(message.getMACAddress());
    }
}
