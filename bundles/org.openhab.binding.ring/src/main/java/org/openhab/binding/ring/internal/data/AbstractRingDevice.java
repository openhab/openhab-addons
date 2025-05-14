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
import org.openhab.binding.ring.handler.RingDeviceHandler;
import org.openhab.binding.ring.internal.ApiConstants;
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * The JsonObject contains the data retrieved from the Ring API,
     * or the data to send to the API.
     */
    protected JsonObject jsonObject = new JsonObject();
    /**
     * The registration status.
     */
    private @NonNullByDefault({}) RingDeviceRegistry.Status registrationStatus;
    /**
     * The linked Ring account.
     */
    private @NonNullByDefault({}) RingAccount ringAccount;
    /**
     * The linked RingDeviceHandler.
     */
    private @NonNullByDefault({}) RingDeviceHandler ringDeviceHandler;

    public AbstractRingDevice(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Get the device id.
     *
     * @return the device id.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getId() {
        return jsonObject.get(ApiConstants.DEVICE_ID).getAsString();
    }

    /**
     * Get the device device_id.
     *
     * @return the device device_id.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getDeviceId() {
        return jsonObject.get(ApiConstants.DEVICE_DEVICE_ID).getAsString();
    }

    /**
     * Get the device description.
     *
     * @return the device description.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getDescription() {
        return jsonObject.get(ApiConstants.DEVICE_DESCRIPTION).getAsString();
    }

    /**
     * Get the device firmware version.
     *
     * @return the device firmware version.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getFirmwareVersion() {
        return jsonObject.get(ApiConstants.DEVICE_FIRMWARE_VERSION).getAsString();
    }

    /**
     * Get the device time zone.
     *
     * @return the device time zone.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getTimeZone() {
        return jsonObject.get(ApiConstants.DEVICE_TIME_ZONE).getAsString();
    }

    /**
     * Get the device kind.
     *
     * @return the device kind.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getKind() {
        return jsonObject.get(ApiConstants.DEVICE_KIND).getAsString();
    }

    /**
     * Get battery level
     *
     * @return battery level (%)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Integer getBattery() {
        if (jsonObject.get(ApiConstants.DEVICE_BATTERY) != null) {
            return Integer.parseInt(jsonObject.get(ApiConstants.DEVICE_BATTERY).getAsString());
        } else {
            return 0;
        }
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

    /**
     * Set the linked Ring account.
     *
     * @param ringAccount
     */
    @Override
    public void setRingAccount(RingAccount ringAccount) {
        this.ringAccount = ringAccount;
    }

    @Override
    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        logger.trace("AbstractRingDevice - setJsonObject - Updated JSON: {}", this.jsonObject.getAsString());
    }

    @Override
    public JsonObject getJsonObject() {
        return this.jsonObject;
    }
}
