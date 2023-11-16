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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseFilteredMessageListenerList} keeps track of a list of {@link PlugwiseFilteredMessageListener}s and
 * facilitates listener operations such as message notification.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PlugwiseFilteredMessageListenerList {

    private final Logger logger = LoggerFactory.getLogger(PlugwiseFilteredMessageListenerList.class);

    private final List<PlugwiseFilteredMessageListener> filteredListeners = new CopyOnWriteArrayList<>();

    public void addListener(PlugwiseMessageListener listener) {
        if (!isExistingListener(listener)) {
            filteredListeners.add(new PlugwiseFilteredMessageListener(listener));
        }
    }

    public void addListener(PlugwiseMessageListener listener, MACAddress macAddress) {
        if (!isExistingListener(listener, macAddress)) {
            filteredListeners.add(new PlugwiseFilteredMessageListener(listener, macAddress));
        }
    }

    public boolean isExistingListener(PlugwiseMessageListener listener) {
        return filteredListeners.stream().anyMatch(filteredListener -> filteredListener.getListener().equals(listener));
    }

    public boolean isExistingListener(PlugwiseMessageListener listener, MACAddress macAddress) {
        return filteredListeners.stream().anyMatch(filteredListener -> filteredListener.getListener().equals(listener)
                && macAddress.equals(filteredListener.getMACAddress()));
    }

    public void notifyListeners(Message message) {
        for (PlugwiseFilteredMessageListener filteredListener : filteredListeners) {
            if (filteredListener.matches(message)) {
                try {
                    filteredListener.getListener().handleResponseMessage(message);
                } catch (Exception e) {
                    logger.warn("Listener failed to handle message: {}", message, e);
                }
            }
        }
    }

    public void removeListener(PlugwiseMessageListener listener) {
        List<PlugwiseFilteredMessageListener> removedListeners = new ArrayList<>();
        for (PlugwiseFilteredMessageListener filteredListener : filteredListeners) {
            if (filteredListener.getListener().equals(listener)) {
                removedListeners.add(filteredListener);
            }
        }

        filteredListeners.removeAll(removedListeners);
    }
}
