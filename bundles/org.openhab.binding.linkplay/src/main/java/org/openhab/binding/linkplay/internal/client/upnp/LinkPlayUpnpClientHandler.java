/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.upnp;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for handlers that use the LinkPlay UPnP client.
 * Defines the callback methods that the UPnP client uses to notify the handler of UPnP events.
 * The handler is responsible for all business logic and state management.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface LinkPlayUpnpClientHandler {

    /**
     * Checks if the handler should process UPnP events.
     * Called before processing any UPnP value updates.
     *
     * @return true if UPnP events should be processed
     */
    boolean shouldProcessUpnpEvents();

    /**
     * Called when the UPnP subscription state changes.
     * The handler should update its online status based on subscription state.
     *
     * @param allSubscriptionsSuccessful true if all required services are subscribed
     */
    void onUpnpSubscriptionStateChanged(boolean allSubscriptionsSuccessful);

    /**
     * Called when the UPnP service becomes available.
     */
    void onUpnpServiceAvailable();

    /**
     * Called when the UPnP service status changes.
     *
     * @param available true if UPnP service is available, false otherwise
     */
    void onUpnpServiceStatusChanged(boolean available);

    /**
     * Called when an AVTransport event is received from the device.
     * The handler is responsible for parsing and acting on the event data.
     *
     * @param avTransportData the parsed AVTransport event data
     */
    void onAvTransportEvent(Map<String, String> avTransportData);

    /**
     * Called when a RenderingControl event is received from the device.
     * The handler is responsible for parsing and acting on the event data.
     *
     * @param renderingControlData the parsed RenderingControl event data
     */
    void onRenderingControlEvent(Map<String, @Nullable String> renderingControlData);
}
