/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.interfaces;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;

/**
 * {@link BaseDevice} interface for common handling of DIRIGERA devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface BaseDevice {

    /**
     * Initialize the device after gateway is ONLINE., Create channels, set initial states, update thing properties etc.
     */
    void initializeDevice();

    /**
     * Check if the handler is still valid for this device. Textual configuration may choose the wrong handler or the
     * device is disconnected from gateway and will be marked as gone.
     *
     * @return true if the handler is valid for this device, false otherwise
     */
    boolean checkHandler();

    /**
     * Handle an update from web-socket
     *
     * @param data as JSONObject
     */
    void handleUpdate(JSONObject data);

    /**
     * Start update of links. This is called before all updates are handled, so the device can prepare for link updates,
     * e.g. clear existing links.
     */
    void updateLinksStart();

    /**
     * Get the list of linked device IDs. This is called after all updates are handled, so the device can return the
     * list
     *
     * @return List of linked device IDs
     */
    List<String> getLinks();

    /**
     * Adds the link from another device to this device,
     *
     * @param linkSourceId source ID of the link (hard-link)
     * @param linkTargetId target ID of the link (soft-link)
     */
    void addSoftlink(String linkSourceId, String linkTargetId);

    /**
     * Finish update of links. This is called after all updates are handled, so the device can finish link updates, e.g.
     * remove old links which are not in the list anymore.
     */
    void updateLinksDone();

    /**
     * Get the name for a device ID. This is used for linked devices, so the name of the linked device can be shown
     *
     * @param deviceId pointing to this device or sub device
     * @return name as string, default implementation returns the device ID itself
     */
    default String getNameForId(String deviceId) {
        return deviceId;
    }

    /**
     * Set debug flag for this device and optionally for all sub devices.
     *
     * @param debugFlag true to enable debug, false to disable debug
     * @param all true to set debug flag for all sub devices, false to set debug flag only for this device
     */
    void setDebug(boolean debugFlag, boolean all);
}
