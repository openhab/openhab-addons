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
package org.openhab.binding.lgwebos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.core.types.Command;

/**
 * Channel Handler mediates between connect sdk device state changes and openhab channel events.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public interface ChannelHandler {

    /**
     * This method will be called whenever a command is received for this handler.
     * All implementations provide custom logic here.
     *
     * @param channelId must not be <code>null</code>
     * @param handler must not be <code>null</code>
     * @param command must not be <code>null</code>
     */
    void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command);

    /**
     * Handle underlying subscription status if device changes online state, capabilities or channel gets linked or
     * unlinked.
     *
     * Implementation first removes any subscription via removeAnySubscription and subsequently establishes any required
     * subscription on this device channel handler.
     *
     * @param channelId must not be <code>null</code>
     * @param handler must not be <code>null</code>
     */
    void refreshSubscription(String channelId, LGWebOSHandler handler);

    /**
     * Removes subscriptions if there are any.
     *
     * @param handler must not be <code>null</code>
     */
    void removeAnySubscription(LGWebOSHandler handler);

    /**
     * Callback method whenever a device disappears.
     *
     * @param channelId must not be <code>null</code>
     * @param handler must not be <code>null</code>
     */
    void onDeviceRemoved(String channelId, LGWebOSHandler handler);

    /**
     * Callback method whenever a device is discovered and ready to operate.
     *
     * @param channelId must not be <code>null</code>
     * @param handler must not be <code>null</code>
     */
    void onDeviceReady(String channelId, LGWebOSHandler handler);
}
