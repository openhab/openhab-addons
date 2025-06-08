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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Interface common to all Ring devices.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public abstract class AbstractRingDevice implements RingDevice {

    private final Logger logger = LoggerFactory.getLogger(AbstractRingDevice.class);
    public final Gson gson = new Gson();

    /**
     * The JsonObject contains the data retrieved from the Ring API,
     * or the data to send to the API.
     */
    protected JsonObject jsonObject = new JsonObject();
    /**
     * The registration status.
     */
    private RingDeviceRegistry.Status registrationStatus = RingDeviceRegistry.Status.ADDED;
    /**
     * The linked Ring account.
     */
    private final RingAccount ringAccount;
    /**
     * The linked RingDeviceHandler.
     */
    private @Nullable RingDeviceHandler ringDeviceHandler;

    protected AbstractRingDevice(JsonObject jsonObject, RingAccount ringAccount) {
        this.jsonObject = jsonObject;
        this.ringAccount = ringAccount;
    }

    /**
     * Get the registration status.
     *
     * @return
     */
    @Override
    public RingDeviceRegistry.Status getRegistrationStatus() {
        return registrationStatus;
    }

    /**
     * Set the registration status.
     *
     * @param status
     */
    @Override
    public void setRegistrationStatus(RingDeviceRegistry.Status registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    /**
     * Get the linked Ring Device Handler.
     *
     * @return the handler.
     */
    @Override
    @Nullable
    public RingDeviceHandler getRingDeviceHandler() {
        return ringDeviceHandler;
    }

    /**
     * Set the linked Ring Device Handler.
     *
     * @param ringDeviceHandler the handler.
     */
    @Override
    public void setRingDeviceHandler(RingDeviceHandler ringDeviceHandler) {
        this.ringDeviceHandler = ringDeviceHandler;
    }

    /**
     * Get the linked Ring account.
     *
     * @return the account.
     */
    @Override
    public RingAccount getRingAccount() {
        return ringAccount;
    }

    @Override
    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        logger.trace("AbstractRingDevice - setJsonObject - Updated JSON: {}", this.jsonObject);
    }

    @Override
    public JsonObject getJsonObject() {
        return this.jsonObject;
    }
}
