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
package org.openhab.binding.ring.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.handler.RingDeviceHandler;
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.core.config.discovery.DiscoveryResult;

import com.google.gson.JsonObject;

/**
 * Interface common to all Ring devices.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public interface RingDevice {

    /**
     * Get the DiscoveryResult object to identify the device as
     * discovered thing.
     *
     * @return the device as DiscoveryResult instance.
     */
    DiscoveryResult getDiscoveryResult(RingDeviceTO deviceTO);

    /**
     * Get the registration status.
     *
     * @return
     */
    RingDeviceRegistry.Status getRegistrationStatus();

    /**
     * Set the registration status.
     *
     * @param registrationStatus
     */
    void setRegistrationStatus(RingDeviceRegistry.Status registrationStatus);

    /**
     * Get the linked Ring account.
     *
     * @return the account.
     */
    RingAccount getRingAccount();

    /**
     * Get the linked Ring Device Handler.
     *
     * @return the handler.
     */
    @Nullable
    RingDeviceHandler getRingDeviceHandler();

    /**
     * Set the linked Ring Device Handler.
     *
     * @param ringDeviceHandler the handler.
     */
    void setRingDeviceHandler(RingDeviceHandler ringDeviceHandler);

    void setJsonObject(JsonObject jsonObject);

    JsonObject getJsonObject();
}
